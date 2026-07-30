import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import conexion.ConexionBD;
import excepciones.AccesoDatosException;
import vista.VentanaPrincipal;

/**
 * Punto de entrada de la aplicacion.
 *
 * Hace tres cosas:
 *  1. intenta la conexion antes de mostrar nada, para avisar con un mensaje
 *     claro si MySQL no esta levantado en lugar de fallar mas adelante;
 *  2. usa el aspecto visual del sistema operativo;
 *  3. crea la ventana dentro de invokeLater, que es la forma correcta de
 *     arrancar una interfaz Swing.
 */
public class Main {

	public static void main(String[] args) {

		try {
			ConexionBD.getInstancia();
		} catch (AccesoDatosException e) {
			JOptionPane.showMessageDialog(null,
					e.getMessage(),
					"No se pudo iniciar el sistema",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// Si falla, Swing usa su aspecto por defecto: no es un error grave.
			System.err.println("No se pudo aplicar el aspecto del sistema.");
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				VentanaPrincipal ventana = new VentanaPrincipal();
				ventana.setVisible(true);
			}
		});
	}
}
