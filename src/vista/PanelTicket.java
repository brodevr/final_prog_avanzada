package vista;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Muestra el texto que devuelve cualquier objeto Imprimible: tanto el ticket de
 * un pedido como el resultado de un reporte.
 *
 * El panel no sabe cual de los dos esta mostrando, y eso es justamente lo que
 * aporta la interfaz Imprimible: un solo panel sirve para las dos cosas.
 */
public class PanelTicket extends JPanel {

	private static final long serialVersionUID = 1L;

	private VentanaPrincipal ventana;
	private JTextArea areaTexto;

	public PanelTicket(VentanaPrincipal ventana) {
		this.ventana = ventana;
		setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		areaTexto = new JTextArea();
		areaTexto.setEditable(false);
		areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 13));
		add(new JScrollPane(areaTexto), BorderLayout.CENTER);

		JPanel botones = new JPanel();
		JButton btnVolver = new JButton("Volver al salon");
		botones.add(btnVolver);
		add(botones, BorderLayout.SOUTH);

		btnVolver.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PanelTicket.this.ventana.irAMesas();
			}
		});
	}

	public void mostrarTexto(String texto) {
		areaTexto.setText(texto);
		areaTexto.setCaretPosition(0);
	}
}
