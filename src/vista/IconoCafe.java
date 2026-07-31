package vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

/**
 * Taza de cafe dibujada con Java2D.
 *
 * POR QUE DIBUJADA Y NO UN .png:
 * el proyecto sigue siendo solo codigo fuente. No hay que acordarse de sumar
 * una carpeta de recursos al Build Path, no se rompe el icono si el archivo no
 * viaja en el ZIP del entregable, y el dibujo sale nitido en cualquier tamano
 * en vez de pixelarse al escalar un mapa de bits.
 */
public final class IconoCafe {

	private IconoCafe() {}

	/** Tamanos que Windows pide para la barra de tareas y el alt-tab. */
	private static final int[] TAMANOS_VENTANA = { 16, 32, 48, 64, 128 };

	public static ImageIcon icono(int tamano, Color color) {
		return new ImageIcon(imagen(tamano, color));
	}

	/** Juego de iconos para JFrame.setIconImages. */
	public static List<java.awt.Image> iconosVentana(Color color) {
		List<java.awt.Image> imagenes = new ArrayList<java.awt.Image>();
		for (int tamano : TAMANOS_VENTANA) {
			imagenes.add(imagen(tamano, color));
		}
		return imagenes;
	}

	public static BufferedImage imagen(int tamano, Color color) {
		BufferedImage img = new BufferedImage(tamano, tamano, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		// El dibujo esta pensado sobre una grilla fija de 64x64 y despues se
		// escala al tamano pedido: asi las proporciones no dependen del tamano.
		double escala = tamano / 64.0;
		g.scale(escala, escala);

		g.setColor(color);
		g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		// Vapor: tres eses que suben desde el borde de la taza
		g.draw(vapor(19));
		g.draw(vapor(28));
		g.draw(vapor(37));

		// Asa, pegada al borde derecho de la taza
		g.draw(new Arc2D.Double(36, 28, 20, 18, 90, -180, Arc2D.OPEN));

		// Cuerpo: apenas mas angosto abajo, con la base redondeada
		Path2D taza = new Path2D.Double();
		taza.moveTo(10, 26);
		taza.lineTo(46, 26);
		taza.lineTo(42, 47);
		taza.curveTo(41, 51, 38, 53, 34, 53);
		taza.lineTo(22, 53);
		taza.curveTo(18, 53, 15, 51, 14, 47);
		taza.closePath();
		g.fill(taza);

		// Plato
		g.fill(new RoundRectangle2D.Double(6, 55, 52, 6, 6, 6));

		g.dispose();
		return img;
	}

	/** Una voluta de vapor que arranca sobre la taza y sube haciendo eses. */
	private static Shape vapor(double x) {
		Path2D p = new Path2D.Double();
		p.moveTo(x, 21);
		p.curveTo(x - 4, 15, x + 4, 11, x, 5);
		return p;
	}
}
