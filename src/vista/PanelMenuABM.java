package vista;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import controlador.Restaurante;
import excepciones.AccesoDatosException;
import excepciones.MontoInvalidoException;
import modelo.Bebida;
import modelo.ItemMenu;
import modelo.Plato;

/**
 * ABM completo de la carta: alta, modificacion, baja y busqueda de productos
 * (requisito 2: "ABM completo para al menos dos entidades").
 *
 * Hay dos formas de baja:
 *  - Eliminar: borrado fisico. Solo funciona si el producto nunca se vendio.
 *  - Dar de baja: baja logica. Sale de la carta pero sigue apareciendo en los
 *    tickets historicos. Es la que se usa normalmente.
 */
public class PanelMenuABM extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final String TIPO_PLATO = "PLATO";
	private static final String TIPO_BEBIDA = "BEBIDA";

	private VentanaPrincipal ventana;

	private JTable tabla;
	private DefaultTableModel modeloTabla;
	private List<ItemMenu> itemsEnTabla = new ArrayList<ItemMenu>();

	private JTextField txtNombre;
	private JTextField txtPrecio;
	private JComboBox<String> comboTipo;
	private JLabel lblNumerico;
	private JTextField txtNumerico;
	private JCheckBox chkOpcion;
	private JCheckBox chkDisponible;
	private JTextField txtBuscar;

	public PanelMenuABM(VentanaPrincipal ventana) {
		this.ventana = ventana;
		setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		add(construirCabecera(), BorderLayout.NORTH);
		add(construirTabla(), BorderLayout.CENTER);
		add(construirFormulario(), BorderLayout.SOUTH);

		actualizarEtiquetasSegunTipo();
	}

	private JPanel construirCabecera() {
		JPanel cabecera = new JPanel(new BorderLayout());

		JLabel titulo = new JLabel("Carta del restaurante");
		titulo.setFont(titulo.getFont().deriveFont(16f));
		cabecera.add(titulo, BorderLayout.WEST);

		JPanel derecha = new JPanel();
		txtBuscar = new JTextField(14);
		JButton btnBuscar = new JButton("Buscar");
		JButton btnVerTodos = new JButton("Ver todos");
		JButton btnVolver = new JButton("Volver al salon");

		derecha.add(new JLabel("Nombre contiene:"));
		derecha.add(txtBuscar);
		derecha.add(btnBuscar);
		derecha.add(btnVerTodos);
		derecha.add(btnVolver);
		cabecera.add(derecha, BorderLayout.EAST);

		btnBuscar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				buscar();
			}
		});
		btnVerTodos.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				txtBuscar.setText("");
				refrescar();
			}
		});
		btnVolver.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ventana.irAMesas();
			}
		});

		return cabecera;
	}

	private JScrollPane construirTabla() {
		String[] columnas = { "ID", "Tipo", "Nombre", "Precio base", "Precio final",
				"Disponible", "Detalle" };

		modeloTabla = new DefaultTableModel(columnas, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int fila, int columna) {
				return false;
			}
		};

		tabla = new JTable(modeloTabla);
		tabla.setRowHeight(22);

		tabla.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					cargarSeleccionEnFormulario();
				}
			}
		});

		return new JScrollPane(tabla);
	}

	private JPanel construirFormulario() {
		JPanel contenedor = new JPanel(new BorderLayout(8, 8));
		contenedor.setBorder(BorderFactory.createTitledBorder("Datos del producto"));

		JPanel campos = new JPanel(new GridLayout(2, 6, 8, 8));

		txtNombre = new JTextField();
		txtPrecio = new JTextField();
		comboTipo = new JComboBox<String>(new String[] { TIPO_PLATO, TIPO_BEBIDA });
		lblNumerico = new JLabel("Minutos:");
		txtNumerico = new JTextField();
		chkOpcion = new JCheckBox("Es entrada");
		chkDisponible = new JCheckBox("Disponible", true);

		campos.add(new JLabel("Nombre:"));
		campos.add(txtNombre);
		campos.add(new JLabel("Precio base:"));
		campos.add(txtPrecio);
		campos.add(new JLabel("Tipo:"));
		campos.add(comboTipo);

		campos.add(lblNumerico);
		campos.add(txtNumerico);
		campos.add(chkOpcion);
		campos.add(chkDisponible);
		campos.add(new JLabel(""));
		campos.add(new JLabel(""));

		contenedor.add(campos, BorderLayout.CENTER);

		JPanel botones = new JPanel();
		JButton btnNuevo = new JButton("Limpiar formulario");
		JButton btnAlta = new JButton("Dar de alta");
		JButton btnModificar = new JButton("Modificar seleccionado");
		JButton btnBaja = new JButton("Dar de baja (logica)");
		JButton btnEliminar = new JButton("Eliminar (fisico)");

		botones.add(btnNuevo);
		botones.add(btnAlta);
		botones.add(btnModificar);
		botones.add(btnBaja);
		botones.add(btnEliminar);
		contenedor.add(botones, BorderLayout.SOUTH);

		comboTipo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actualizarEtiquetasSegunTipo();
			}
		});
		btnNuevo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				limpiarFormulario();
			}
		});
		btnAlta.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				darDeAlta();
			}
		});
		btnModificar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				modificar();
			}
		});
		btnBaja.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				darDeBaja();
			}
		});
		btnEliminar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				eliminar();
			}
		});

		return contenedor;
	}

	/** Los campos especificos cambian de nombre segun el tipo elegido. */
	private void actualizarEtiquetasSegunTipo() {
		if (esPlatoSeleccionado()) {
			lblNumerico.setText("Minutos prep.:");
			chkOpcion.setText("Es entrada");
		} else {
			lblNumerico.setText("Mililitros:");
			chkOpcion.setText("Con alcohol");
		}
	}

	private boolean esPlatoSeleccionado() {
		return TIPO_PLATO.equals(comboTipo.getSelectedItem());
	}

	// ------------------------------------------------------------------
	// Datos
	// ------------------------------------------------------------------

	public void refrescar() {
		try {
			mostrar(Restaurante.getInstancia().listarMenuCompleto());
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}

	private void buscar() {
		try {
			mostrar(Restaurante.getInstancia().buscarProductos(txtBuscar.getText()));
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}

	private void mostrar(List<ItemMenu> items) {
		itemsEnTabla = items;
		modeloTabla.setRowCount(0);

		for (int i = 0; i < items.size(); i++) {
			ItemMenu item = items.get(i);
			modeloTabla.addRow(new Object[] {
					Integer.valueOf(item.getId()),
					item.getTipo(),
					item.getNombre(),
					String.format("%.2f", item.getPrecioBase()),
					String.format("%.2f", item.calcularPrecioFinal()),
					item.isDisponible() ? "SI" : "NO",
					item.getDescripcionDetallada() });
		}
	}

	private ItemMenu getSeleccionado() {
		int fila = tabla.getSelectedRow();
		if (fila < 0 || fila >= itemsEnTabla.size()) {
			return null;
		}
		return itemsEnTabla.get(fila);
	}

	private void cargarSeleccionEnFormulario() {
		ItemMenu item = getSeleccionado();
		if (item == null) {
			return;
		}

		txtNombre.setText(item.getNombre());
		txtPrecio.setText(String.valueOf(item.getPrecioBase()));
		chkDisponible.setSelected(item.isDisponible());
		comboTipo.setSelectedItem(item.getTipo());
		actualizarEtiquetasSegunTipo();

		if (item instanceof Plato) {
			Plato plato = (Plato) item;
			txtNumerico.setText(String.valueOf(plato.getMinutosPreparacion()));
			chkOpcion.setSelected(plato.isEntrada());
		} else {
			Bebida bebida = (Bebida) item;
			txtNumerico.setText(String.valueOf(bebida.getMililitros()));
			chkOpcion.setSelected(bebida.isAlcoholica());
		}
	}

	private void limpiarFormulario() {
		txtNombre.setText("");
		txtPrecio.setText("");
		txtNumerico.setText("");
		chkOpcion.setSelected(false);
		chkDisponible.setSelected(true);
		tabla.clearSelection();
	}

	// ------------------------------------------------------------------
	// Operaciones
	// ------------------------------------------------------------------

	private void darDeAlta() {
		try {
			double precio = leerDouble(txtPrecio.getText(), "el precio base");
			int numerico = leerEntero(txtNumerico.getText(),
					esPlatoSeleccionado() ? "los minutos de preparacion" : "los mililitros");

			if (esPlatoSeleccionado()) {
				Restaurante.getInstancia().altaPlato(txtNombre.getText(), precio,
						numerico, chkOpcion.isSelected());
			} else {
				Restaurante.getInstancia().altaBebida(txtNombre.getText(), precio,
						numerico, chkOpcion.isSelected());
			}

			ventana.mostrarInfo("Producto dado de alta.");
			limpiarFormulario();
			refrescar();

		} catch (MontoInvalidoException e) {
			ventana.mostrarError(e.getMessage());
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}

	private void modificar() {
		ItemMenu seleccionado = getSeleccionado();
		if (seleccionado == null) {
			ventana.mostrarError("Seleccione un producto de la tabla.");
			return;
		}

		try {
			double precio = leerDouble(txtPrecio.getText(), "el precio base");
			int numerico = leerEntero(txtNumerico.getText(),
					esPlatoSeleccionado() ? "los minutos de preparacion" : "los mililitros");

			ItemMenu modificado;
			if (esPlatoSeleccionado()) {
				modificado = new Plato(seleccionado.getId(), txtNombre.getText().trim(), precio,
						chkDisponible.isSelected(), numerico, chkOpcion.isSelected());
			} else {
				modificado = new Bebida(seleccionado.getId(), txtNombre.getText().trim(), precio,
						chkDisponible.isSelected(), numerico, chkOpcion.isSelected());
			}

			Restaurante.getInstancia().modificarProducto(modificado);
			ventana.mostrarInfo("Producto modificado.");
			refrescar();

		} catch (MontoInvalidoException e) {
			ventana.mostrarError(e.getMessage());
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}

	private void darDeBaja() {
		ItemMenu seleccionado = getSeleccionado();
		if (seleccionado == null) {
			ventana.mostrarError("Seleccione un producto de la tabla.");
			return;
		}
		if (!ventana.confirmar("Sacar '" + seleccionado.getNombre() + "' de la carta?")) {
			return;
		}

		try {
			Restaurante.getInstancia().darDeBajaProducto(seleccionado.getId());
			refrescar();
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}

	private void eliminar() {
		ItemMenu seleccionado = getSeleccionado();
		if (seleccionado == null) {
			ventana.mostrarError("Seleccione un producto de la tabla.");
			return;
		}
		if (!ventana.confirmar("Eliminar definitivamente '" + seleccionado.getNombre()
				+ "'? Si ya se vendio, use la baja logica.")) {
			return;
		}

		try {
			Restaurante.getInstancia().eliminarProducto(seleccionado.getId());
			limpiarFormulario();
			refrescar();
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}

	// ------------------------------------------------------------------
	// Lectura de campos numericos
	// ------------------------------------------------------------------

	private double leerDouble(String texto, String nombreCampo) throws MontoInvalidoException {
		try {
			return Double.parseDouble(texto.trim().replace(",", "."));
		} catch (NumberFormatException e) {
			throw new MontoInvalidoException("Revise " + nombreCampo + ": debe ser un numero.");
		}
	}

	private int leerEntero(String texto, String nombreCampo) throws MontoInvalidoException {
		try {
			return Integer.parseInt(texto.trim());
		} catch (NumberFormatException e) {
			throw new MontoInvalidoException("Revise " + nombreCampo
					+ ": debe ser un numero entero.");
		}
	}
}
