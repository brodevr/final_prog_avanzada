package modelo;

/**
 * Una linea de la comanda: un producto, una cantidad y el precio al que se
 * vendio.
 *
 * DECISION DE DISENO IMPORTANTE: precioUnitario se guarda como copia y no se
 * lee del item cada vez. Si manana el restaurante cambia el precio de la
 * milanesa, los tickets ya emitidos deben seguir mostrando el precio viejo.
 * Congelar el precio en el detalle es lo que garantiza esa integridad
 * historica.
 */
public class DetallePedido {

	private int id;
	private ItemMenu item;
	private int cantidad;
	private double precioUnitario;

	/** Constructor completo, usado por el DAO al leer de la base de datos. */
	public DetallePedido(int id, ItemMenu item, int cantidad, double precioUnitario) {
		this.id = id;
		this.item = item;
		this.cantidad = cantidad;
		this.precioUnitario = precioUnitario;
	}

	/**
	 * Constructor para una linea nueva: toma el precio vigente del producto.
	 * Aca ocurre una llamada polimorfica: no sabemos si item es un Plato o una
	 * Bebida, y cada uno calcula su precio final con su propia regla.
	 */
	public DetallePedido(ItemMenu item, int cantidad) {
		this(0, item, cantidad, item.calcularPrecioFinal());
	}

	public double calcularSubtotal() {
		return precioUnitario * cantidad;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ItemMenu getItem() {
		return item;
	}

	public void setItem(ItemMenu item) {
		this.item = item;
	}

	public int getCantidad() {
		return cantidad;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}

	public double getPrecioUnitario() {
		return precioUnitario;
	}

	public void setPrecioUnitario(double precioUnitario) {
		this.precioUnitario = precioUnitario;
	}

	@Override
	public String toString() {
		return cantidad + " x " + item.getNombre() + " = $" + String.format("%.2f", calcularSubtotal());
	}
}
