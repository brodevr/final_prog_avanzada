package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import excepciones.ItemNoDisponibleException;
import excepciones.MontoInvalidoException;
import excepciones.PedidoCerradoException;
import modelo.Bebida;
import modelo.Empleado;
import modelo.EstadoPedido;
import modelo.Mesa;
import modelo.Pedido;
import modelo.Plato;
import modelo.Rol;
import promociones.DescuentoMontoFijo;
import promociones.DescuentoPorcentaje;

/**
 * Pruebas del ciclo de apertura y cierre, que es el corazon del sistema.
 *
 * Todas las pruebas trabajan en memoria: no tocan la base de datos. Eso es
 * intencional, porque lo que se esta probando son las REGLAS DE NEGOCIO de la
 * clase Pedido, no la persistencia.
 *
 * Para verificar las excepciones se usa el patron try/fail/catch en lugar de
 * assertThrows con lambdas, para no depender de expresiones lambda.
 */
class PedidoTest {

	private static final double TOLERANCIA = 0.001;

	private Pedido pedido;
	private Plato milanesa;
	private Bebida gaseosa;

	@BeforeEach
	void prepararEscenario() {
		Mesa mesa = new Mesa(1, 4, "Salon");
		Empleado mozo = new Empleado(1, "Martin Rodriguez", "mrodriguez", "1234", true, Rol.EMPLEADO);
		pedido = new Pedido(mesa, mozo);

		// Se asignan ids distintos porque el pedido agrupa las lineas por id.
		milanesa = new Plato(10, "Milanesa napolitana", 10000, true, 25, false);
		gaseosa = new Bebida(20, "Gaseosa", 2500, true, 500, false);
	}

	// ------------------------------------------------------------------
	// Apertura
	// ------------------------------------------------------------------

	@Test
	@DisplayName("Un pedido recien creado nace abierto, vacio y con fecha de apertura")
	void pedidoNuevoNaceAbierto() {
		assertEquals(EstadoPedido.ABIERTO, pedido.getEstado());
		assertTrue(pedido.estaAbierto());
		assertTrue(pedido.getDetalles().isEmpty());
		assertNotNull(pedido.getFechaApertura());
		assertEquals(0, pedido.calcularTotal(), TOLERANCIA);
	}

	// ------------------------------------------------------------------
	// Carga de consumos
	// ------------------------------------------------------------------

	@Test
	@DisplayName("Al agregar consumos el subtotal usa el precio final de cada item")
	void subtotalSumaPreciosFinales() throws Exception {
		pedido.agregarItem(milanesa, 2);   // 11000 x 2 = 22000
		pedido.agregarItem(gaseosa, 1);    //  2500 x 1 =  2500

		assertEquals(2, pedido.getDetalles().size());
		assertEquals(24500, pedido.calcularSubtotal(), TOLERANCIA);
		assertEquals(3, pedido.contarUnidades());
	}

	@Test
	@DisplayName("Agregar dos veces el mismo producto acumula cantidad, no duplica la linea")
	void agregarItemRepetidoAcumula() throws Exception {
		pedido.agregarItem(milanesa, 1);
		pedido.agregarItem(milanesa, 3);

		assertEquals(1, pedido.getDetalles().size());
		assertEquals(4, pedido.getDetalles().get(0).getCantidad());
	}

	@Test
	@DisplayName("La demora estimada es la del producto mas lento de la comanda")
	void demoraEstimadaEsElMaximo() throws Exception {
		pedido.agregarItem(milanesa, 1);   // 25 minutos
		pedido.agregarItem(gaseosa, 2);    //  2 minutos

		assertEquals(25, pedido.calcularDemoraEstimada());
	}

	@Test
	@DisplayName("Una cantidad menor o igual a cero es rechazada")
	void cantidadInvalidaEsRechazada() throws Exception {
		try {
			pedido.agregarItem(milanesa, 0);
			fail("Deberia haber lanzado MontoInvalidoException");
		} catch (MontoInvalidoException e) {
			assertTrue(pedido.getDetalles().isEmpty());
		}
	}

	@Test
	@DisplayName("Un producto no disponible no se puede cargar")
	void itemNoDisponibleEsRechazado() throws Exception {
		Plato fueraDeCarta = new Plato(30, "Cordero patagonico", 25000, false, 60, false);
		try {
			pedido.agregarItem(fueraDeCarta, 1);
			fail("Deberia haber lanzado ItemNoDisponibleException");
		} catch (ItemNoDisponibleException e) {
			assertTrue(pedido.getDetalles().isEmpty());
		}
	}

	// ------------------------------------------------------------------
	// Descuentos
	// ------------------------------------------------------------------

	@Test
	@DisplayName("Un descuento por porcentaje se resta del subtotal")
	void descuentoPorcentajeSeAplica() throws Exception {
		pedido.agregarItem(milanesa, 1);   // 11000
		pedido.setDescuento(new DescuentoPorcentaje(10));

		assertEquals(1100, pedido.calcularDescuento(), TOLERANCIA);
		assertEquals(9900, pedido.calcularTotal(), TOLERANCIA);
	}

	@Test
	@DisplayName("Un cupon mayor al consumo nunca deja el total en negativo")
	void descuentoFijoNoDejaTotalNegativo() throws Exception {
		pedido.agregarItem(gaseosa, 1);    // 2500
		pedido.setDescuento(new DescuentoMontoFijo(9999));

		assertEquals(2500, pedido.calcularDescuento(), TOLERANCIA);
		assertEquals(0, pedido.calcularTotal(), TOLERANCIA);
	}

	// ------------------------------------------------------------------
	// Cierre: el nucleo del ciclo
	// ------------------------------------------------------------------

	@Test
	@DisplayName("Al cerrar, el pedido queda CERRADO y con fecha de cierre")
	void cerrarCambiaEstadoYFecha() throws Exception {
		pedido.agregarItem(milanesa, 1);
		pedido.cerrar();

		assertEquals(EstadoPedido.CERRADO, pedido.getEstado());
		assertFalse(pedido.estaAbierto());
		assertNotNull(pedido.getFechaCierre());
	}

	@Test
	@DisplayName("No se puede cobrar una cuenta sin consumos")
	void noSePuedeCerrarPedidoVacio() throws Exception {
		try {
			pedido.cerrar();
			fail("Deberia haber lanzado MontoInvalidoException");
		} catch (MontoInvalidoException e) {
			assertEquals(EstadoPedido.ABIERTO, pedido.getEstado());
		}
	}

	@Test
	@DisplayName("Un pedido cerrado no admite nuevos consumos")
	void pedidoCerradoRechazaModificaciones() throws Exception {
		pedido.agregarItem(milanesa, 1);
		pedido.cerrar();

		try {
			pedido.agregarItem(gaseosa, 1);
			fail("Deberia haber lanzado PedidoCerradoException");
		} catch (PedidoCerradoException e) {
			assertEquals(1, pedido.getDetalles().size());
		}
	}

	@Test
	@DisplayName("Un pedido cerrado no se puede volver a cerrar")
	void noSePuedeCerrarDosVeces() throws Exception {
		pedido.agregarItem(milanesa, 1);
		pedido.cerrar();

		try {
			pedido.cerrar();
			fail("Deberia haber lanzado PedidoCerradoException");
		} catch (PedidoCerradoException e) {
			assertEquals(EstadoPedido.CERRADO, pedido.getEstado());
		}
	}

	@Test
	@DisplayName("Se puede anular una cuenta abierta, incluso vacia")
	void anularCuentaAbierta() throws Exception {
		pedido.anular();
		assertEquals(EstadoPedido.ANULADO, pedido.getEstado());
		assertNotNull(pedido.getFechaCierre());
	}

	// ------------------------------------------------------------------
	// Ticket
	// ------------------------------------------------------------------

	@Test
	@DisplayName("El ticket incluye los productos y el total")
	void ticketIncluyeDatosBasicos() throws Exception {
		pedido.agregarItem(milanesa, 2);
		pedido.cerrar();

		String ticket = pedido.generarTexto();

		assertTrue(ticket.contains("Milanesa napolitana"));
		assertTrue(ticket.contains("TOTAL A PAGAR"));
		assertTrue(ticket.contains("Martin Rodriguez"));
	}
}
