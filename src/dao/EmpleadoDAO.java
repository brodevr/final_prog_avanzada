package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import conexion.ConexionBD;
import excepciones.AccesoDatosException;
import modelo.Empleado;

/**
 * Acceso a datos de la tabla empleado (patron DAO).
 *
 * El patron DAO aisla el SQL en una capa propia: el controlador pide
 * "guardame este empleado" y no sabe si por debajo hay MySQL, un archivo o un
 * servicio web. Si manana cambia el motor de base de datos, se reescribe esta
 * capa y el resto del sistema queda intacto.
 */
public class EmpleadoDAO {

	public void insertar(Empleado empleado) throws AccesoDatosException {
		String sql = "INSERT INTO empleado (nombre, usuario, clave, activo) VALUES (?, ?, ?, ?)";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, empleado.getNombre());
			ps.setString(2, empleado.getUsuario());
			ps.setString(3, empleado.getClave());
			ps.setBoolean(4, empleado.isActivo());
			ps.executeUpdate();

			try (ResultSet claves = ps.getGeneratedKeys()) {
				if (claves.next()) {
					empleado.setId(claves.getInt(1));
				}
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("No se pudo dar de alta el empleado. "
					+ "Verifique que el usuario no este repetido.", e);
		}
	}

	public void actualizar(Empleado empleado) throws AccesoDatosException {
		String sql = "UPDATE empleado SET nombre = ?, usuario = ?, clave = ?, activo = ? WHERE id = ?";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setString(1, empleado.getNombre());
			ps.setString(2, empleado.getUsuario());
			ps.setString(3, empleado.getClave());
			ps.setBoolean(4, empleado.isActivo());
			ps.setInt(5, empleado.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new AccesoDatosException("No se pudo modificar el empleado.", e);
		}
	}

	/**
	 * Borrado fisico. Falla si el empleado tiene pedidos asociados, porque la
	 * base de datos protege la integridad referencial.
	 */
	public void eliminar(int id) throws AccesoDatosException {
		String sql = "DELETE FROM empleado WHERE id = ?";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setInt(1, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new AccesoDatosException("No se pudo eliminar el empleado. "
					+ "Probablemente tenga pedidos registrados: use la baja logica.", e);
		}
	}

	/** Baja logica: el empleado deja de poder operar pero conserva su historia. */
	public void darDeBaja(int id) throws AccesoDatosException {
		String sql = "UPDATE empleado SET activo = FALSE WHERE id = ?";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setInt(1, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new AccesoDatosException("No se pudo dar de baja el empleado.", e);
		}
	}

	public Empleado buscarPorId(int id) throws AccesoDatosException {
		String sql = "SELECT * FROM empleado WHERE id = ?";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapear(rs);
				}
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("Error al buscar el empleado.", e);
		}
		return null;
	}

	/** Se usa en el login. Devuelve null si las credenciales no coinciden. */
	public Empleado buscarPorCredenciales(String usuario, String clave) throws AccesoDatosException {
		String sql = "SELECT * FROM empleado WHERE usuario = ? AND clave = ? AND activo = TRUE";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setString(1, usuario);
			ps.setString(2, clave);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapear(rs);
				}
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("Error al validar las credenciales.", e);
		}
		return null;
	}

	public List<Empleado> listarTodos() throws AccesoDatosException {
		String sql = "SELECT * FROM empleado ORDER BY nombre";
		return ejecutarListado(sql);
	}

	public List<Empleado> listarActivos() throws AccesoDatosException {
		String sql = "SELECT * FROM empleado WHERE activo = TRUE ORDER BY nombre";
		return ejecutarListado(sql);
	}

	public List<Empleado> buscarPorNombre(String texto) throws AccesoDatosException {
		String sql = "SELECT * FROM empleado WHERE nombre LIKE ? ORDER BY nombre";
		Connection conexion = ConexionBD.getInstancia().getConexion();
		List<Empleado> lista = new ArrayList<Empleado>();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setString(1, "%" + texto + "%");
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					lista.add(mapear(rs));
				}
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("Error al buscar empleados.", e);
		}
		return lista;
	}

	private List<Empleado> ejecutarListado(String sql) throws AccesoDatosException {
		Connection conexion = ConexionBD.getInstancia().getConexion();
		List<Empleado> lista = new ArrayList<Empleado>();

		try (PreparedStatement ps = conexion.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				lista.add(mapear(rs));
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("Error al listar los empleados.", e);
		}
		return lista;
	}

	/** Convierte una fila del ResultSet en un objeto del modelo. */
	private Empleado mapear(ResultSet rs) throws SQLException {
		return new Empleado(
				rs.getInt("id"),
				rs.getString("nombre"),
				rs.getString("usuario"),
				rs.getString("clave"),
				rs.getBoolean("activo"));
	}
}
