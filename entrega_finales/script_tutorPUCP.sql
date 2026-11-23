create database if not exists db2026lab1;                                                                                                                    
use db2026lab1;   

drop table if exists reclamo;
drop table if exists sesion_tema;
drop table if exists sesion;
drop table if exists mensaje;
drop table if exists tarifa;
drop table if exists postulacion;
drop table if exists tema;
drop table if exists curso;
drop table if exists estudiante;
drop table if exists tutor;
drop table if exists administrador;
drop table if exists usuario;

create table usuario (
    id int auto_increment primary key,
    nombres varchar(100) not null,
    apellidos varchar(100) not null,
    correo_institucional varchar(150) not null unique,
    codigo_pucp char(8) not null unique,
    facultad varchar(100),
    especialidad varchar(100),
    password_hash varchar(255) not null,
    fecha_registro date not null default (curdate()),
    estado varchar(20) not null default 'ACTIVA',
    correo_verificado boolean not null default false,
    codigo_verificacion char(6),
    fecha_expiracion_codigo datetime,
    consentimiento_datos boolean not null default false,
    consentimiento_calificaciones boolean not null default false,
    constraint ck_usuario_correo check (correo_institucional like '%_@pucp.edu.pe'),
    constraint ck_usuario_codigo check (codigo_pucp regexp '^[0-9]{8}$'),
    constraint ck_usuario_estado check (estado in ('ACTIVA', 'BLOQUEADA')),
    constraint ck_usuario_codigo_verif check (codigo_verificacion regexp '^[0-9]{6}$'),
    constraint ck_usuario_verif_completa check ((codigo_verificacion is null) = (fecha_expiracion_codigo is null))
);

create table administrador (
    id int auto_increment primary key,
    nombre varchar(150) not null,
    correo varchar(150) not null unique,
    password_hash varchar(255) not null,
    fecha_creacion date not null default (curdate()),
    nivel_permiso varchar(30)
);

-- tutor y estudiante son perfiles de un usuario: su clave es la del usuario.
create table tutor (
    id_usuario int primary key,
    constraint fk_tutor_usuario foreign key (id_usuario) references usuario(id) on delete cascade
);

create table estudiante (
    id_usuario int primary key,
    constraint fk_estudiante_usuario foreign key (id_usuario) references usuario(id) on delete cascade
);

create table curso (
    id int auto_increment primary key,
    codigo varchar(20) not null unique,
    nombre varchar(150) not null,
    activo boolean not null default true
);

create table tema (
    id int auto_increment primary key,
    id_curso int not null,
    nombre varchar(150) not null,
    constraint fk_tema_curso foreign key (id_curso) references curso(id),
    constraint uq_tema_curso_nombre unique (id_curso, nombre)
);

create table postulacion (
    id int auto_increment primary key,
    id_tutor int not null,
    id_curso int not null,
    archivo_certificado_url varchar(255) not null,
    estado varchar(20) not null default 'PENDIENTE',
    fecha_postulacion date not null default (curdate()),
    fecha_resolucion date,
    resolucion varchar(255),
    id_administrador_revisor int,
    constraint fk_postulacion_tutor foreign key (id_tutor) references tutor(id_usuario),
    constraint fk_postulacion_curso foreign key (id_curso) references curso(id),
    constraint fk_postulacion_administrador foreign key (id_administrador_revisor) references administrador(id),
    constraint ck_postulacion_estado check (estado in ('PENDIENTE', 'APROBADA', 'RECHAZADA')),
    constraint ck_postulacion_pdf check (archivo_certificado_url like '%.pdf'),
    -- pendiente: sin revision; aprobada o rechazada: con fecha y administrador
    constraint ck_postulacion_revision check (
        (estado = 'PENDIENTE' and fecha_resolucion is null and id_administrador_revisor is null)
        or (estado <> 'PENDIENTE' and fecha_resolucion is not null and id_administrador_revisor is not null)),
    constraint ck_postulacion_motivo check (estado <> 'RECHAZADA' or resolucion is not null),
    constraint ck_postulacion_fechas check (fecha_resolucion >= fecha_postulacion)
);

create table tarifa (
    id int auto_increment primary key,
    id_tutor int not null,
    id_curso int not null,
    monto_por_hora decimal(6,2) not null,
    constraint fk_tarifa_tutor foreign key (id_tutor) references tutor(id_usuario),
    constraint fk_tarifa_curso foreign key (id_curso) references curso(id),
    constraint uq_tarifa_tutor_curso unique (id_tutor, id_curso),
    constraint ck_tarifa_monto check (monto_por_hora > 0)
);

create table mensaje (
    id int auto_increment primary key,
    id_usuario_emisor int not null,
    id_usuario_receptor int not null,
    contenido text not null,
    fecha_envio datetime not null default current_timestamp,
    leido boolean not null default false,
    constraint fk_mensaje_emisor foreign key (id_usuario_emisor) references usuario(id),
    constraint fk_mensaje_receptor foreign key (id_usuario_receptor) references usuario(id),
    constraint ck_mensaje_distintos check (id_usuario_emisor <> id_usuario_receptor)
);

create table sesion (
    id int auto_increment primary key,
    id_tutor int not null,
    id_estudiante int not null,
    id_curso int not null,
    fecha_hora_inicio datetime not null,
    fecha_hora_fin datetime,
    estado varchar(25) not null default 'PROGRAMADA',
    check_in_tutor boolean not null default false,
    check_in_estudiante boolean not null default false,
    check_out_tutor boolean not null default false,
    check_out_estudiante boolean not null default false,
    puntaje int,
    comentario text,
    motivo_cancelacion varchar(255),
    id_usuario_cancelo int,
    constraint fk_sesion_tutor foreign key (id_tutor) references tutor(id_usuario),
    constraint fk_sesion_estudiante foreign key (id_estudiante) references estudiante(id_usuario),
    constraint fk_sesion_curso foreign key (id_curso) references curso(id),
    constraint fk_sesion_usuario_cancelo foreign key (id_usuario_cancelo) references usuario(id),
    constraint ck_sesion_estado check (estado in ('PROGRAMADA', 'EN_CURSO', 'REALIZADA', 'CANCELADA', 'CANCELADA_INASISTENCIA')),
    constraint ck_sesion_participantes check (id_tutor <> id_estudiante),
    constraint ck_sesion_fechas check (fecha_hora_fin > fecha_hora_inicio),
    constraint ck_sesion_puntaje check (puntaje between 1 and 5),
    -- no hay check-out sin check-in previo de la misma persona
    constraint ck_sesion_check_out check (
        (check_out_tutor = false or check_in_tutor = true)
        and (check_out_estudiante = false or check_in_estudiante = true)),
    -- en curso o realizada: ambos hicieron check-in
    constraint ck_sesion_en_curso check (
        estado not in ('EN_CURSO', 'REALIZADA') or (check_in_tutor = true and check_in_estudiante = true)),
    -- realizada: ambos hicieron check-out y hay hora de fin
    constraint ck_sesion_realizada check (
        estado <> 'REALIZADA' or (check_out_tutor = true and check_out_estudiante = true and fecha_hora_fin is not null)),
    -- cancelada: con motivo y con quien cancelo, que debe ser el tutor o el estudiante
    constraint ck_sesion_cancelacion check (
        (estado = 'CANCELADA' and motivo_cancelacion is not null and id_usuario_cancelo is not null)
        or (estado <> 'CANCELADA' and motivo_cancelacion is null and id_usuario_cancelo is null)),
    constraint ck_sesion_quien_cancela check (id_usuario_cancelo in (id_tutor, id_estudiante)),
    -- solo se califica una sesion realizada; el comentario acompana al puntaje
    constraint ck_sesion_calificacion check (puntaje is null or estado = 'REALIZADA'),
    constraint ck_sesion_comentario check (comentario is null or puntaje is not null)
);

create index idx_sesion_fecha_inicio on sesion (fecha_hora_inicio);

create table sesion_tema (
    id int auto_increment primary key,
    id_sesion int not null,
    id_tema int not null,
    constraint fk_sesion_tema_sesion foreign key (id_sesion) references sesion(id) on delete cascade,
    constraint fk_sesion_tema_tema foreign key (id_tema) references tema(id),
    constraint uq_sesion_tema unique (id_sesion, id_tema)
);

create table reclamo (
    id int auto_increment primary key,
    id_sesion int not null,
    id_usuario_reclamante int not null,
    rol_reclamante varchar(12) not null,
    motivo varchar(255) not null,
    fecha_reclamo date not null default (curdate()),
    estado varchar(20) not null default 'PENDIENTE',
    resolucion varchar(255),
    id_administrador_resuelve int,
    fecha_resolucion date,
    constraint fk_reclamo_sesion foreign key (id_sesion) references sesion(id),
    constraint fk_reclamo_usuario foreign key (id_usuario_reclamante) references usuario(id),
    constraint fk_reclamo_administrador foreign key (id_administrador_resuelve) references administrador(id),
    constraint ck_reclamo_rol check (rol_reclamante in ('TUTOR', 'ESTUDIANTE')),
    constraint ck_reclamo_estado check (estado in ('PENDIENTE', 'APROBADA', 'RECHAZADA')),
    -- pendiente: sin resolucion; resuelto: con resolucion, administrador y fecha
    constraint ck_reclamo_resolucion check (
        (estado = 'PENDIENTE' and resolucion is null and id_administrador_resuelve is null and fecha_resolucion is null)
        or (estado <> 'PENDIENTE' and resolucion is not null and id_administrador_resuelve is not null and fecha_resolucion is not null)),
    constraint ck_reclamo_fechas check (fecha_resolucion >= fecha_reclamo)
);

create index idx_reclamo_fecha on reclamo (fecha_reclamo);

-- ============================================================
-- procedimientos almacenados
-- ============================================================

drop procedure if exists insertar_usuario;
drop procedure if exists modificar_usuario;
drop procedure if exists eliminar_usuario;
drop procedure if exists buscar_usuario_por_id;
drop procedure if exists listar_usuarios;
drop procedure if exists buscar_usuario_por_correo;
drop procedure if exists buscar_usuario_por_codigo;
drop procedure if exists insertar_tutor;
drop procedure if exists eliminar_tutor;
drop procedure if exists insertar_estudiante;
drop procedure if exists eliminar_estudiante;
drop procedure if exists insertar_administrador;
drop procedure if exists modificar_administrador;
drop procedure if exists eliminar_administrador;
drop procedure if exists buscar_administrador_por_id;
drop procedure if exists listar_administradores;
drop procedure if exists buscar_administrador_por_correo;
drop procedure if exists insertar_curso;
drop procedure if exists modificar_curso;
drop procedure if exists eliminar_curso;
drop procedure if exists buscar_curso_por_id;
drop procedure if exists listar_cursos;
drop procedure if exists buscar_curso_por_codigo;
drop procedure if exists insertar_tema;
drop procedure if exists modificar_tema;
drop procedure if exists eliminar_tema;
drop procedure if exists buscar_tema_por_id;
drop procedure if exists listar_temas;
drop procedure if exists listar_temas_por_curso;
drop procedure if exists insertar_postulacion;
drop procedure if exists modificar_postulacion;
drop procedure if exists eliminar_postulacion;
drop procedure if exists buscar_postulacion_por_id;
drop procedure if exists listar_postulaciones;
drop procedure if exists listar_postulaciones_por_tutor;
drop procedure if exists insertar_tarifa;
drop procedure if exists modificar_tarifa;
drop procedure if exists eliminar_tarifa;
drop procedure if exists buscar_tarifa_por_id;
drop procedure if exists listar_tarifas;
drop procedure if exists listar_tarifas_por_tutor;
drop procedure if exists insertar_mensaje;
drop procedure if exists modificar_mensaje;
drop procedure if exists eliminar_mensaje;
drop procedure if exists buscar_mensaje_por_id;
drop procedure if exists listar_mensajes;
drop procedure if exists listar_mensajes_entre_usuarios;
drop procedure if exists insertar_sesion;
drop procedure if exists modificar_sesion;
drop procedure if exists eliminar_sesion;
drop procedure if exists buscar_sesion_por_id;
drop procedure if exists listar_sesiones;
drop procedure if exists insertar_sesion_tema;
drop procedure if exists eliminar_temas_por_sesion;
drop procedure if exists listar_temas_por_sesion;
drop procedure if exists insertar_reclamo;
drop procedure if exists modificar_reclamo;
drop procedure if exists eliminar_reclamo;
drop procedure if exists buscar_reclamo_por_id;
drop procedure if exists listar_reclamos;
drop procedure if exists listar_reclamos_pendientes;
drop procedure if exists listar_reclamos_por_sesion;

delimiter //

-- ------------------------------------------------------------
-- usuario
-- ------------------------------------------------------------

create procedure insertar_usuario(
    in p_nombres varchar(100),
    in p_apellidos varchar(100),
    in p_correo_institucional varchar(150),
    in p_codigo_pucp char(8),
    in p_facultad varchar(100),
    in p_especialidad varchar(100),
    in p_password_hash varchar(255),
    in p_codigo_verificacion char(6),
    in p_fecha_expiracion_codigo datetime,
    in p_consentimiento_datos boolean,
    in p_consentimiento_calificaciones boolean,
    out p_id int
)
begin
    insert into usuario(nombres, apellidos, correo_institucional, codigo_pucp, facultad, especialidad,
                        password_hash, codigo_verificacion, fecha_expiracion_codigo,
                        consentimiento_datos, consentimiento_calificaciones)
    values(p_nombres, p_apellidos, p_correo_institucional, p_codigo_pucp, p_facultad, p_especialidad,
           p_password_hash, p_codigo_verificacion, p_fecha_expiracion_codigo,
           p_consentimiento_datos, p_consentimiento_calificaciones);

    set p_id = last_insert_id();
end //

create procedure modificar_usuario(
    in p_id int,
    in p_nombres varchar(100),
    in p_apellidos varchar(100),
    in p_correo_institucional varchar(150),
    in p_codigo_pucp char(8),
    in p_facultad varchar(100),
    in p_especialidad varchar(100),
    in p_password_hash varchar(255),
    in p_estado varchar(20),
    in p_correo_verificado boolean,
    in p_codigo_verificacion char(6),
    in p_fecha_expiracion_codigo datetime,
    in p_consentimiento_datos boolean,
    in p_consentimiento_calificaciones boolean
)
begin
    update usuario
    set nombres = p_nombres,
        apellidos = p_apellidos,
        correo_institucional = p_correo_institucional,
        codigo_pucp = p_codigo_pucp,
        facultad = p_facultad,
        especialidad = p_especialidad,
        password_hash = p_password_hash,
        estado = p_estado,
        correo_verificado = p_correo_verificado,
        codigo_verificacion = p_codigo_verificacion,
        fecha_expiracion_codigo = p_fecha_expiracion_codigo,
        consentimiento_datos = p_consentimiento_datos,
        consentimiento_calificaciones = p_consentimiento_calificaciones
    where id = p_id;
end //

-- eliminacion logica: el usuario queda bloqueado y se conserva su historial
create procedure eliminar_usuario(in p_id int)
begin
    update usuario
    set estado = 'BLOQUEADA'
    where id = p_id;
end //

-- es_tutor y es_estudiante le indican al DAO que perfiles tiene el usuario
create procedure buscar_usuario_por_id(in p_id int)
begin
    select u.id, u.nombres, u.apellidos, u.correo_institucional, u.codigo_pucp, u.facultad, u.especialidad,
           u.password_hash, u.fecha_registro, u.estado, u.correo_verificado, u.codigo_verificacion,
           u.fecha_expiracion_codigo, u.consentimiento_datos, u.consentimiento_calificaciones,
           exists(select 1 from tutor t where t.id_usuario = u.id) as es_tutor,
           exists(select 1 from estudiante e where e.id_usuario = u.id) as es_estudiante
    from usuario u
    where u.id = p_id;
end //

create procedure listar_usuarios()
begin
    select u.id, u.nombres, u.apellidos, u.correo_institucional, u.codigo_pucp, u.facultad, u.especialidad,
           u.password_hash, u.fecha_registro, u.estado, u.correo_verificado, u.codigo_verificacion,
           u.fecha_expiracion_codigo, u.consentimiento_datos, u.consentimiento_calificaciones,
           exists(select 1 from tutor t where t.id_usuario = u.id) as es_tutor,
           exists(select 1 from estudiante e where e.id_usuario = u.id) as es_estudiante
    from usuario u
    order by u.id;
end //

create procedure buscar_usuario_por_correo(in p_correo_institucional varchar(150))
begin
    select u.id, u.nombres, u.apellidos, u.correo_institucional, u.codigo_pucp, u.facultad, u.especialidad,
           u.password_hash, u.fecha_registro, u.estado, u.correo_verificado, u.codigo_verificacion,
           u.fecha_expiracion_codigo, u.consentimiento_datos, u.consentimiento_calificaciones,
           exists(select 1 from tutor t where t.id_usuario = u.id) as es_tutor,
           exists(select 1 from estudiante e where e.id_usuario = u.id) as es_estudiante
    from usuario u
    where u.correo_institucional = p_correo_institucional;
end //

create procedure buscar_usuario_por_codigo(in p_codigo_pucp char(8))
begin
    select u.id, u.nombres, u.apellidos, u.correo_institucional, u.codigo_pucp, u.facultad, u.especialidad,
           u.password_hash, u.fecha_registro, u.estado, u.correo_verificado, u.codigo_verificacion,
           u.fecha_expiracion_codigo, u.consentimiento_datos, u.consentimiento_calificaciones,
           exists(select 1 from tutor t where t.id_usuario = u.id) as es_tutor,
           exists(select 1 from estudiante e where e.id_usuario = u.id) as es_estudiante
    from usuario u
    where u.codigo_pucp = p_codigo_pucp;
end //

-- perfiles de tutor y estudiante: su clave es el id del usuario

create procedure insertar_tutor(in p_id_usuario int)
begin
    insert into tutor(id_usuario)
    values(p_id_usuario);
end //

create procedure eliminar_tutor(in p_id_usuario int)
begin
    delete from tutor
    where id_usuario = p_id_usuario;
end //

create procedure insertar_estudiante(in p_id_usuario int)
begin
    insert into estudiante(id_usuario)
    values(p_id_usuario);
end //

create procedure eliminar_estudiante(in p_id_usuario int)
begin
    delete from estudiante
    where id_usuario = p_id_usuario;
end //

-- ------------------------------------------------------------
-- administrador
-- ------------------------------------------------------------

create procedure insertar_administrador(
    in p_nombre varchar(150),
    in p_correo varchar(150),
    in p_password_hash varchar(255),
    in p_nivel_permiso varchar(30),
    out p_id int
)
begin
    insert into administrador(nombre, correo, password_hash, nivel_permiso)
    values(p_nombre, p_correo, p_password_hash, p_nivel_permiso);

    set p_id = last_insert_id();
end //

create procedure modificar_administrador(
    in p_id int,
    in p_nombre varchar(150),
    in p_correo varchar(150),
    in p_password_hash varchar(255),
    in p_nivel_permiso varchar(30)
)
begin
    update administrador
    set nombre = p_nombre,
        correo = p_correo,
        password_hash = p_password_hash,
        nivel_permiso = p_nivel_permiso
    where id = p_id;
end //

create procedure eliminar_administrador(in p_id int)
begin
    delete from administrador
    where id = p_id;
end //

create procedure buscar_administrador_por_id(in p_id int)
begin
    select id, nombre, correo, password_hash, fecha_creacion, nivel_permiso
    from administrador
    where id = p_id;
end //

create procedure listar_administradores()
begin
    select id, nombre, correo, password_hash, fecha_creacion, nivel_permiso
    from administrador
    order by id;
end //

create procedure buscar_administrador_por_correo(in p_correo varchar(150))
begin
    select id, nombre, correo, password_hash, fecha_creacion, nivel_permiso
    from administrador
    where correo = p_correo;
end //

-- ------------------------------------------------------------
-- curso
-- ------------------------------------------------------------

create procedure insertar_curso(
    in p_codigo varchar(20),
    in p_nombre varchar(150),
    out p_id int
)
begin
    insert into curso(codigo, nombre)
    values(p_codigo, p_nombre);

    set p_id = last_insert_id();
end //

create procedure modificar_curso(
    in p_id int,
    in p_codigo varchar(20),
    in p_nombre varchar(150),
    in p_activo boolean
)
begin
    update curso
    set codigo = p_codigo,
        nombre = p_nombre,
        activo = p_activo
    where id = p_id;
end //

-- eliminacion logica: el curso queda inactivo y se conserva su historial
create procedure eliminar_curso(in p_id int)
begin
    update curso
    set activo = false
    where id = p_id;
end //

create procedure buscar_curso_por_id(in p_id int)
begin
    select id, codigo, nombre, activo
    from curso
    where id = p_id;
end //

create procedure listar_cursos()
begin
    select id, codigo, nombre, activo
    from curso
    order by id;
end //

create procedure buscar_curso_por_codigo(in p_codigo varchar(20))
begin
    select id, codigo, nombre, activo
    from curso
    where codigo = p_codigo;
end //

-- ------------------------------------------------------------
-- tema
-- ------------------------------------------------------------

create procedure insertar_tema(
    in p_id_curso int,
    in p_nombre varchar(150),
    out p_id int
)
begin
    insert into tema(id_curso, nombre)
    values(p_id_curso, p_nombre);

    set p_id = last_insert_id();
end //

create procedure modificar_tema(
    in p_id int,
    in p_id_curso int,
    in p_nombre varchar(150)
)
begin
    update tema
    set id_curso = p_id_curso,
        nombre = p_nombre
    where id = p_id;
end //

create procedure eliminar_tema(in p_id int)
begin
    delete from tema
    where id = p_id;
end //

create procedure buscar_tema_por_id(in p_id int)
begin
    select id, id_curso, nombre
    from tema
    where id = p_id;
end //

create procedure listar_temas()
begin
    select id, id_curso, nombre
    from tema
    order by id;
end //

create procedure listar_temas_por_curso(in p_id_curso int)
begin
    select id, id_curso, nombre
    from tema
    where id_curso = p_id_curso
    order by id;
end //

-- ------------------------------------------------------------
-- postulacion
-- ------------------------------------------------------------

create procedure insertar_postulacion(
    in p_id_tutor int,
    in p_id_curso int,
    in p_archivo_certificado_url varchar(255),
    out p_id int
)
begin
    insert into postulacion(id_tutor, id_curso, archivo_certificado_url)
    values(p_id_tutor, p_id_curso, p_archivo_certificado_url);

    set p_id = last_insert_id();
end //

-- tambien sirve para aprobar o rechazar la postulacion
create procedure modificar_postulacion(
    in p_id int,
    in p_id_tutor int,
    in p_id_curso int,
    in p_archivo_certificado_url varchar(255),
    in p_estado varchar(20),
    in p_fecha_resolucion date,
    in p_resolucion varchar(255),
    in p_id_administrador_revisor int
)
begin
    update postulacion
    set id_tutor = p_id_tutor,
        id_curso = p_id_curso,
        archivo_certificado_url = p_archivo_certificado_url,
        estado = p_estado,
        fecha_resolucion = p_fecha_resolucion,
        resolucion = p_resolucion,
        id_administrador_revisor = p_id_administrador_revisor
    where id = p_id;
end //

create procedure eliminar_postulacion(in p_id int)
begin
    delete from postulacion
    where id = p_id;
end //

create procedure buscar_postulacion_por_id(in p_id int)
begin
    select id, id_tutor, id_curso, archivo_certificado_url, estado, fecha_postulacion,
           fecha_resolucion, resolucion, id_administrador_revisor
    from postulacion
    where id = p_id;
end //

create procedure listar_postulaciones()
begin
    select id, id_tutor, id_curso, archivo_certificado_url, estado, fecha_postulacion,
           fecha_resolucion, resolucion, id_administrador_revisor
    from postulacion
    order by id;
end //

create procedure listar_postulaciones_por_tutor(in p_id_tutor int)
begin
    select id, id_tutor, id_curso, archivo_certificado_url, estado, fecha_postulacion,
           fecha_resolucion, resolucion, id_administrador_revisor
    from postulacion
    where id_tutor = p_id_tutor
    order by id;
end //

-- ------------------------------------------------------------
-- tarifa
-- ------------------------------------------------------------

create procedure insertar_tarifa(
    in p_id_tutor int,
    in p_id_curso int,
    in p_monto_por_hora decimal(6,2),
    out p_id int
)
begin
    insert into tarifa(id_tutor, id_curso, monto_por_hora)
    values(p_id_tutor, p_id_curso, p_monto_por_hora);

    set p_id = last_insert_id();
end //

create procedure modificar_tarifa(
    in p_id int,
    in p_id_tutor int,
    in p_id_curso int,
    in p_monto_por_hora decimal(6,2)
)
begin
    update tarifa
    set id_tutor = p_id_tutor,
        id_curso = p_id_curso,
        monto_por_hora = p_monto_por_hora
    where id = p_id;
end //

create procedure eliminar_tarifa(in p_id int)
begin
    delete from tarifa
    where id = p_id;
end //

create procedure buscar_tarifa_por_id(in p_id int)
begin
    select id, id_tutor, id_curso, monto_por_hora
    from tarifa
    where id = p_id;
end //

create procedure listar_tarifas()
begin
    select id, id_tutor, id_curso, monto_por_hora
    from tarifa
    order by id;
end //

create procedure listar_tarifas_por_tutor(in p_id_tutor int)
begin
    select id, id_tutor, id_curso, monto_por_hora
    from tarifa
    where id_tutor = p_id_tutor
    order by id;
end //

-- ------------------------------------------------------------
-- mensaje
-- ------------------------------------------------------------

create procedure insertar_mensaje(
    in p_id_usuario_emisor int,
    in p_id_usuario_receptor int,
    in p_contenido text,
    out p_id int
)
begin
    insert into mensaje(id_usuario_emisor, id_usuario_receptor, contenido)
    values(p_id_usuario_emisor, p_id_usuario_receptor, p_contenido);

    set p_id = last_insert_id();
end //

-- tambien sirve para marcar el mensaje como leido
create procedure modificar_mensaje(
    in p_id int,
    in p_contenido text,
    in p_leido boolean
)
begin
    update mensaje
    set contenido = p_contenido,
        leido = p_leido
    where id = p_id;
end //

create procedure eliminar_mensaje(in p_id int)
begin
    delete from mensaje
    where id = p_id;
end //

create procedure buscar_mensaje_por_id(in p_id int)
begin
    select id, id_usuario_emisor, id_usuario_receptor, contenido, fecha_envio, leido
    from mensaje
    where id = p_id;
end //

create procedure listar_mensajes()
begin
    select id, id_usuario_emisor, id_usuario_receptor, contenido, fecha_envio, leido
    from mensaje
    order by id;
end //

-- conversacion entre dos usuarios, en orden de envio
create procedure listar_mensajes_entre_usuarios(
    in p_id_usuario_1 int,
    in p_id_usuario_2 int
)
begin
    select id, id_usuario_emisor, id_usuario_receptor, contenido, fecha_envio, leido
    from mensaje
    where (id_usuario_emisor = p_id_usuario_1 and id_usuario_receptor = p_id_usuario_2)
       or (id_usuario_emisor = p_id_usuario_2 and id_usuario_receptor = p_id_usuario_1)
    order by fecha_envio, id;
end //

-- ------------------------------------------------------------
-- sesion
-- ------------------------------------------------------------

create procedure insertar_sesion(
    in p_id_tutor int,
    in p_id_estudiante int,
    in p_id_curso int,
    in p_fecha_hora_inicio datetime,
    out p_id int
)
begin
    insert into sesion(id_tutor, id_estudiante, id_curso, fecha_hora_inicio)
    values(p_id_tutor, p_id_estudiante, p_id_curso, p_fecha_hora_inicio);

    set p_id = last_insert_id();
end //

-- tambien sirve para check-in, check-out, cancelar y calificar
create procedure modificar_sesion(
    in p_id int,
    in p_id_tutor int,
    in p_id_estudiante int,
    in p_id_curso int,
    in p_fecha_hora_inicio datetime,
    in p_fecha_hora_fin datetime,
    in p_estado varchar(25),
    in p_check_in_tutor boolean,
    in p_check_in_estudiante boolean,
    in p_check_out_tutor boolean,
    in p_check_out_estudiante boolean,
    in p_puntaje int,
    in p_comentario text,
    in p_motivo_cancelacion varchar(255),
    in p_id_usuario_cancelo int
)
begin
    update sesion
    set id_tutor = p_id_tutor,
        id_estudiante = p_id_estudiante,
        id_curso = p_id_curso,
        fecha_hora_inicio = p_fecha_hora_inicio,
        fecha_hora_fin = p_fecha_hora_fin,
        estado = p_estado,
        check_in_tutor = p_check_in_tutor,
        check_in_estudiante = p_check_in_estudiante,
        check_out_tutor = p_check_out_tutor,
        check_out_estudiante = p_check_out_estudiante,
        puntaje = p_puntaje,
        comentario = p_comentario,
        motivo_cancelacion = p_motivo_cancelacion,
        id_usuario_cancelo = p_id_usuario_cancelo
    where id = p_id;
end //

-- sus temas se borran solos por el on delete cascade de sesion_tema
create procedure eliminar_sesion(in p_id int)
begin
    delete from sesion
    where id = p_id;
end //

create procedure buscar_sesion_por_id(in p_id int)
begin
    select id, id_tutor, id_estudiante, id_curso, fecha_hora_inicio, fecha_hora_fin, estado,
           check_in_tutor, check_in_estudiante, check_out_tutor, check_out_estudiante,
           puntaje, comentario, motivo_cancelacion, id_usuario_cancelo
    from sesion
    where id = p_id;
end //

create procedure listar_sesiones()
begin
    select id, id_tutor, id_estudiante, id_curso, fecha_hora_inicio, fecha_hora_fin, estado,
           check_in_tutor, check_in_estudiante, check_out_tutor, check_out_estudiante,
           puntaje, comentario, motivo_cancelacion, id_usuario_cancelo
    from sesion
    order by id;
end //

-- temas de una sesion (tabla sesion_tema)

create procedure insertar_sesion_tema(
    in p_id_sesion int,
    in p_id_tema int,
    out p_id int
)
begin
    insert into sesion_tema(id_sesion, id_tema)
    values(p_id_sesion, p_id_tema);

    set p_id = last_insert_id();
end //

create procedure eliminar_temas_por_sesion(in p_id_sesion int)
begin
    delete from sesion_tema
    where id_sesion = p_id_sesion;
end //

create procedure listar_temas_por_sesion(in p_id_sesion int)
begin
    select t.id, t.id_curso, t.nombre
    from sesion_tema st
    inner join tema t on t.id = st.id_tema
    where st.id_sesion = p_id_sesion
    order by st.id;
end //

-- ------------------------------------------------------------
-- reclamo
-- ------------------------------------------------------------

create procedure insertar_reclamo(
    in p_id_sesion int,
    in p_id_usuario_reclamante int,
    in p_rol_reclamante varchar(12),
    in p_motivo varchar(255),
    out p_id int
)
begin
    insert into reclamo(id_sesion, id_usuario_reclamante, rol_reclamante, motivo)
    values(p_id_sesion, p_id_usuario_reclamante, p_rol_reclamante, p_motivo);

    set p_id = last_insert_id();
end //

-- tambien sirve para resolver el reclamo
create procedure modificar_reclamo(
    in p_id int,
    in p_motivo varchar(255),
    in p_estado varchar(20),
    in p_resolucion varchar(255),
    in p_id_administrador_resuelve int,
    in p_fecha_resolucion date
)
begin
    update reclamo
    set motivo = p_motivo,
        estado = p_estado,
        resolucion = p_resolucion,
        id_administrador_resuelve = p_id_administrador_resuelve,
        fecha_resolucion = p_fecha_resolucion
    where id = p_id;
end //

create procedure eliminar_reclamo(in p_id int)
begin
    delete from reclamo
    where id = p_id;
end //

create procedure buscar_reclamo_por_id(in p_id int)
begin
    select id, id_sesion, id_usuario_reclamante, rol_reclamante, motivo, fecha_reclamo,
           estado, resolucion, id_administrador_resuelve, fecha_resolucion
    from reclamo
    where id = p_id;
end //

create procedure listar_reclamos()
begin
    select id, id_sesion, id_usuario_reclamante, rol_reclamante, motivo, fecha_reclamo,
           estado, resolucion, id_administrador_resuelve, fecha_resolucion
    from reclamo
    order by id;
end //

create procedure listar_reclamos_pendientes()
begin
    select id, id_sesion, id_usuario_reclamante, rol_reclamante, motivo, fecha_reclamo,
           estado, resolucion, id_administrador_resuelve, fecha_resolucion
    from reclamo
    where estado = 'PENDIENTE'
    order by fecha_reclamo, id;
end //

create procedure listar_reclamos_por_sesion(in p_id_sesion int)
begin
    select id, id_sesion, id_usuario_reclamante, rol_reclamante, motivo, fecha_reclamo,
           estado, resolucion, id_administrador_resuelve, fecha_resolucion
    from reclamo
    where id_sesion = p_id_sesion
    order by id;
end //

delimiter ;

-- ============================================================
-- datos de prueba
-- ============================================================

-- administradores: el 1 aprueba postulaciones, el 2 revisa y resuelve reclamos
insert into administrador(id, nombre, correo, password_hash, fecha_creacion, nivel_permiso) values
(1, 'Carla Mendoza Rios', 'cmendoza@pucp.edu.pe', sha2('admin123', 256), '2026-03-01', 'TOTAL'),
(2, 'Jorge Salas Vega', 'jsalas@pucp.edu.pe', sha2('admin456', 256), '2026-03-01', 'REVISOR');

-- usuarios: 1 y 2 tutores; 3, 4, 6 y 7 estudiantes; 5 es tutor y estudiante (AMBOS)
-- 6 esta bloqueado; 7 aun no verifica su correo (tiene codigo pendiente)
insert into usuario(id, nombres, apellidos, correo_institucional, codigo_pucp, facultad, especialidad,
                    password_hash, fecha_registro, estado, correo_verificado, codigo_verificacion,
                    fecha_expiracion_codigo, consentimiento_datos, consentimiento_calificaciones) values
(1, 'Ana Lucia', 'Quispe Huaman', 'a20201234@pucp.edu.pe', '20201234', 'Ciencias e Ingenieria', 'Ingenieria Informatica',
    sha2('clave123', 256), '2026-03-05', 'ACTIVA', true, null, null, true, true),
(2, 'Diego', 'Ramirez Torres', 'a20211567@pucp.edu.pe', '20211567', 'Ciencias e Ingenieria', 'Ingenieria Industrial',
    sha2('clave123', 256), '2026-03-06', 'ACTIVA', true, null, null, true, true),
(3, 'Valeria', 'Castro Paredes', 'a20223345@pucp.edu.pe', '20223345', 'Estudios Generales Ciencias', null,
    sha2('clave123', 256), '2026-03-08', 'ACTIVA', true, null, null, true, true),
(4, 'Mateo', 'Flores Chavez', 'a20224410@pucp.edu.pe', '20224410', 'Estudios Generales Ciencias', null,
    sha2('clave123', 256), '2026-03-09', 'ACTIVA', true, null, null, true, false),
(5, 'Sofia', 'Gutierrez Leon', 'a20190876@pucp.edu.pe', '20190876', 'Ciencias e Ingenieria', 'Ingenieria Mecatronica',
    sha2('clave123', 256), '2026-03-10', 'ACTIVA', true, null, null, true, true),
(6, 'Renzo', 'Vargas Soto', 'a20235521@pucp.edu.pe', '20235521', 'Estudios Generales Letras', null,
    sha2('clave123', 256), '2026-03-12', 'BLOQUEADA', true, null, null, true, false),
(7, 'Lucia', 'Paz Mendoza', 'a20241188@pucp.edu.pe', '20241188', 'Estudios Generales Ciencias', null,
    sha2('clave123', 256), '2026-09-27', 'ACTIVA', false, '482913', '2026-09-28 23:59:00', true, false);

insert into tutor(id_usuario) values (1), (2), (5);

insert into estudiante(id_usuario) values (3), (4), (5), (6), (7);

-- cursos: el 4 esta inactivo (eliminacion logica)
insert into curso(id, codigo, nombre, activo) values
(1, '1INF01', 'Fundamentos de Programacion', true),
(2, '1MAT05', 'Calculo Diferencial', true),
(3, '1FIS01', 'Fisica 1', true),
(4, '1QUI01', 'Quimica 1', false);

insert into tema(id, id_curso, nombre) values
(1, 1, 'Variables y tipos de datos'),
(2, 1, 'Estructuras condicionales'),
(3, 1, 'Estructuras repetitivas'),
(4, 2, 'Limites'),
(5, 2, 'Derivadas'),
(6, 2, 'Regla de la cadena'),
(7, 3, 'Cinematica'),
(8, 3, 'Leyes de Newton'),
(9, 4, 'Estequiometria');

-- postulaciones: tres aprobadas, una rechazada y una pendiente
insert into postulacion(id, id_tutor, id_curso, archivo_certificado_url, estado, fecha_postulacion,
                        fecha_resolucion, resolucion, id_administrador_revisor) values
(1, 1, 1, 'certificados/20201234_1INF01.pdf', 'APROBADA', '2026-03-15', '2026-03-18', 'Nota 18 en el curso', 1),
(2, 2, 2, 'certificados/20211567_1MAT05.pdf', 'APROBADA', '2026-03-16', '2026-03-19', 'Nota 17 en el curso', 1),
(3, 5, 3, 'certificados/20190876_1FIS01.pdf', 'APROBADA', '2026-03-20', '2026-03-22', 'Nota 16 en el curso', 2),
(4, 2, 3, 'certificados/20211567_1FIS01.pdf', 'RECHAZADA', '2026-03-21', '2026-03-23', 'El certificado no es legible', 2),
(5, 1, 2, 'certificados/20201234_1MAT05.pdf', 'PENDIENTE', '2026-09-20', null, null, null);

-- cada tutor cobra solo en los cursos que le aprobaron
insert into tarifa(id, id_tutor, id_curso, monto_por_hora) values
(1, 1, 1, 25.00),
(2, 2, 2, 30.00),
(3, 5, 3, 20.00);

-- conversacion entre Valeria (3) y Ana (1), y un mensaje sin leer de Mateo (4) a Diego (2)
insert into mensaje(id, id_usuario_emisor, id_usuario_receptor, contenido, fecha_envio, leido) values
(1, 3, 1, 'Hola, tienes disponibilidad el viernes para repasar estructuras repetitivas?', '2026-04-01 10:00:00', true),
(2, 1, 3, 'Si, el viernes a las 4 pm te parece bien?', '2026-04-01 10:15:00', true),
(3, 3, 1, 'Perfecto, nos vemos.', '2026-04-01 10:20:00', true),
(4, 4, 2, 'Buenas, necesito ayuda con derivadas para el parcial.', '2026-09-25 18:00:00', false);

-- sesiones: tres realizadas y calificadas, una cancelada por el estudiante y una programada
-- en la sesion 4 Sofia (5) participa como estudiante
insert into sesion(id, id_tutor, id_estudiante, id_curso, fecha_hora_inicio, fecha_hora_fin, estado,
                   check_in_tutor, check_in_estudiante, check_out_tutor, check_out_estudiante,
                   puntaje, comentario, motivo_cancelacion, id_usuario_cancelo) values
(1, 1, 3, 1, '2026-04-03 16:00:00', '2026-04-03 17:00:00', 'REALIZADA',
    true, true, true, true, 5, 'Muy clara la explicacion', null, null),
(2, 2, 4, 2, '2026-04-10 15:00:00', '2026-04-10 16:30:00', 'REALIZADA',
    true, true, true, true, 3, null, null, null),
(3, 5, 3, 3, '2026-04-15 10:00:00', null, 'CANCELADA',
    false, false, false, false, null, null, 'Cruce con un examen', 3),
(4, 1, 5, 1, '2026-10-05 16:00:00', null, 'PROGRAMADA',
    false, false, false, false, null, null, null, null),
(5, 2, 3, 2, '2026-04-20 11:00:00', '2026-04-20 12:00:00', 'REALIZADA',
    true, true, true, true, 2, 'El tutor llego tarde', null, null);

-- los temas de cada sesion son de su mismo curso
insert into sesion_tema(id_sesion, id_tema) values
(1, 3), (1, 2),
(2, 4), (2, 5),
(3, 7),
(4, 1),
(5, 5), (5, 6);

-- reclamos: uno pendiente y uno ya resuelto
insert into reclamo(id, id_sesion, id_usuario_reclamante, rol_reclamante, motivo, fecha_reclamo,
                    estado, resolucion, id_administrador_resuelve, fecha_resolucion) values
(1, 2, 4, 'ESTUDIANTE', 'El tutor termino la sesion 30 minutos antes', '2026-04-11',
    'PENDIENTE', null, null, null),
(2, 5, 2, 'TUTOR', 'La calificacion no refleja la sesion, el retraso fue avisado', '2026-04-21',
    'RECHAZADA', 'Se reviso la sesion y la calificacion se mantiene', 2, '2026-04-24');
