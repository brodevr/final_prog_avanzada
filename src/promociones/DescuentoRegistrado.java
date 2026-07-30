package promociones;

/**
 * Descuento reconstruido desde la base de datos.
 *
 * Cuando se guarda un pedido no se guarda el objeto estrategia, sino el importe
 * descontado y su descripcion. Al volver a leer el pedido se usa esta clase
 * para reponer exactamente el descuento que se habia aplicado, sin necesidad de
 * saber cual de las estrategias lo genero.
 */
public class DescuentoRegistrado implements Descuento {

	private double monto;
	private String descripcion;

	public DescuentoRegistrado(double monto, String descripcion) {
		this.monto = monto < 0 ? 0 : monto;
		this.descripcion = (descripcion == null || descripcion.isEmpty())
				? "Descuento aplicado" : descripcion;
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
		return descripcion;
	}

	@Override
	public String toString() {
		return descripcion;
	}
}
