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
import modelo.Empleado;
import modelo.Rol;

public class PanelEmpleadosABM extends JPanel {

	private static final long serialVersionUID = 1L;

	private VentanaPrincipal ventana;

	private JTable tabla;
	private DefaultTableModel modeloTabla;
	private List<Empleado> empleadosEnTabla = new ArrayList<>();

	private JTextField txtNombre;
	private JTextField txtUsuario;
	private JTextField txtClave;
	private JComboBox<Rol> cmbRol;
	private JCheckBox chkActivo;
	private JTextField txtBuscar;

	/** Cambia entre "Activar" y "Desactivar" segun la fila elegida. */
	private JButton btnEstado;

	public PanelEmpleadosABM(VentanaPrincipal ventana) {
		this.ventana = ventana;
		setLayout(new BorderLayout(0, 0));
		setBackground(EstiloUI.C_CREMA);

		add(construirCabecera(),   BorderLayout.NORTH);
		add(construirTabla(),      BorderLayout.CENTER);
		add(construirFormulario(), BorderLayout.SOUTH);
	}

	private JPanel construirCabecera() {
		JPanel cab = new JPanel(new BorderLayout());
		cab.setBackground(EstiloUI.C_NAVBAR);
		cab.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

		JLabel titulo = new JLabel("Personal del café");
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
		String[] cols = { "ID", "Nombre", "Usuario", "Perfil", "Activo" };
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
		contenedor.add(EstiloUI.barraTitulo("Datos del empleado"), BorderLayout.NORTH);

		JPanel campos = new JPanel(new GridLayout(3, 4, 10, 10));
		campos.setBackground(Color.WHITE);
		campos.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));

		txtNombre  = EstiloUI.campo(0);
		txtUsuario = EstiloUI.campo(0);
		txtClave   = EstiloUI.campo(0);
		cmbRol     = new JComboBox<Rol>(Rol.values());
		cmbRol.setFont(EstiloUI.F_NORMAL);
		cmbRol.setBackground(Color.WHITE);
		cmbRol.setSelectedItem(Rol.EMPLEADO);
		chkActivo  = new JCheckBox("Activo", true);
		chkActivo.setFont(EstiloUI.F_NORMAL);
		chkActivo.setBackground(Color.WHITE);

		campos.add(lbl("Nombre completo:")); campos.add(txtNombre);
		campos.add(lbl("Usuario:"));         campos.add(txtUsuario);
		campos.add(lbl("Clave:"));           campos.add(txtClave);
		campos.add(lbl("Perfil:"));          campos.add(cmbRol);
		campos.add(chkActivo);               campos.add(new JLabel(""));
		campos.add(new JLabel(""));          campos.add(new JLabel(""));
		contenedor.add(campos, BorderLayout.CENTER);

		JButton btnNuevo    = EstiloUI.btnBorde("Limpiar");
		JButton btnAlta     = EstiloUI.btnVerde("Dar de alta");
		JButton btnModif    = EstiloUI.btnPrimario("Actualizar");
		btnEstado           = EstiloUI.btnBorde("Desactivar");
		JButton btnEliminar = EstiloUI.btnRojo("Eliminar");

		contenedor.add(EstiloUI.barraAcciones(
				new JButton[] { btnNuevo, btnAlta, btnModif, btnEstado },
				btnEliminar), BorderLayout.SOUTH);

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

	private static JLabel lbl(String t) {
		JLabel l = new JLabel(t);
		l.setFont(EstiloUI.F_NEGRITA);
		l.setForeground(EstiloUI.C_CAFE);
		return l;
	}

	public void refrescar() {
		try { mostrar(Cafe.getInstancia().listarEmpleados()); }
		catch (AccesoDatosException e) { ventana.mostrarError(e.getMessage()); }
	}

	private void buscar() {
		try { mostrar(Cafe.getInstancia().buscarEmpleados(txtBuscar.getText())); }
		catch (AccesoDatosException e) { ventana.mostrarError(e.getMessage()); }
	}

	private void mostrar(List<Empleado> lista) {
		empleadosEnTabla = lista;
		modeloTabla.setRowCount(0);
		for (Empleado emp : lista) {
			modeloTabla.addRow(new Object[] {
					Integer.valueOf(emp.getId()),
					emp.getNombre(),
					emp.getUsuario(),
					emp.getRol().getEtiqueta(),
					emp.isActivo() ? "SI" : "NO" });
		}
		// Recargar la tabla borra la seleccion: el boton tiene que acompañar.
		actualizarBotonEstado();
	}

	private Empleado getSeleccionado() {
		int fila = tabla.getSelectedRow();
		if (fila < 0 || fila >= empleadosEnTabla.size()) return null;
		return empleadosEnTabla.get(fila);
	}

	/**
	 * Deja el boton de estado acorde a la fila elegida: si el empleado esta
	 * activo ofrece desactivarlo y al reves. Sin seleccion no hay accion
	 * posible, asi que queda deshabilitado en vez de mentir una etiqueta.
	 */
	private void actualizarBotonEstado() {
		Empleado emp = getSeleccionado();
		if (emp == null) {
			btnEstado.setText("Desactivar");
			btnEstado.setEnabled(false);
			return;
		}
		btnEstado.setText(emp.isActivo() ? "Desactivar" : "Activar");
		btnEstado.setEnabled(true);
	}

	private void cargarSeleccionEnFormulario() {
		actualizarBotonEstado();
		Empleado emp = getSeleccionado();
		if (emp == null) return;
		txtNombre.setText(emp.getNombre());
		txtUsuario.setText(emp.getUsuario());
		txtClave.setText(emp.getClave());
		cmbRol.setSelectedItem(emp.getRol());
		chkActivo.setSelected(emp.isActivo());
	}

	private void limpiarFormulario() {
		txtNombre.setText(""); txtUsuario.setText(""); txtClave.setText("");
		cmbRol.setSelectedItem(Rol.EMPLEADO);
		chkActivo.setSelected(true); tabla.clearSelection();
		actualizarBotonEstado();
	}

	private void darDeAlta() {
		try {
			Cafe.getInstancia().altaEmpleado(txtNombre.getText(),
					txtUsuario.getText(), txtClave.getText(),
					(Rol) cmbRol.getSelectedItem());
			ventana.mostrarInfo("Empleado dado de alta.");
			limpiarFormulario(); refrescar();
		} catch (MontoInvalidoException e) { ventana.mostrarError(e.getMessage());
		} catch (AccesoDatosException e)    { ventana.mostrarError(e.getMessage()); }
	}

	private void modificar() {
		Empleado sel = getSeleccionado();
		if (sel == null) { ventana.mostrarError("Seleccione un empleado de la tabla."); return; }
		try {
			Empleado mod = new Empleado(sel.getId(), txtNombre.getText().trim(),
					txtUsuario.getText().trim(), txtClave.getText(), chkActivo.isSelected(),
					(Rol) cmbRol.getSelectedItem());
			Cafe.getInstancia().modificarEmpleado(mod);
			ventana.mostrarInfo("Empleado modificado.");
			refrescar();
		} catch (MontoInvalidoException e) { ventana.mostrarError(e.getMessage());
		} catch (AccesoDatosException e)    { ventana.mostrarError(e.getMessage()); }
	}

	private void cambiarEstado() {
		Empleado sel = getSeleccionado();
		if (sel == null) { ventana.mostrarError("Seleccione un empleado de la tabla."); return; }

		boolean activar = !sel.isActivo();

		// Solo se controla la desactivacion: dejarse a uno mismo afuera cortaria
		// la sesion en curso. Reactivarse no rompe nada.
		if (!activar && sel.getId() == ventana.getEmpleadoActual().getId()) {
			ventana.mostrarError("No puede desactivarse a si mismo mientras usa el sistema.");
			return;
		}

		String accion = activar ? "Activar" : "Desactivar";
		if (!ventana.confirmar(accion + " a " + sel.getNombre() + "?")) return;
		try { Cafe.getInstancia().cambiarEstadoEmpleado(sel.getId(), activar); refrescar(); }
		catch (AccesoDatosException e) { ventana.mostrarError(e.getMessage()); }
	}

	private void eliminar() {
		Empleado sel = getSeleccionado();
		if (sel == null) { ventana.mostrarError("Seleccione un empleado de la tabla."); return; }
		if (!ventana.confirmar("Eliminar definitivamente a " + sel.getNombre() + "?")) return;
		try { Cafe.getInstancia().eliminarEmpleado(sel.getId()); limpiarFormulario(); refrescar(); }
		catch (AccesoDatosException e) { ventana.mostrarError(e.getMessage()); }
	}
}
