-- Base de datos Fidecompro para MySQL (ejecutar una vez en MySQL Workbench).
CREATE DATABASE IF NOT EXISTS fidecompro;
USE fidecompro;
-- Luego, en la ventana del servidor, elegir "MySQL · fidecompro" (usuario y clave en ConexionBD).

CREATE TABLE usuarios (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tipo_identificacion VARCHAR(15) NOT NULL,
    identificacion VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    correo VARCHAR(100),
    nombre_usuario VARCHAR(30) NOT NULL UNIQUE,
    contrasena VARCHAR(64) NOT NULL,
    rol VARCHAR(15) NOT NULL,
    activo BOOLEAN NOT NULL
);

CREATE TABLE clientes (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tipo_identificacion VARCHAR(15) NOT NULL,
    identificacion VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    correo VARCHAR(100),
    direccion VARCHAR(200),
    fecha_registro DATE NOT NULL,
    activo BOOLEAN NOT NULL
);

CREATE TABLE categorias (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(200)
);

CREATE TABLE productos (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    tipo VARCHAR(10) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(200),
    valor_compra DECIMAL(12,2) NOT NULL,
    valor_venta DECIMAL(12,2) NOT NULL,
    existencias INT NOT NULL,
    stock_minimo INT NOT NULL,
    id_categoria INT NOT NULL REFERENCES categorias(id),
    fecha_vencimiento DATE,
    peso_kg DECIMAL(8,2),
    canasta_basica BOOLEAN,
    volumen_ml INT,
    unidades_paquete INT,
    retornable BOOLEAN,
    marca VARCHAR(50),
    presentacion VARCHAR(50),
    advertencias VARCHAR(300),
    activo BOOLEAN NOT NULL
);

CREATE TABLE facturas (
    numero INT NOT NULL PRIMARY KEY,
    fecha DATETIME NOT NULL,
    id_cliente INT NOT NULL REFERENCES clientes(id),
    id_usuario INT NOT NULL REFERENCES usuarios(id),
    porcentaje_descuento DECIMAL(5,2) NOT NULL,
    metodo_pago VARCHAR(15) NOT NULL,
    estado VARCHAR(10) NOT NULL,
    total DECIMAL(14,2) NOT NULL
);

CREATE TABLE detalle_factura (
    numero_factura INT NOT NULL REFERENCES facturas(numero),
    linea INT NOT NULL,
    id_producto INT NOT NULL REFERENCES productos(id),
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    porcentaje_iva DECIMAL(5,4) NOT NULL,
    PRIMARY KEY (numero_factura, linea)
);
