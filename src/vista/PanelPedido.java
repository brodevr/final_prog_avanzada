package vista;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import controlador.Caja;
import controlador.Cafe;
import excepciones.AccesoDatosException;
import excepciones.ItemNoDisponibleException;
import excepciones.MontoInvalidoException;
import excepciones.PedidoCerradoException;
import modelo.DetallePedido;
import modelo.ItemMenu;
import modelo.Pedido;
import promociones.Descuento;
import promociones.DescuentoMontoFijo;
import promociones.DescuentoPorcentaje;
import promociones.SinDescuento;

public class PanelPedido extends JPanel {

	private static final long serialVersionUID = 1L;

	private VentanaPrincipal ventana;
	private Pedido pedidoActual;

	private JLabel lblCabecera;
	private JComboBox<ItemMenu> comboProductos;
	private JTextField txtCantidad;
	private JTable tabla;
	private DefaultTableModel modeloTabla;
	private JComboBox<Descuento> comboDescuentos;
	private JLabel lblSubtotal;
	private JLabel lblDescuento;
	private JLabel lblTotal;
	private JLabel lblDemora;

	public PanelPedido(VentanaPrincipal ventana) {
		this.ventana = ventana;
		setLayout(new BorderLayout(0, 0));
		setBackground(EstiloUI.C_CREMA);

		add(construirCabecera(), BorderLayout.NORTH);
		add(construirCentro(), BorderLayout.CENTER);
		add(construirPie(), BorderLayout.SOUTH);
	}

	private JPanel construirCabecera() {
		JPanel cab = new JPanel(new BorderLayout());
		cab.setBackground(EstiloUI.C_CAFE);
		cab.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

		lblCabecera = new JLabel("Comanda");
		lblCabecera.setFont(new Font("Segoe UI", Font.BOLD, 15));
		lblCabecera.setForeground(Color.WHITE);
		cab.add(lblCabecera, BorderLayout.CENTER);
		return cab;
	}

	private JPanel construirCentro() {
		JPanel centro = new JPanel(new BorderLayout(0, 0));
		centro.setBackground(EstiloUI.C_CREMA);

		// --- Sección cargar consumo ---
		JPanel cargaWrap = new JPanel(new BorderLayout(0, 0));
		cargaWrap.setBackground(EstiloUI.C_CREMA);
		cargaWrap.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloUI.C_BORDE));
		cargaWrap.add(EstiloUI.barraTitulo("Agregar consumo"), BorderLayout.NORTH);

		JPanel carga = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		carga.setBackground(Color.WHITE);

		comboProductos = new JComboBox<>();
		comboProductos.setFont(EstiloUI.F_NORMAL);
		comboProductos.setPreferredSize(new Dimension(240, 32));

		txtCantidad = EstiloUI.campo(4);
		txtCantidad.setText("1");

		JButton btnAgregar = EstiloUI.btnAcento("+ Agregar");
		JButton btnQuitar  = EstiloUI.btnBorde("Quitar linea");

		carga.add(new JLabel("Producto:") {{ setFont(EstiloUI.F_NEGRITA); setForeground(EstiloUI.C_CAFE); }});
		carga.add(comboProductos);
		carga.add(new JLabel("Cant.:") {{ setFont(EstiloUI.F_NEGRITA); setForeground(EstiloUI.C_CAFE); }});
		carga.add(txtCantidad);
		carga.add(btnAgregar);
		carga.add(btnQuitar);
		cargaWrap.add(carga, BorderLayout.CENTER);
		centro.add(cargaWrap, BorderLayout.NORTH);

		// --- Tabla ---
		String[] columnas = { "Producto", "Detalle", "Cant.", "P. unitario", "Subtotal" };
		modeloTabla = new DefaultTableModel(columnas, 0) {
			private static final long serialVersionUID = 1L;
			@Override public boolean isCellEditable(int r, int c) { return false; }
		};
		tabla = new JTable(modeloTabla);
		EstiloUI.estilizarTabla(tabla);

		JScrollPane scrollTabla = EstiloUI.scroll(tabla);
		scrollTabla.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, EstiloUI.C_BORDE));
		centro.add(scrollTabla, BorderLayout.CENTER);

		btnAgregar.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { agregarConsumo(); }
		});
		btnQuitar.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { quitarConsumo(); }
		});

		return centro;
	}

	private JPanel construirPie() {
		JPanel pie = new JPanel(new BorderLayout(0, 0));
		pie.setBackground(Color.WHITE);
		pie.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloUI.C_BORDE));

		// Totales
		JPanel panelTotales = new JPanel(new GridLayout(4, 1, 0, 4));
		panelTotales.setBackground(Color.WHITE);
		panelTotales.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 0, 1, EstiloUI.C_BORDE),
				BorderFactory.createEmptyBorder(14, 20, 14, 20)));

		lblSubtotal  = new JLabel("Subtotal: $0.00");
		lblDescuento = new JLabel("Descuento: $0.00");
		lblTotal     = new JLabel("TOTAL: $0.00");
		lblDemora    = new JLabel("Demora estimada: 0 min");

		lblSubtotal.setFont(EstiloUI.F_NORMAL);
		lblSubtotal.setForeground(EstiloUI.C_CAFE);
		lblDescuento.setFont(EstiloUI.F_NORMAL);
		lblDescuento.setForeground(new Color(150, 80, 30));
		lblTotal.setFont(EstiloUI.F_TOTAL);
		lblTotal.setForeground(EstiloUI.C_LIBRE);
		lblDemora.setFont(EstiloUI.F_SMALL);
		lblDemora.setForeground(new Color(130, 100, 70));

		panelTotales.add(lblSubtotal);
		panelTotales.add(lblDescuento);
		panelTotales.add(lblTotal);
		panelTotales.add(lblDemora);

		// Promociones
		JPanel panelPromos = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 14));
		panelPromos.setBackground(Color.WHITE);
		panelPromos.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 0, 1, EstiloUI.C_BORDE),
				BorderFactory.createEmptyBorder(0, 16, 0, 0)));

		JLabel lblPromo = new JLabel("Promo:");
		lblPromo.setFont(EstiloUI.F_NEGRITA);
		lblPromo.setForeground(EstiloUI.C_CAFE);

		comboDescuentos = new JComboBox<>();
		comboDescuentos.setFont(EstiloUI.F_NORMAL);
		comboDescuentos.setPreferredSize(new Dimension(200, 32));
		comboDescuentos.addItem(new SinDescuento());
		comboDescuentos.addItem(new DescuentoPorcentaje(10));
		comboDescuentos.addItem(new DescuentoPorcentaje(20));
		comboDescuentos.addItem(new DescuentoMontoFijo(2000));

		JButton btnAplicar = EstiloUI.btnBorde("Aplicar");

		panelPromos.add(lblPromo);
		panelPromos.add(comboDescuentos);
		panelPromos.add(btnAplicar);

		// Acciones
		JPanel panelAcciones = new JPanel(new GridLayout(3, 1, 6, 6));
		panelAcciones.setBackground(Color.WHITE);
		panelAcciones.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

		JButton btnCobrar = EstiloUI.btnVerde("Cobrar y cerrar cuenta");
		JButton btnAnular = EstiloUI.btnRojo("Anular cuenta");
		JButton btnVolver = EstiloUI.btnBorde("← Volver al salon");

		panelAcciones.add(btnCobrar);
		panelAcciones.add(btnAnular);
		panelAcciones.add(btnVolver);

		pie.add(panelTotales,  BorderLayout.WEST);
		pie.add(panelPromos,   BorderLayout.CENTER);
		pie.add(panelAcciones, BorderLayout.EAST);

		btnAplicar.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { aplicarDescuento(); }
		});
		btnCobrar.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { cobrar(); }
		});
		btnAnular.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { anular(); }
		});
		btnVolver.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { ventana.irAMesas(); }
		});

		return pie;
	}

	// ------------------------------------------------------------------
	// Carga y refresco
	// ------------------------------------------------------------------

	public void cargarPedido(Pedido pedido) {
		this.pedidoActual = pedido;
		cargarProductosDisponibles();
		refrescarTabla();
	}

	private void cargarProductosDisponibles() {
		comboProductos.removeAllItems();
		try {
			for (ItemMenu item : Cafe.getInstancia().listarMenuDisponible()) {
				comboProductos.addItem(item);
			}
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}

	private void refrescarTabla() {
		modeloTabla.setRowCount(0);
		if (pedidoActual == null) return;

		lblCabecera.setText("Cuenta #" + pedidoActual.getId()
				+ "   ·   Mesa " + pedidoActual.getMesa().getNumero()
				+ " — " + pedidoActual.getMesa().getSector()
				+ "   ·   Atiende: " + pedidoActual.getEmpleado().getNombre()
				+ "   ·   " + pedidoActual.getEstado());

		for (DetallePedido d : pedidoActual.getDetalles()) {
			modeloTabla.addRow(new Object[] {
					d.getItem().getNombre(),
					d.getItem().getDescripcionDetallada(),
					Integer.valueOf(d.getCantidad()),
					String.format("$%.2f", d.getPrecioUnitario()),
					String.format("$%.2f", d.calcularSubtotal()) });
		}

		lblSubtotal.setText(String.format("Subtotal:   $%.2f", pedidoActual.calcularSubtotal()));
		lblDescuento.setText(String.format("Descuento (%s):   -$%.2f",
				pedidoActual.getDescuento().getDescripcion(), pedidoActual.calcularDescuento()));
		lblTotal.setText(String.format("TOTAL   $%.2f", pedidoActual.calcularTotal()));
		lblDemora.setText("⏱  Demora estimada: " + pedidoActual.calcularDemoraEstimada() + " min");
	}

	// ------------------------------------------------------------------
	// Acciones
	// ------------------------------------------------------------------

	private void agregarConsumo() {
		try {
			ItemMenu seleccionado = (ItemMenu) comboProductos.getSelectedItem();
			int cantidad = leerCantidad();
			Caja.getInstancia().agregarItem(pedidoActual, seleccionado, cantidad);
			txtCantidad.setText("1");
			refrescarTabla();
		} catch (MontoInvalidoException e) {
			ventana.mostrarError(e.getMessage());
		} catch (ItemNoDisponibleException e) {
			ventana.mostrarError(e.getMessage());
		} catch (PedidoCerradoException e) {
			ventana.mostrarError(e.getMessage());
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}

	private int leerCantidad() throws MontoInvalidoException {
		try {
			return Integer.parseInt(txtCantidad.getText().trim());
		} catch (NumberFormatException e) {
			throw new MontoInvalidoException("La cantidad debe ser un numero entero.");
		}
	}

	private void quitarConsumo() {
		int fila = tabla.getSelectedRow();
		if (fila < 0) {
			ventana.mostrarError("Seleccione una linea de la comanda para quitarla.");
			return;
		}
		try {
			Caja.getInstancia().quitarItem(pedidoActual, fila);
			refrescarTabla();
		} catch (MontoInvalidoException e) {
			ventana.mostrarError(e.getMessage());
		} catch (PedidoCerradoException e) {
			ventana.mostrarError(e.getMessage());
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}

	private void aplicarDescuento() {
		try {
			Descuento elegido = (Descuento) comboDescuentos.getSelectedItem();
			Caja.getInstancia().aplicarDescuento(pedidoActual, elegido);
			refrescarTabla();
		} catch (MontoInvalidoException e) {
			ventana.mostrarError(e.getMessage());
		} catch (PedidoCerradoException e) {
			ventana.mostrarError(e.getMessage());
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}

	private void cobrar() {
		if (pedidoActual == null) { ventana.mostrarError("No hay cuenta cargada."); return; }
		if (!ventana.confirmar(String.format("Cobrar mesa %d por $%.2f?",
				pedidoActual.getMesa().getNumero(), pedidoActual.calcularTotal()))) return;
		try {
			Caja.getInstancia().cerrarCuenta(pedidoActual);
			ventana.irATicket(pedidoActual.generarTexto());
		} catch (MontoInvalidoException e) {
			ventana.mostrarError(e.getMessage());
		} catch (PedidoCerradoException e) {
			ventana.mostrarError(e.getMessage());
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}

	private void anular() {
		if (pedidoActual == null) { ventana.mostrarError("No hay cuenta cargada."); return; }
		if (!ventana.confirmar("Anular la cuenta de la mesa "
				+ pedidoActual.getMesa().getNumero() + "?\nNo se registrara facturacion.")) return;
		try {
			Caja.getInstancia().anularCuenta(pedidoActual);
			ventana.mostrarInfo("Cuenta anulada. La mesa quedo libre.");
			ventana.irAMesas();
		} catch (MontoInvalidoException e) {
			ventana.mostrarError(e.getMessage());
		} catch (PedidoCerradoException e) {
			ventana.mostrarError(e.getMessage());
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}
}
