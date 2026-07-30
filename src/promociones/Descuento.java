package promociones;

/**
 * Estrategia de descuento aplicable a un pedido (patron Strategy).
 *
 * El pedido no sabe COMO se calcula el descuento: solo le pide a su estrategia
 * que lo calcule. Agregar una promocion nueva es crear una clase mas que
 * implemente esta interfaz, sin tocar la clase Pedido.
 */
public interface Descuento {

	/** Devuelve el importe a descontar sobre el subtotal recibido. */
	double calcularDescuento(double subtotal);

	/** Texto corto para mostrar en el ticket y en los combos de la vista. */
	String getDescripcion();
}
