# Cómo abrir y ejecutar el proyecto en Eclipse

Sistema de Gestión de Restaurante — Examen Final, Programación Avanzada

> **Antes de empezar:** este código no pasó por un compilador todavía. Si Eclipse marca algún error al importarlo, andá a la sección 8 (Troubleshooting) — están listados los casos típicos y cómo resolverlos.

---

## 1. Lo que necesitás tener instalado

| Herramienta | Versión | Nota |
|---|---|---|
| JDK | 11 o superior | Java 8 también sirve |
| Eclipse IDE for Java Developers | Cualquiera reciente | |
| MySQL Server | 8.0 recomendado | Con 5.7 también funciona |
| MySQL Connector/J | 8.x | El archivo `.jar` del driver JDBC |

Si no tenés el Connector/J, se descarga de `dev.mysql.com/downloads/connector/j`. Elegí **Platform Independent** y bajá el ZIP: adentro está `mysql-connector-j-8.x.x.jar`. Guardalo en una carpeta que puedas encontrar después, por ejemplo `C:\drivers\`.

---

## 2. Paso 1 — Crear la base de datos

Abrí **MySQL Workbench**, conectate a tu servidor local y:

1. `File` → `Open SQL Script...`
2. elegí `sql/restaurante_final.sql`
3. ejecutá todo el script con el botón del rayo (⚡) o `Ctrl + Shift + Enter`

El script hace un `DROP DATABASE IF EXISTS` al inicio, así que podés volver a ejecutarlo cuantas veces quieras para dejar todo como al principio.

Al final vas a ver una tabla de control con estas cifras:

| Control | Cantidad esperada |
|---|---|
| Empleados cargados | 4 |
| Mesas cargadas | 10 |
| Productos en la carta | 18 |
| Pedidos históricos | 7 |
| Líneas de detalle | 18 |

Si ves esos números, la base quedó bien.

**Alternativa por consola:**

```bash
mysql -u root -p < sql/restaurante_final.sql
```

---

## 3. Paso 2 — Importar el proyecto en Eclipse

El proyecto ya trae los archivos `.project` y `.classpath`, así que Eclipse lo reconoce solo.

1. `File` → `Import...`
2. `General` → `Existing Projects into Workspace` → `Next`
3. En **Select root directory**, apretá `Browse...` y elegí la carpeta `restaurante_final`
4. Tiene que aparecer `restaurante_final` tildado en la lista de proyectos
5. `Finish`

### Si algo falla en la importación

Creá el proyecto a mano, que funciona igual:

1. `File` → `New` → `Java Project`
2. Nombre: `restaurante_final`, `Finish`
3. En el explorador de Windows, copiá **el contenido** de la carpeta `src/` del entregable
4. Pegalo dentro de la carpeta `src` del proyecto nuevo en Eclipse
5. Clic derecho en el proyecto → `Refresh` (o F5)

Deberías ver los nueve paquetes: `conexion`, `controlador`, `dao`, `excepciones`, `modelo`, `promociones`, `test`, `util`, `vista`, más `Main.java` en el paquete por defecto.

---

## 4. Paso 3 — Agregar el driver de MySQL

Sin este paso la aplicación arranca pero no se conecta.

1. Clic derecho en el proyecto → `Properties`
2. `Java Build Path` → pestaña `Libraries`
3. Seleccioná `Classpath` y apretá `Add External JARs...`
4. Elegí tu `mysql-connector-j-8.x.x.jar`
5. `Apply and Close`

Para verificar: en el explorador del proyecto, desplegá `Referenced Libraries`. El jar tiene que estar ahí.

---

## 5. Paso 4 — Ajustar usuario y clave de MySQL

Abrí `src/conexion/ConexionBD.java`. Arriba de la clase están las tres constantes de conexión:

```java
private static final String URL =
        "jdbc:mysql://localhost:3306/restaurante_final?useSSL=false&serverTimezone=America/Argentina/Buenos_Aires";
private static final String USUARIO = "root";
private static final String CLAVE = "";
```

Vienen configuradas para **root sin clave**. Si tu MySQL tiene clave, ponela en `CLAVE`. Si usás otro puerto, cambialo en la `URL`.

---

## 6. Paso 5 — Ejecutar

1. Abrí `src/Main.java`
2. Clic derecho → `Run As` → `Java Application`

Se abre la pantalla de login. Usuarios de prueba, todos con clave **1234**:

| Usuario | Nombre | Estado |
|---|---|---|
| `mrodriguez` | Martin Rodriguez | activo |
| `lgomez` | Lucia Gomez | activo |
| `dfernandez` | Diego Fernandez | activo |
| `sruiz` | Sofia Ruiz | **dado de baja** — sirve para probar que no puede entrar |

---

## 7. Paso 6 — Correr las pruebas (opcional)

Clic derecho en el paquete `test` → `Run As` → `JUnit Test`. Son 26 pruebas y ninguna necesita que MySQL esté prendido.

Si Eclipse marca errores en el paquete `test` porque no encuentra JUnit: clic derecho en el proyecto → `Properties` → `Java Build Path` → `Libraries` → `Add Library...` → `JUnit` → `JUnit 5` → `Finish`.

JUnit **no** es un requisito de este examen, así que si te da problemas podés borrar el paquete `test` sin que nada más se rompa.

---

## 8. Troubleshooting

| Mensaje / síntoma | Causa | Solución |
|---|---|---|
| *"No se encontró el driver de MySQL"* | El jar no está en el Build Path | Repetir el paso 3 |
| `Communications link failure` | El servidor MySQL está apagado | Iniciar el servicio MySQL desde Servicios de Windows o desde Workbench |
| `Access denied for user 'root'@'localhost'` | Usuario o clave incorrectos | Corregir `USUARIO` / `CLAVE` en `ConexionBD` |
| `Unknown database 'restaurante_final'` | No se ejecutó el script SQL | Volver al paso 1 |
| `Public Key Retrieval is not allowed` | Configuración de MySQL 8 | Agregar `&allowPublicKeyRetrieval=true` al final de la `URL` |
| `The server time zone value ... is unrecognized` | Zona horaria del servidor | Cambiar `serverTimezone=America/Argentina/Buenos_Aires` por `serverTimezone=UTC` |
| Errores rojos en el paquete `test` | Falta JUnit 5 | Ver paso 6, o borrar el paquete `test` |
| *"Java compiler level does not match"* | Versión del JDK | Clic derecho en el proyecto → `Properties` → `Java Compiler` → destildar `Enable project specific settings` |
| El salón aparece vacío | No hay mesas en la base | Verificar que el script SQL corrió completo |
| La ventana se abre muy chica | Resolución de pantalla | Maximizar la ventana; los paneles se adaptan |

**Sobre los acentos:** los textos del código están escritos sin tildes a propósito, para que no dependan de la codificación con la que Eclipse abra los archivos. Si querés agregarlos, verificá que el proyecto esté en UTF-8: `Properties` → `Resource` → `Text file encoding`.

---

## 9. Recorrido de prueba sugerido

Este recorrido toca todos los requisitos de la consigna. Serví también para sacar las capturas de pantalla que hay que entregar.

**1. Login** — entrá con `mrodriguez` / `1234`.
Probá primero con `sruiz` / `1234`: tiene que rechazarte porque está dada de baja.

**2. El salón** — vas a ver 10 mesas. Nueve verdes y **la mesa 7 en rojo**, porque el script deja una cuenta abierta a propósito.
📷 *Captura 1: el plano del salón.*

**3. Retomar una cuenta existente** — hacé clic en la mesa 7. Se abre su comanda con tres consumos ya cargados. Fijate en la columna **Detalle**: cada fila dice algo distinto según sea plato o bebida. Ese es el polimorfismo funcionando.
📷 *Captura 2: la comanda de la mesa 7.*

**4. Abrir una mesa nueva** — volvé al salón y hacé clic en la mesa 3. Se abre una cuenta nueva y la mesa pasa a rojo.

**5. Cargar consumos** — agregá 2 milanesas napolitanas y 1 cerveza. Mirá cómo se actualizan el total y la **demora estimada** (25 minutos: el máximo entre los 25 de la milanesa y los 2 de la cerveza, no la suma).

**6. Probar las excepciones** — vale la pena mostrar estas tres en la defensa:

- poné cantidad `0` y agregá → *"La cantidad debe ser mayor que cero"* (`MontoInvalidoException`)
- poné cantidad `abc` → *"La cantidad debe ser un número entero"*
- volvé al salón sin cerrar, y desde el ABM del menú intentá **eliminar** una milanesa → falla porque ya se vendió, y te sugiere la baja lógica

**7. Aplicar un descuento** — elegí `Descuento 10%` y apretá `Aplicar`. El total baja.
📷 *Captura 3: la comanda con descuento aplicado.*

**8. Cerrar la cuenta** — `Cobrar y cerrar cuenta` → confirmá. Aparece el ticket, y al volver al salón la mesa 3 está verde otra vez.
📷 *Captura 4: el ticket de consumo.*

**9. Probar que el ciclo se respeta** — la cuenta que acabás de cerrar ya no se puede modificar. Si volvés a la mesa 3 se abre una cuenta **nueva**, no la anterior.

**10. ABM del menú** — `Carta / Menu`. Dá de alta un postre nuevo, modificalo, buscalo por nombre, y dalo de baja. Fijate en la columna **Precio final**: es distinta del precio base según el recargo que aplica cada subclase.
📷 *Captura 5: el ABM de la carta.*

**11. ABM de empleados** — `Empleados`. Mismo recorrido: alta, modificación, búsqueda, baja.
📷 *Captura 6: el ABM de empleados.*

**12. Reportes** — `Reportes`. Poné como rango desde el primer día del mes hasta hoy y generá los cuatro:

- **Facturación por período** — día por día
- **Productos más vendidos** — top 5 y el producto estrella (acá corre el método genérico)
- **Ventas por mozo**
- **Ticket promedio**

Notá que el pedido **anulado** de la mesa 4 no figura en ningún total, y que la cuenta abierta de la mesa 7 tampoco.
📷 *Captura 7 y 8: dos reportes distintos.*

---

## 10. Qué falta para entregar

- [ ] Sacar las capturas de pantalla del recorrido de la sección 9
- [ ] Exportar los diagramas de `docs/*.mermaid` a imagen — pegá el contenido en `mermaid.live` y descargá el PNG
- [ ] Poner tu nombre y tu división en la portada de `docs/documentacion.md`
- [ ] Comprimir todo en un ZIP: `src/`, `sql/`, `docs/` y este archivo
