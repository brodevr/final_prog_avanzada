package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import excepciones.AccesoDatosException;

/**
 * Punto unico de acceso a la base de datos. Implementa el patron SINGLETON.
 *
 * POR QUE SINGLETON:
 * abrir una conexion a MySQL es una operacion costosa. Si cada DAO abriera la
 * suya, la aplicacion tendria decenas de conexiones vivas contra el mismo
 * servidor. El Singleton garantiza que exista una sola instancia compartida
 * por toda la aplicacion.
 *
 * COMO SE IMPLEMENTA:
 *  1. el constructor es privado, de modo que nadie pueda hacer new ConexionBD(),
 *  2. la propia clase guarda la unica instancia en un atributo static,
 *  3. el acceso se hace por getInstancia(), que la crea la primera vez que se
 *     la pide y despues devuelve siempre la misma.
 */
public class ConexionBD {

	// ------------------------------------------------------------------
	// AJUSTAR ESTOS TRES VALORES SI TU MySQL TIENE OTRO USUARIO O CLAVE
	// ------------------------------------------------------------------
	private static final String URL =
			"jdbc:mysql://localhost:3306/cafe_final?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
	private static final String USUARIO = "root";
	private static final String CLAVE = "";

	private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

	/** La unica instancia de la clase. */
	private static ConexionBD instancia;

	private Connection conexion;

	/** Constructor privado: nadie puede instanciar la clase desde afuera. */
	private ConexionBD() throws AccesoDatosException {
		abrirConexion();
	}

	/** Unica puerta de entrada a la instancia. */
	public static ConexionBD getInstancia() throws AccesoDatosException {
		if (instancia == null) {
			instancia = new ConexionBD();
		}
		return instancia;
	}

	private void abrirConexion() throws AccesoDatosException {
		try {
			Class.forName(DRIVER);
			this.conexion = DriverManager.getConnection(URL, USUARIO, CLAVE);
		} catch (ClassNotFoundException e) {
			throw new AccesoDatosException(
					"No se encontro el driver de MySQL. Verifique que el archivo "
					+ "mysql-connector-j.jar este agregado al Build Path del proyecto.", e);
		} catch (SQLException e) {
			throw new AccesoDatosException(
					"No se pudo conectar a la base de datos 'cafe_final'. "
					+ "Verifique que el servidor MySQL este iniciado y que el usuario "
					+ "y la clave configurados en ConexionBD sean correctos.", e);
		}
	}

	/**
	 * Devuelve la conexion activa. Si se cerro por inactividad, la reabre.
	 */
	public Connection getConexion() throws AccesoDatosException {
		try {
			if (conexion == null || conexion.isClosed()) {
				abrirConexion();
			}
		} catch (SQLException e) {
			throw new AccesoDatosException("Error al verificar el estado de la conexion.", e);
		}
		return conexion;
	}

	/** Cierra la conexion al terminar la aplicacion. */
	public void cerrar() {
		try {
			if (conexion != null && !conexion.isClosed()) {
				conexion.close();
			}
		} catch (SQLException e) {
			System.err.println("Error al cerrar la conexion: " + e.getMessage());
		}
	}
}
