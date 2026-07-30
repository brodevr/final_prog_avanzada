-- =====================================================================
-- EXAMEN FINAL - PROGRAMACION AVANZADA
-- Sistema de Gestion de Cafeteria
--
-- Ejecutar completo en MySQL Workbench o por consola:
--   mysql -u root -p < restaurante_final.sql
-- =====================================================================

DROP DATABASE IF EXISTS restaurante_final;
CREATE DATABASE restaurante_final
	CHARACTER SET utf8mb4
	COLLATE utf8mb4_unicode_ci;
USE restaurante_final;

-- ---------------------------------------------------------------------
-- EMPLEADO
-- ---------------------------------------------------------------------
CREATE TABLE empleado (
	id      INT AUTO_INCREMENT PRIMARY KEY,
	nombre  VARCHAR(80) NOT NULL,
	usuario VARCHAR(30) NOT NULL UNIQUE,
	clave   VARCHAR(30) NOT NULL,
	activo  BOOLEAN NOT NULL DEFAULT TRUE
);

-- ---------------------------------------------------------------------
-- MESA
-- ---------------------------------------------------------------------
CREATE TABLE mesa (
	numero     INT PRIMARY KEY,
	capacidad  INT NOT NULL,
	sector     VARCHAR(30) NOT NULL,
	ocupada    BOOLEAN NOT NULL DEFAULT FALSE
);

-- ---------------------------------------------------------------------
-- ITEM_MENU  (tabla unica con discriminador tipo PLATO / BEBIDA)
-- ---------------------------------------------------------------------
CREATE TABLE item_menu (
	id                  INT AUTO_INCREMENT PRIMARY KEY,
	tipo                VARCHAR(10) NOT NULL,
	nombre              VARCHAR(80) NOT NULL,
	precio_base         DECIMAL(10,2) NOT NULL,
	disponible          BOOLEAN NOT NULL DEFAULT TRUE,
	minutos_preparacion INT NULL,
	es_entrada          BOOLEAN NULL,
	mililitros          INT NULL,
	alcoholica          BOOLEAN NULL,
	CONSTRAINT chk_tipo_item CHECK (tipo IN ('PLATO', 'BEBIDA'))
);

-- ---------------------------------------------------------------------
-- PEDIDO
-- ---------------------------------------------------------------------
CREATE TABLE pedido (
	id             INT AUTO_INCREMENT PRIMARY KEY,
	mesa_numero    INT NOT NULL,
	empleado_id    INT NOT NULL,
	fecha_apertura DATETIME NOT NULL,
	fecha_cierre   DATETIME NULL,
	estado         VARCHAR(10) NOT NULL,
	subtotal       DECIMAL(10,2) NOT NULL DEFAULT 0,
	descuento      DECIMAL(10,2) NOT NULL DEFAULT 0,
	descuento_desc VARCHAR(50) NULL,
	total          DECIMAL(10,2) NOT NULL DEFAULT 0,
	CONSTRAINT fk_pedido_mesa     FOREIGN KEY (mesa_numero) REFERENCES mesa(numero),
	CONSTRAINT fk_pedido_empleado FOREIGN KEY (empleado_id) REFERENCES empleado(id),
	CONSTRAINT chk_estado_pedido  CHECK (estado IN ('ABIERTO', 'CERRADO', 'ANULADO'))
);

CREATE INDEX idx_pedido_cierre ON pedido (estado, fecha_cierre);

-- ---------------------------------------------------------------------
-- DETALLE_PEDIDO
-- ---------------------------------------------------------------------
CREATE TABLE detalle_pedido (
	id              INT AUTO_INCREMENT PRIMARY KEY,
	pedido_id       INT NOT NULL,
	item_id         INT NOT NULL,
	cantidad        INT NOT NULL,
	precio_unitario DECIMAL(10,2) NOT NULL,
	CONSTRAINT fk_detalle_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id)
		ON DELETE CASCADE,
	CONSTRAINT fk_detalle_item   FOREIGN KEY (item_id) REFERENCES item_menu(id),
	CONSTRAINT chk_cantidad      CHECK (cantidad > 0)
);

-- =====================================================================
-- DATOS DE PRUEBA
-- =====================================================================

-- --- Empleados (clave de todos: 1234) --------------------------------
INSERT INTO empleado (nombre, usuario, clave, activo) VALUES
	('Martin Rodriguez',  'mrodriguez', '1234', TRUE),
	('Lucia Gomez',       'lgomez',     '1234', TRUE),
	('Diego Fernandez',   'dfernandez', '1234', TRUE),
	('Ana Lopez',         'alopez',     '1234', TRUE),
	('Carlos Mendez',     'cmendez',    '1234', TRUE),
	('Sofia Ruiz',        'sruiz',      '1234', FALSE);

-- --- Mesas -----------------------------------------------------------
-- Sectores: Salon (interior), Terraza (exterior), Barra, Privado
INSERT INTO mesa (numero, capacidad, sector, ocupada) VALUES
	( 1, 2, 'Salon',   FALSE),
	( 2, 2, 'Salon',   FALSE),
	( 3, 4, 'Salon',   FALSE),
	( 4, 4, 'Salon',   FALSE),
	( 5, 6, 'Salon',   FALSE),
	( 6, 2, 'Terraza', FALSE),
	( 7, 4, 'Terraza', FALSE),
	( 8, 2, 'Terraza', FALSE),
	( 9, 2, 'Barra',   FALSE),
	(10, 2, 'Barra',   FALSE),
	(11, 8, 'Privado', FALSE),
	(12, 10,'Privado', FALSE);

-- --- Carta: comidas --------------------------------------------------
-- Regla de precios (Java):
--   PLATO entrada     -> precio_base (sin recargo)
--   PLATO principal   -> precio_base * 1.10
--
-- Precio final de cada plato:
--   1  Medialuna de manteca (3u)    2200 * 1.00 = 2200
--   2  Croissant de jamon y queso   3800 * 1.10 = 4180
--   3  Tostado mixto                3500 * 1.10 = 3850
--   4  Sandwich de miga (2u)        3000 * 1.10 = 3300
--   5  Empanada de verdura (1u)     1600 * 1.00 = 1600
--   6  Muffin de arandanos          2400 * 1.00 = 2400
--   7  Brownie con helado           4500 * 1.10 = 4950
--   8  Cheesecake del dia           5000 * 1.10 = 5500
--   9  Tostada con palta y huevo    5200 * 1.10 = 5720
--  10  Porcion de focaccia          3200 * 1.00 = 3200
INSERT INTO item_menu (tipo, nombre, precio_base, disponible, minutos_preparacion, es_entrada, mililitros, alcoholica) VALUES
	('PLATO', 'Medialuna de manteca (3u)',   2200.00, TRUE,  5,  TRUE,  NULL, NULL),
	('PLATO', 'Croissant de jamon y queso',  3800.00, TRUE,  8,  FALSE, NULL, NULL),
	('PLATO', 'Tostado mixto',               3500.00, TRUE,  10, FALSE, NULL, NULL),
	('PLATO', 'Sandwich de miga (2u)',        3000.00, TRUE,  5,  FALSE, NULL, NULL),
	('PLATO', 'Empanada de verdura (1u)',     1600.00, TRUE,  8,  TRUE,  NULL, NULL),
	('PLATO', 'Muffin de arandanos',          2400.00, TRUE,  3,  TRUE,  NULL, NULL),
	('PLATO', 'Brownie con helado',           4500.00, TRUE,  5,  FALSE, NULL, NULL),
	('PLATO', 'Cheesecake del dia',           5000.00, TRUE,  3,  FALSE, NULL, NULL),
	('PLATO', 'Tostada con palta y huevo',    5200.00, TRUE,  8,  FALSE, NULL, NULL),
	('PLATO', 'Porcion de focaccia',          3200.00, TRUE,  12, TRUE,  NULL, NULL);

-- --- Carta: bebidas --------------------------------------------------
-- Regla de precios (Java):
--   BEBIDA alcoholica     -> precio_base * 1.15
--   BEBIDA no alcoholica  -> precio_base
--
-- Precio final de cada bebida:
--  11  Cafe espresso       1800 * 1.00 = 1800
--  12  Cortado             2000 * 1.00 = 2000
--  13  Cafe con leche      2400 * 1.00 = 2400
--  14  Cappuccino          2800 * 1.00 = 2800
--  15  Latte               3200 * 1.00 = 3200
--  16  Submarino           2600 * 1.00 = 2600
--  17  Te surtido          2000 * 1.00 = 2000
--  18  Agua mineral        1200 * 1.00 = 1200
--  19  Jugo de naranja     3200 * 1.00 = 3200
--  20  Cerveza artesanal   4500 * 1.15 = 5175
INSERT INTO item_menu (tipo, nombre, precio_base, disponible, minutos_preparacion, es_entrada, mililitros, alcoholica) VALUES
	('BEBIDA', 'Cafe espresso',      1800.00, TRUE, NULL, NULL,  60,  FALSE),
	('BEBIDA', 'Cortado',            2000.00, TRUE, NULL, NULL, 100,  FALSE),
	('BEBIDA', 'Cafe con leche',     2400.00, TRUE, NULL, NULL, 280,  FALSE),
	('BEBIDA', 'Cappuccino',         2800.00, TRUE, NULL, NULL, 250,  FALSE),
	('BEBIDA', 'Latte',              3200.00, TRUE, NULL, NULL, 350,  FALSE),
	('BEBIDA', 'Submarino',          2600.00, TRUE, NULL, NULL, 220,  FALSE),
	('BEBIDA', 'Te surtido',         2000.00, TRUE, NULL, NULL, 300,  FALSE),
	('BEBIDA', 'Agua mineral',       1200.00, TRUE, NULL, NULL, 500,  FALSE),
	('BEBIDA', 'Jugo de naranja',    3200.00, TRUE, NULL, NULL, 350,  FALSE),
	('BEBIDA', 'Cerveza artesanal',  4500.00, TRUE, NULL, NULL, 473,  TRUE);

-- =====================================================================
-- PEDIDOS HISTORICOS
-- Los precios_unitario son los precios FINALES (con recargo ya incluido)
-- =====================================================================

-- Pedido 1: Mesa 3, Martin, hace 3 dias, CERRADO
-- 3x Cafe con leche(13)=2400  2x Medialuna(1)=2200  1x Tostado(3)=3850
-- Subtotal = 7200+4400+3850 = 15450
INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, estado, subtotal, descuento, descuento_desc, total) VALUES
	(3, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 55 MINUTE, 'CERRADO', 15450.00, 0.00, 'Sin descuento', 15450.00);
SET @p1 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) VALUES
	(@p1, 13, 3, 2400.00),
	(@p1,  1, 2, 2200.00),
	(@p1,  3, 1, 3850.00);

-- Pedido 2: Mesa 6, Lucia, hace 2 dias, CERRADO, desc 10%
-- 1x Cappuccino(14)=2800  1x Tostada palta(9)=5720  2x Muffin(6)=2400
-- Subtotal = 2800+5720+4800 = 13320, desc 10% = 1332, total = 11988
INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, estado, subtotal, descuento, descuento_desc, total) VALUES
	(6, 2, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 40 MINUTE, 'CERRADO', 13320.00, 1332.00, 'Descuento 10%', 11988.00);
SET @p2 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) VALUES
	(@p2, 14, 1, 2800.00),
	(@p2,  9, 1, 5720.00),
	(@p2,  6, 2, 2400.00);

-- Pedido 3: Mesa 1, Ana, hace 2 dias, CERRADO
-- 2x Latte(15)=3200  1x Brownie(7)=4950
-- Subtotal = 6400+4950 = 11350
INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, estado, subtotal, descuento, descuento_desc, total) VALUES
	(1, 4, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 35 MINUTE, 'CERRADO', 11350.00, 0.00, 'Sin descuento', 11350.00);
SET @p3 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) VALUES
	(@p3, 15, 2, 3200.00),
	(@p3,  7, 1, 4950.00);

-- Pedido 4: Mesa 9, Diego, ayer, CERRADO, cupon $2000
-- 4x Cafe espresso(11)=1800  2x Croissant(2)=4180  1x Cheesecake(8)=5500
-- Subtotal = 7200+8360+5500 = 21060, desc = 2000, total = 19060
INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, estado, subtotal, descuento, descuento_desc, total) VALUES
	(9, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 50 MINUTE, 'CERRADO', 21060.00, 2000.00, 'Cupon de $2000.00', 19060.00);
SET @p4 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) VALUES
	(@p4, 11, 4, 1800.00),
	(@p4,  2, 2, 4180.00),
	(@p4,  8, 1, 5500.00);

-- Pedido 5: Mesa 2, Carlos, ayer, CERRADO
-- 2x Cafe con leche(13)=2400  1x Sandwich(4)=3300  1x Empanada(5)=1600
-- Subtotal = 4800+3300+1600 = 9700
INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, estado, subtotal, descuento, descuento_desc, total) VALUES
	(2, 5, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 30 MINUTE, 'CERRADO', 9700.00, 0.00, 'Sin descuento', 9700.00);
SET @p5 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) VALUES
	(@p5, 13, 2, 2400.00),
	(@p5,  4, 1, 3300.00),
	(@p5,  5, 1, 1600.00);

-- Pedido 6: Mesa 5, Lucia, ayer, CERRADO, desc 20%
-- 3x Latte(15)=3200  3x Tostado(3)=3850  2x Muffin(6)=2400
-- Subtotal = 9600+11550+4800 = 25950, desc 20% = 5190, total = 20760
INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, estado, subtotal, descuento, descuento_desc, total) VALUES
	(5, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 60 MINUTE, 'CERRADO', 25950.00, 5190.00, 'Descuento 20%', 20760.00);
SET @p6 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) VALUES
	(@p6, 15, 3, 3200.00),
	(@p6,  3, 3, 3850.00),
	(@p6,  6, 2, 2400.00);

-- Pedido 7: Mesa 11, Diego, hoy manana, CERRADO
-- 5x Cafe espresso(11)=1800  4x Medialuna(1)=2200  1x Focaccia(10)=3200
-- Subtotal = 9000+8800+3200 = 21000
INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, estado, subtotal, descuento, descuento_desc, total) VALUES
	(11, 3, DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 4 HOUR), 'CERRADO', 21000.00, 0.00, 'Sin descuento', 21000.00);
SET @p7 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) VALUES
	(@p7, 11, 5, 1800.00),
	(@p7,  1, 4, 2200.00),
	(@p7, 10, 1, 3200.00);

-- Pedido 8: Mesa 4, Martin, hoy, CERRADO
-- 2x Cappuccino(14)=2800  1x Brownie(7)=4950  1x Submarino(16)=2600
-- Subtotal = 5600+4950+2600 = 13150
INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, estado, subtotal, descuento, descuento_desc, total) VALUES
	(4, 1, DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), 'CERRADO', 13150.00, 0.00, 'Sin descuento', 13150.00);
SET @p8 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) VALUES
	(@p8, 14, 2, 2800.00),
	(@p8,  7, 1, 4950.00),
	(@p8, 16, 1, 2600.00);

-- Pedido 9: Mesa 8, Ana, hoy, CERRADO
-- 1x Te surtido(17)=2000  2x Empanada(5)=1600  1x Jugo naranja(19)=3200
-- Subtotal = 2000+3200+3200 = 8400
INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, estado, subtotal, descuento, descuento_desc, total) VALUES
	(8, 4, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 90 MINUTE), 'CERRADO', 8400.00, 0.00, 'Sin descuento', 8400.00);
SET @p9 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) VALUES
	(@p9, 17, 1, 2000.00),
	(@p9,  5, 2, 1600.00),
	(@p9, 19, 1, 3200.00);

-- Pedido 10: Mesa 12, Lucia, hoy, CERRADO
-- 4x Cafe espresso(11)=1800  2x Croissant(2)=4180  1x Cerveza(20)=5175
-- Subtotal = 7200+8360+5175 = 20735
INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, estado, subtotal, descuento, descuento_desc, total) VALUES
	(12, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 'CERRADO', 20735.00, 0.00, 'Sin descuento', 20735.00);
SET @p10 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) VALUES
	(@p10, 11, 4, 1800.00),
	(@p10,  2, 2, 4180.00),
	(@p10, 20, 1, 5175.00);

-- Pedido 11: Mesa 10, Carlos, hoy, ANULADO
-- Sirve para comprobar que los reportes NO cuentan cuentas anuladas.
INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, estado, subtotal, descuento, descuento_desc, total) VALUES
	(10, 5, DATE_SUB(NOW(), INTERVAL 90 MINUTE), DATE_SUB(NOW(), INTERVAL 88 MINUTE), 'ANULADO', 2400.00, 0.00, 'Sin descuento', 2400.00);
SET @p11 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) VALUES
	(@p11, 13, 1, 2400.00);

-- Pedido 12: Mesa 7, Martin, ABIERTO en este momento
-- Al arrancar el sistema la mesa 7 aparece en rojo.
-- 1x Latte(15)=3200  2x Medialuna(1)=2200
-- Subtotal = 3200+4400 = 7600
INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, estado, subtotal, descuento, descuento_desc, total) VALUES
	(7, 1, DATE_SUB(NOW(), INTERVAL 30 MINUTE), NULL, 'ABIERTO', 7600.00, 0.00, 'Sin descuento', 7600.00);
SET @p12 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) VALUES
	(@p12, 15, 1, 3200.00),
	(@p12,  1, 2, 2200.00);

UPDATE mesa SET ocupada = TRUE WHERE numero = 7;

-- =====================================================================
-- VERIFICACION RAPIDA
-- =====================================================================
SELECT 'Empleados cargados'    AS control, COUNT(*) AS cantidad FROM empleado
UNION ALL SELECT 'Mesas cargadas',          COUNT(*) FROM mesa
UNION ALL SELECT 'Productos en la carta',   COUNT(*) FROM item_menu
UNION ALL SELECT 'Pedidos historicos',      COUNT(*) FROM pedido
UNION ALL SELECT 'Lineas de detalle',       COUNT(*) FROM detalle_pedido;
