# Sistema de Gestión de Restaurante

Examen Final — **Programación Avanzada**
Analista en Sistemas · Escuela Superior de Arte Multimedial Da Vinci

Aplicación de escritorio en Java para administrar la operación diaria de un restaurante: el estado de las mesas del salón, la carta, el personal, y el ciclo completo de una cuenta desde que se abre la mesa hasta que se cobra.

---

## Stack

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 11 (compatible con Java 8) |
| Interfaz gráfica | Swing — `JFrame` con `CardLayout` |
| Persistencia | JDBC sobre MySQL 8 |
| Pruebas | JUnit 5 |
| IDE | Eclipse |

## Puesta en marcha rápida

```bash
# 1. Crear la base de datos
mysql -u root -p < sql/restaurante_final.sql

# 2. Importar en Eclipse: File > Import > Existing Projects into Workspace
# 3. Agregar mysql-connector-j.jar al Build Path
# 4. Ejecutar src/Main.java como Java Application
```

Usuario de prueba: **`mrodriguez`** / **`1234`**

El detalle paso a paso, con troubleshooting, está en **[INSTRUCCIONES-ECLIPSE.md](INSTRUCCIONES-ECLIPSE.md)**.

## Estructura

```
src/
├── modelo/        Entidades del dominio, la clase abstracta y la interfaz Imprimible
├── promociones/   Estrategias de descuento (patrón Strategy)
├── excepciones/   Las cinco excepciones propias del sistema
├── conexion/      ConexionBD, el Singleton de acceso a MySQL
├── dao/           Un DAO por entidad, más ReporteDAO
├── controlador/   Restaurante y Caja: lógica de negocio y validaciones
├── vista/         VentanaPrincipal y los siete paneles
├── util/          ColeccionUtil, con los métodos genéricos
└── test/          26 pruebas JUnit 5 del modelo de dominio
sql/               Script de creación con datos de prueba
docs/              Documentación y diagramas UML (Mermaid)
```

Regla de dependencias, de una sola vía: **Vista → Controlador → DAO → MySQL**. La vista nunca escribe SQL ni valida reglas de negocio.

## El proceso central: apertura y cierre

```
mesa libre → [abrir mesa] → pedido ABIERTO → [cargar consumos] →
   → [cobrar]  → CERRADO + mesa liberada + ticket
   → [anular]  → ANULADO + mesa liberada (sin facturación)
```

Desde `CERRADO` o `ANULADO` no hay transición posible. La regla está escrita una sola vez, en `Pedido.verificarAbierto()`, y todas las operaciones que modifican el pedido pasan por ahí.

## Decisiones de diseño destacadas

**`ItemMenu` es abstracta** y se concreta en `Plato` y `Bebida`, cada una con su propia regla de precio bajo la misma firma:

| Subclase | Regla |
|---|---|
| `Plato` principal | precio base + 10% de recargo de cocina |
| `Plato` entrada | precio base |
| `Bebida` con alcohol | precio base + 15% de recargo de barra |
| `Bebida` sin alcohol | precio base |

El polimorfismo se ve funcionando en `Pedido.calcularDemoraEstimada()`, que recorre una lista de productos mezclados llamando `getMinutosDemora()` sin preguntar el tipo concreto — y además resuelve el máximo con el método genérico `ColeccionUtil.obtenerMaximo()`.

**`Imprimible` es una interfaz** implementada por `Pedido` (ticket) y `ReporteVentas` (informe): dos clases sin ningún parentesco que solo comparten el saber convertirse en texto. Que no es decorativa se comprueba en `PanelReportes.mostrar(Imprimible)`, que recibe la interfaz y no la clase concreta.

**La herencia en la base de datos** usa tabla única con discriminador: `item_menu` con una columna `tipo` y los campos específicos admitiendo `NULL`. Con dos subclases evita cualquier JOIN para leer la carta.

**Los precios se congelan** en `detalle_pedido.precio_unitario`. Si mañana sube la milanesa, los tickets de la semana pasada siguen mostrando lo que el cliente pagó.

La justificación completa de cada decisión está en [`docs/documentacion.md`](docs/documentacion.md).

## Diagramas

- [`docs/uml-clases.mermaid`](docs/uml-clases.mermaid) — diagrama de clases
- [`docs/uml-secuencia-cierre.mermaid`](docs/uml-secuencia-cierre.mermaid) — secuencia del ciclo de apertura y cierre

Se visualizan pegando el contenido en [mermaid.live](https://mermaid.live).

## Cumplimiento de la consigna

| Requisito | Implementación |
|---|---|
| Herencia, polimorfismo, abstracción | `ItemMenu` → `Plato`, `Bebida` |
| Interfaz + clase abstracta | `Imprimible`, `Descuento` / `ItemMenu` |
| ABM completo (≥2 entidades) | Carta, empleados y mesas — tres |
| Ciclo de apertura y cierre | `Caja.abrirMesa()` → `Caja.cerrarCuenta()` |
| Excepciones personalizadas | 5 clases en `excepciones/` |
| Colecciones genéricas | `ArrayList<DetallePedido>`, `HashMap<Integer, Mesa>` |
| Método genérico | `ColeccionUtil.obtenerMaximo()`, `primerosN()` |
| JDBC + CRUD completo | 5 DAO sobre 5 tablas |
| Singleton | `ConexionBD`, `Restaurante`, `Caja` |
| Patrón adicional | Strategy en `promociones/` |
| Interfaz gráfica JFrame | `VentanaPrincipal` con `CardLayout` |
| Reportes y estadísticas | 4 reportes con filtro por fechas en SQL |
