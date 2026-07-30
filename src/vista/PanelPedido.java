package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import controlador.Caja;
import controlador.Restaurante;
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

/**
 * Comanda de una mesa: carga de consumos, descuentos y cierre de la cuenta.
 *
 * Es la pantalla donde se ve el polimorfismo funcionando: la columna
 * "Detalle" de la tabla se llena con getDescripcionDetallada(), y cada fila
 * muestra un texto distinto segun sea un plato o una bebida, sin que el panel
 * pregunte nunca de que tipo es cada item.
 */
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
		setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		lblCabecera = new JLabel("Comanda");
		lblCabecera.setFont(new Font("SansSerif", Font.BOLD, 16));
		add(lblCabecera, BorderLayout.NORTH);

		add(construirZonaCentral(), BorderLayout.CENTER);
		add(construirZonaInferior(), BorderLayout.SOUTH);
	}

	private JPanel construirZonaCentral() {
		JPanel centro = new JPanel(new BorderLayout(8, 8));

		// --- Alta de consumos ---
		JPanel carga = new JPanel();
		carga.setBorder(BorderFactory.createTitledBorder("Cargar consumo"));

		comboProductos = new JComboBox<ItemMenu>();
		txtCantidad = new JTextField("1", 4);
		JButton btnAgregar = new JButton("Agregar");
		JButton btnQuitar = new JButton("Quitar linea seleccionada");

		carga.add(new JLabel("Producto:"));
		carga.add(comboProductos);
		carga.add(new JLabel("Cantidad:"));
		carga.add(txtCantidad);
		carga.add(btnAgregar);
		carga.add(btnQuitar);
		centro.add(carga, BorderLayout.NORTH);

		// --- Tabla de detalles ---
		String[] columnas = { "Producto", "Detalle", "Cant.", "P. unitario", "Subtotal" };
		modeloTabla = new DefaultTableModel(columnas, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int fila, int columna) {
				return false;
			}
		};
		tabla = new JTable(modeloTabla);
		tabla.setRowHeight(24);
		centro.add(new JScrollPane(tabla), BorderLayout.CENTER);

		btnAgregar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				agregarConsumo();
			}
		});
		btnQuitar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				quitarConsumo();
			}
		});

		return centro;
	}

	private JPanel construirZonaInferior() {
		JPanel inferior = new JPanel(new BorderLayout(10, 10));

		// --- Totales ---
		JPanel totales = new JPanel(new GridLayout(4, 1, 2, 2));
		totales.setBorder(BorderFactory.createTitledBorder("Totales"));

		lblSubtotal = new JLabel("Subtotal: $0.00");
		lblDescuento = new JLabel("Descuento: $0.00");
		lblTotal = new JLabel("TOTAL: $0.00");
		lblTotal.setFont(new Font("SansSerif", Font.BOLD, 20));
		lblTotal.setForeground(new Color(0, 120, 0));
		lblDemora = new JLabel("Demora estimada: 0 min");

		totales.add(lblSubtotal);
		totales.add(lblDescuento);
		totales.add(lblTotal);
		totales.add(lblDemora);
		inferior.add(totales, BorderLayout.WEST);

		// --- Descuentos ---
		JPanel promos = new JPanel();
		promos.setBorder(BorderFactory.createTitledBorder("Promociones"));

		comboDescuentos = new JComboBox<Descuento>();
		comboDescuentos.addItem(new SinDescuento());
		comboDescuentos.addItem(new DescuentoPorcentaje(10));
		comboDescuentos.addItem(new DescuentoPorcentaje(20));
		comboDescuentos.addItem(new DescuentoMontoFijo(2000));

		JButton btnAplicar = new JButton("Aplicar");
		promos.add(comboDescuentos);
		promos.add(btnAplicar);
		inferior.add(promos, BorderLayout.CENTER);

		// --- Acciones del ciclo de vida ---
		JPanel acciones = new JPanel(new GridLayout(3, 1, 4, 4));

		JButton btnCobrar = new JButton("Cobrar y cerrar cuenta");
		btnCobrar.setBackground(new Color(0, 153, 76));
		btnCobrar.setForeground(Color.WHITE);
		btnCobrar.setOpaque(true);
		btnCobrar.setBorderPainted(false);
		btnCobrar.setFont(btnCobrar.getFont().deriveFont(Font.BOLD));

		JButton btnAnular = new JButton("Anular cuenta");
		btnAnular.setBackground(new Color(200, 30, 30));
		btnAnular.setForeground(Color.WHITE);
		btnAnular.setOpaque(true);
		btnAnular.setBorderPainted(false);

		JButton btnVolver = new JButton("Volver al salon");

		acciones.add(btnCobrar);
		acciones.add(btnAnular);
		acciones.add(btnVolver);
		inferior.add(acciones, BorderLayout.EAST);

		btnAplicar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				aplicarDescuento();
			}
		});
		btnCobrar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cobrar();
			}
		});
		btnAnular.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				anular();
			}
		});
		btnVolver.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ventana.irAMesas();
			}
		});

		return inferior;
	}

	// ------------------------------------------------------------------
	// Carga y refresco
	// ------------------------------------------------------------------

	/** Recibe el pedido con el que se va a trabajar y arma la pantalla. */
	public void cargarPedido(Pedido pedido) {
		this.pedidoActual = pedido;
		cargarProductosDisponibles();
		refrescarTabla();
	}

	private void cargarProductosDisponibles() {
		comboProductos.removeAllItems();
		try {
			List<ItemMenu> disponibles = Restaurante.getInstancia().listarMenuDisponible();
			for (int i = 0; i < disponibles.size(); i++) {
				comboProductos.addItem(disponibles.get(i));
			}
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}

	private void refrescarTabla() {
		modeloTabla.setRowCount(0);

		if (pedidoActual == null) {
			return;
		}

		lblCabecera.setText("Cuenta #" + pedidoActual.getId()
				+ "   |   Mesa " + pedidoActual.getMesa().getNumero()
				+ " (" + pedidoActual.getMesa().getSector() + ")"
				+ "   |   Atiende: " + pedidoActual.getEmpleado().getNombre()
				+ "   |   Estado: " + pedidoActual.getEstado());

		List<DetallePedido> detalles = pedidoActual.getDetalles();
		for (int i = 0; i < detalles.size(); i++) {
			DetallePedido detalle = detalles.get(i);
			modeloTabla.addRow(new Object[] {
					detalle.getItem().getNombre(),
					detalle.getItem().getDescripcionDetallada(),
					Integer.valueOf(detalle.getCantidad()),
					String.format("%.2f", detalle.getPrecioUnitario()),
					String.format("%.2f", detalle.calcularSubtotal()) });
		}

		lblSubtotal.setText(String.format("Subtotal: $%.2f", pedidoActual.calcularSubtotal()));
		lblDescuento.setText(String.format("Descuento (%s): $%.2f",
				pedidoActual.getDescuento().getDescripcion(), pedidoActual.calcularDescuento()));
		lblTotal.setText(String.format("TOTAL: $%.2f", pedidoActual.calcularTotal()));
		lblDemora.setText("Demora estimada: " + pedidoActual.calcularDemoraEstimada() + " min");
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
		if (pedidoActual == null) {
			ventana.mostrarError("No hay ninguna cuenta cargada.");
			return;
		}
		if (!ventana.confirmar(String.format("Cobrar la mesa %d por $%.2f?",
				pedidoActual.getMesa().getNumero(), pedidoActual.calcularTotal()))) {
			return;
		}

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
		if (pedidoActual == null) {
			ventana.mostrarError("No hay ninguna cuenta cargada.");
			return;
		}
		if (!ventana.confirmar("Anular la cuenta de la mesa "
				+ pedidoActual.getMesa().getNumero() + "? No se registrara facturacion.")) {
			return;
		}

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
