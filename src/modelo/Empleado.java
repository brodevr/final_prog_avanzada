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

	public Empleado(int id, String nombre, String usuario, String clave, boolean activo) {
		this.id = id;
		this.nombre = nombre;
		this.usuario = usuario;
		this.clave = clave;
		this.activo = activo;
	}

	public Empleado(String nombre, String usuario, String clave) {
		this(0, nombre, usuario, clave, true);
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

	@Override
	public String toString() {
		return nombre;
	}
}
