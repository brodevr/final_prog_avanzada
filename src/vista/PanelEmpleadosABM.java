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
import modelo.Empleado;

/**
 * ABM completo de empleados: es la segunda entidad con alta, modificacion, baja
 * y busqueda, que es lo que exige el requisito 2 de la consigna.
 */
public class PanelEmpleadosABM extends JPanel {

	private static final long serialVersionUID = 1L;

	private VentanaPrincipal ventana;

	private JTable tabla;
	private DefaultTableModel modeloTabla;
	private List<Empleado> empleadosEnTabla = new ArrayList<Empleado>();

	private JTextField txtNombre;
	private JTextField txtUsuario;
	private JTextField txtClave;
	private JCheckBox chkActivo;
	private JTextField txtBuscar;

	public PanelEmpleadosABM(VentanaPrincipal ventana) {
		this.ventana = ventana;
		setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		add(construirCabecera(), BorderLayout.NORTH);
		add(construirTabla(), BorderLayout.CENTER);
		add(construirFormulario(), BorderLayout.SOUTH);
	}

	private JPanel construirCabecera() {
		JPanel cabecera = new JPanel(new BorderLayout());

		JLabel titulo = new JLabel("Personal del restaurante");
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
		String[] columnas = { "ID", "Nombre", "Usuario", "Activo" };

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
		contenedor.setBorder(BorderFactory.createTitledBorder("Datos del empleado"));

		JPanel campos = new JPanel(new GridLayout(2, 4, 8, 8));

		txtNombre = new JTextField();
		txtUsuario = new JTextField();
		txtClave = new JTextField();
		chkActivo = new JCheckBox("Activo", true);

		campos.add(new JLabel("Nombre completo:"));
		campos.add(txtNombre);
		campos.add(new JLabel("Usuario:"));
		campos.add(txtUsuario);
		campos.add(new JLabel("Clave:"));
		campos.add(txtClave);
		campos.add(chkActivo);
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

	// ------------------------------------------------------------------
	// Datos
	// ------------------------------------------------------------------

	public void refrescar() {
		try {
			mostrar(Restaurante.getInstancia().listarEmpleados());
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}

	private void buscar() {
		try {
			mostrar(Restaurante.getInstancia().buscarEmpleados(txtBuscar.getText()));
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}

	private void mostrar(List<Empleado> empleados) {
		empleadosEnTabla = empleados;
		modeloTabla.setRowCount(0);

		for (int i = 0; i < empleados.size(); i++) {
			Empleado empleado = empleados.get(i);
			modeloTabla.addRow(new Object[] {
					Integer.valueOf(empleado.getId()),
					empleado.getNombre(),
					empleado.getUsuario(),
					empleado.isActivo() ? "SI" : "NO" });
		}
	}

	private Empleado getSeleccionado() {
		int fila = tabla.getSelectedRow();
		if (fila < 0 || fila >= empleadosEnTabla.size()) {
			return null;
		}
		return empleadosEnTabla.get(fila);
	}

	private void cargarSeleccionEnFormulario() {
		Empleado empleado = getSeleccionado();
		if (empleado == null) {
			return;
		}
		txtNombre.setText(empleado.getNombre());
		txtUsuario.setText(empleado.getUsuario());
		txtClave.setText(empleado.getClave());
		chkActivo.setSelected(empleado.isActivo());
	}

	private void limpiarFormulario() {
		txtNombre.setText("");
		txtUsuario.setText("");
		txtClave.setText("");
		chkActivo.setSelected(true);
		tabla.clearSelection();
	}

	// ------------------------------------------------------------------
	// Operaciones
	// ------------------------------------------------------------------

	private void darDeAlta() {
		try {
			Restaurante.getInstancia().altaEmpleado(txtNombre.getText(),
					txtUsuario.getText(), txtClave.getText());
			ventana.mostrarInfo("Empleado dado de alta.");
			limpiarFormulario();
			refrescar();

		} catch (MontoInvalidoException e) {
			ventana.mostrarError(e.getMessage());
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}

	private void modificar() {
		Empleado seleccionado = getSeleccionado();
		if (seleccionado == null) {
			ventana.mostrarError("Seleccione un empleado de la tabla.");
			return;
		}

		try {
			Empleado modificado = new Empleado(seleccionado.getId(),
					txtNombre.getText().trim(),
					txtUsuario.getText().trim(),
					txtClave.getText(),
					chkActivo.isSelected());

			Restaurante.getInstancia().modificarEmpleado(modificado);
			ventana.mostrarInfo("Empleado modificado.");
			refrescar();

		} catch (MontoInvalidoException e) {
			ventana.mostrarError(e.getMessage());
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}

	private void darDeBaja() {
		Empleado seleccionado = getSeleccionado();
		if (seleccionado == null) {
			ventana.mostrarError("Seleccione un empleado de la tabla.");
			return;
		}
		if (seleccionado.getId() == ventana.getEmpleadoActual().getId()) {
			ventana.mostrarError("No puede darse de baja a si mismo mientras usa el sistema.");
			return;
		}
		if (!ventana.confirmar("Dar de baja a " + seleccionado.getNombre() + "?")) {
			return;
		}

		try {
			Restaurante.getInstancia().darDeBajaEmpleado(seleccionado.getId());
			refrescar();
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}

	private void eliminar() {
		Empleado seleccionado = getSeleccionado();
		if (seleccionado == null) {
			ventana.mostrarError("Seleccione un empleado de la tabla.");
			return;
		}
		if (!ventana.confirmar("Eliminar definitivamente a " + seleccionado.getNombre()
				+ "? Si tiene pedidos registrados, use la baja logica.")) {
			return;
		}

		try {
			Restaurante.getInstancia().eliminarEmpleado(seleccionado.getId());
			limpiarFormulario();
			refrescar();
		} catch (AccesoDatosException e) {
			ventana.mostrarError(e.getMessage());
		}
	}
}
