-- =====================================================================
-- EXAMEN FINAL - PROGRAMACION AVANZADA
-- Sistema de Gestion de Restaurante
-- Script de creacion de la base de datos y datos de prueba
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
-- Mozos y encargados. La baja es logica (campo activo) para no perder
-- la trazabilidad de quien atendio cada pedido historico.
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
-- El numero de mesa es la clave primaria: es un identificador natural
-- que el restaurante ya usa, asi que no hace falta inventar otro.
-- El campo 'ocupada' es una comodidad para pintar el salon; la verdad
-- sobre el estado real esta en la tabla pedido.
-- ---------------------------------------------------------------------
CREATE TABLE mesa (
	numero     INT PRIMARY KEY,
	capacidad  INT NOT NULL,
	sector     VARCHAR(30) NOT NULL,
	ocupada    BOOLEAN NOT NULL DEFAULT FALSE
);

-- ---------------------------------------------------------------------
-- ITEM_MENU
-- Aca se resuelve la HERENCIA de Java en la base de datos, con la
-- estrategia de TABLA UNICA CON DISCRIMINADOR:
--
--   tipo = 'PLATO'   usa minutos_preparacion y es_entrada
--   tipo = 'BEBIDA'  usa mililitros y alcoholica
--
-- Las columnas que no corresponden al tipo quedan en NULL. Con dos
-- subclases es la opcion mas simple y no requiere ningun JOIN para
-- leer la carta completa.
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
-- Cabecera de la cuenta de una mesa. Implementa el ciclo de apertura y
-- cierre a traves del campo 'estado'.
--
-- Se guardan subtotal, descuento y total ya calculados porque son
-- valores historicos: si manana cambia el precio de un producto o la
-- politica de descuentos, los tickets ya emitidos no deben cambiar.
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

-- Indice para acelerar los reportes, que siempre filtran por
-- estado + fecha de cierre.
CREATE INDEX idx_pedido_cierre ON pedido (estado, fecha_cierre);

-- ---------------------------------------------------------------------
-- DETALLE_PEDIDO
-- Lineas de la comanda. precio_unitario se guarda congelado: es el
-- precio al que efectivamente se vendio el producto ese dia.
--
-- ON DELETE CASCADE: si se borra un pedido, sus lineas se van con el.
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

-- --- Empleados (clave de todos: 1234) -------------------------------
INSERT INTO empleado (nombre, usuario, clave, activo) VALUES
	('Martin Rodriguez', 'mrodriguez', '1234', TRUE),
	('Lucia Gomez',      'lgomez',     '1234', TRUE),
	('Diego Fernandez',  'dfernandez', '1234', TRUE),
	('Sofia Ruiz',       'sruiz',      '1234', FALSE);

-- --- Mesas ----------------------------------------------------------
INSERT INTO mesa (numero, capacidad, sector, ocupada) VALUES
	( 1, 2, 'Salon',   FALSE),
	( 2, 2, 'Salon',   FALSE),
	( 3, 4, 'Salon',   FALSE),
	( 4, 4, 'Salon',   FALSE),
	( 5, 6, 'Salon',   FALSE),
	( 6, 4, 'Vereda',  FALSE),
	( 7, 4, 'Vereda',  FALSE),
	( 8, 2, 'Vereda',  FALSE),
	( 9, 8, 'Entrepiso', FALSE),
	(10, 6, 'Entrepiso', FALSE);

-- --- Carta: platos --------------------------------------------------
INSERT INTO item_menu (tipo, nombre, precio_base, disponible, minutos_preparacion, es_entrada, mililitros, alcoholica) VALUES
	('PLATO', 'Empanadas de carne (3u)',  4500.00, TRUE, 10, TRUE,  NULL, NULL),
	('PLATO', 'Provoleta a la parrilla',  5800.00, TRUE, 12, TRUE,  NULL, NULL),
	('PLATO', 'Rabas a la provenzal',     8200.00, TRUE, 15, TRUE,  NULL, NULL),
	('PLATO', 'Milanesa napolitana',     11500.00, TRUE, 25, FALSE, NULL, NULL),
	('PLATO', 'Bife de chorizo',         16800.00, TRUE, 22, FALSE, NULL, NULL),
	('PLATO', 'Ravioles de ricota',        9900.00, TRUE, 18, FALSE, NULL, NULL),
	('PLATO', 'Risotto de hongos',       10400.00, TRUE, 30, FALSE, NULL, NULL),
	('PLATO', 'Salmon grillado',         18500.00, TRUE, 24, FALSE, NULL, NULL),
	('PLATO', 'Flan con dulce de leche',  4200.00, TRUE, 5,  FALSE, NULL, NULL),
	('PLATO', 'Volcan de chocolate',      5600.00, TRUE, 14, FALSE, NULL, NULL),
	('PLATO', 'Cordero patagonico',      24000.00, FALSE, 60, FALSE, NULL, NULL);

-- --- Carta: bebidas -------------------------------------------------
INSERT INTO item_menu (tipo, nombre, precio_base, disponible, minutos_preparacion, es_entrada, mililitros, alcoholica) VALUES
	('BEBIDA', 'Agua mineral sin gas', 2200.00, TRUE, NULL, NULL, 500,  FALSE),
	('BEBIDA', 'Gaseosa linea Coca',   2600.00, TRUE, NULL, NULL, 500,  FALSE),
	('BEBIDA', 'Limonada casera',      3400.00, TRUE, NULL, NULL, 500,  FALSE),
	('BEBIDA', 'Cerveza artesanal IPA',5900.00, TRUE, NULL, NULL, 473,  TRUE),
	('BEBIDA', 'Vino malbec (copa)',   4800.00, TRUE, NULL, NULL, 180,  TRUE),
	('BEBIDA', 'Vino malbec (botella)',18900.00,TRUE, NULL, NULL, 750,  TRUE),
	('BEBIDA', 'Cafe expreso',         2400.00, TRUE, NULL, NULL, 60,   FALSE);

-- =====================================================================
-- PEDIDOS HISTORICOS
-- Se cargan cuentas ya cerradas de dias anteriores para que los
-- reportes tengan datos desde el primer arranque del sistema.
--
-- Los precios unitarios ya incluyen el recargo que aplica cada
-- subclase: 10% los platos principales, 15% las bebidas con alcohol.
-- =====================================================================

-- IDS DE REFERENCIA (segun el orden de los INSERT de arriba)
--  PLATOS :  1 Empanadas | 2 Provoleta | 3 Rabas | 4 Milanesa | 5 Bife
--            6 Ravioles  | 7 Risotto   | 8 Salmon | 9 Flan | 10 Volcan | 11 Cordero
--  BEBIDAS: 12 Agua | 13 Gaseosa | 14 Limonada | 15 Cerveza
--           16 Vino copa | 17 Vino botella | 18 Cafe
--
-- Los precios unitarios ya incluyen el recargo de cada subclase:
--   plato principal = base * 1.10   |   entrada = base
--   bebida con alcohol = base * 1.15 |  bebida sin alcohol = base

-- Pedido 1: mesa 3, Martin, hace dos dias
INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, estado, subtotal, descuento, descuento_desc, total) VALUES
	(3, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 75 MINUTE, 'CERRADO', 43370.00, 0.00, 'Sin descuento', 43370.00);
SET @p1 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) VALUES
	(@p1,  4, 2, 12650.00),   -- Milanesa napolitana
	(@p1,  1, 1,  4500.00),   -- Empanadas (entrada, sin recargo)
	(@p1, 15, 2,  6785.00);   -- Cerveza IPA (con alcohol)

-- Pedido 2: mesa 6, Lucia, ayer, con 10% de descuento
INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, estado, subtotal, descuento, descuento_desc, total) VALUES
	(6, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 95 MINUTE, 'CERRADO', 64495.00, 6449.50, 'Descuento 10%', 58045.50);
SET @p2 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) VALUES
	(@p2,  5, 2, 18480.00),   -- Bife de chorizo
	(@p2,  2, 1,  5800.00),   -- Provoleta (entrada)
	(@p2, 17, 1, 21735.00);   -- Vino malbec botella

-- Pedido 3: mesa 1, Lucia, ayer
INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, estado, subtotal, descuento, descuento_desc, total) VALUES
	(1, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 50 MINUTE, 'CERRADO', 18110.00, 0.00, 'Sin descuento', 18110.00);
SET @p3 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) VALUES
	(@p3,  6, 1, 10890.00),   -- Ravioles de ricota
	(@p3, 13, 1,  2600.00),   -- Gaseosa
	(@p3,  9, 1,  4620.00);   -- Flan

-- Pedido 4: mesa 9, Diego, hoy, con cupon de $2000
INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, estado, subtotal, descuento, descuento_desc, total) VALUES
	(9, 3, DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 'CERRADO', 93635.00, 2000.00, 'Cupon de $2000.00', 91635.00);
SET @p4 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) VALUES
	(@p4,  4, 3, 12650.00),   -- Milanesa napolitana
	(@p4,  8, 2, 20350.00),   -- Salmon grillado
	(@p4,  3, 1,  8200.00),   -- Rabas (entrada)
	(@p4, 15, 1,  6785.00);   -- Cerveza IPA

-- Pedido 5: mesa 2, Martin, hoy
INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, estado, subtotal, descuento, descuento_desc, total) VALUES
	(2, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 40 MINUTE), 'CERRADO', 25080.00, 0.00, 'Sin descuento', 25080.00);
SET @p5 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) VALUES
	(@p5,  7, 2, 11440.00),   -- Risotto de hongos
	(@p5, 12, 1,  2200.00);   -- Agua mineral

-- Pedido 6: mesa 4, Diego, hoy - ANULADO
-- Sirve para comprobar que los reportes NO cuentan las cuentas anuladas.
INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, estado, subtotal, descuento, descuento_desc, total) VALUES
	(4, 3, DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 4 HOUR) + INTERVAL 8 MINUTE, 'ANULADO', 12650.00, 0.00, 'Sin descuento', 12650.00);
SET @p6 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) VALUES
	(@p6,  4, 1, 12650.00);   -- Milanesa napolitana

-- Pedido 7: mesa 7, Martin, ABIERTO en este momento
-- Al arrancar el sistema la mesa 7 aparece en rojo, y al hacerle clic se
-- retoma esta cuenta en lugar de abrir una nueva.
INSERT INTO pedido (mesa_numero, empleado_id, fecha_apertura, fecha_cierre, estado, subtotal, descuento, descuento_desc, total) VALUES
	(7, 1, DATE_SUB(NOW(), INTERVAL 25 MINUTE), NULL, 'ABIERTO', 19810.00, 0.00, 'Sin descuento', 19810.00);
SET @p7 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, item_id, cantidad, precio_unitario) VALUES
	(@p7,  6, 1, 10890.00),   -- Ravioles de ricota
	(@p7, 14, 1,  3400.00),   -- Limonada casera
	(@p7, 16, 1,  5520.00);   -- Vino malbec copa

UPDATE mesa SET ocupada = TRUE WHERE numero = 7;

-- =====================================================================
-- VERIFICACION RAPIDA
-- =====================================================================
SELECT 'Empleados cargados' AS control, COUNT(*) AS cantidad FROM empleado
UNION ALL SELECT 'Mesas cargadas',       COUNT(*) FROM mesa
UNION ALL SELECT 'Productos en la carta',COUNT(*) FROM item_menu
UNION ALL SELECT 'Pedidos historicos',   COUNT(*) FROM pedido
UNION ALL SELECT 'Lineas de detalle',    COUNT(*) FROM detalle_pedido;
