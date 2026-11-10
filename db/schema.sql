-- Esquema que usa la API. Ajusta los nombres si tus tablas ya existen con otros.
CREATE DATABASE IF NOT EXISTS healthblood CHARACTER SET utf8mb4;
USE healthblood;

CREATE TABLE IF NOT EXISTS users (
    id_usuario       INT AUTO_INCREMENT PRIMARY KEY,
    nombre           VARCHAR(80)  NOT NULL,
    primer_apellido  VARCHAR(80),
    segundo_apellido VARCHAR(80),
    email            VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS medidas (
    id_medida     INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario    INT NOT NULL,
    glucosa_mg_dl DECIMAL(6,2) NOT NULL,
    fecha_hora    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    contexto      VARCHAR(40),
    FOREIGN KEY (id_usuario) REFERENCES users(id_usuario),
    INDEX idx_usuario_fecha (id_usuario, fecha_hora)
);

-- Datos de prueba (datos ficticios)
INSERT INTO users (nombre, primer_apellido, segundo_apellido, email)
VALUES ('Ana', 'Lopez', 'Perez', 'ana@example.com');
INSERT INTO medidas (id_usuario, glucosa_mg_dl, fecha_hora, contexto)
VALUES (1, 98, '2026-11-07 08:00:00', 'ayuno'), (1, 132, '2026-11-07 14:30:00', 'postprandial');
