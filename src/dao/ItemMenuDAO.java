package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import conexion.ConexionBD;
import excepciones.AccesoDatosException;
import modelo.Bebida;
import modelo.ItemMenu;
import modelo.Plato;

/**
 * Acceso a datos de la tabla item_menu.
 *
 * COMO SE GUARDA LA HERENCIA EN LA BASE DE DATOS:
 * se usa la estrategia de TABLA UNICA CON DISCRIMINADOR. Una sola tabla
 * item_menu guarda platos y bebidas, con una columna 'tipo' que dice cual es y
 * las columnas propias de cada subclase admitiendo NULL.
 *
 * Ventaja: no hace falta ningun JOIN para leer el menu completo, y el codigo
 * queda simple.
 * Desventaja: hay columnas que siempre estan en NULL para la mitad de las
 * filas. Con dos subclases el costo es despreciable; con quince, conviene
 * revisar la decision.
 *
 * ATENCION AL setNull: cuando se guarda un Plato hay que dejar en NULL las
 * columnas de Bebida, y para eso setNull exige el TIPO SQL de la columna
 * (Types.INTEGER, Types.BOOLEAN). Es un error frecuente pasarle otra constante.
 */
public class ItemMenuDAO {

	public void insertar(ItemMenu item) throws AccesoDatosException {
		String sql = "INSERT INTO item_menu (tipo, nombre, precio_base, disponible, "
				+ "minutos_preparacion, es_entrada, mililitros, alcoholica) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, item.getTipo());
			ps.setString(2, item.getNombre());
			ps.setDouble(3, item.getPrecioBase());
			ps.setBoolean(4, item.isDisponible());
			cargarCamposEspecificos(ps, item);
			ps.executeUpdate();

			try (ResultSet claves = ps.getGeneratedKeys()) {
				if (claves.next()) {
					item.setId(claves.getInt(1));
				}
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("No se pudo dar de alta el producto.", e);
		}
	}

	public void actualizar(ItemMenu item) throws AccesoDatosException {
		String sql = "UPDATE item_menu SET tipo = ?, nombre = ?, precio_base = ?, disponible = ?, "
				+ "minutos_preparacion = ?, es_entrada = ?, mililitros = ?, alcoholica = ? "
				+ "WHERE id = ?";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setString(1, item.getTipo());
			ps.setString(2, item.getNombre());
			ps.setDouble(3, item.getPrecioBase());
			ps.setBoolean(4, item.isDisponible());
			cargarCamposEspecificos(ps, item);
			ps.setInt(9, item.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new AccesoDatosException("No se pudo modificar el producto.", e);
		}
	}

	/**
	 * Carga los parametros 5 a 8, que son los especificos de cada subclase.
	 * Los que no corresponden al tipo se dejan explicitamente en NULL.
	 */
	private void cargarCamposEspecificos(PreparedStatement ps, ItemMenu item) throws SQLException {
		if (item instanceof Plato) {
			Plato plato = (Plato) item;
			ps.setInt(5, plato.getMinutosPreparacion());
			ps.setBoolean(6, plato.isEntrada());
			ps.setNull(7, Types.INTEGER);   // mililitros: no aplica a un plato
			ps.setNull(8, Types.BOOLEAN);   // alcoholica: no aplica a un plato
		} else {
			Bebida bebida = (Bebida) item;
			ps.setNull(5, Types.INTEGER);   // minutos_preparacion: no aplica
			ps.setNull(6, Types.BOOLEAN);   // es_entrada: no aplica
			ps.setInt(7, bebida.getMililitros());
			ps.setBoolean(8, bebida.isAlcoholica());
		}
	}

	public void eliminar(int id) throws AccesoDatosException {
		String sql = "DELETE FROM item_menu WHERE id = ?";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setInt(1, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new AccesoDatosException("No se pudo eliminar el producto. "
					+ "Probablemente figure en pedidos ya emitidos: use la baja logica.", e);
		}
	}

	/** Baja logica: el producto sale de la carta pero sigue en los tickets viejos. */
	public void darDeBaja(int id) throws AccesoDatosException {
		cambiarDisponibilidad(id, false);
	}

	public void cambiarDisponibilidad(int id, boolean disponible) throws AccesoDatosException {
		String sql = "UPDATE item_menu SET disponible = ? WHERE id = ?";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setBoolean(1, disponible);
			ps.setInt(2, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new AccesoDatosException("No se pudo cambiar la disponibilidad del producto.", e);
		}
	}

	public ItemMenu buscarPorId(int id) throws AccesoDatosException {
		String sql = "SELECT * FROM item_menu WHERE id = ?";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapear(rs);
				}
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("Error al buscar el producto.", e);
		}
		return null;
	}

	public List<ItemMenu> listarTodos() throws AccesoDatosException {
		return ejecutarListado("SELECT * FROM item_menu ORDER BY tipo, nombre");
	}

	public List<ItemMenu> listarDisponibles() throws AccesoDatosException {
		return ejecutarListado("SELECT * FROM item_menu WHERE disponible = TRUE ORDER BY tipo, nombre");
	}

	public List<ItemMenu> buscarPorNombre(String texto) throws AccesoDatosException {
		String sql = "SELECT * FROM item_menu WHERE nombre LIKE ? ORDER BY tipo, nombre";
		Connection conexion = ConexionBD.getInstancia().getConexion();
		List<ItemMenu> lista = new ArrayList<ItemMenu>();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setString(1, "%" + texto + "%");
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					lista.add(mapear(rs));
				}
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("Error al buscar productos.", e);
		}
		return lista;
	}

	private List<ItemMenu> ejecutarListado(String sql) throws AccesoDatosException {
		Connection conexion = ConexionBD.getInstancia().getConexion();
		List<ItemMenu> lista = new ArrayList<ItemMenu>();

		try (PreparedStatement ps = conexion.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				lista.add(mapear(rs));
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("Error al listar el menu.", e);
		}
		return lista;
	}

	/**
	 * Fabrica la subclase correcta segun el discriminador.
	 * Es el punto donde la base de datos "recuerda" la jerarquia de clases.
	 */
	private ItemMenu mapear(ResultSet rs) throws SQLException {
		int id = rs.getInt("id");
		String nombre = rs.getString("nombre");
		double precioBase = rs.getDouble("precio_base");
		boolean disponible = rs.getBoolean("disponible");
		String tipo = rs.getString("tipo");

		if ("PLATO".equals(tipo)) {
			return new Plato(id, nombre, precioBase, disponible,
					rs.getInt("minutos_preparacion"), rs.getBoolean("es_entrada"));
		}
		return new Bebida(id, nombre, precioBase, disponible,
				rs.getInt("mililitros"), rs.getBoolean("alcoholica"));
	}
}
