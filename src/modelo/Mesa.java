package modelo;

/**
 * Mesa fisica del salon. El numero de mesa es su identificador natural.
 */
public class Mesa {

	private int numero;
	private int capacidad;
	private String sector;
	private boolean ocupada;

	public Mesa(int numero, int capacidad, String sector, boolean ocupada) {
		this.numero = numero;
		this.capacidad = capacidad;
		this.sector = sector;
		this.ocupada = ocupada;
	}

	public Mesa(int numero, int capacidad, String sector) {
		this(numero, capacidad, sector, false);
	}

	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}

	public int getCapacidad() {
		return capacidad;
	}

	public void setCapacidad(int capacidad) {
		this.capacidad = capacidad;
	}

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public boolean isOcupada() {
		return ocupada;
	}

	public void setOcupada(boolean ocupada) {
		this.ocupada = ocupada;
	}

	@Override
	public String toString() {
		return "Mesa " + numero + " (" + capacidad + " pers.)";
	}
}
