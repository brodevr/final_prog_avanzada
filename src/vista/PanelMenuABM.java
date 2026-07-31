package vista;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;

import controlador.Cafe;
import excepciones.AccesoDatosException;
import excepciones.MontoInvalidoException;
import modelo.Bebida;
import modelo.ItemMenu;
import modelo.Plato;

public class PanelMenuABM extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final String TIPO_PLATO  = "PLATO";
	private static final String TIPO_BEBIDA = "BEBIDA";

	private VentanaPrincipal ventana;

	private JTable tabla;
	private DefaultTableModel modeloTabla;
	private List<ItemMenu> itemsEnTabla = new ArrayList<>();

	private JTextField txtNombre;
	private JTextField txtPrecio;
	private JComboBox<String> comboTipo;
	private JLabel lblNumerico;
	private JTextField txtNumerico;
	private JCheckBox chkOpcion;
	private JCheckBox chkDisponible;
	private JTextField txtBuscar;

	/** Cambia entre "Activar" y "Desactivar" segun la fila elegida. */
	private JButton btnEstado;

	public PanelMenuABM(VentanaPrincipal ventana) {
		this.ventana = ventana;
		setLayout(new BorderLayout(0, 0));
		setBackground(EstiloUI.C_CREMA);

		add(construirCabecera(), BorderLayout.NORTH);
		add(construirTabla(),    BorderLayout.CENTER);
		add(construirFormulario(), BorderLayout.SOUTH);

		actualizarEtiquetas();
	}

	private JPanel construirCabecera() {
		JPanel cab = new JPanel(new BorderLayout());
		cab.setBackground(EstiloUI.C_NAVBAR);
		cab.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

		JLabel titulo = new JLabel("Carta del café");
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
		titulo.setForeground(Color.WHITE);
		cab.add(titulo, BorderLayout.WEST);

		JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		der.setOpaque(false);

		txtBuscar = EstiloUI.campo(14);
		JButton btnBuscar  = EstiloUI.btnNavbar("Buscar");
		JButton btnVerTodos= EstiloUI.btnNavbar("Ver todos");
		JButton btnVolver  = EstiloUI.btnNavbar("← Salon");

		der.add(new JLabel("Nombre:") {{ setFont(EstiloUI.F_SMALL); setForeground(new Color(180,150,110)); }});
		der.add(txtBuscar);
		der.add(btnBuscar);
		der.add(btnVerTodos);
		der.add(btnVolver);
		cab.add(der, BorderLayout.EAST);

		btnBuscar.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { buscar(); }
		});
		btnVerTodos.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { txtBuscar.setText(""); refrescar(); }
		});
		btnVolver.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { ventana.irAMesas(); }
		});

		return cab;
	}

	private JScrollPane construirTabla() {
		String[] cols = { "ID", "Tipo", "Nombre", "Precio base", "Precio final", "Disponible", "Detalle" };
		modeloTabla = new DefaultTableModel(cols, 0) {
			private static final long serialVersionUID = 1L;
			@Override public boolean isCellEditable(int r, int c) { return false; }
		};
		tabla = new JTable(modeloTabla);
		EstiloUI.estilizarTabla(tabla);

		tabla.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) cargarSeleccionEnFormulario();
			}
		});

		JScrollPane sp = EstiloUI.scroll(tabla);
		sp.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, EstiloUI.C_BORDE));
		return sp;
	}

	private JPanel construirFormulario() {
		JPanel contenedor = new JPanel(new BorderLayout(0, 0));
		contenedor.setBackground(Color.WHITE);
		contenedor.add(EstiloUI.barraTitulo("Datos del producto"), BorderLayout.NORTH);

		JPanel campos = new JPanel(new GridLayout(2, 6, 10, 10));
		campos.setBackground(Color.WHITE);
		campos.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));

		txtNombre   = EstiloUI.campo(0);
		txtPrecio   = EstiloUI.campo(0);
		txtNumerico = EstiloUI.campo(0);
		comboTipo   = new JComboBox<>(new String[]{ TIPO_PLATO, TIPO_BEBIDA });
		comboTipo.setFont(EstiloUI.F_NORMAL);
		chkOpcion    = new JCheckBox("Es entrada");
		chkOpcion.setFont(EstiloUI.F_NORMAL);
		chkOpcion.setBackground(Color.WHITE);
		chkDisponible = new JCheckBox("Disponible", true);
		chkDisponible.setFont(EstiloUI.F_NORMAL);
		chkDisponible.setBackground(Color.WHITE);

		campos.add(lbl("Nombre:"));       campos.add(txtNombre);
		campos.add(lbl("Precio base:"));  campos.add(txtPrecio);
		campos.add(lbl("Tipo:"));         campos.add(comboTipo);
		lblNumerico = lbl("Minutos prep.:");
		campos.add(lblNumerico);          campos.add(txtNumerico);
		campos.add(chkOpcion);            campos.add(chkDisponible);
		campos.add(new JLabel(""));       campos.add(new JLabel(""));
		contenedor.add(campos, BorderLayout.CENTER);

		JButton btnNuevo    = EstiloUI.btnBorde("Limpiar");
		JButton btnAlta     = EstiloUI.btnVerde("Dar de alta");
		JButton btnModif    = EstiloUI.btnPrimario("Actualizar");
		btnEstado           = EstiloUI.btnBorde("Desactivar");
		JButton btnEliminar = EstiloUI.btnRojo("Eliminar");

		contenedor.add(EstiloUI.barraAcciones(
				new JButton[] { btnNuevo, btnAlta, btnModif, btnEstado },
				btnEliminar), BorderLayout.SOUTH);

		comboTipo.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { actualizarEtiquetas(); }
		});
		btnNuevo.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { limpiarFormulario(); }
		});
		btnAlta.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { darDeAlta(); }
		});
		btnModif.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { modificar(); }
		});
		btnEstado.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { cambiarEstado(); }
		});
		btnEliminar.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { eliminar(); }
		});

		return contenedor;
	}

	private static JLabel lbl(String texto) {
		JLabel l = new JLabel(texto);
		l.setFont(EstiloUI.F_NEGRITA);
		l.setForeground(EstiloUI.C_CAFE);
		return l;
	}

	private void actualizarEtiquetas() {
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

	public void refrescar() {
		try { mostrar(Cafe.getInstancia().listarMenuCompleto()); }
		catch (AccesoDatosException e) { ventana.mostrarError(e.getMessage()); }
	}

	private void buscar() {
		try { mostrar(Cafe.getInstancia().buscarProductos(txtBuscar.getText())); }
		catch (AccesoDatosException e) { ventana.mostrarError(e.getMessage()); }
	}

	private void mostrar(List<ItemMenu> items) {
		itemsEnTabla = items;
		modeloTabla.setRowCount(0);
		for (ItemMenu item : items) {
			modeloTabla.addRow(new Object[] {
					Integer.valueOf(item.getId()),
					item.getTipo(),
					item.getNombre(),
					String.format("$%.2f", item.getPrecioBase()),
					String.format("$%.2f", item.calcularPrecioFinal()),
					item.isDisponible() ? "SI" : "NO",
					item.getDescripcionDetallada() });
		}
		// Recargar la tabla borra la seleccion: el boton tiene que acompañar.
		actualizarBotonEstado();
	}

	private ItemMenu getSeleccionado() {
		int fila = tabla.getSelectedRow();
		if (fila < 0 || fila >= itemsEnTabla.size()) return null;
		return itemsEnTabla.get(fila);
	}

	/**
	 * Deja el boton de estado acorde a la fila elegida: si el producto esta en
	 * la carta ofrece sacarlo y al reves. Sin seleccion queda deshabilitado.
	 */
	private void actualizarBotonEstado() {
		ItemMenu item = getSeleccionado();
		if (item == null) {
			btnEstado.setText("Desactivar");
			btnEstado.setEnabled(false);
			return;
		}
		btnEstado.setText(item.isDisponible() ? "Desactivar" : "Activar");
		btnEstado.setEnabled(true);
	}

	private void cargarSeleccionEnFormulario() {
		actualizarBotonEstado();
		ItemMenu item = getSeleccionado();
		if (item == null) return;
		txtNombre.setText(item.getNombre());
		txtPrecio.setText(String.valueOf(item.getPrecioBase()));
		chkDisponible.setSelected(item.isDisponible());
		comboTipo.setSelectedItem(item.getTipo());
		actualizarEtiquetas();
		if (item instanceof Plato) {
			Plato p = (Plato) item;
			txtNumerico.setText(String.valueOf(p.getMinutosPreparacion()));
			chkOpcion.setSelected(p.isEntrada());
		} else {
			Bebida b = (Bebida) item;
			txtNumerico.setText(String.valueOf(b.getMililitros()));
			chkOpcion.setSelected(b.isAlcoholica());
		}
	}

	private void limpiarFormulario() {
		txtNombre.setText(""); txtPrecio.setText(""); txtNumerico.setText("");
		chkOpcion.setSelected(false); chkDisponible.setSelected(true);
		tabla.clearSelection();
		actualizarBotonEstado();
	}

	private void darDeAlta() {
		try {
			double precio = leerDouble(txtPrecio.getText(), "el precio base");
			int num = leerEntero(txtNumerico.getText(),
					esPlatoSeleccionado() ? "los minutos" : "los mililitros");
			if (esPlatoSeleccionado()) {
				Cafe.getInstancia().altaPlato(txtNombre.getText(), precio, num, chkOpcion.isSelected());
			} else {
				Cafe.getInstancia().altaBebida(txtNombre.getText(), precio, num, chkOpcion.isSelected());
			}
			ventana.mostrarInfo("Producto dado de alta.");
			limpiarFormulario(); refrescar();
		} catch (MontoInvalidoException e) { ventana.mostrarError(e.getMessage());
		} catch (AccesoDatosException e)    { ventana.mostrarError(e.getMessage()); }
	}

	private void modificar() {
		ItemMenu sel = getSeleccionado();
		if (sel == null) { ventana.mostrarError("Seleccione un producto de la tabla."); return; }
		try {
			double precio = leerDouble(txtPrecio.getText(), "el precio base");
			int num = leerEntero(txtNumerico.getText(),
					esPlatoSeleccionado() ? "los minutos" : "los mililitros");
			ItemMenu modificado;
			if (esPlatoSeleccionado()) {
				modificado = new Plato(sel.getId(), txtNombre.getText().trim(), precio,
						chkDisponible.isSelected(), num, chkOpcion.isSelected());
			} else {
				modificado = new Bebida(sel.getId(), txtNombre.getText().trim(), precio,
						chkDisponible.isSelected(), num, chkOpcion.isSelected());
			}
			Cafe.getInstancia().modificarProducto(modificado);
			ventana.mostrarInfo("Producto modificado.");
			refrescar();
		} catch (MontoInvalidoException e) { ventana.mostrarError(e.getMessage());
		} catch (AccesoDatosException e)    { ventana.mostrarError(e.getMessage()); }
	}

	private void cambiarEstado() {
		ItemMenu sel = getSeleccionado();
		if (sel == null) { ventana.mostrarError("Seleccione un producto de la tabla."); return; }

		boolean activar = !sel.isDisponible();
		String pregunta = activar
				? "Volver a poner '" + sel.getNombre() + "' en la carta?"
				: "Sacar '" + sel.getNombre() + "' de la carta?";
		if (!ventana.confirmar(pregunta)) return;

		try { Cafe.getInstancia().cambiarDisponibilidad(sel.getId(), activar); refrescar(); }
		catch (AccesoDatosException e) { ventana.mostrarError(e.getMessage()); }
	}

	private void eliminar() {
		ItemMenu sel = getSeleccionado();
		if (sel == null) { ventana.mostrarError("Seleccione un producto de la tabla."); return; }
		if (!ventana.confirmar("Eliminar definitivamente '" + sel.getNombre() + "'?")) return;
		try { Cafe.getInstancia().eliminarProducto(sel.getId()); limpiarFormulario(); refrescar(); }
		catch (AccesoDatosException e) { ventana.mostrarError(e.getMessage()); }
	}

	private double leerDouble(String t, String campo) throws MontoInvalidoException {
		try { return Double.parseDouble(t.trim().replace(",", ".")); }
		catch (NumberFormatException e) {
			throw new MontoInvalidoException("Revise " + campo + ": debe ser un numero.");
		}
	}

	private int leerEntero(String t, String campo) throws MontoInvalidoException {
		try { return Integer.parseInt(t.trim()); }
		catch (NumberFormatException e) {
			throw new MontoInvalidoException("Revise " + campo + ": debe ser un numero entero.");
		}
	}
}
