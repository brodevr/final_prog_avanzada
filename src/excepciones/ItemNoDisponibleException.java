package excepciones;

/**
 * Se lanza cuando se intenta cargar en una comanda un producto que esta
 * marcado como no disponible, o cuando no se selecciono ningun producto.
 */
public class ItemNoDisponibleException extends Exception {

	private static final long serialVersionUID = 1L;

	public ItemNoDisponibleException(String mensaje) {
		super(mensaje);
	}
}
