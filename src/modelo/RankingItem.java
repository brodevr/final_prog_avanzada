package modelo;

/**
 * Fila de un ranking: un nombre, una cantidad y un importe.
 *
 * Implementa Comparable para poder usarse con el metodo generico
 * ColeccionUtil.obtenerMaximo, que exige que el tipo sepa compararse consigo
 * mismo. Se ordena por cantidad y, si empatan, por importe.
 */
public class RankingItem implements Comparable<RankingItem> {

	private String nombre;
	private int cantidad;
	private double monto;

	public RankingItem(String nombre, int cantidad, double monto) {
		this.nombre = nombre;
		this.cantidad = cantidad;
		this.monto = monto;
	}

	@Override
	public int compareTo(RankingItem otro) {
		if (this.cantidad != otro.cantidad) {
			return Integer.compare(this.cantidad, otro.cantidad);
		}
		return Double.compare(this.monto, otro.monto);
	}

	public String getNombre() {
		return nombre;
	}

	public int getCantidad() {
		return cantidad;
	}

	public double getMonto() {
		return monto;
	}

	@Override
	public String toString() {
		return nombre + " (" + cantidad + " u. - $" + String.format("%.2f", monto) + ")";
	}
}
