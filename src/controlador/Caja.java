package controlador;

import java.time.LocalDate;
import java.util.List;

import dao.MesaDAO;
import dao.PedidoDAO;
import dao.ReporteDAO;
import excepciones.AccesoDatosException;
import excepciones.ItemNoDisponibleException;
import excepciones.MesaOcupadaException;
import excepciones.MontoInvalidoException;
import excepciones.PedidoCerradoException;
import modelo.Empleado;
import modelo.EstadoPedido;
import modelo.ItemMenu;
import modelo.Mesa;
import modelo.Pedido;
import modelo.ReporteVentas;
import promociones.Descuento;

/**
 * Controlador de la operacion diaria. Implementa SINGLETON.
 *
 * Aca vive el CICLO DE APERTURA Y CIERRE que pide el requisito 2 de la
 * consigna:
 *
 *   abrirMesa()     la mesa pasa a ocupada y nace un pedido ABIERTO
 *   agregarItem()   se cargan consumos mientras la cuenta este abierta
 *   cerrarCuenta()  el pedido pasa a CERRADO y la mesa se libera
 *   anularCuenta()  el pedido pasa a ANULADO y la mesa se libera
 *
 * La vista nunca habla con un DAO: siempre pasa por este controlador. Ese es el
 * contrato del MVC y lo que mantiene la logica de negocio en un solo lugar.
 */
public class Caja {

	private static Caja instancia;

	private PedidoDAO pedidoDAO;
	private MesaDAO mesaDAO;
	private ReporteDAO reporteDAO;

	private Caja() {
		this.pedidoDAO = new PedidoDAO();
		this.mesaDAO = new MesaDAO();
		this.reporteDAO = new ReporteDAO();
	}

	public static Caja getInstancia() {
		if (instancia == null) {
			instancia = new Caja();
		}
		return instancia;
	}

	// ==================================================================
	// APERTURA
	// ==================================================================

	/**
	 * Abre la cuenta de una mesa.
	 *
	 * No confia en el flag 'ocupada' de la mesa: consulta la base de datos para
	 * ver si ya existe un pedido ABIERTO. El estado real esta en los pedidos, y
	 * el flag es solo una comodidad para pintar el salon.
	 */
	public Pedido abrirMesa(Mesa mesa, Empleado empleado)
			throws AccesoDatosException, MesaOcupadaException, MontoInvalidoException {

		if (mesa == null) {
			throw new MontoInvalidoException("No se selecciono ninguna mesa.");
		}
		if (empleado == null) {
			throw new MontoInvalidoException("No hay ningun empleado con sesion iniciada.");
		}

		Pedido existente = pedidoDAO.buscarAbiertoPorMesa(mesa.getNumero());
		if (existente != null) {
			throw new MesaOcupadaException("La mesa " + mesa.getNumero()
					+ " ya tiene la cuenta #" + existente.getId() + " abierta.");
		}

		Pedido pedido = new Pedido(mesa, empleado);
		pedidoDAO.insertar(pedido);

		mesa.setOcupada(true);
		mesaDAO.actualizarOcupada(mesa.getNumero(), true);

		return pedido;
	}

	/** Devuelve la cuenta abierta de una mesa, o null si esta libre. */
	public Pedido buscarCuentaAbierta(int numeroMesa) throws AccesoDatosException {
		return pedidoDAO.buscarAbiertoPorMesa(numeroMesa);
	}

	// ==================================================================
	// CARGA DE CONSUMOS
	// ==================================================================

	public void agregarItem(Pedido pedido, ItemMenu item, int cantidad)
			throws AccesoDatosException, PedidoCerradoException,
			ItemNoDisponibleException, MontoInvalidoException {

		verificarPedido(pedido);
		pedido.agregarItem(item, cantidad);
		pedidoDAO.actualizar(pedido);
	}

	public void quitarItem(Pedido pedido, int indice)
			throws AccesoDatosException, PedidoCerradoException, MontoInvalidoException {

		verificarPedido(pedido);
		pedido.quitarItem(indice);
		pedidoDAO.actualizar(pedido);
	}

	public void aplicarDescuento(Pedido pedido, Descuento descuento)
			throws AccesoDatosException, PedidoCerradoException, MontoInvalidoException {

		verificarPedido(pedido);
		if (!pedido.estaAbierto()) {
			throw new PedidoCerradoException("La cuenta ya esta cerrada: no admite descuentos.");
		}
		pedido.setDescuento(descuento);
		pedidoDAO.actualizar(pedido);
	}

	// ==================================================================
	// CIERRE
	// ==================================================================

	/** Cobra la cuenta: el pedido queda CERRADO y la mesa se libera. */
	public void cerrarCuenta(Pedido pedido)
			throws AccesoDatosException, PedidoCerradoException, MontoInvalidoException {

		verificarPedido(pedido);
		pedido.cerrar();
		pedidoDAO.actualizar(pedido);
		liberarMesa(pedido);
	}

	/** Anula la cuenta: la mesa se libera pero no se registra facturacion. */
	public void anularCuenta(Pedido pedido)
			throws AccesoDatosException, PedidoCerradoException, MontoInvalidoException {

		verificarPedido(pedido);
		pedido.anular();
		pedidoDAO.actualizar(pedido);
		liberarMesa(pedido);
	}

	private void liberarMesa(Pedido pedido) throws AccesoDatosException {
		pedido.getMesa().setOcupada(false);
		mesaDAO.actualizarOcupada(pedido.getMesa().getNumero(), false);
	}

	private void verificarPedido(Pedido pedido) throws MontoInvalidoException {
		if (pedido == null) {
			throw new MontoInvalidoException("No hay ninguna cuenta abierta seleccionada.");
		}
	}

	// ==================================================================
	// CONSULTAS E HISTORIAL
	// ==================================================================

	public List<Pedido> listarCuentasAbiertas() throws AccesoDatosException {
		return pedidoDAO.listarPorEstado(EstadoPedido.ABIERTO);
	}

	public List<Pedido> listarHistorial() throws AccesoDatosException {
		return pedidoDAO.listarTodos();
	}

	public Pedido buscarPedido(int id) throws AccesoDatosException {
		return pedidoDAO.buscarPorId(id);
	}

	// ==================================================================
	// REPORTES
	// ==================================================================

	public ReporteVentas reporteFacturacion(LocalDate desde, LocalDate hasta)
			throws AccesoDatosException, MontoInvalidoException {
		validarPeriodo(desde, hasta);
		return reporteDAO.facturacionPorPeriodo(desde, hasta);
	}

	public ReporteVentas reporteProductosMasVendidos(LocalDate desde, LocalDate hasta)
			throws AccesoDatosException, MontoInvalidoException {
		validarPeriodo(desde, hasta);
		return reporteDAO.rankingProductos(desde, hasta);
	}

	public ReporteVentas reporteVentasPorMozo(LocalDate desde, LocalDate hasta)
			throws AccesoDatosException, MontoInvalidoException {
		validarPeriodo(desde, hasta);
		return reporteDAO.ventasPorMozo(desde, hasta);
	}

	public ReporteVentas reporteTicketPromedio(LocalDate desde, LocalDate hasta)
			throws AccesoDatosException, MontoInvalidoException {
		validarPeriodo(desde, hasta);
		return reporteDAO.ticketPromedio(desde, hasta);
	}

	private void validarPeriodo(LocalDate desde, LocalDate hasta) throws MontoInvalidoException {
		if (desde == null || hasta == null) {
			throw new MontoInvalidoException("Debe indicar las dos fechas del periodo.");
		}
		if (desde.isAfter(hasta)) {
			throw new MontoInvalidoException("La fecha 'desde' no puede ser posterior a 'hasta'.");
		}
	}
}
