package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import conexion.ConexionBD;
import excepciones.AccesoDatosException;
import modelo.RankingItem;
import modelo.ReporteVentas;
import util.ColeccionUtil;

/**
 * Consultas de gestion (requisito 2, apartado adicional: reportes y
 * estadisticas).
 *
 * REGLA QUE NO SE NEGOCIA: el filtro por fechas va SIEMPRE en el WHERE de la
 * consulta SQL, nunca trayendo toda la tabla a memoria y filtrando en Java.
 * Traer todo y filtrar despues funciona con veinte pedidos de prueba y colapsa
 * con veinte mil reales. Ademas es la base de datos la que esta optimizada para
 * filtrar.
 *
 * Todos los reportes consideran unicamente pedidos CERRADOS: los ABIERTOS
 * todavia no se cobraron y los ANULADOS no representan facturacion.
 */
public class ReporteDAO {

	/** Facturacion dia por dia dentro del periodo. */
	public ReporteVentas facturacionPorPeriodo(LocalDate desde, LocalDate hasta)
			throws AccesoDatosException {

		String sql = "SELECT DATE(fecha_cierre) AS dia, COUNT(*) AS cantidad, "
				+ "SUM(total) AS monto "
				+ "FROM pedido "
				+ "WHERE estado = 'CERRADO' AND DATE(fecha_cierre) BETWEEN ? AND ? "
				+ "GROUP BY DATE(fecha_cierre) "
				+ "ORDER BY dia";

		ReporteVentas reporte = new ReporteVentas("Facturacion por periodo", desde, hasta);
		Connection conexion = ConexionBD.getInstancia().getConexion();

		double totalGeneral = 0;
		int totalPedidos = 0;

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setDate(1, Date.valueOf(desde));
			ps.setDate(2, Date.valueOf(hasta));

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String dia = rs.getString("dia");
					int cantidad = rs.getInt("cantidad");
					double monto = rs.getDouble("monto");

					totalGeneral += monto;
					totalPedidos += cantidad;

					reporte.agregarLinea(String.format("%-14s %4d cuentas %14.2f",
							dia, cantidad, monto));
				}
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("Error al generar el reporte de facturacion.", e);
		}

		reporte.setResumen(String.format("TOTAL DEL PERIODO: %d cuentas cerradas por $%.2f",
				totalPedidos, totalGeneral));
		return reporte;
	}

	/**
	 * Ranking de productos vendidos.
	 *
	 * Este reporte es el que usa el metodo generico: arma una lista de
	 * RankingItem (que implementa Comparable) y le pide a
	 * ColeccionUtil.obtenerMaximo cual fue el mas vendido, y a
	 * ColeccionUtil.primerosN el top cinco.
	 */
	public ReporteVentas rankingProductos(LocalDate desde, LocalDate hasta)
			throws AccesoDatosException {

		String sql = "SELECT im.nombre AS nombre, im.tipo AS tipo, "
				+ "SUM(dp.cantidad) AS unidades, "
				+ "SUM(dp.cantidad * dp.precio_unitario) AS monto "
				+ "FROM detalle_pedido dp "
				+ "JOIN pedido p ON p.id = dp.pedido_id "
				+ "JOIN item_menu im ON im.id = dp.item_id "
				+ "WHERE p.estado = 'CERRADO' AND DATE(p.fecha_cierre) BETWEEN ? AND ? "
				+ "GROUP BY im.id, im.nombre, im.tipo "
				+ "ORDER BY unidades DESC";

		ReporteVentas reporte = new ReporteVentas("Productos mas vendidos", desde, hasta);
		List<RankingItem> ranking = new ArrayList<RankingItem>();
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setDate(1, Date.valueOf(desde));
			ps.setDate(2, Date.valueOf(hasta));

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ranking.add(new RankingItem(
							rs.getString("nombre") + " [" + rs.getString("tipo") + "]",
							rs.getInt("unidades"),
							rs.getDouble("monto")));
				}
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("Error al generar el ranking de productos.", e);
		}

		// Metodo generico 1: recorta el top cinco de una lista de cualquier tipo.
		List<RankingItem> topCinco = ColeccionUtil.primerosN(ranking, 5);
		for (int i = 0; i < topCinco.size(); i++) {
			RankingItem item = topCinco.get(i);
			reporte.agregarLinea(String.format("%d) %-30s %4d u. %13.2f",
					i + 1, item.getNombre(), item.getCantidad(), item.getMonto()));
		}

		// Metodo generico 2: exige que el tipo sea Comparable, y RankingItem lo es.
		RankingItem masVendido = ColeccionUtil.obtenerMaximo(ranking);
		if (masVendido == null) {
			reporte.setResumen("No hubo ventas en el periodo.");
		} else {
			reporte.setResumen("PRODUCTO ESTRELLA: " + masVendido.getNombre()
					+ " con " + masVendido.getCantidad() + " unidades vendidas.");
		}
		return reporte;
	}

	/** Cuanto vendio cada mozo en el periodo. */
	public ReporteVentas ventasPorMozo(LocalDate desde, LocalDate hasta)
			throws AccesoDatosException {

		String sql = "SELECT e.nombre AS nombre, COUNT(*) AS cuentas, SUM(p.total) AS monto "
				+ "FROM pedido p "
				+ "JOIN empleado e ON e.id = p.empleado_id "
				+ "WHERE p.estado = 'CERRADO' AND DATE(p.fecha_cierre) BETWEEN ? AND ? "
				+ "GROUP BY e.id, e.nombre "
				+ "ORDER BY monto DESC";

		ReporteVentas reporte = new ReporteVentas("Ventas por mozo", desde, hasta);
		List<RankingItem> ranking = new ArrayList<RankingItem>();
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setDate(1, Date.valueOf(desde));
			ps.setDate(2, Date.valueOf(hasta));

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ranking.add(new RankingItem(
							rs.getString("nombre"),
							rs.getInt("cuentas"),
							rs.getDouble("monto")));
				}
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("Error al generar el reporte por mozo.", e);
		}

		for (int i = 0; i < ranking.size(); i++) {
			RankingItem item = ranking.get(i);
			reporte.agregarLinea(String.format("%-24s %4d cuentas %14.2f",
					item.getNombre(), item.getCantidad(), item.getMonto()));
		}

		RankingItem mejor = ColeccionUtil.obtenerMaximo(ranking);
		if (mejor == null) {
			reporte.setResumen("No hubo cuentas cerradas en el periodo.");
		} else {
			reporte.setResumen("MAS CUENTAS ATENDIDAS: " + mejor.getNombre()
					+ " (" + mejor.getCantidad() + ").");
		}
		return reporte;
	}

	/** Ticket promedio, minimo y maximo del periodo. */
	public ReporteVentas ticketPromedio(LocalDate desde, LocalDate hasta)
			throws AccesoDatosException {

		String sql = "SELECT COUNT(*) AS cuentas, SUM(total) AS suma, AVG(total) AS promedio, "
				+ "MIN(total) AS minimo, MAX(total) AS maximo "
				+ "FROM pedido "
				+ "WHERE estado = 'CERRADO' AND DATE(fecha_cierre) BETWEEN ? AND ?";

		ReporteVentas reporte = new ReporteVentas("Ticket promedio", desde, hasta);
		Connection conexion = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setDate(1, Date.valueOf(desde));
			ps.setDate(2, Date.valueOf(hasta));

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					int cuentas = rs.getInt("cuentas");
					if (cuentas == 0) {
						reporte.setResumen("No hubo cuentas cerradas en el periodo.");
						return reporte;
					}
					reporte.agregarLinea(String.format("%-24s %14d", "Cuentas cerradas", cuentas));
					reporte.agregarLinea(String.format("%-24s %14.2f", "Facturacion total",
							rs.getDouble("suma")));
					reporte.agregarLinea(String.format("%-24s %14.2f", "Ticket minimo",
							rs.getDouble("minimo")));
					reporte.agregarLinea(String.format("%-24s %14.2f", "Ticket maximo",
							rs.getDouble("maximo")));
					reporte.setResumen(String.format("TICKET PROMEDIO: $%.2f",
							rs.getDouble("promedio")));
				}
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("Error al calcular el ticket promedio.", e);
		}
		return reporte;
	}
}
