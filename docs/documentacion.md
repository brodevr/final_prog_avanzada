# Sistema de Gestión de Restaurante

**Examen Final — Programación Avanzada**
Carrera: Analista en Sistemas · Escuela Superior de Arte Multimedial Da Vinci
Alumno: Aristides Azócar
Tecnologías: Java (Swing) · MySQL · JDBC

---

## 1. Descripción general y tema elegido

El sistema administra la operación diaria de un restaurante: el estado de las mesas del salón, la carta de productos, el personal, y el ciclo completo de una cuenta desde que se abre la mesa hasta que se cobra.

El tema se eligió porque su proceso central tiene un **ciclo de apertura y cierre nativo del dominio**: en un restaurante literalmente se "abre" y se "cierra" una mesa. Eso permite cumplir el requisito 2 de la consigna sin forzar la interpretación. Además genera datos ricos para reportes: qué se vendió, cuándo, quién lo atendió y por cuánto.

### Entidades principales

| Entidad | Rol |
|---|---|
| `Mesa` | Mesa física del salón. Su número es el identificador natural |
| `Empleado` | Mozo o encargado que opera el sistema |
| `ItemMenu` | Producto de la carta. Es abstracto: se concreta en `Plato` o `Bebida` |
| `Pedido` | Cuenta de una mesa. Es la entidad que implementa el ciclo apertura/cierre |
| `DetallePedido` | Línea de la comanda: un producto, una cantidad y su precio de venta |

### Flujo de una cuenta

```
mesa libre → [abrir mesa] → pedido ABIERTO → [cargar consumos] →
   → [cobrar]  → pedido CERRADO + mesa liberada + ticket
   → [anular]  → pedido ANULADO + mesa liberada (sin facturación)
```

Desde `CERRADO` o `ANULADO` no hay transición posible: el pedido queda histórico y no admite modificaciones. Esa regla está escrita una sola vez, en el método privado `Pedido.verificarAbierto()`, y todas las operaciones que modifican el pedido pasan por él.

### Alcance

**Incluido:** ABM de mesas, carta y empleados · apertura, carga, cobro y anulación de cuentas · descuentos · ticket de consumo · cuatro reportes con filtro por fechas · login de empleado.

**Deliberadamente fuera de alcance:** reservas anticipadas, delivery, stock de insumos con recetas, división de cuenta entre comensales, propinas, múltiples medios de pago, facturación fiscal y pantalla de cocina en tiempo real. La decisión fue mantener acotado el alcance para poder implementar en profundidad todo lo que la consigna pide, en lugar de dejar muchas funciones a medias.

---

## 2. Arquitectura

El proyecto sigue **MVC con capa de acceso a datos**, organizado en ocho paquetes:

```
modelo        Entidades del dominio, la clase abstracta y la interfaz Imprimible
promociones   Estrategias de descuento (patrón Strategy)
excepciones   Las cinco excepciones propias del sistema
conexion      ConexionBD, el Singleton de acceso a MySQL
dao           Un DAO por entidad, más ReporteDAO para las consultas de gestión
controlador   Restaurante y Caja: la lógica de negocio y las validaciones
vista         VentanaPrincipal (JFrame) y los siete paneles
util          ColeccionUtil, con los métodos genéricos
test          Pruebas JUnit 5 del modelo de dominio
```

### Regla de dependencias

```
Vista → Controlador → DAO → MySQL
```

La dirección es de una sola vía y no se saltea ningún eslabón:

- **la vista nunca instancia un DAO ni escribe SQL.** Solo recolecta datos, se los pasa al controlador y muestra lo que recibe;
- **la vista no valida reglas de negocio.** Validar que un precio sea positivo o que una mesa esté libre es responsabilidad del controlador, que es el único lugar donde esa regla vive;
- **el modelo no conoce la base de datos.** `Pedido` sabe calcular su total y cerrar su ciclo; no sabe que existe MySQL;
- **la vista no conoce JDBC.** Los DAO capturan `SQLException` y la re-lanzan como `AccesoDatosException`, que es una excepción del dominio.

El beneficio concreto: si mañana se reemplaza Swing por una interfaz web, o MySQL por PostgreSQL, se reescribe una sola capa y el resto queda intacto.

---

## 3. Justificación de las decisiones de diseño

### 3.1 Por qué `ItemMenu` es una clase abstracta

Un "ítem de menú" genérico no existe en la realidad: lo que existe son platos y bebidas. La clase abstracta concentra lo común (id, nombre, precio base, disponibilidad) y declara abstracto lo que cambia según el tipo:

```java
public abstract double calcularPrecioFinal();
public abstract String getDescripcionDetallada();
public abstract int getMinutosDemora();
public abstract String getTipo();
```

Es abstracta y no concreta porque instanciar un `ItemMenu` suelto no tendría sentido: no se sabría con qué regla calcular su precio.

### 3.2 Dónde está el polimorfismo, concretamente

Cada subclase aplica su propia regla de precio bajo la misma firma:

| Subclase | Regla |
|---|---|
| `Plato` (principal) | precio base + 10% de recargo de cocina |
| `Plato` (entrada) | precio base, sin recargo |
| `Bebida` (con alcohol) | precio base + 15% de recargo de barra |
| `Bebida` (sin alcohol) | precio base, sin recargo |

El polimorfismo se ve funcionando en tres lugares:

**1. `Pedido.calcularDemoraEstimada()`** recorre una lista de productos mezclados y llama `getMinutosDemora()` sin preguntar de qué tipo es cada uno. Un plato devuelve su tiempo de cocción, una bebida devuelve los dos minutos de barra.

**2. `DetallePedido`** toma el precio con `item.calcularPrecioFinal()`. La llamada es idéntica para un plato y para una bebida; el resultado no.

**3. La tabla de la comanda** (`PanelPedido`) llena su columna "Detalle" con `getDescripcionDetallada()`, y cada fila muestra un texto distinto sin que el panel pregunte nunca por el tipo concreto.

La prueba de que el diseño sirve: agregar un `Postre` con su propia regla de precio no obliga a tocar ni una línea de `Pedido`, ni del panel, ni del cálculo del total.

### 3.3 Por qué `Imprimible` es una interfaz y no una superclase

`Imprimible` declara un solo método: `generarTexto()`. La implementan `Pedido` (que genera el ticket de consumo) y `ReporteVentas` (que genera un informe de gestión).

Son dos clases **sin ningún parentesco conceptual**: una es una transacción comercial, la otra es un informe estadístico. No comparten estado ni comportamiento; lo único que tienen en común es *saber convertirse en texto*.

Ese es exactamente el criterio para elegir entre interfaz y herencia:

- la **herencia** modela *"es un"* — `Plato` **es un** `ItemMenu`;
- la **interfaz** modela *"sabe hacer"* — `Pedido` **sabe** imprimirse, igual que `ReporteVentas`.

Forzar una superclase común entre un pedido y un reporte sería inventar un parentesco que no existe.

Que la interfaz no es decorativa se comprueba en `PanelReportes`:

```java
private void mostrar(Imprimible imprimible) {
    areaResultado.setText(imprimible.generarTexto());
}
```

El método recibe la **interfaz**, no la clase concreta. Al panel le da lo mismo qué le pasen: mientras sepa generar su texto, lo puede mostrar.

### 3.4 Cómo se guarda la herencia en la base de datos

Se usa **tabla única con discriminador**: una sola tabla `item_menu` con una columna `tipo` ('PLATO' o 'BEBIDA') y las columnas propias de cada subclase admitiendo `NULL`.

| Ventaja | Desventaja |
|---|---|
| Leer la carta completa no requiere ningún JOIN | Hay columnas siempre en `NULL` para la mitad de las filas |
| El código del DAO queda simple y directo | No se puede poner `NOT NULL` en los campos específicos |

Con dos subclases el costo del espacio desperdiciado es despreciable. Con quince subclases convendría revisar la decisión y pasar a una tabla por subclase.

El punto donde la base de datos "recuerda" la jerarquía es `ItemMenuDAO.mapear()`: lee el discriminador y fabrica la subclase correcta.

Un detalle técnico que importa: al guardar un `Plato` hay que dejar en `NULL` las columnas de `Bebida`, y `setNull()` exige el **tipo SQL** de la columna:

```java
ps.setNull(7, Types.INTEGER);   // mililitros: no aplica a un plato
ps.setNull(8, Types.BOOLEAN);   // alcoholica: no aplica a un plato
```

Es un error frecuente pasarle otra constante ahí (por ejemplo `Statement.RETURN_GENERATED_KEYS`, que se usa en `prepareStatement` y no tiene nada que ver).

### 3.5 Por qué el precio se congela en el detalle

`DetallePedido` guarda su propio `precioUnitario` en lugar de leerlo del producto cada vez que se muestra el ticket.

El motivo es la integridad histórica: si mañana el restaurante aumenta la milanesa, los tickets emitidos la semana pasada deben seguir mostrando el precio que el cliente efectivamente pagó. Si el detalle leyera el precio actual del producto, toda la facturación histórica cambiaría sola con cada aumento de precios.

Por la misma razón, la tabla `pedido` guarda `subtotal`, `descuento` y `total` ya calculados.

### 3.6 Baja física y baja lógica

Un producto que ya se vendió no puede borrarse: la clave foránea de `detalle_pedido` lo protege, y con razón, porque borrarlo destruiría los tickets históricos.

Por eso hay dos operaciones distintas y explícitas, en lugar de una que adivine:

- **Eliminar** (`DELETE`): borrado físico. Solo funciona si el producto nunca se vendió;
- **Dar de baja** (`UPDATE disponible = FALSE`): baja lógica. Sale de la carta pero sigue apareciendo en los pedidos viejos. Es la que se usa normalmente.

Lo mismo aplica a los empleados: el campo `activo` permite que un mozo deje de operar sin perder la trazabilidad de a quién correspondía cada venta.

---

## 4. Patrones de diseño utilizados

### 4.1 Singleton — obligatorio (requisito 6)

**`ConexionBD`** — Abrir una conexión a MySQL es costoso. Si cada DAO abriera la suya, la aplicación mantendría decenas de conexiones vivas contra el mismo servidor. El Singleton garantiza una sola instancia compartida.

Se implementa con tres piezas:

1. constructor `private`, para que nadie pueda hacer `new ConexionBD()`;
2. atributo `private static ConexionBD instancia`, donde la clase guarda su única instancia;
3. método `public static getInstancia()`, que la crea la primera vez y después devuelve siempre la misma.

**`Restaurante`** y **`Caja`** también son Singletons. La configuración del restaurante es única — no tiene sentido que existan dos objetos `Restaurante` con dos cartas distintas — y así los paneles comparten el mismo estado sin necesidad de pasárselo entre sí.

### 4.2 DAO — opcional, implementado

Un DAO por entidad. El controlador pide *"guardame este pedido"* y no sabe si por debajo hay MySQL, un archivo de texto o un servicio web. Todo el SQL del sistema está en el paquete `dao`, y en ningún otro lugar.

### 4.3 DTO — evaluado y descartado con criterio

**No se implementó una capa DTO separada**, y la decisión es deliberada.

Un DTO existe para transportar datos entre capas cuando el objeto de dominio no puede o no debe cruzar una frontera — típicamente en aplicaciones distribuidas, donde la entidad tiene comportamiento o relaciones que no viajan bien por la red.

En esta aplicación de escritorio, `Pedido`, `Mesa` y `ItemMenu` ya cumplen ese rol: son objetos simples que viajan del DAO al controlador y del controlador a la vista sin ningún problema. Agregar un `PedidoDTO` que fuera una copia exacta de `Pedido` sumaría clases y código de conversión sin resolver ningún problema real.

### 4.4 Strategy — patrón adicional (requisito 6, opcional)

La interfaz `Descuento` define cómo se calcula un descuento; cada implementación es una política distinta:

| Clase | Política |
|---|---|
| `SinDescuento` | No descuenta nada |
| `DescuentoPorcentaje` | Un porcentaje del subtotal |
| `DescuentoMontoFijo` | Un importe fijo, acotado al subtotal |
| `DescuentoRegistrado` | Repone desde la base de datos el descuento que se había aplicado |

`Pedido` no sabe *cómo* se calcula el descuento: le pide a su estrategia que lo calcule. Agregar una promoción nueva —un 2x1, un descuento por día de la semana— es escribir una clase más, sin tocar `Pedido`.

`SinDescuento` merece una mención: existe para que `Pedido` no tenga que preguntar `if (descuento == null)` en cada cálculo. Es el criterio del patrón **Null Object**: en lugar de un `null` que hay que chequear en todos lados, un objeto que responde "cero".

---

## 5. Excepciones

Las cinco excepciones son **checked** (extienden `Exception`), de modo que el compilador obliga a decidir qué hacer con cada una.

| Excepción | Cuándo se lanza | Regla que protege |
|---|---|---|
| `MesaOcupadaException` | Se intenta abrir una mesa con cuenta activa | Una mesa, una sola cuenta abierta |
| `PedidoCerradoException` | Se intenta modificar un pedido cerrado o anulado | El ciclo de apertura y cierre |
| `ItemNoDisponibleException` | Se carga un producto fuera de carta | No se vende lo que no hay |
| `MontoInvalidoException` | Cantidad, precio o fecha inválidos | Integridad de los datos |
| `AccesoDatosException` | Cualquier problema de base de datos | Aísla JDBC de las capas superiores |

`AccesoDatosException` cumple una función arquitectónica: envuelve `SQLException` y su mensaje original con `super(mensaje, causa)`, así el usuario ve *"No se pudo conectar a la base de datos, verifique que MySQL esté iniciado"* en lugar de un volcado técnico, pero la causa real sigue disponible para depurar.

Todos los `catch` de la vista terminan mostrando un diálogo al usuario. Ninguna excepción se traga en silencio.

---

## 6. Genéricos y colecciones

### Colecciones utilizadas

- `ArrayList<DetallePedido>` — las líneas de una comanda, donde el orden de carga importa;
- `HashMap<Integer, Mesa>` — caché de mesas indexadas por número, para acceso inmediato sin volver a consultar la base;
- `ArrayList<ItemMenu>`, `ArrayList<Empleado>`, `ArrayList<RankingItem>` — resultados de los listados.

Sobre el `HashMap`: da búsqueda inmediata por número pero **no conserva ningún orden**. Como el salón se dibuja de la mesa 1 en adelante, `listarMesasDeCache()` ordena las claves antes de devolver la lista. Es el compromiso típico de esta estructura: se gana velocidad de búsqueda y se pierde el orden.

### Métodos genéricos

```java
public static <T extends Comparable<T>> T obtenerMaximo(List<T> lista)
public static <T> List<T> primerosN(List<T> lista, int n)
```

En `obtenerMaximo` el parámetro de tipo está **acotado**: `<T extends Comparable<T>>`. Sin esa cota, la llamada a `compareTo()` no compilaría, porque un `T` cualquiera no tiene por qué saber compararse. La cota es la forma de exigirle al tipo la capacidad que el método necesita.

Se usan en tres lugares:

- `Pedido.calcularDemoraEstimada()` — el máximo de una `List<Integer>`;
- `ReporteDAO.rankingProductos()` — el producto más vendido de una `List<RankingItem>`, y el top cinco con `primerosN`;
- `ReporteDAO.ventasPorMozo()` — el mozo con más cuentas atendidas.

`RankingItem` implementa `Comparable<RankingItem>` justamente para poder pasar por `obtenerMaximo`. `ColeccionUtilTest` lo prueba con `Integer`, con `String` y con `RankingItem`, para demostrar que el método es genérico de verdad y no está atado al dominio.

---

## 7. Persistencia (JDBC)

Cinco tablas: `empleado`, `mesa`, `item_menu`, `pedido`, `detalle_pedido`.

CRUD completo en las cuatro entidades principales, como pide el requisito 5. Todas las consultas usan `PreparedStatement` con parámetros — nunca concatenación de strings —, lo que además de ser más rápido evita inyección de SQL.

### El caso de `PedidoDAO`

Un pedido es un **agregado**: la cabecera vive en `pedido` y sus líneas en `detalle_pedido`. Guardarlo significa:

1. `INSERT` de la cabecera con `Statement.RETURN_GENERATED_KEYS`;
2. recuperar el id autogenerado con `getGeneratedKeys()`;
3. recién entonces insertar las líneas apuntando a ese id.

Al modificar, las líneas se borran y se reinsertan en lugar de calcular qué cambió. Para la cantidad de líneas que tiene una comanda es la solución más simple y la menos propensa a errores.

### La regla de los reportes

**El filtro por fechas va siempre en el `WHERE` de la consulta, nunca trayendo toda la tabla a memoria y filtrando en Java.**

```sql
WHERE estado = 'CERRADO' AND DATE(fecha_cierre) BETWEEN ? AND ?
```

Filtrar en Java después de traer todo funciona con veinte pedidos de prueba y colapsa con veinte mil reales. Además, la base de datos es el componente optimizado para filtrar, y hay un índice (`idx_pedido_cierre`) puesto exactamente sobre `estado` y `fecha_cierre` para acelerarlo.

Los cuatro reportes consideran únicamente pedidos `CERRADO`: los `ABIERTO` todavía no se cobraron y los `ANULADO` no representan facturación. Los datos de prueba incluyen un pedido anulado a propósito, para poder verificar que no aparece en los totales.

---

## 8. Interfaz gráfica

`VentanaPrincipal` es un `JFrame` único con `CardLayout`. En lugar de abrir una ventana nueva por cada función, todas las pantallas son paneles apilados en un mismo contenedor y se muestra uno a la vez.

| Panel | Función |
|---|---|
| `PanelLogin` | Acceso con usuario y clave |
| `PanelMesas` | Plano del salón. Es el **menú principal** del sistema |
| `PanelPedido` | Comanda: carga de consumos, descuentos, cobro y anulación |
| `PanelMenuABM` | ABM completo de la carta |
| `PanelEmpleadosABM` | ABM completo del personal |
| `PanelReportes` | Los cuatro reportes con filtro de fechas |
| `PanelTicket` | Muestra el ticket de consumo emitido |

`PanelMesas` cumple el requisito del menú principal: cada mesa es un botón (verde si está libre, rojo si está ocupada) y la barra superior da acceso a todas las demás funciones. Un clic sobre una mesa abre su cuenta o retoma la existente — es la puerta de entrada al ciclo de apertura y cierre.

Los diálogos están centralizados en `VentanaPrincipal.mostrarError()`, `mostrarInfo()` y `confirmar()`, para que todos los paneles avisen de la misma manera.

---

## 9. Pruebas

`JUnit 5` no figura entre los requisitos de este examen, pero se incluyen tres clases de prueba porque validan las reglas más delicadas del modelo:

| Clase | Qué verifica |
|---|---|
| `ItemMenuTest` | Las cuatro reglas de precio y que una lista heterogénea resuelva cada una con su subclase |
| `PedidoTest` | El ciclo completo: apertura, acumulación de cantidades, descuentos, cierre, anulación, y que un pedido cerrado rechace modificaciones |
| `ColeccionUtilTest` | Los métodos genéricos con tres tipos distintos |

Las pruebas trabajan **en memoria y no tocan la base de datos**. Eso es intencional: lo que se prueba son las reglas de negocio del modelo, no la persistencia. Una prueba que necesita una base de datos levantada para correr no es una prueba unitaria.

Para verificar las excepciones se usa el patrón `try` / `fail` / `catch` en lugar de `assertThrows` con lambdas, para no depender de expresiones lambda.

---

## 10. Instrucciones de compilación y ejecución

Paso a paso detallado en **`INSTRUCCIONES-ECLIPSE.md`**. En resumen:

1. ejecutar `sql/restaurante_final.sql` en MySQL;
2. importar el proyecto en Eclipse;
3. agregar `mysql-connector-j.jar` al Build Path;
4. si el usuario o la clave de MySQL no son `root` / vacío, ajustar las constantes al inicio de `ConexionBD`;
5. ejecutar `Main.java` como Java Application;
6. entrar con `mrodriguez` / `1234`.

---

## 11. Cumplimiento de la consigna

| # | Requisito | Implementación |
|---|---|---|
| 1 | Diagrama de clases UML | `docs/uml-clases.mermaid` |
| 1 | Herencia, polimorfismo, abstracción | `ItemMenu` → `Plato`, `Bebida` |
| 1 | Al menos una interfaz y una abstracta | `Imprimible`, `Descuento` / `ItemMenu` |
| 2 | ABM completo en ≥2 entidades | `ItemMenu`, `Empleado` y `Mesa` — tres |
| 2 | Proceso con ciclo apertura/cierre | `Caja.abrirMesa()` → `Caja.cerrarCuenta()` |
| 2 | *Adicional:* reportes o estadísticas | Cuatro reportes con rango de fechas |
| 3 | Excepciones en operaciones críticas | Paquete `excepciones`, cinco clases |
| 3 | Excepciones personalizadas | `MesaOcupadaException`, `PedidoCerradoException`, `ItemNoDisponibleException` |
| 4 | Colecciones genéricas | `ArrayList<DetallePedido>`, `HashMap<Integer, Mesa>` |
| 4 | *Adicional:* método genérico | `ColeccionUtil.obtenerMaximo()` y `primerosN()` |
| 5 | JDBC | `ConexionBD` + cinco DAO |
| 5 | CRUD en todas las entidades | Alta, baja, modificación y consulta en las cuatro |
| 6 | Singleton para la conexión | `ConexionBD` |
| 6 | *Opcional:* DAO y DTO | DAO implementado; DTO evaluado y descartado (§4.3) |
| 6 | *Opcional:* patrón adicional | Strategy en `promociones` |
| 7 | Interfaz gráfica JFrame | `VentanaPrincipal` con `CardLayout` |
| 7 | Menú principal a todas las funciones | `PanelMesas` |

**Entregables:** código fuente (`src/`) · UML de clases y de secuencia (`docs/`) · script SQL (`sql/`) · esta documentación · capturas de pantalla *(pendientes: sacar al ejecutar)*.
