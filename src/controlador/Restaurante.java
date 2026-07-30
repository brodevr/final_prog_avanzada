package controlador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dao.EmpleadoDAO;
import dao.ItemMenuDAO;
import dao.MesaDAO;
import excepciones.AccesoDatosException;
import excepciones.MontoInvalidoException;
import modelo.Bebida;
import modelo.Empleado;
import modelo.ItemMenu;
import modelo.Mesa;
import modelo.Plato;

/**
 * Controlador de configuracion del sistema: login y ABM de mesas, menu y
 * empleados. Implementa SINGLETON.
 *
 * POR QUE SINGLETON ACA TAMBIEN: la configuracion del restaurante es unica.
 * No tiene sentido que existan dos objetos Restaurante con dos menues
 * distintos. Ademas permite mantener en memoria el mapa de mesas sin volver a
 * consultarlo desde cada pantalla.
 *
 * Su responsabilidad es VALIDAR las reglas de negocio antes de tocar los DAO.
 * La vista no valida datos: solo los recolecta y los pasa.
 */
public class Restaurante {

	private static Restaurante instancia;

	private EmpleadoDAO empleadoDAO;
	private MesaDAO mesaDAO;
	private ItemMenuDAO itemMenuDAO;

	/** Cache de mesas por numero. Cumple el requisito de usar un HashMap. */
	private Map<Integer, Mesa> mesasPorNumero;

	private Restaurante() {
		this.empleadoDAO = new EmpleadoDAO();
		this.mesaDAO = new MesaDAO();
		this.itemMenuDAO = new ItemMenuDAO();
		this.mesasPorNumero = new HashMap<Integer, Mesa>();
	}

	public static Restaurante getInstancia() {
		if (instancia == null) {
			instancia = new Restaurante();
		}
		return instancia;
	}

	// ==================================================================
	// LOGIN
	// ==================================================================

	public Empleado iniciarSesion(String usuario, String clave)
			throws AccesoDatosException, MontoInvalidoException {

		if (usuario == null || usuario.trim().isEmpty()) {
			throw new MontoInvalidoException("Ingrese el usuario.");
		}
		if (clave == null || clave.isEmpty()) {
			throw new MontoInvalidoException("Ingrese la clave.");
		}
		return empleadoDAO.buscarPorCredenciales(usuario.trim(), clave);
	}

	// ==================================================================
	// ABM DE MESAS
	// ==================================================================

	public void altaMesa(int numero, int capacidad, String sector)
			throws AccesoDatosException, MontoInvalidoException {

		if (numero <= 0) {
			throw new MontoInvalidoException("El numero de mesa debe ser mayor que cero.");
		}
		if (capacidad <= 0) {
			throw new MontoInvalidoException("La capacidad debe ser mayor que cero.");
		}
		if (mesaDAO.buscarPorNumero(numero) != null) {
			throw new MontoInvalidoException("Ya existe la mesa numero " + numero + ".");
		}
		mesaDAO.insertar(new Mesa(numero, capacidad, sector));
		refrescarMesas();
	}

	public void modificarMesa(int numero, int capacidad, String sector)
			throws AccesoDatosException, MontoInvalidoException {

		Mesa mesa = mesaDAO.buscarPorNumero(numero);
		if (mesa == null) {
			throw new MontoInvalidoException("No existe la mesa numero " + numero + ".");
		}
		if (capacidad <= 0) {
			throw new MontoInvalidoException("La capacidad debe ser mayor que cero.");
		}
		mesa.setCapacidad(capacidad);
		mesa.setSector(sector);
		mesaDAO.actualizar(mesa);
		refrescarMesas();
	}

	public void eliminarMesa(int numero) throws AccesoDatosException, MontoInvalidoException {
		Mesa mesa = mesaDAO.buscarPorNumero(numero);
		if (mesa == null) {
			throw new MontoInvalidoException("No existe la mesa numero " + numero + ".");
		}
		if (mesa.isOcupada()) {
			throw new MontoInvalidoException(
					"La mesa esta ocupada. Cierre o anule la cuenta antes de eliminarla.");
		}
		mesaDAO.eliminar(numero);
		refrescarMesas();
	}

	public List<Mesa> listarMesas() throws AccesoDatosException {
		return mesaDAO.listarTodas();
	}

	public Mesa buscarMesa(int numero) throws AccesoDatosException {
		return mesaDAO.buscarPorNumero(numero);
	}

	/** Recarga el cache de mesas desde la base de datos. */
	public void refrescarMesas() throws AccesoDatosException {
		mesasPorNumero.clear();
		List<Mesa> mesas = mesaDAO.listarTodas();
		for (int i = 0; i < mesas.size(); i++) {
			Mesa mesa = mesas.get(i);
			mesasPorNumero.put(Integer.valueOf(mesa.getNumero()), mesa);
		}
	}

	/**
	 * Devuelve las mesas guardadas en el cache, ordenadas por numero.
	 *
	 * El HashMap da acceso inmediato a una mesa por su numero, pero no
	 * conserva ningun orden. Como el salon se dibuja de la mesa 1 en adelante,
	 * aca se ordenan las claves antes de devolver la lista. Es el compromiso
	 * tipico del HashMap: se gana velocidad de busqueda y se pierde el orden.
	 */
	public List<Mesa> listarMesasDeCache() throws AccesoDatosException {
		if (mesasPorNumero.isEmpty()) {
			refrescarMesas();
		}

		List<Integer> numeros = new ArrayList<Integer>(mesasPorNumero.keySet());
		Collections.sort(numeros);

		List<Mesa> ordenadas = new ArrayList<Mesa>();
		for (int i = 0; i < numeros.size(); i++) {
			ordenadas.add(mesasPorNumero.get(numeros.get(i)));
		}
		return ordenadas;
	}

	/** Busqueda directa por numero, sin volver a consultar la base de datos. */
	public Mesa obtenerMesaDeCache(int numero) throws AccesoDatosException {
		if (mesasPorNumero.isEmpty()) {
			refrescarMesas();
		}
		return mesasPorNumero.get(Integer.valueOf(numero));
	}

	public Map<Integer, Mesa> getMesasPorNumero() {
		return mesasPorNumero;
	}

	// ==================================================================
	// ABM DEL MENU
	// ==================================================================

	public void altaPlato(String nombre, double precioBase, int minutosPreparacion, boolean entrada)
			throws AccesoDatosException, MontoInvalidoException {

		validarProducto(nombre, precioBase);
		if (minutosPreparacion < 0) {
			throw new MontoInvalidoException("Los minutos de preparacion no pueden ser negativos.");
		}
		itemMenuDAO.insertar(new Plato(nombre.trim(), precioBase, minutosPreparacion, entrada));
	}

	public void altaBebida(String nombre, double precioBase, int mililitros, boolean alcoholica)
			throws AccesoDatosException, MontoInvalidoException {

		validarProducto(nombre, precioBase);
		if (mililitros <= 0) {
			throw new MontoInvalidoException("Los mililitros deben ser mayores que cero.");
		}
		itemMenuDAO.insertar(new Bebida(nombre.trim(), precioBase, mililitros, alcoholica));
	}

	public void modificarProducto(ItemMenu item) throws AccesoDatosException, MontoInvalidoException {
		if (item == null) {
			throw new MontoInvalidoException("No hay ningun producto seleccionado.");
		}
		validarProducto(item.getNombre(), item.getPrecioBase());
		itemMenuDAO.actualizar(item);
	}

	public void eliminarProducto(int id) throws AccesoDatosException {
		itemMenuDAO.eliminar(id);
	}

	public void darDeBajaProducto(int id) throws AccesoDatosException {
		itemMenuDAO.darDeBaja(id);
	}

	public void cambiarDisponibilidad(int id, boolean disponible) throws AccesoDatosException {
		itemMenuDAO.cambiarDisponibilidad(id, disponible);
	}

	public List<ItemMenu> listarMenuCompleto() throws AccesoDatosException {
		return itemMenuDAO.listarTodos();
	}

	public List<ItemMenu> listarMenuDisponible() throws AccesoDatosException {
		return itemMenuDAO.listarDisponibles();
	}

	public List<ItemMenu> buscarProductos(String texto) throws AccesoDatosException {
		return itemMenuDAO.buscarPorNombre(texto == null ? "" : texto.trim());
	}

	private void validarProducto(String nombre, double precioBase) throws MontoInvalidoException {
		if (nombre == null || nombre.trim().isEmpty()) {
			throw new MontoInvalidoException("El nombre del producto no puede estar vacio.");
		}
		if (precioBase <= 0) {
			throw new MontoInvalidoException("El precio debe ser mayor que cero.");
		}
	}

	// ==================================================================
	// ABM DE EMPLEADOS
	// ==================================================================

	public void altaEmpleado(String nombre, String usuario, String clave)
			throws AccesoDatosException, MontoInvalidoException {

		validarEmpleado(nombre, usuario, clave);
		empleadoDAO.insertar(new Empleado(nombre.trim(), usuario.trim(), clave));
	}

	public void modificarEmpleado(Empleado empleado)
			throws AccesoDatosException, MontoInvalidoException {

		if (empleado == null) {
			throw new MontoInvalidoException("No hay ningun empleado seleccionado.");
		}
		validarEmpleado(empleado.getNombre(), empleado.getUsuario(), empleado.getClave());
		empleadoDAO.actualizar(empleado);
	}

	public void eliminarEmpleado(int id) throws AccesoDatosException {
		empleadoDAO.eliminar(id);
	}

	public void darDeBajaEmpleado(int id) throws AccesoDatosException {
		empleadoDAO.darDeBaja(id);
	}

	public List<Empleado> listarEmpleados() throws AccesoDatosException {
		return empleadoDAO.listarTodos();
	}

	public List<Empleado> buscarEmpleados(String texto) throws AccesoDatosException {
		return empleadoDAO.buscarPorNombre(texto == null ? "" : texto.trim());
	}

	private void validarEmpleado(String nombre, String usuario, String clave)
			throws MontoInvalidoException {

		if (nombre == null || nombre.trim().isEmpty()) {
			throw new MontoInvalidoException("El nombre no puede estar vacio.");
		}
		if (usuario == null || usuario.trim().isEmpty()) {
			throw new MontoInvalidoException("El usuario no puede estar vacio.");
		}
		if (clave == null || clave.length() < 3) {
			throw new MontoInvalidoException("La clave debe tener al menos 3 caracteres.");
		}
	}
}
