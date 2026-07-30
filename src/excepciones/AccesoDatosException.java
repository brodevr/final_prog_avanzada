package excepciones;

/**
 * Excepcion que envuelve cualquier problema de acceso a la base de datos.
 *
 * Su razon de ser es que las capas superiores (controlador y vista) no deben
 * conocer JDBC. Los DAO capturan SQLException y la re-lanzan como
 * AccesoDatosException, de modo que la vista trabaja siempre con excepciones
 * propias del dominio.
 */
public class AccesoDatosException extends Exception {

	private static final long serialVersionUID = 1L;

	public AccesoDatosException(String mensaje) {
		super(mensaje);
	}

	public AccesoDatosException(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}
}
