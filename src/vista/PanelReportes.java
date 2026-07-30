package vista;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.*;

import controlador.Caja;
import excepciones.AccesoDatosException;
import excepciones.MontoInvalidoException;
import modelo.Imprimible;
import modelo.ReporteVentas;

public class PanelReportes extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final String FACTURACION = "Facturacion por periodo";
	private static final String PRODUCTOS   = "Productos mas vendidos";
	private static final String MOZOS       = "Ventas por empleado";
	private static final String TICKET      = "Ticket promedio";

	private VentanaPrincipal ventana;
	private JComboBox<String> comboReporte;
	private JTextField txtDesde;
	private JTextField txtHasta;
	private JTextArea areaResultado;

	public PanelReportes(VentanaPrincipal ventana) {
		this.ventana = ventana;
		setLayout(new BorderLayout(0, 0));
		setBackground(EstiloUI.C_CREMA);

		add(construirCabecera(), BorderLayout.NORTH);

		areaResultado = new JTextArea();
		areaResultado.setEditable(false);
		areaResultado.setFont(EstiloUI.F_MONO);
		areaResultado.setBackground(Color.WHITE);
		areaResultado.setForeground(EstiloUI.C_CAFE);
		areaResultado.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

		JScrollPane sp = EstiloUI.scroll(areaResultado);
		sp.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, EstiloUI.C_BORDE));
		add(sp, BorderLayout.CENTER);

		JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
		pie.setBackground(new Color(248, 242, 234));
		pie.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloUI.C_BORDE));
		JButton btnVolver = EstiloUI.btnBorde("← Volver al salon");
		pie.add(btnVolver);
		add(pie, BorderLayout.SOUTH);

		btnVolver.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { ventana.irAMesas(); }
		});
	}

	private JPanel construirCabecera() {
		JPanel cab = new JPanel(new BorderLayout());
		cab.setBackground(EstiloUI.C_NAVBAR);
		cab.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

		JLabel titulo = new JLabel("Reportes y estadísticas");
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
		titulo.setForeground(Color.WHITE);
		cab.add(titulo, BorderLayout.WEST);

		JPanel filtros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		filtros.setOpaque(false);

		LocalDate hoy = LocalDate.now();
		comboReporte = new JComboBox<>(new String[]{ FACTURACION, PRODUCTOS, MOZOS, TICKET });
		comboReporte.setFont(EstiloUI.F_NORMAL);
		comboReporte.setPreferredSize(new Dimension(220, 30));

		txtDesde = EstiloUI.campo(10);
		txtDesde.setText(hoy.withDayOfMonth(1).toString());
		txtHasta = EstiloUI.campo(10);
		txtHasta.setText(hoy.toString());

		JButton btnGenerar = EstiloUI.btnAcento("Generar");

		filtros.add(comboReporte);
		filtros.add(new JLabel("Desde:") {{ setFont(EstiloUI.F_SMALL); setForeground(new Color(180,150,110)); }});
		filtros.add(txtDesde);
		filtros.add(new JLabel("Hasta:") {{ setFont(EstiloUI.F_SMALL); setForeground(new Color(180,150,110)); }});
		filtros.add(txtHasta);
		filtros.add(btnGenerar);
		cab.add(filtros, BorderLayout.EAST);

		btnGenerar.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { generar(); }
		});

		return cab;
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
		} catch (MontoInvalidoException e) { ventana.mostrarError(e.getMessage());
		} catch (AccesoDatosException e)    { ventana.mostrarError(e.getMessage()); }
	}

	private void mostrar(Imprimible imp) {
		areaResultado.setText(imp.generarTexto());
		areaResultado.setCaretPosition(0);
	}

	private LocalDate leerFecha(String texto, String campo) throws MontoInvalidoException {
		try { return LocalDate.parse(texto.trim()); }
		catch (DateTimeParseException e) {
			throw new MontoInvalidoException(
					"La fecha '" + campo + "' debe tener el formato aaaa-mm-dd.");
		}
	}
}
