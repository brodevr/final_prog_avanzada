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

	private VentanaPrincipal ventana;

	private JTextField txtUsuario;
	private JPasswordField txtClave;

	public PanelLogin(VentanaPrincipal ventana) {
		this.ventana = ventana;
		setLayout(new BorderLayout());

		JLabel titulo = new JLabel("RESTAURANTE LA ESQUINA", SwingConstants.CENTER);
		titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
		titulo.setBorder(BorderFactory.createEmptyBorder(50, 20, 10, 20));
		add(titulo, BorderLayout.NORTH);

		JPanel formulario = new JPanel(new GridLayout(3, 2, 10, 12));
		formulario.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

		txtUsuario = new JTextField(14);
		txtClave = new JPasswordField(14);

		formulario.add(new JLabel("Usuario:", SwingConstants.RIGHT));
		formulario.add(txtUsuario);
		formulario.add(new JLabel("Clave:", SwingConstants.RIGHT));
		formulario.add(txtClave);

		JButton btnIngresar = new JButton("Ingresar");
		formulario.add(new JLabel(""));
		formulario.add(btnIngresar);

		JPanel centro = new JPanel(new FlowLayout(FlowLayout.CENTER));
		formulario.setPreferredSize(new Dimension(420, 140));
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
