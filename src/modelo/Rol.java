package modelo;

/**
 * Perfil con el que un empleado opera el sistema.
 *
 *   ADMIN    --> encargado: ademas de atender mesas administra la carta,
 *                el personal y consulta los reportes.
 *   EMPLEADO --> mozo: solo trabaja sobre el salon y las comandas.
 *
 * El rol se guarda en la base como texto (columna ENUM de MySQL), asi que la
 * conversion de ida y vuelta pasa siempre por esta clase.
 */
public enum Rol {

	ADMIN("Administrador"),
	EMPLEADO("Empleado");

	private final String etiqueta;

	private Rol(String etiqueta) {
		this.etiqueta = etiqueta;
	}

	/** Nombre legible, para mostrar en pantalla. */
	public String getEtiqueta() {
		return etiqueta;
	}

	/**
	 * Convierte el texto guardado en la base al valor del enum. Si el dato
	 * viene nulo o desconocido devuelve EMPLEADO, que es el perfil con menos
	 * permisos: ante la duda, no se abre el acceso a la administracion.
	 */
	public static Rol desdeTexto(String texto) {
		if (texto != null) {
			for (Rol rol : values()) {
				if (rol.name().equalsIgnoreCase(texto.trim())) {
					return rol;
				}
			}
		}
		return EMPLEADO;
	}

	@Override
	public String toString() {
		return etiqueta;
	}
}
