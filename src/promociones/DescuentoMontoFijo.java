package promociones;

/**
 * Descuento de un importe fijo (por ejemplo un cupon de $2000).
 * Nunca descuenta mas que el subtotal, para que el total no quede negativo.
 */
public class DescuentoMontoFijo implements Descuento {

	private double monto;

	public DescuentoMontoFijo(double monto) {
		if (monto < 0) {
			monto = 0;
		}
		this.monto = monto;
	}

	@Override
	public double calcularDescuento(double subtotal) {
		if (monto > subtotal) {
			return subtotal;
		}
		return monto;
	}

	@Override
	public String getDescripcion() {
		return "Cupon de $" + String.format("%.2f", monto);
	}

	public double getMonto() {
		return monto;
	}

	@Override
	public String toString() {
		return getDescripcion();
	}
}
