package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import controlador.Restaurante;
import excepciones.AccesoDatosException;
import excepciones.MontoInvalidoException;
import modelo.Empleado;

/**
 * Pantalla de acceso. Valida las credenciales contra la tabla empleado.
 */
public class PanelLogin extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final Color COLOR_HEADER = new Color(30, 70, 130);

	private VentanaPrincipal ventana;

	private JTextField txtUsuario;
	private JPasswordField txtClave;

	public PanelLogin(VentanaPrincipal ventana) {
		this.ventana = ventana;
		setLayout(new BorderLayout());

		// --- Cabecera con color institucional ---
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(COLOR_HEADER);
		header.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

		JLabel titulo = new JLabel("RESTAURANTE LA ESQUINA", SwingConstants.CENTER);
		titulo.setFont(new Font("SansSerif", Font.BOLD, 28));
		titulo.setForeground(Color.WHITE);

		JLabel subtitulo = new JLabel("Sistema de Gestion", SwingConstants.CENTER);
		subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 14));
		subtitulo.setForeground(new Color(180, 210, 255));
		subtitulo.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

		JPanel textos = new JPanel(new GridLayout(2, 1));
		textos.setOpaque(false);
		textos.add(titulo);
		textos.add(subtitulo);
		header.add(textos, BorderLayout.CENTER);
		add(header, BorderLayout.NORTH);

		// --- Formulario ---
		JPanel formulario = new JPanel(new GridLayout(3, 2, 10, 14));
		formulario.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

		txtUsuario = new JTextField(14);
		txtClave = new JPasswordField(14);

		JLabel lblUsuario = new JLabel("Usuario:", SwingConstants.RIGHT);
		lblUsuario.setFont(lblUsuario.getFont().deriveFont(Font.BOLD));
		JLabel lblClave = new JLabel("Clave:", SwingConstants.RIGHT);
		lblClave.setFont(lblClave.getFont().deriveFont(Font.BOLD));

		formulario.add(lblUsuario);
		formulario.add(txtUsuario);
		formulario.add(lblClave);
		formulario.add(txtClave);

		JButton btnIngresar = new JButton("  Ingresar  ");
		btnIngresar.setBackground(COLOR_HEADER);
		btnIngresar.setForeground(Color.WHITE);
		btnIngresar.setOpaque(true);
		btnIngresar.setFont(btnIngresar.getFont().deriveFont(Font.BOLD, 13f));
		formulario.add(new JLabel(""));
		formulario.add(btnIngresar);

		JPanel centro = new JPanel(new FlowLayout(FlowLayout.CENTER));
		formulario.setPreferredSize(new Dimension(420, 150));
		centro.add(formulario);
		add(centro, BorderLayout.CENTER);

		JLabel ayuda = new JLabel(
				"Usuarios de prueba: mrodriguez / 1234    -    lgomez / 1234",
				SwingConstants.CENTER);
		ayuda.setForeground(new Color(90, 90, 90));
		ayuda.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));
		add(ayuda, BorderLayout.SOUTH);

		ActionListener accionIngresar = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				intentarIngresar();
			}
		};

		btnIngresar.addActionListener(accionIngresar);
		txtClave.addActionListener(accionIngresar);
		txtUsuario.addActionListener(accionIngresar);
	}

	private void intentarIngresar() {
		String usuario = txtUsuario.getText();
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
