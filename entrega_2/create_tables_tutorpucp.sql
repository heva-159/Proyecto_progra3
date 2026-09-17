-- ============================================
-- TutorPUCP - Script de creacion de tablas (v3, para entrega/presentacion)
-- Incluye el ajuste confirmado: "administrador" es tabla independiente,
-- ya NO esta ligada a "usuario" (coincide con Administrador.java real).
-- ============================================

CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    correo_institucional VARCHAR(150) NOT NULL UNIQUE,
    codigo_pucp INT NOT NULL UNIQUE,
    facultad VARCHAR(100),
    especialidad VARCHAR(100),
    password_hash VARCHAR(255) NOT NULL,
    fecha_registro DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA' CHECK (estado IN ('ACTIVA', 'BLOQUEADA')),
    correo_verificado BOOLEAN NOT NULL DEFAULT FALSE,
    codigo_verificacion VARCHAR(6),
    fecha_expiracion_codigo DATETIME,
    consentimiento_datos BOOLEAN NOT NULL DEFAULT FALSE,
    consentimiento_calificaciones BOOLEAN NOT NULL DEFAULT FALSE
);

-- administrador: tabla independiente, sin relacion con usuario.
CREATE TABLE administrador (
    id_administrador INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    fecha_creacion DATE NOT NULL,
    nivel_permiso VARCHAR(30)
);

CREATE TABLE tutor (
    id_usuario INT PRIMARY KEY,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

CREATE TABLE estudiante (
    id_usuario INT PRIMARY KEY,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

CREATE TABLE curso (
    id_curso INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE tema (
    id_tema INT AUTO_INCREMENT PRIMARY KEY,
    id_curso INT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    FOREIGN KEY (id_curso) REFERENCES curso(id_curso)
);

CREATE TABLE postulacion (
    id_postulacion INT AUTO_INCREMENT PRIMARY KEY,
    id_tutor INT NOT NULL,
    id_curso INT NOT NULL,
    archivo_certificado_url VARCHAR(255) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_postulacion DATE NOT NULL,
    fecha_resolucion DATE,
    resolucion VARCHAR(255),
    id_administrador_revisor INT,
    FOREIGN KEY (id_tutor) REFERENCES tutor(id_usuario),
    FOREIGN KEY (id_curso) REFERENCES curso(id_curso),
    FOREIGN KEY (id_administrador_revisor) REFERENCES administrador(id_administrador)
);

-- tarifa: tabla intermedia tutor <-> curso.
CREATE TABLE tarifa (
    id_tarifa INT AUTO_INCREMENT PRIMARY KEY,
    id_tutor INT NOT NULL,
    id_curso INT NOT NULL,
    monto_por_hora DECIMAL(6,2) NOT NULL,
    FOREIGN KEY (id_tutor) REFERENCES tutor(id_usuario),
    FOREIGN KEY (id_curso) REFERENCES curso(id_curso),
    UNIQUE (id_tutor, id_curso)
);

CREATE TABLE mensaje (
    id_mensaje INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario_emisor INT NOT NULL,
    id_usuario_receptor INT NOT NULL,
    contenido TEXT NOT NULL,
    fecha_envio DATETIME NOT NULL,
    leido BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (id_usuario_emisor) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_usuario_receptor) REFERENCES usuario(id_usuario)
);

-- sesion: conectada directo a tutor, estudiante y curso.
-- La calificacion (puntaje/comentario) va embebida aqui mismo.
CREATE TABLE sesion (
    id_sesion INT AUTO_INCREMENT PRIMARY KEY,
    id_tutor INT NOT NULL,
    id_estudiante INT NOT NULL,
    id_curso INT NOT NULL,
    fecha_hora_inicio DATETIME NOT NULL,
    fecha_hora_fin DATETIME,
    estado VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADA',
    check_in_tutor BOOLEAN NOT NULL DEFAULT FALSE,
    check_in_estudiante BOOLEAN NOT NULL DEFAULT FALSE,
    check_out_tutor BOOLEAN NOT NULL DEFAULT FALSE,
    check_out_estudiante BOOLEAN NOT NULL DEFAULT FALSE,
    puntaje INT CHECK (puntaje BETWEEN 1 AND 5),
    comentario TEXT,
    motivo_cancelacion VARCHAR(255),
    id_usuario_cancelo INT,
    FOREIGN KEY (id_tutor) REFERENCES tutor(id_usuario),
    FOREIGN KEY (id_estudiante) REFERENCES estudiante(id_usuario),
    FOREIGN KEY (id_curso) REFERENCES curso(id_curso),
    FOREIGN KEY (id_usuario_cancelo) REFERENCES usuario(id_usuario)
);

-- sesion_tema: tabla intermedia porque una sesion puede tratar varios temas.
CREATE TABLE sesion_tema (
    id_sesion INT NOT NULL,
    id_tema INT NOT NULL,
    PRIMARY KEY (id_sesion, id_tema),
    FOREIGN KEY (id_sesion) REFERENCES sesion(id_sesion),
    FOREIGN KEY (id_tema) REFERENCES tema(id_tema)
);

-- reclamo: reclamante = usuario + rol_reclamante.
CREATE TABLE reclamo (
    id_reclamo INT AUTO_INCREMENT PRIMARY KEY,
    id_sesion INT NOT NULL,
    id_usuario_reclamante INT NOT NULL,
    rol_reclamante VARCHAR(12) NOT NULL CHECK (rol_reclamante IN ('TUTOR', 'ESTUDIANTE')),
    motivo VARCHAR(255) NOT NULL,
    fecha_reclamo DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    resolucion VARCHAR(255),
    id_administrador_resuelve INT,
    fecha_resolucion DATETIME,
    FOREIGN KEY (id_sesion) REFERENCES sesion(id_sesion),
    FOREIGN KEY (id_usuario_reclamante) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_administrador_resuelve) REFERENCES administrador(id_administrador)
);
