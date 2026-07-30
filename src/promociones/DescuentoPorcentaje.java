package promociones;

/**
 * Descuento proporcional al subtotal (por ejemplo 10% para el personal).
 */
public class DescuentoPorcentaje implements Descuento {

	private double porcentaje;

	public DescuentoPorcentaje(double porcentaje) {
		if (porcentaje < 0) {
			porcentaje = 0;
		}
		if (porcentaje > 100) {
			porcentaje = 100;
		}
		this.porcentaje = porcentaje;
	}

	@Override
	public double calcularDescuento(double subtotal) {
		return subtotal * porcentaje / 100;
	}

	@Override
	public String getDescripcion() {
		return "Descuento " + (int) porcentaje + "%";
	}

	public double getPorcentaje() {
		return porcentaje;
	}

	@Override
	public String toString() {
		return getDescripcion();
	}
}
