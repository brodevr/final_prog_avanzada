package modelo;

/**
 * Mozo o encargado que opera el sistema.
 */
public class Empleado {

	private int id;
	private String nombre;
	private String usuario;
	private String clave;
	private boolean activo;
	private Rol rol;

	public Empleado(int id, String nombre, String usuario, String clave, boolean activo, Rol rol) {
		this.id = id;
		this.nombre = nombre;
		this.usuario = usuario;
		this.clave = clave;
		this.activo = activo;
		this.rol = (rol != null ? rol : Rol.EMPLEADO);
	}

	/** Alta rapida de un mozo: rol EMPLEADO y activo por defecto. */
	public Empleado(String nombre, String usuario, String clave) {
		this(0, nombre, usuario, clave, true, Rol.EMPLEADO);
	}

	public Empleado(String nombre, String usuario, String clave, Rol rol) {
		this(0, nombre, usuario, clave, true, rol);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public Rol getRol() {
		return rol;
	}

	public void setRol(Rol rol) {
		this.rol = (rol != null ? rol : Rol.EMPLEADO);
	}

	/** Atajo para las validaciones de permisos de la vista. */
	public boolean esAdmin() {
		return rol == Rol.ADMIN;
	}

	@Override
	public String toString() {
		return nombre;
	}
}
