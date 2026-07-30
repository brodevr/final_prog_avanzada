package promociones;

/**
 * Estrategia por defecto: no aplica ningun descuento.
 *
 * Tenerla evita que Pedido tenga que preguntar "si el descuento es null...".
 * Es el mismo criterio del patron Null Object.
 */
public class SinDescuento implements Descuento {

	@Override
	public double calcularDescuento(double subtotal) {
		return 0;
	}

	@Override
	public String getDescripcion() {
		return "Sin descuento";
	}

	@Override
	public String toString() {
		return getDescripcion();
	}
}
