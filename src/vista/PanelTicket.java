package vista;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import javax.swing.*;

public class PanelTicket extends JPanel {

	private static final long serialVersionUID = 1L;

	private VentanaPrincipal ventana;
	private JTextArea areaTexto;

	public PanelTicket(VentanaPrincipal ventana) {
		this.ventana = ventana;
		setLayout(new BorderLayout(0, 0));
		setBackground(EstiloUI.C_CREMA);

		// Cabecera
		JPanel cab = new JPanel(new BorderLayout());
		cab.setBackground(EstiloUI.C_NAVBAR);
		cab.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
		JLabel titulo = new JLabel("Ticket de consumo");
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
		titulo.setForeground(Color.WHITE);
		cab.add(titulo, BorderLayout.WEST);
		add(cab, BorderLayout.NORTH);

		// Área de texto
		areaTexto = new JTextArea();
		areaTexto.setEditable(false);
		areaTexto.setFont(EstiloUI.F_MONO);
		areaTexto.setBackground(Color.WHITE);
		areaTexto.setForeground(EstiloUI.C_CAFE);
		areaTexto.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

		JScrollPane sp = EstiloUI.scroll(areaTexto);
		sp.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, EstiloUI.C_BORDE));
		add(sp, BorderLayout.CENTER);

		// Pie
		JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
		pie.setBackground(new Color(248, 242, 234));
		pie.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloUI.C_BORDE));
		JButton btnImprimir = EstiloUI.btnVerde("Imprimir");
		JButton btnVolver = EstiloUI.btnPrimario("← Volver al salon");
		pie.add(btnImprimir);
		pie.add(btnVolver);
		add(pie, BorderLayout.SOUTH);

		btnImprimir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				imprimir();
			}
		});
		btnVolver.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PanelTicket.this.ventana.irAMesas();
			}
		});
	}

	/**
	 * Manda el ticket a la impresora.
	 *
	 * JTextArea ya sabe imprimirse solo: print() arma la pagina con el texto y
	 * abre el dialogo de impresion del sistema. Como "Microsoft Print to PDF"
	 * figura ahi como una impresora mas, de este mismo boton sale el PDF si no
	 * hay una impresora fisica a mano.
	 */
	private void imprimir() {
		if (areaTexto.getText().trim().isEmpty()) {
			ventana.mostrarError("No hay ningun ticket para imprimir.");
			return;
		}
		try {
			// Devuelve false si el usuario cancela el dialogo: en ese caso no
			// hay nada para avisar, la cancelacion no es un error.
			if (areaTexto.print()) {
				ventana.mostrarInfo("El ticket se envio a la impresora.");
			}
		} catch (PrinterException e) {
			ventana.mostrarError("No se pudo imprimir el ticket: " + e.getMessage());
		}
	}

	public void mostrarTexto(String texto) {
		areaTexto.setText(texto);
		areaTexto.setCaretPosition(0);
	}
}
