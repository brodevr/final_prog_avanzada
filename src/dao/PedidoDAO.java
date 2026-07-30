package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import conexion.ConexionBD;
import excepciones.AccesoDatosException;
import modelo.DetallePedido;
import modelo.Empleado;
import modelo.EstadoPedido;
import modelo.ItemMenu;
import modelo.Mesa;
import modelo.Pedido;
import promociones.DescuentoRegistrado;

/**
 * Acceso a datos de las tablas pedido y detalle_pedido.
 *
 * Es el DAO mas complejo porque un pedido es un agregado: la cabecera vive en
 * la tabla pedido y sus lineas en detalle_pedido. Guardar un pedido significa
 * guardar la cabecera, recuperar el id que genero la base de datos, y recien
 * entonces insertar las lineas apuntando a ese id.
 */
public class PedidoDAO {

	private MesaDAO mesaDAO = new MesaDAO();
	private EmpleadoDAO empleadoDAO = new EmpleadoDAO();
	private ItemMenuDAO itemMenuDAO = new ItemMenuDAO();

	// ------------------------------------------------------------------
	// Alta
	// ------------------------------------------------------------------

	public void insertar(Pedido pedido) throws AccesoDatosException {
		String sql = "INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, "
				+ "estado, subtotal, descuento, descuento_desc, total) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			cargarCabecera(ps, pedido);
			ps.executeUpdate();

			try (ResultSet claves = ps.getGeneratedKeys()) {
				if (claves.next()) {
					pedido.setId(claves.getInt(1));
				}
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("No se pudo guardar el pedido.", e);
		}

		insertarDetalles(pedido);
	}

	// ------------------------------------------------------------------
	// Modificacion
	// ------------------------------------------------------------------

	/**
	 * Actualiza la cabecera y reescribe las lineas.
	 *
	 * Se borran y se vuelven a insertar los detalles en lugar de calcular que
	 * cambio. Para la cantidad de lineas que tiene una comanda es la solucion
	 * mas simple y menos propensa a errores.
	 */
	public void actualizar(Pedido pedido) throws AccesoDatosException {
		String sql = "UPDATE pedido SET mesa_numero = ?, empleado_id = ?, fecha_apertura = ?, "
				+ "fecha_cierre = ?, estado = ?, subtotal = ?, descuento = ?, "
				+ "descuento_desc = ?, total = ? WHERE id = ?";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			cargarCabecera(ps, pedido);
			ps.setInt(10, pedido.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new AccesoDatosException("No se pudo actualizar el pedido.", e);
		}

		borrarDetalles(pedido.getId());
		insertarDetalles(pedido);
	}

	/** Carga los nueve parametros comunes al alta y a la modificacion. */
	private void cargarCabecera(PreparedStatement ps, Pedido pedido) throws SQLException {
		ps.setInt(1, pedido.getMesa().getNumero());
		ps.setInt(2, pedido.getEmpleado().getId());
		ps.setTimestamp(3, Timestamp.valueOf(pedido.getFechaApertura()));

		if (pedido.getFechaCierre() == null) {
			ps.setNull(4, java.sql.Types.TIMESTAMP);
		} else {
			ps.setTimestamp(4, Timestamp.valueOf(pedido.getFechaCierre()));
		}

		ps.setString(5, pedido.getEstado().name());
		ps.setDouble(6, pedido.calcularSubtotal());
		ps.setDouble(7, pedido.calcularDescuento());
		ps.setString(8, pedido.getDescuento().getDescripcion());
		ps.setDouble(9, pedido.calcularTotal());
	}

	private void insertarDetalles(Pedido pedido) throws AccesoDatosException {
		if (pedido.getDetalles().isEmpty()) {
			return;
		}

		String sql = "INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) "
				+ "VALUES (?, ?, ?, ?)";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			for (int i = 0; i < pedido.getDetalles().size(); i++) {
				DetallePedido detalle = pedido.getDetalles().get(i);
				ps.setInt(1, pedido.getId());
				ps.setInt(2, detalle.getItem().getId());
				ps.setInt(3, detalle.getCantidad());
				ps.setDouble(4, detalle.getPrecioUnitario());
				ps.addBatch();
			}
			ps.executeBatch();
		} catch (SQLException e) {
			throw new AccesoDatosException("No se pudieron guardar las lineas del pedido.", e);
		}
	}

	private void borrarDetalles(int pedidoId) throws AccesoDatosException {
		String sql = "DELETE FROM detalle_pedido WHERE pedido_id = ?";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setInt(1, pedidoId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new AccesoDatosException("No se pudieron borrar las lineas del pedido.", e);
		}
	}

	// ------------------------------------------------------------------
	// Baja
	// ------------------------------------------------------------------

	/** El ON DELETE CASCADE de la tabla detalle_pedido borra las lineas solo. */
	public void eliminar(int id) throws AccesoDatosException {
		String sql = "DELETE FROM pedido WHERE id = ?";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setInt(1, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new AccesoDatosException("No se pudo eliminar el pedido.", e);
		}
	}

	// ------------------------------------------------------------------
	// Consultas
	// ------------------------------------------------------------------

	public Pedido buscarPorId(int id) throws AccesoDatosException {
		String sql = "SELECT * FROM pedido WHERE id = ?";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapear(rs);
				}
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("Error al buscar el pedido.", e);
		}
		return null;
	}

	/**
	 * Devuelve la cuenta abierta de una mesa, o null si la mesa esta libre.
	 * Es la consulta que sostiene la regla "una mesa, una sola cuenta abierta".
	 */
	public Pedido buscarAbiertoPorMesa(int numeroMesa) throws AccesoDatosException {
		String sql = "SELECT * FROM pedido WHERE mesa_numero = ? AND estado = 'ABIERTO' "
				+ "ORDER BY id DESC LIMIT 1";
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setInt(1, numeroMesa);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapear(rs);
				}
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("Error al buscar la cuenta abierta de la mesa.", e);
		}
		return null;
	}

	public List<Pedido> listarTodos() throws AccesoDatosException {
		return ejecutarListado("SELECT * FROM pedido ORDER BY id DESC", null);
	}

	public List<Pedido> listarPorEstado(EstadoPedido estado) throws AccesoDatosException {
		return ejecutarListado("SELECT * FROM pedido WHERE estado = ? ORDER BY id DESC",
				estado.name());
	}

	private List<Pedido> ejecutarListado(String sql, String parametro) throws AccesoDatosException {
		Connection conexion = ConexionBD.getInstancia().getConexion();
		List<Pedido> lista = new ArrayList<Pedido>();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			if (parametro != null) {
				ps.setString(1, parametro);
			}
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					lista.add(mapear(rs));
				}
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("Error al listar los pedidos.", e);
		}
		return lista;
	}

	/**
	 * Reconstruye un pedido completo: cabecera, mesa, empleado, descuento y
	 * todas sus lineas con sus productos.
	 */
	private Pedido mapear(ResultSet rs) throws SQLException, AccesoDatosException {
		int id = rs.getInt("id");
		Mesa mesa = mesaDAO.buscarPorNumero(rs.getInt("mesa_numero"));
		Empleado empleado = empleadoDAO.buscarPorId(rs.getInt("empleado_id"));

		Timestamp apertura = rs.getTimestamp("fecha_apertura");
		Timestamp cierre = rs.getTimestamp("fecha_cierre");

		Pedido pedido = new Pedido(
				id,
				mesa,
				empleado,
				apertura.toLocalDateTime(),
				cierre == null ? null : cierre.toLocalDateTime(),
				EstadoPedido.valueOf(rs.getString("estado")));

		double descuento = rs.getDouble("descuento");
		if (descuento > 0) {
			pedido.setDescuento(new DescuentoRegistrado(descuento, rs.getString("descuento_desc")));
		}

		pedido.setDetalles(listarDetalles(id));
		return pedido;
	}

	private List<DetallePedido> listarDetalles(int pedidoId) throws AccesoDatosException {
		String sql = "SELECT * FROM detalle_pedido WHERE pedido_id = ? ORDER BY id";
		Connection conexion = ConexionBD.getInstancia().getConexion();
		List<DetallePedido> lista = new ArrayList<DetallePedido>();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setInt(1, pedidoId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ItemMenu item = itemMenuDAO.buscarPorId(rs.getInt("item_id"));
					lista.add(new DetallePedido(
							rs.getInt("id"),
							item,
							rs.getInt("cantidad"),
							rs.getDouble("precio_unitario")));
				}
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("Error al leer las lineas del pedido.", e);
		}
		return lista;
	}
}
