package excepciones;

/**
 * Se lanza cuando un valor numerico ingresado por el usuario no es valido:
 * cantidades menores o iguales a cero, precios negativos, capacidades invalidas.
 */
public class MontoInvalidoException extends Exception {

	private static final long serialVersionUID = 1L;

	public MontoInvalidoException(String mensaje) {
		super(mensaje);
	}
}
