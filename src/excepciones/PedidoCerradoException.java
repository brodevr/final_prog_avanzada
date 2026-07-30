package excepciones;

/**
 * Se lanza cuando se intenta modificar, cerrar o anular un pedido que ya no
 * esta abierto. Es la excepcion que protege el ciclo de apertura y cierre.
 */
public class PedidoCerradoException extends Exception {

	private static final long serialVersionUID = 1L;

	public PedidoCerradoException(String mensaje) {
		super(mensaje);
	}
}
