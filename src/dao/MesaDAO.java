package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import conexion.ConexionBD;
import excepciones.AccesoDatosException;
import modelo.Mesa;

/**
 * Acceso a datos de la tabla mesa.
 *
 * La mesa usa el numero como clave primaria (clave natural), asi que el alta
 * recibe el numero desde la vista y no lo genera la base de datos.
 */
public class MesaDAO {

	public void insertar(Mesa mesa) throws AccesoDatosException {
		String sql = "INSERT INTO mesa (numero, capacidad, sector, ocupada) VALUES (?, ?, ?, ?)";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setInt(1, mesa.getNumero());
			ps.setInt(2, mesa.getCapacidad());
			ps.setString(3, mesa.getSector());
			ps.setBoolean(4, mesa.isOcupada());
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new AccesoDatosException("No se pudo crear la mesa. "
					+ "Verifique que el numero " + mesa.getNumero() + " no exista ya.", e);
		}
	}

	public void actualizar(Mesa mesa) throws AccesoDatosException {
		String sql = "UPDATE mesa SET capacidad = ?, sector = ?, ocupada = ? WHERE numero = ?";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setInt(1, mesa.getCapacidad());
			ps.setString(2, mesa.getSector());
			ps.setBoolean(3, mesa.isOcupada());
			ps.setInt(4, mesa.getNumero());
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new AccesoDatosException("No se pudo modificar la mesa.", e);
		}
	}

	/** Marca o libera la mesa. Se usa al abrir y al cerrar una cuenta. */
	public void actualizarOcupada(int numero, boolean ocupada) throws AccesoDatosException {
		String sql = "UPDATE mesa SET ocupada = ? WHERE numero = ?";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setBoolean(1, ocupada);
			ps.setInt(2, numero);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new AccesoDatosException("No se pudo actualizar el estado de la mesa.", e);
		}
	}

	public void eliminar(int numero) throws AccesoDatosException {
		String sql = "DELETE FROM mesa WHERE numero = ?";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setInt(1, numero);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new AccesoDatosException("No se pudo eliminar la mesa. "
					+ "Probablemente tenga pedidos registrados.", e);
		}
	}

	public Mesa buscarPorNumero(int numero) throws AccesoDatosException {
		String sql = "SELECT * FROM mesa WHERE numero = ?";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setInt(1, numero);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapear(rs);
				}
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("Error al buscar la mesa.", e);
		}
		return null;
	}

	public List<Mesa> listarTodas() throws AccesoDatosException {
		String sql = "SELECT * FROM mesa ORDER BY numero";
		Connection conexion = ConexionBD.getInstancia().getConexion();
		List<Mesa> lista = new ArrayList<Mesa>();

		try (PreparedStatement ps = conexion.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				lista.add(mapear(rs));
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("Error al listar las mesas.", e);
		}
		return lista;
	}

	private Mesa mapear(ResultSet rs) throws SQLException {
		return new Mesa(
				rs.getInt("numero"),
				rs.getInt("capacidad"),
				rs.getString("sector"),
				rs.getBoolean("ocupada"));
	}
}
