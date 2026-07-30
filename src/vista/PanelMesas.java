package vista;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

import controlador.Caja;
import controlador.Restaurante;
import excepciones.AccesoDatosException;
import excepciones.MesaOcupadaException;
import excepciones.MontoInvalidoException;
import modelo.Mesa;
import modelo.Pedido;

public class PanelMesas extends JPanel {

	private static final long serialVersionUID = 1L;

	private VentanaPrincipal ventana;
	private JPanel grillaMesas;
	private JLabel lblSesion;

	public PanelMesas(VentanaPrincipal ventana) {
		this.ventana = ventana;
		setLayout(new BorderLayout());
		setBackground(EstiloUI.C_CREMA);

		add(construirNavbar(), BorderLayout.NORTH);

		// Grilla de mesas sobre fondo crema
		grillaMesas = new JPanel(new GridLayout(0, 4, 14, 14));
		grillaMesas.setBackground(EstiloUI.C_CREMA);
		grillaMesas.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

		JScrollPane scroll = EstiloUI.scroll(grillaMesas);
		scroll.setBorder(null);
		scroll.getViewport().setBackground(EstiloUI.C_CREMA);
		add(scroll, BorderLayout.CENTER);

		add(construirLeyenda(), BorderLayout.SOUTH);
	}

	private JPanel construirNavbar() {
		JPanel nav = new JPanel();
		nav.setLayout(new BoxLayout(nav, BoxLayout.X_AXIS));
		nav.setBackground(EstiloUI.C_NAVBAR);
		nav.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

		JLabel logo = new JLabel("☕  Cafetería La Esquina");
		logo.setFont(new Font("Segoe UI", Font.BOLD, 16));
		logo.setForeground(Color.WHITE);

		lblSesion = new JLabel("");
		lblSesion.setFont(EstiloUI.F_SMALL);
		lblSesion.setForeground(new Color(170, 140, 105));

		JButton btnCarta = EstiloUI.btnNavbar("Carta");
		JButton btnEmp   = EstiloUI.btnNavbar("Empleados");
		JButton btnRep   = EstiloUI.btnNavbar("Reportes");
		JButton btnCerrar= EstiloUI.btnNavbar("Cerrar sesion");
		JButton btnSalir = EstiloUI.btnRojo("Salir");
		btnSalir.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));

		nav.add(logo);
		nav.add(Box.createHorizontalStrut(16));
		nav.add(lblSesion);
		nav.add(Box.createHorizontalGlue());
		nav.add(btnCarta);
		nav.add(Box.createHorizontalStrut(4));
		nav.add(btnEmp);
		nav.add(Box.createHorizontalStrut(4));
		nav.add(btnRep);
		nav.add(Box.createHorizontalStrut(16));
		nav.add(btnCerrar);
		nav.add(Box.createHorizontalStrut(8));
		nav.add(btnSalir);

		btnCarta.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { ventana.irAMenu(); }
		});
		btnEmp.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { ventana.irAEmpleados(); }
		});
		btnRep.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { ventana.irAReportes(); }
		});
		btnCerrar.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { ventana.cerrarSesion(); }
		});
		btnSalir.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { ventana.salir(); }
		});

		return nav;
	}

	private JPanel construirLeyenda() {
		JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
		leyenda.setBackground(new Color(240, 230, 215));
		leyenda.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloUI.C_BORDE));

		JLabel lLibre = tag("  LIBRE  ", EstiloUI.C_LIBRE);
		JLabel lOcupada = tag("  OCUPADA  ", EstiloUI.C_OCUPADA);

		leyenda.add(new JLabel("Referencias:") {{ setFont(EstiloUI.F_SMALL); setForeground(EstiloUI.C_CAFE); }});
		leyenda.add(lLibre);
		leyenda.add(lOcupada);
		JLabel hint = new JLabel("  Hacé clic en una mesa para abrir o retomar su cuenta.");
		hint.setFont(EstiloUI.F_SMALL);
		hint.setForeground(new Color(120, 90, 60));
		leyenda.add(hint);
		return leyenda;
	}

	private static JLabel tag(String texto, Color color) {
		JLabel l = new JLabel(texto);
		l.setFont(EstiloUI.F_SMALL.deriveFont(Font.BOLD));
		l.setOpaque(true);
		l.setBackground(color);
		l.setForeground(Color.WHITE);
		l.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
		return l;
	}

	public void refrescar() {
		if (ventana.getEmpleadoActual() != null) {
			lblSesion.setText("Atiende:  " + ventana.getEmpleadoActual().getNombre());
		}

		grillaMesas.removeAll();

		try {
			Restaurante.getInstancia().refrescarMesas();
			List<Mesa> mesas = Restaurante.getInstancia().listarMesasDeCache();

			if (mesas.isEmpty()) {
				grillaMesas.add(new JLabel("No hay mesas. Ejecute el script SQL."));
			}
			for (Mesa mesa : mesas) {
				grillaMesas.add(crearBotonMesa(mesa));
			}
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}

		grillaMesas.revalidate();
		grillaMesas.repaint();
	}

	private JButton crearBotonMesa(final Mesa mesa) {
		boolean ocupada = mesa.isOcupada();
		Color fondo = ocupada ? EstiloUI.C_OCUPADA : EstiloUI.C_LIBRE;
		String estado = ocupada ? "OCUPADA" : "LIBRE";

		JButton boton = new JButton(
				"<html><center>"
				+ "<span style='font-size:16pt; font-weight:bold;'>Mesa " + mesa.getNumero() + "</span><br>"
				+ "<span style='font-size:9pt;'>" + mesa.getCapacidad() + " pers. &bull; " + mesa.getSector() + "</span><br>"
				+ "<span style='font-size:9pt; font-weight:bold;'>" + estado + "</span>"
				+ "</center></html>");

		boton.setPreferredSize(new Dimension(160, 110));
		boton.setBackground(fondo);
		boton.setForeground(Color.WHITE);
		boton.setOpaque(true);
		boton.setBorderPainted(false);
		boton.setFocusPainted(false);
		boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		boton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abrirORetomar(mesa);
			}
		});
		return boton;
	}

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
