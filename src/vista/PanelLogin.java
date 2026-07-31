package vista;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import controlador.Cafe;
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

		// Taza dibujada en vez del emoji ☕: el emoji depende de que la fuente
		// del sistema lo tenga y se ve distinto en cada maquina.
		JLabel icono = new JLabel(IconoCafe.icono(64, new Color(210, 175, 130)),
				SwingConstants.CENTER);
		icono.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

		JLabel titulo = new JLabel("Café La Esquina", SwingConstants.CENTER);
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
		// Card blanca con los campos. GridBagLayout en lugar de GridLayout para
		// que las etiquetas queden pegadas a la derecha, los dos campos tengan
		// exactamente el mismo ancho y el boton quede centrado abajo de todo.
		JPanel card = new JPanel(new GridBagLayout());
		card.setBackground(Color.WHITE);
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(EstiloUI.C_BORDE, 1),
				BorderFactory.createEmptyBorder(32, 40, 32, 40)));

		JLabel lblUsr = new JLabel("Usuario:", SwingConstants.RIGHT);
		lblUsr.setFont(EstiloUI.F_NEGRITA);
		lblUsr.setForeground(EstiloUI.C_CAFE);

		JLabel lblClave = new JLabel("Clave:", SwingConstants.RIGHT);
		lblClave.setFont(EstiloUI.F_NEGRITA);
		lblClave.setForeground(EstiloUI.C_CAFE);

		txtUsuario = EstiloUI.campo(14);
		txtClave = EstiloUI.campoClave(14);

		JButton btnIngresar = EstiloUI.btnPrimario("Ingresar");

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(8, 6, 8, 6);

		// Columna 0: etiquetas, alineadas a la derecha contra el campo
		c.gridx = 0;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LINE_END;
		c.gridy = 0;
		card.add(lblUsr, c);
		c.gridy = 1;
		card.add(lblClave, c);

		// Columna 1: campos, ambos estirados al mismo ancho
		c.gridx = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LINE_START;
		c.gridy = 0;
		card.add(txtUsuario, c);
		c.gridy = 1;
		card.add(txtClave, c);

		// Boton: ocupa las dos columnas y queda centrado
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(22, 6, 0, 6);
		card.add(btnIngresar, c);

		// Un GridBagLayout sin pesos deja al unico hijo centrado en los dos
		// ejes, asi la card queda al medio aunque se redimensione la ventana.
		JPanel centro = new JPanel(new GridBagLayout());
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
		JPanel pie = new JPanel(new GridLayout(3, 1, 0, 5));
		pie.setBackground(EstiloUI.C_CREMA);
		pie.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

		pie.add(ayuda("Usuarios de prueba"));
		pie.add(ayuda("Administrador:  admin  /  admin123"));
		pie.add(ayuda("Empleado:  empleado  /  emp123"));
		return pie;
	}

	private JLabel ayuda(String texto) {
		JLabel etiqueta = new JLabel(texto, SwingConstants.CENTER);
		etiqueta.setFont(EstiloUI.F_SMALL);
		etiqueta.setForeground(new Color(140, 110, 80));
		return etiqueta;
	}

	private void intentarIngresar() {
		String usuario = txtUsuario.getText().trim();
		String clave = new String(txtClave.getPassword());
		try {
			Empleado empleado = Cafe.getInstancia().iniciarSesion(usuario, clave);
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
