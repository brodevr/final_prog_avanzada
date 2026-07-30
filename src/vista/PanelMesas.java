package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import controlador.Caja;
import controlador.Restaurante;
import excepciones.AccesoDatosException;
import excepciones.MesaOcupadaException;
import excepciones.MontoInvalidoException;
import modelo.Mesa;
import modelo.Pedido;

/**
 * Plano del salon y menu principal del sistema (requisito 7: "menu principal
 * con opciones para acceder a todas las funcionalidades").
 *
 * Cada mesa es un boton. Verde = libre, rojo = ocupada. Un clic sobre una mesa
 * libre ABRE la cuenta; sobre una ocupada, la retoma. Ese clic es la puerta de
 * entrada al ciclo de apertura y cierre.
 */
public class PanelMesas extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final Color VERDE_LIBRE = new Color(198, 239, 206);
	private static final Color ROJO_OCUPADA = new Color(255, 199, 206);

	private VentanaPrincipal ventana;
	private JPanel grillaMesas;
	private JLabel lblSesion;

	public PanelMesas(VentanaPrincipal ventana) {
		this.ventana = ventana;
		setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		add(construirBarraSuperior(), BorderLayout.NORTH);

		grillaMesas = new JPanel(new GridLayout(0, 5, 12, 12));
		JScrollPane scroll = new JScrollPane(grillaMesas);
		scroll.setBorder(BorderFactory.createTitledBorder("Salon"));
		add(scroll, BorderLayout.CENTER);

		add(construirLeyenda(), BorderLayout.SOUTH);
	}

	private JPanel construirBarraSuperior() {
		JPanel barra = new JPanel(new BorderLayout());

		lblSesion = new JLabel("Sesion iniciada");
		lblSesion.setFont(new Font("SansSerif", Font.BOLD, 14));
		barra.add(lblSesion, BorderLayout.WEST);

		JPanel botones = new JPanel();

		JButton btnMenu = new JButton("Carta / Menu");
		JButton btnEmpleados = new JButton("Empleados");
		JButton btnReportes = new JButton("Reportes");
		JButton btnCerrarSesion = new JButton("Cerrar sesion");
		JButton btnSalir = new JButton("Salir");

		botones.add(btnMenu);
		botones.add(btnEmpleados);
		botones.add(btnReportes);
		botones.add(btnCerrarSesion);
		botones.add(btnSalir);
		barra.add(botones, BorderLayout.EAST);

		btnMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ventana.irAMenu();
			}
		});
		btnEmpleados.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ventana.irAEmpleados();
			}
		});
		btnReportes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ventana.irAReportes();
			}
		});
		btnCerrarSesion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ventana.cerrarSesion();
			}
		});
		btnSalir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ventana.salir();
			}
		});

		return barra;
	}

	private JPanel construirLeyenda() {
		JPanel leyenda = new JPanel();

		JLabel libre = new JLabel("  LIBRE  ");
		libre.setOpaque(true);
		libre.setBackground(VERDE_LIBRE);

		JLabel ocupada = new JLabel("  OCUPADA  ");
		ocupada.setOpaque(true);
		ocupada.setBackground(ROJO_OCUPADA);

		leyenda.add(new JLabel("Referencias:"));
		leyenda.add(libre);
		leyenda.add(ocupada);
		leyenda.add(new JLabel("   |   Un clic sobre la mesa abre o retoma su cuenta."));

		return leyenda;
	}

	/** Vuelve a leer las mesas y redibuja el salon. */
	public void refrescar() {
		if (ventana.getEmpleadoActual() != null) {
			lblSesion.setText("Atiende: " + ventana.getEmpleadoActual().getNombre());
		}

		grillaMesas.removeAll();

		try {
			// Una sola lectura a la base, que ademas deja el cache actualizado.
			Restaurante.getInstancia().refrescarMesas();
			List<Mesa> mesas = Restaurante.getInstancia().listarMesasDeCache();

			if (mesas.isEmpty()) {
				grillaMesas.add(new JLabel("No hay mesas cargadas. Ejecute el script SQL."));
			}

			for (int i = 0; i < mesas.size(); i++) {
				grillaMesas.add(crearBotonMesa(mesas.get(i)));
			}

		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}

		grillaMesas.revalidate();
		grillaMesas.repaint();
	}

	private JButton crearBotonMesa(final Mesa mesa) {
		String estado = mesa.isOcupada() ? "OCUPADA" : "LIBRE";

		JButton boton = new JButton("<html><center><b>Mesa " + mesa.getNumero() + "</b><br>"
				+ mesa.getCapacidad() + " pers.<br>" + mesa.getSector() + "<br>"
				+ estado + "</center></html>");

		boton.setPreferredSize(new Dimension(140, 100));
		boton.setBackground(mesa.isOcupada() ? ROJO_OCUPADA : VERDE_LIBRE);
		boton.setOpaque(true);
		boton.setHorizontalAlignment(SwingConstants.CENTER);

		boton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abrirORetomar(mesa);
			}
		});

		return boton;
	}

	/**
	 * Si la mesa tiene una cuenta abierta la retoma; si no, abre una nueva.
	 * Consulta siempre la base de datos antes de decidir.
	 */
	private void abrirORetomar(Mesa mesa) {
		try {
			Pedido existente = Caja.getInstancia().buscarCuentaAbierta(mesa.getNumero());

			if (existente != null) {
				ventana.irAPedido(existente);
				return;
			}

			Pedido nuevo = Caja.getInstancia().abrirMesa(mesa, ventana.getEmpleadoActual());
			ventana.irAPedido(nuevo);

		} catch (MesaOcupadaException e) {
			ventana.mostrarError(e.getMessage());
			refrescar();
		} catch (MontoInvalidoException e) {
			ventana.mostrarError(e.getMessage());
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}
}
