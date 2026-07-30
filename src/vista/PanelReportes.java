package vista;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import controlador.Caja;
import excepciones.AccesoDatosException;
import excepciones.MontoInvalidoException;
import modelo.Imprimible;
import modelo.ReporteVentas;

/**
 * Reportes y estadisticas del negocio.
 *
 * Fijarse en el metodo mostrar(Imprimible): recibe la INTERFAZ, no la clase
 * concreta. Al panel le da lo mismo si le pasan un ReporteVentas o un Pedido:
 * mientras sepa generar su texto, lo puede mostrar. Eso es programar contra
 * abstracciones y no contra implementaciones.
 */
public class PanelReportes extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final String FACTURACION = "Facturacion por periodo";
	private static final String PRODUCTOS = "Productos mas vendidos";
	private static final String MOZOS = "Ventas por mozo";
	private static final String TICKET = "Ticket promedio";

	private VentanaPrincipal ventana;

	private JComboBox<String> comboReporte;
	private JTextField txtDesde;
	private JTextField txtHasta;
	private JTextArea areaResultado;

	public PanelReportes(VentanaPrincipal ventana) {
		this.ventana = ventana;
		setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		add(construirFiltros(), BorderLayout.NORTH);

		areaResultado = new JTextArea();
		areaResultado.setEditable(false);
		areaResultado.setFont(new Font("Monospaced", Font.PLAIN, 13));
		add(new JScrollPane(areaResultado), BorderLayout.CENTER);

		JPanel pie = new JPanel();
		JButton btnVolver = new JButton("Volver al salon");
		pie.add(btnVolver);
		add(pie, BorderLayout.SOUTH);

		btnVolver.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PanelReportes.this.ventana.irAMesas();
			}
		});
	}

	private JPanel construirFiltros() {
		JPanel filtros = new JPanel();
		filtros.setBorder(BorderFactory.createTitledBorder("Filtros"));

		comboReporte = new JComboBox<String>(new String[] {
				FACTURACION, PRODUCTOS, MOZOS, TICKET });

		LocalDate hoy = LocalDate.now();
		txtDesde = new JTextField(hoy.withDayOfMonth(1).toString(), 10);
		txtHasta = new JTextField(hoy.toString(), 10);

		JButton btnGenerar = new JButton("Generar");

		filtros.add(new JLabel("Reporte:"));
		filtros.add(comboReporte);
		filtros.add(new JLabel("Desde (aaaa-mm-dd):"));
		filtros.add(txtDesde);
		filtros.add(new JLabel("Hasta (aaaa-mm-dd):"));
		filtros.add(txtHasta);
		filtros.add(btnGenerar);

		btnGenerar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				generar();
			}
		});

		return filtros;
	}

	private void generar() {
		try {
			LocalDate desde = leerFecha(txtDesde.getText(), "desde");
			LocalDate hasta = leerFecha(txtHasta.getText(), "hasta");
			String elegido = (String) comboReporte.getSelectedItem();

			ReporteVentas reporte;
			if (FACTURACION.equals(elegido)) {
				reporte = Caja.getInstancia().reporteFacturacion(desde, hasta);
			} else if (PRODUCTOS.equals(elegido)) {
				reporte = Caja.getInstancia().reporteProductosMasVendidos(desde, hasta);
			} else if (MOZOS.equals(elegido)) {
				reporte = Caja.getInstancia().reporteVentasPorMozo(desde, hasta);
			} else {
				reporte = Caja.getInstancia().reporteTicketPromedio(desde, hasta);
			}

			mostrar(reporte);

		} catch (MontoInvalidoException e) {
			ventana.mostrarError(e.getMessage());
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}

	/**
	 * Recibe cualquier Imprimible. Es el metodo que demuestra para que sirve la
	 * interfaz: un mismo panel muestra reportes y tickets sin saber la clase
	 * concreta que tiene entre manos.
	 */
	private void mostrar(Imprimible imprimible) {
		areaResultado.setText(imprimible.generarTexto());
		areaResultado.setCaretPosition(0);
	}

	private LocalDate leerFecha(String texto, String nombreCampo) throws MontoInvalidoException {
		try {
			return LocalDate.parse(texto.trim());
		} catch (DateTimeParseException e) {
			throw new MontoInvalidoException("La fecha '" + nombreCampo
					+ "' debe tener el formato aaaa-mm-dd. Por ejemplo: 2026-07-29.");
		}
	}
}
