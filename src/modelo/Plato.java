package modelo;

/**
 * Producto elaborado en la cocina.
 *
 * Su regla de precio: los platos principales llevan un recargo del 10% por
 * servicio de cocina; las entradas se venden a precio base.
 */
public class Plato extends ItemMenu {

	private static final double RECARGO_COCINA = 0.10;

	private int minutosPreparacion;
	private boolean entrada;

	public Plato(int id, String nombre, double precioBase, boolean disponible,
			int minutosPreparacion, boolean entrada) {
		super(id, nombre, precioBase, disponible);
		this.minutosPreparacion = minutosPreparacion;
		this.entrada = entrada;
	}

	public Plato(String nombre, double precioBase, int minutosPreparacion, boolean entrada) {
		this(0, nombre, precioBase, true, minutosPreparacion, entrada);
	}

	@Override
	public double calcularPrecioFinal() {
		if (entrada) {
			return getPrecioBase();
		}
		return getPrecioBase() * (1 + RECARGO_COCINA);
	}

	@Override
	public String getDescripcionDetallada() {
		String categoria = entrada ? "Entrada" : "Plato principal";
		return categoria + " - listo en " + minutosPreparacion + " min";
	}

	@Override
	public int getMinutosDemora() {
		return minutosPreparacion;
	}

	@Override
	public String getTipo() {
		return "PLATO";
	}

	public int getMinutosPreparacion() {
		return minutosPreparacion;
	}

	public void setMinutosPreparacion(int minutosPreparacion) {
		this.minutosPreparacion = minutosPreparacion;
	}

	public boolean isEntrada() {
		return entrada;
	}

	public void setEntrada(boolean entrada) {
		this.entrada = entrada;
	}
}
