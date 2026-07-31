package modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import excepciones.ItemNoDisponibleException;
import excepciones.MontoInvalidoException;
import excepciones.PedidoCerradoException;
import promociones.Descuento;
import promociones.SinDescuento;
import util.ColeccionUtil;

/**
 * Cuenta de una mesa. Es la entidad central del sistema y la que implementa el
 * ciclo de apertura y cierre que pide la consigna:
 *
 *   se abre la mesa  -> el pedido nace ABIERTO
 *   se cargan items  -> solo permitido mientras esta ABIERTO
 *   se cobra         -> pasa a CERRADO y se libera la mesa
 *   o se cancela     -> pasa a ANULADO
 *
 * Implementa Imprimible porque sabe generar su propio ticket de consumo.
 */
public class Pedido implements Imprimible {

	private int id;
	private Mesa mesa;
	private Empleado empleado;
	private LocalDateTime fechaApertura;
	private LocalDateTime fechaCierre;
	private EstadoPedido estado;
	private List<DetallePedido> detalles;
	private Descuento descuento;

	/** Constructor completo, usado por el DAO al leer de la base de datos. */
	public Pedido(int id, Mesa mesa, Empleado empleado, LocalDateTime fechaApertura,
			LocalDateTime fechaCierre, EstadoPedido estado) {
		this.id = id;
		this.mesa = mesa;
		this.empleado = empleado;
		this.fechaApertura = fechaApertura;
		this.fechaCierre = fechaCierre;
		this.estado = estado;
		this.detalles = new ArrayList<DetallePedido>();
		this.descuento = new SinDescuento();
	}

	/** Apertura de una mesa: el pedido nace abierto, ahora, y sin consumos. */
	public Pedido(Mesa mesa, Empleado empleado) {
		this(0, mesa, empleado, LocalDateTime.now(), null, EstadoPedido.ABIERTO);
	}

	// ------------------------------------------------------------------
	// Operaciones del ciclo de vida
	// ------------------------------------------------------------------

	/**
	 * Carga un producto en la comanda. Si el producto ya estaba cargado, suma
	 * la cantidad en lugar de duplicar la linea.
	 */
	public void agregarItem(ItemMenu item, int cantidad)
			throws PedidoCerradoException, ItemNoDisponibleException, MontoInvalidoException {

		verificarAbierto();

		if (item == null) {
			throw new ItemNoDisponibleException("No se selecciono ningun producto.");
		}
		if (!item.isDisponible()) {
			throw new ItemNoDisponibleException(
					"El producto '" + item.getNombre() + "' no esta disponible.");
		}
		if (cantidad <= 0) {
			throw new MontoInvalidoException("La cantidad debe ser mayor que cero.");
		}

		DetallePedido existente = buscarDetallePorItem(item);
		if (existente != null) {
			existente.setCantidad(existente.getCantidad() + cantidad);
		} else {
			detalles.add(new DetallePedido(item, cantidad));
		}
	}

	/** Quita una linea de la comanda por su posicion en la lista. */
	public void quitarItem(int indice) throws PedidoCerradoException, MontoInvalidoException {
		verificarAbierto();
		if (indice < 0 || indice >= detalles.size()) {
			throw new MontoInvalidoException("No hay ninguna linea seleccionada.");
		}
		detalles.remove(indice);
	}

	/** Cierra la cuenta: el pedido pasa a CERRADO y queda historico. */
	public void cerrar() throws PedidoCerradoException, MontoInvalidoException {
		verificarAbierto();
		if (detalles.isEmpty()) {
			throw new MontoInvalidoException(
					"No se puede cobrar una cuenta sin consumos. Anule la mesa en su lugar.");
		}
		this.estado = EstadoPedido.CERRADO;
		this.fechaCierre = LocalDateTime.now();
	}

	/** Anula el pedido: la mesa se libera pero no se registra facturacion. */
	public void anular() throws PedidoCerradoException {
		verificarAbierto();
		this.estado = EstadoPedido.ANULADO;
		this.fechaCierre = LocalDateTime.now();
	}

	/**
	 * Guardia del ciclo de vida. Todas las operaciones que modifican el pedido
	 * pasan por aca, de modo que la regla esta escrita una sola vez.
	 */
	private void verificarAbierto() throws PedidoCerradoException {
		if (estado != EstadoPedido.ABIERTO) {
			throw new PedidoCerradoException("El pedido #" + id + " esta " + estado
					+ " y no admite modificaciones.");
		}
	}

	public boolean estaAbierto() {
		return estado == EstadoPedido.ABIERTO;
	}

	private DetallePedido buscarDetallePorItem(ItemMenu item) {
		for (int i = 0; i < detalles.size(); i++) {
			if (detalles.get(i).getItem().getId() == item.getId()) {
				return detalles.get(i);
			}
		}
		return null;
	}

	// ------------------------------------------------------------------
	// Calculos
	// ------------------------------------------------------------------

	public double calcularSubtotal() {
		double acumulado = 0;
		for (int i = 0; i < detalles.size(); i++) {
			acumulado += detalles.get(i).calcularSubtotal();
		}
		return acumulado;
	}

	public double calcularDescuento() {
		return descuento.calcularDescuento(calcularSubtotal());
	}

	public double calcularTotal() {
		return calcularSubtotal() - calcularDescuento();
	}

	/**
	 * Demora estimada de la comanda, en minutos.
	 *
	 * Este metodo concentra tres requisitos de la consigna en pocas lineas:
	 *  - recorre una coleccion generica (List) de items heterogeneos,
	 *  - llama polimorficamente a getMinutosDemora() sin preguntar el tipo
	 *    concreto (un Plato devuelve su tiempo de coccion, una Bebida devuelve
	 *    los 2 minutos de barra),
	 *  - resuelve el maximo con el metodo generico ColeccionUtil.obtenerMaximo.
	 *
	 * Se toma el maximo y no la suma porque en un cafe la comanda sale
	 * completa a la mesa: la demora es la del producto mas lento.
	 */
	public int calcularDemoraEstimada() {
		List<Integer> demoras = new ArrayList<Integer>();
		for (int i = 0; i < detalles.size(); i++) {
			demoras.add(Integer.valueOf(detalles.get(i).getItem().getMinutosDemora()));
		}
		Integer maxima = ColeccionUtil.obtenerMaximo(demoras);
		if (maxima == null) {
			return 0;
		}
		return maxima.intValue();
	}

	public int contarUnidades() {
		int total = 0;
		for (int i = 0; i < detalles.size(); i++) {
			total += detalles.get(i).getCantidad();
		}
		return total;
	}

	// ------------------------------------------------------------------
	// Imprimible
	// ------------------------------------------------------------------

	@Override
	public String generarTexto() {
		DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		StringBuilder sb = new StringBuilder();

		sb.append("==================================================\n");
		sb.append("            CAFE LA ESQUINA\n");
		sb.append("         Ticket de consumo - NO FISCAL\n");
		sb.append("==================================================\n");
		sb.append("Pedido N.  : ").append(id).append("\n");
		sb.append("Mesa       : ").append(mesa.getNumero());
		sb.append("  (").append(mesa.getSector()).append(")\n");
		sb.append("Lo atendio : ").append(empleado.getNombre()).append("\n");
		sb.append("Apertura   : ").append(fechaApertura.format(formato)).append("\n");
		if (fechaCierre != null) {
			sb.append("Cierre     : ").append(fechaCierre.format(formato)).append("\n");
		}
		sb.append("Estado     : ").append(estado).append("\n");
		sb.append("--------------------------------------------------\n");
		sb.append(String.format("%-22s %3s %10s %11s%n", "PRODUCTO", "CAN", "P.UNIT", "SUBTOTAL"));
		sb.append("--------------------------------------------------\n");

		for (int i = 0; i < detalles.size(); i++) {
			DetallePedido detalle = detalles.get(i);
			sb.append(String.format("%-22s %3d %10.2f %11.2f%n",
					recortar(detalle.getItem().getNombre(), 22),
					detalle.getCantidad(),
					detalle.getPrecioUnitario(),
					detalle.calcularSubtotal()));
		}

		sb.append("--------------------------------------------------\n");
		sb.append(String.format("%-37s %11.2f%n", "SUBTOTAL", calcularSubtotal()));
		if (calcularDescuento() > 0) {
			sb.append(String.format("%-37s %11.2f%n",
					descuento.getDescripcion(), -calcularDescuento()));
		}
		sb.append(String.format("%-37s %11.2f%n", "TOTAL A PAGAR", calcularTotal()));
		sb.append("--------------------------------------------------\n");
		sb.append("Unidades consumidas: ").append(contarUnidades()).append("\n");
		sb.append("\n           Gracias por su visita!\n");
		sb.append("==================================================\n");

		return sb.toString();
	}

	/** Corta un texto para que no rompa el ancho de las columnas del ticket. */
	private String recortar(String texto, int maximo) {
		if (texto == null) {
			return "";
		}
		if (texto.length() <= maximo) {
			return texto;
		}
		return texto.substring(0, maximo - 1) + ".";
	}

	// ------------------------------------------------------------------
	// Getters y setters
	// ------------------------------------------------------------------

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Mesa getMesa() {
		return mesa;
	}

	public void setMesa(Mesa mesa) {
		this.mesa = mesa;
	}

	public Empleado getEmpleado() {
		return empleado;
	}

	public void setEmpleado(Empleado empleado) {
		this.empleado = empleado;
	}

	public LocalDateTime getFechaApertura() {
		return fechaApertura;
	}

	public void setFechaApertura(LocalDateTime fechaApertura) {
		this.fechaApertura = fechaApertura;
	}

	public LocalDateTime getFechaCierre() {
		return fechaCierre;
	}

	public void setFechaCierre(LocalDateTime fechaCierre) {
		this.fechaCierre = fechaCierre;
	}

	public EstadoPedido getEstado() {
		return estado;
	}

	public void setEstado(EstadoPedido estado) {
		this.estado = estado;
	}

	public List<DetallePedido> getDetalles() {
		return detalles;
	}

	public void setDetalles(List<DetallePedido> detalles) {
		this.detalles = detalles;
	}

	public Descuento getDescuento() {
		return descuento;
	}

	public void setDescuento(Descuento descuento) {
		if (descuento == null) {
			this.descuento = new SinDescuento();
		} else {
			this.descuento = descuento;
		}
	}

	@Override
	public String toString() {
		return "Pedido #" + id + " - Mesa " + mesa.getNumero() + " - " + estado;
	}
}
