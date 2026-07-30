package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import modelo.Bebida;
import modelo.ItemMenu;
import modelo.Plato;

/**
 * Pruebas de las reglas de precio de cada subclase.
 *
 * NOTA: JUnit no figura entre los requisitos de este examen final. Estas
 * pruebas se agregan porque validan las reglas de negocio mas delicadas del
 * modelo y sirven para demostrar que el polimorfismo funciona.
 */
class ItemMenuTest {

	private static final double TOLERANCIA = 0.001;

	@Test
	@DisplayName("Un plato principal lleva 10% de recargo de cocina")
	void platoPrincipalAplicaRecargo() {
		Plato milanesa = new Plato("Milanesa napolitana", 10000, 25, false);
		assertEquals(11000, milanesa.calcularPrecioFinal(), TOLERANCIA);
	}

	@Test
	@DisplayName("Una entrada se vende a precio base, sin recargo")
	void entradaNoAplicaRecargo() {
		Plato empanadas = new Plato("Empanadas", 4000, 10, true);
		assertEquals(4000, empanadas.calcularPrecioFinal(), TOLERANCIA);
	}

	@Test
	@DisplayName("Una bebida con alcohol lleva 15% de recargo de barra")
	void bebidaAlcoholicaAplicaRecargo() {
		Bebida cerveza = new Bebida("Cerveza artesanal", 5000, 500, true);
		assertEquals(5750, cerveza.calcularPrecioFinal(), TOLERANCIA);
	}

	@Test
	@DisplayName("Una bebida sin alcohol se vende a precio base")
	void bebidaSinAlcoholNoAplicaRecargo() {
		Bebida agua = new Bebida("Agua mineral", 2000, 500, false);
		assertEquals(2000, agua.calcularPrecioFinal(), TOLERANCIA);
	}

	@Test
	@DisplayName("Una lista de ItemMenu resuelve cada precio con la regla de su subclase")
	void listaHeterogeneaResuelvePolimorficamente() {
		List<ItemMenu> carta = new ArrayList<ItemMenu>();
		carta.add(new Plato("Bife de chorizo", 10000, 20, false));   // 11000
		carta.add(new Plato("Provoleta", 5000, 8, true));            //  5000
		carta.add(new Bebida("Vino malbec", 8000, 750, true));       //  9200
		carta.add(new Bebida("Gaseosa", 2500, 500, false));          //  2500

		double total = 0;
		for (int i = 0; i < carta.size(); i++) {
			total += carta.get(i).calcularPrecioFinal();
		}

		assertEquals(27700, total, TOLERANCIA);
	}

	@Test
	@DisplayName("Cada subclase informa su propia demora")
	void demoraDependeDelTipo() {
		Plato guiso = new Plato("Guiso de lentejas", 9000, 35, false);
		Bebida soda = new Bebida("Soda", 1500, 500, false);

		assertEquals(35, guiso.getMinutosDemora());
		assertEquals(2, soda.getMinutosDemora());
		assertTrue(guiso.getMinutosDemora() > soda.getMinutosDemora());
	}
}
