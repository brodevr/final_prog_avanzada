package excepciones;

/**
 * Se lanza cuando se intenta abrir una mesa que ya tiene un pedido activo.
 * Protege la regla de negocio "una mesa no puede tener dos cuentas abiertas".
 */
public class MesaOcupadaException extends Exception {

	private static final long serialVersionUID = 1L;

	public MesaOcupadaException(String mensaje) {
		super(mensaje);
	}
}
