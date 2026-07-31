package modelo;

/**
 * Producto ofrecido por el cafe. Es la clase abstracta del sistema.
 *
 * Es abstracta porque un "item de menu" generico no existe en la realidad:
 * lo que existe son platos y bebidas. Concentra el estado y el comportamiento
 * comun (id, nombre, precio base, disponibilidad) y delega en las subclases
 * las reglas que cambian segun el tipo de producto.
 */
public abstract class ItemMenu {

	private int id;
	private String nombre;
	private double precioBase;
	private boolean disponible;

	/** Constructor completo, usado por el DAO al leer de la base de datos. */
	public ItemMenu(int id, String nombre, double precioBase, boolean disponible) {
		this.id = id;
		this.nombre = nombre;
		this.precioBase = precioBase;
		this.disponible = disponible;
	}

	/** Constructor para productos nuevos, todavia sin id asignado. */
	public ItemMenu(String nombre, double precioBase) {
		this(0, nombre, precioBase, true);
	}

	/**
	 * Precio de venta al publico. Cada subclase aplica su propio recargo.
	 * Este es el metodo que hace posible el polimorfismo del sistema.
	 */
	public abstract double calcularPrecioFinal();

	/** Descripcion ampliada, con los datos propios de cada tipo de producto. */
	public abstract String getDescripcionDetallada();

	/** Minutos que demora en estar listo el producto. */
	public abstract int getMinutosDemora();

	/** Discriminador que se guarda en la columna 'tipo' de la tabla item_menu. */
	public abstract String getTipo();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public double getPrecioBase() {
		return precioBase;
	}

	public void setPrecioBase(double precioBase) {
		this.precioBase = precioBase;
	}

	public boolean isDisponible() {
		return disponible;
	}

	public void setDisponible(boolean disponible) {
		this.disponible = disponible;
	}

	@Override
	public String toString() {
		return nombre + " - $" + String.format("%.2f", calcularPrecioFinal());
	}
}
