package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import modelo.RankingItem;
import util.ColeccionUtil;

/**
 * Pruebas del metodo generico. Se lo prueba con tres tipos distintos
 * (Integer, String y RankingItem) justamente para demostrar que es generico.
 */
class ColeccionUtilTest {

	@Test
	@DisplayName("obtenerMaximo funciona con numeros")
	void maximoDeEnteros() {
		List<Integer> numeros = new ArrayList<Integer>();
		numeros.add(Integer.valueOf(4));
		numeros.add(Integer.valueOf(27));
		numeros.add(Integer.valueOf(15));

		assertEquals(Integer.valueOf(27), ColeccionUtil.obtenerMaximo(numeros));
	}

	@Test
	@DisplayName("obtenerMaximo funciona con texto, en orden alfabetico")
	void maximoDeTextos() {
		List<String> nombres = new ArrayList<String>();
		nombres.add("Ana");
		nombres.add("Zulema");
		nombres.add("Bruno");

		assertEquals("Zulema", ColeccionUtil.obtenerMaximo(nombres));
	}

	@Test
	@DisplayName("obtenerMaximo funciona con objetos propios del dominio")
	void maximoDeRanking() {
		List<RankingItem> ranking = new ArrayList<RankingItem>();
		ranking.add(new RankingItem("Empanadas", 12, 48000));
		ranking.add(new RankingItem("Milanesa", 31, 341000));
		ranking.add(new RankingItem("Flan", 7, 21000));

		RankingItem masVendido = ColeccionUtil.obtenerMaximo(ranking);
		assertEquals("Milanesa", masVendido.getNombre());
	}

	@Test
	@DisplayName("obtenerMaximo devuelve null si no hay nada que comparar")
	void maximoDeListaVacia() {
		assertNull(ColeccionUtil.obtenerMaximo(new ArrayList<Integer>()));
		assertNull(ColeccionUtil.obtenerMaximo((List<Integer>) null));
	}

	@Test
	@DisplayName("primerosN recorta la lista sin modificar la original")
	void primerosNRecorta() {
		List<String> original = new ArrayList<String>();
		original.add("a");
		original.add("b");
		original.add("c");
		original.add("d");

		List<String> tres = ColeccionUtil.primerosN(original, 3);

		assertEquals(3, tres.size());
		assertEquals("a", tres.get(0));
		assertEquals(4, original.size());
	}

	@Test
	@DisplayName("primerosN no falla si se piden mas elementos de los que hay")
	void primerosNConLimiteExcesivo() {
		List<String> original = new ArrayList<String>();
		original.add("unico");

		assertEquals(1, ColeccionUtil.primerosN(original, 50).size());
		assertTrue(ColeccionUtil.primerosN(original, 0).isEmpty());
	}
}
