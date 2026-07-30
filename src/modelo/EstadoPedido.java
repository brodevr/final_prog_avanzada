package modelo;

/**
 * Estados posibles de un pedido. Modela el ciclo de apertura y cierre:
 *
 *   ABIERTO --> CERRADO   (se cobro la cuenta)
 *   ABIERTO --> ANULADO   (se cancelo la mesa)
 *
 * Desde CERRADO o ANULADO no hay transicion posible: el pedido queda historico.
 */
public enum EstadoPedido {
	ABIERTO, CERRADO, ANULADO
}
