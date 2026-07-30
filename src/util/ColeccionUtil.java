package util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilidades genericas para trabajar con colecciones.
 *
 * Cumple el requisito 4 de la consigna ("implementar un metodo generico util
 * para el sistema"). Los metodos son genericos de verdad: no saben nada del
 * dominio del restaurante y sirven para cualquier tipo de dato.
 */
public class ColeccionUtil {

	/** Clase de utilidades: no se instancia. */
	private ColeccionUtil() {
	}

	/**
	 * Devuelve el elemento mayor de una lista.
	 *
	 * El parametro de tipo esta acotado con &lt;T extends Comparable&lt;T&gt;&gt;
	 * porque para comparar dos elementos necesitamos garantizar que sepan
	 * compararse entre si. Sin esa cota, la llamada a compareTo no compilaria.
	 *
	 * @return el elemento mayor, o null si la lista es nula o esta vacia.
	 */
	public static <T extends Comparable<T>> T obtenerMaximo(List<T> lista) {
		if (lista == null || lista.isEmpty()) {
			return null;
		}
		T maximo = lista.get(0);
		for (int i = 1; i < lista.size(); i++) {
			T actual = lista.get(i);
			if (actual.compareTo(maximo) > 0) {
				maximo = actual;
			}
		}
		return maximo;
	}

	/**
	 * Devuelve una nueva lista con los primeros n elementos de la original.
	 * Se usa para armar los "top 5" de los reportes.
	 */
	public static <T> List<T> primerosN(List<T> lista, int n) {
		List<T> resultado = new ArrayList<T>();
		if (lista == null || n <= 0) {
			return resultado;
		}
		int limite = Math.min(n, lista.size());
		for (int i = 0; i < limite; i++) {
			resultado.add(lista.get(i));
		}
		return resultado;
	}
}
