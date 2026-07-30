package vista;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import controlador.Restaurante;
import excepciones.AccesoDatosException;
import excepciones.MontoInvalidoException;
import modelo.Empleado;

public class PanelLogin extends JPanel {

	private static final long serialVersionUID = 1L;

	private VentanaPrincipal ventana;
	private JTextField txtUsuario;
	private JPasswordField txtClave;

	public PanelLogin(VentanaPrincipal ventana) {
		this.ventana = ventana;
		setLayout(new BorderLayout());
		setBackground(EstiloUI.C_CREMA);

		add(construirHeader(), BorderLayout.NORTH);
		add(construirFormulario(), BorderLayout.CENTER);
		add(construirPie(), BorderLayout.SOUTH);
	}

	private JPanel construirHeader() {
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(EstiloUI.C_NAVBAR);
		header.setBorder(BorderFactory.createEmptyBorder(36, 40, 36, 40));

		JLabel icono = new JLabel("☕", SwingConstants.CENTER);
		icono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
		icono.setForeground(new Color(210, 175, 130));
		icono.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

		JLabel titulo = new JLabel("Cafetería La Esquina", SwingConstants.CENTER);
		titulo.setFont(EstiloUI.F_TITULO);
		titulo.setForeground(Color.WHITE);

		JLabel subtitulo = new JLabel("Sistema de Gestión", SwingConstants.CENTER);
		subtitulo.setFont(EstiloUI.F_NORMAL);
		subtitulo.setForeground(new Color(175, 150, 115));
		subtitulo.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

		JPanel textos = new JPanel(new GridLayout(3, 1));
		textos.setOpaque(false);
		textos.add(icono);
		textos.add(titulo);
		textos.add(subtitulo);
		header.add(textos, BorderLayout.CENTER);
		return header;
	}

	private JPanel construirFormulario() {
		// Card blanca centrada sobre fondo crema
		JPanel card = new JPanel(new GridLayout(3, 2, 10, 14));
		card.setBackground(Color.WHITE);
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(EstiloUI.C_BORDE, 1),
				BorderFactory.createEmptyBorder(30, 40, 30, 40)));
		card.setPreferredSize(new Dimension(420, 160));

		JLabel lblUsr = new JLabel("Usuario:", SwingConstants.RIGHT);
		lblUsr.setFont(EstiloUI.F_NEGRITA);
		lblUsr.setForeground(EstiloUI.C_CAFE);

		JLabel lblClave = new JLabel("Clave:", SwingConstants.RIGHT);
		lblClave.setFont(EstiloUI.F_NEGRITA);
		lblClave.setForeground(EstiloUI.C_CAFE);

		txtUsuario = EstiloUI.campo(14);
		txtClave = EstiloUI.campoClave(14);

		JButton btnIngresar = EstiloUI.btnPrimario("Ingresar");

		card.add(lblUsr);
		card.add(txtUsuario);
		card.add(lblClave);
		card.add(txtClave);
		card.add(new JLabel(""));
		card.add(btnIngresar);

		JPanel centro = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 40));
		centro.setBackground(EstiloUI.C_CREMA);
		centro.add(card);

		ActionListener accion = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				intentarIngresar();
			}
		};
		btnIngresar.addActionListener(accion);
		txtUsuario.addActionListener(accion);
		txtClave.addActionListener(accion);

		return centro;
	}

	private JPanel construirPie() {
		JPanel pie = new JPanel();
		pie.setBackground(EstiloUI.C_CREMA);
		pie.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
		JLabel ayuda = new JLabel("Usuarios de prueba: mrodriguez / 1234   —   lgomez / 1234",
				SwingConstants.CENTER);
		ayuda.setFont(EstiloUI.F_SMALL);
		ayuda.setForeground(new Color(140, 110, 80));
		pie.add(ayuda);
		return pie;
	}

	private void intentarIngresar() {
		String usuario = txtUsuario.getText().trim();
		String clave = new String(txtClave.getPassword());
		try {
			Empleado empleado = Restaurante.getInstancia().iniciarSesion(usuario, clave);
			if (empleado == null) {
				ventana.mostrarError("Usuario o clave incorrectos, o el empleado esta dado de baja.");
				txtClave.setText("");
				return;
			}
			ventana.setEmpleadoActual(empleado);
			limpiar();
			ventana.irAMesas();
		} catch (MontoInvalidoException e) {
			ventana.mostrarError(e.getMessage());
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}

	public void limpiar() {
		txtUsuario.setText("");
		txtClave.setText("");
		txtUsuario.requestFocusInWindow();
	}
}
