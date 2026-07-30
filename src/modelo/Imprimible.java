package modelo;

/**
 * Contrato para todo objeto capaz de representarse como texto imprimible.
 *
 * JUSTIFICACION DE POR QUE ES UNA INTERFAZ Y NO UNA CLASE ABSTRACTA:
 * la implementan Pedido (que genera el ticket de consumo) y ReporteVentas
 * (que genera un informe). Son dos clases sin ningun parentesco conceptual
 * -una es una transaccion, la otra es un informe- que solo comparten un
 * comportamiento. Cuando lo unico en comun es "saber hacer algo" y no "ser
 * algo", corresponde una interfaz.
 */
public interface Imprimible {

	/** Devuelve el contenido listo para mostrarse en pantalla o imprimirse. */
	String generarTexto();
}
