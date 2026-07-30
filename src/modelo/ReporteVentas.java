package modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Informe de gestion sobre un rango de fechas.
 *
 * Es la segunda implementadora de Imprimible. No comparte ninguna jerarquia
 * con Pedido: la unica cosa en comun es saber convertirse en texto, y por eso
 * lo que las vincula es una interfaz y no una superclase.
 */
public class ReporteVentas implements Imprimible {

	private String titulo;
	private LocalDate desde;
	private LocalDate hasta;
	private List<String> lineas;
	private String resumen;

	public ReporteVentas(String titulo, LocalDate desde, LocalDate hasta) {
		this.titulo = titulo;
		this.desde = desde;
		this.hasta = hasta;
		this.lineas = new ArrayList<String>();
		this.resumen = "";
	}

	public void agregarLinea(String linea) {
		lineas.add(linea);
	}

	@Override
	public String generarTexto() {
		DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		StringBuilder sb = new StringBuilder();

		sb.append("==================================================\n");
		sb.append("  ").append(titulo.toUpperCase()).append("\n");
		sb.append("==================================================\n");
		sb.append("Periodo: ").append(desde.format(formato));
		sb.append("  al  ").append(hasta.format(formato)).append("\n");
		sb.append("--------------------------------------------------\n");

		if (lineas.isEmpty()) {
			sb.append("Sin movimientos registrados en el periodo.\n");
		} else {
			for (int i = 0; i < lineas.size(); i++) {
				sb.append(lineas.get(i)).append("\n");
			}
		}

		if (resumen != null && !resumen.isEmpty()) {
			sb.append("--------------------------------------------------\n");
			sb.append(resumen).append("\n");
		}
		sb.append("==================================================\n");

		return sb.toString();
	}

	public String getTitulo() {
		return titulo;
	}

	public LocalDate getDesde() {
		return desde;
	}

	public LocalDate getHasta() {
		return hasta;
	}

	public List<String> getLineas() {
		return lineas;
	}

	public String getResumen() {
		return resumen;
	}

	public void setResumen(String resumen) {
		this.resumen = resumen;
	}

	@Override
	public String toString() {
		return titulo;
	}
}
