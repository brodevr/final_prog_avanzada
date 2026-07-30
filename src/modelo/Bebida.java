package modelo;

/**
 * Producto servido en la barra.
 *
 * Su regla de precio: las bebidas con alcohol llevan un recargo del 15% por
 * servicio de barra; las sin alcohol se venden a precio base.
 *
 * Notar que calcularPrecioFinal tiene la misma firma que en Plato pero una
 * regla distinta: eso es polimorfismo.
 */
public class Bebida extends ItemMenu {

	private static final double RECARGO_BARRA = 0.15;
	private static final int MINUTOS_SERVICIO = 2;

	private int mililitros;
	private boolean alcoholica;

	public Bebida(int id, String nombre, double precioBase, boolean disponible,
			int mililitros, boolean alcoholica) {
		super(id, nombre, precioBase, disponible);
		this.mililitros = mililitros;
		this.alcoholica = alcoholica;
	}

	public Bebida(String nombre, double precioBase, int mililitros, boolean alcoholica) {
		this(0, nombre, precioBase, true, mililitros, alcoholica);
	}

	@Override
	public double calcularPrecioFinal() {
		if (alcoholica) {
			return getPrecioBase() * (1 + RECARGO_BARRA);
		}
		return getPrecioBase();
	}

	@Override
	public String getDescripcionDetallada() {
		String tipoBebida = alcoholica ? "Bebida con alcohol" : "Bebida sin alcohol";
		return tipoBebida + " - " + mililitros + " ml";
	}

	@Override
	public int getMinutosDemora() {
		return MINUTOS_SERVICIO;
	}

	@Override
	public String getTipo() {
		return "BEBIDA";
	}

	public int getMililitros() {
		return mililitros;
	}

	public void setMililitros(int mililitros) {
		this.mililitros = mililitros;
	}

	public boolean isAlcoholica() {
		return alcoholica;
	}

	public void setAlcoholica(boolean alcoholica) {
		this.alcoholica = alcoholica;
	}
}
