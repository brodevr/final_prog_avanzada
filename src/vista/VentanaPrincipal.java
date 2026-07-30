package vista;

import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import conexion.ConexionBD;
import excepciones.AccesoDatosException;
import modelo.Empleado;
import modelo.Pedido;

/**
 * Ventana unica de la aplicacion (requisito 7: interfaz grafica con JFrame).
 *
 * Usa CardLayout: en lugar de abrir una ventana nueva por cada funcion, todas
 * las pantallas son paneles apilados en un mismo contenedor y se muestra uno a
 * la vez. Es mas prolijo para el usuario (no se le llena el escritorio de
 * ventanas) y mas simple de coordinar desde el codigo.
 *
 * La ventana tambien guarda el empleado con sesion iniciada y ofrece los
 * metodos de navegacion que usan los paneles.
 */
public class VentanaPrincipal extends JFrame {

	private static final long serialVersionUID = 1L;

	public static final String LOGIN = "LOGIN";
	public static final String MESAS = "MESAS";
	public static final String PEDIDO = "PEDIDO";
	public static final String MENU_ABM = "MENU_ABM";
	public static final String EMPLEADOS_ABM = "EMPLEADOS_ABM";
	public static final String REPORTES = "REPORTES";
	public static final String TICKET = "TICKET";

	private CardLayout cardLayout;
	private JPanel contenedor;

	private Empleado empleadoActual;

	private PanelLogin panelLogin;
	private PanelMesas panelMesas;
	private PanelPedido panelPedido;
	private PanelMenuABM panelMenuABM;
	private PanelEmpleadosABM panelEmpleadosABM;
	private PanelReportes panelReportes;
	private PanelTicket panelTicket;

	public VentanaPrincipal() {
		setTitle("Restaurante La Esquina - Sistema de Gestion");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(980, 640));
		setMinimumSize(new Dimension(880, 580));

		cardLayout = new CardLayout();
		contenedor = new JPanel(cardLayout);

		panelLogin = new PanelLogin(this);
		panelMesas = new PanelMesas(this);
		panelPedido = new PanelPedido(this);
		panelMenuABM = new PanelMenuABM(this);
		panelEmpleadosABM = new PanelEmpleadosABM(this);
		panelReportes = new PanelReportes(this);
		panelTicket = new PanelTicket(this);

		contenedor.add(panelLogin, LOGIN);
		contenedor.add(panelMesas, MESAS);
		contenedor.add(panelPedido, PEDIDO);
		contenedor.add(panelMenuABM, MENU_ABM);
		contenedor.add(panelEmpleadosABM, EMPLEADOS_ABM);
		contenedor.add(panelReportes, REPORTES);
		contenedor.add(panelTicket, TICKET);

		add(contenedor);
		pack();
		setLocationRelativeTo(null);

		cardLayout.show(contenedor, LOGIN);
	}

	// ------------------------------------------------------------------
	// Navegacion
	// ------------------------------------------------------------------

	public void irAMesas() {
		panelMesas.refrescar();
		cardLayout.show(contenedor, MESAS);
	}

	public void irAPedido(Pedido pedido) {
		panelPedido.cargarPedido(pedido);
		cardLayout.show(contenedor, PEDIDO);
	}

	public void irAMenu() {
		panelMenuABM.refrescar();
		cardLayout.show(contenedor, MENU_ABM);
	}

	public void irAEmpleados() {
		panelEmpleadosABM.refrescar();
		cardLayout.show(contenedor, EMPLEADOS_ABM);
	}

	public void irAReportes() {
		cardLayout.show(contenedor, REPORTES);
	}

	public void irATicket(String texto) {
		panelTicket.mostrarTexto(texto);
		cardLayout.show(contenedor, TICKET);
	}

	public void cerrarSesion() {
		empleadoActual = null;
		panelLogin.limpiar();
		cardLayout.show(contenedor, LOGIN);
	}

	// ------------------------------------------------------------------
	// Dialogos: centralizados para que todos los paneles avisen igual
	// ------------------------------------------------------------------

	public void mostrarError(String mensaje) {
		JOptionPane.showMessageDialog(this, mensaje, "Atencion", JOptionPane.ERROR_MESSAGE);
	}

	public void mostrarInfo(String mensaje) {
		JOptionPane.showMessageDialog(this, mensaje, "Informacion",
				JOptionPane.INFORMATION_MESSAGE);
	}

	public boolean confirmar(String mensaje) {
		int respuesta = JOptionPane.showConfirmDialog(this, mensaje, "Confirmar",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		return respuesta == JOptionPane.YES_OPTION;
	}

	// ------------------------------------------------------------------
	// Sesion
	// ------------------------------------------------------------------

	public Empleado getEmpleadoActual() {
		return empleadoActual;
	}

	public void setEmpleadoActual(Empleado empleadoActual) {
		this.empleadoActual = empleadoActual;
	}

	/** Cierra la conexion antes de terminar la aplicacion. */
	public void salir() {
		if (!confirmar("Desea salir del sistema?")) {
			return;
		}
		try {
			ConexionBD.getInstancia().cerrar();
		} catch (AccesoDatosException e) {
			// Si nunca se pudo conectar, no hay nada que cerrar.
			System.err.println("No habia una conexion abierta: " + e.getMessage());
		}
		dispose();
		System.exit(0);
	}
}
