-- =====================================================================================
-- TutorPUCP · Limpieza de los datos que deja el Main + cursos reales del plan PUCP
-- Base de datos: db2026lab1 (la RDS del equipo). Preparado el 22/11/2026.
--
-- COMO EJECUTARLO (MySQL Workbench):
--   1. Abrir este archivo (File > Open SQL Script) conectado a la RDS.
--   2. Ejecutar TODO con el rayo (Execute). Corre dentro de una transaccion.
--   3. Si sale algun error en rojo: ejecutar  rollback;  y no se cambia nada.
--   4. Al final se muestra un resumen por curso para revisar.
--
-- QUE HACE:
--   1. Borra los cursos PRB-xxxxx y CRUD-xxxxx que crea el Main de consola, con sus temas,
--      postulaciones, tarifas, sesiones y reclamos, y los mensajes de prueba del Main.
--   2. Borra los usuarios de prueba del Main (pruebaNNNNN, crudNNNNN y tutorNNNNN @pucp.edu.pe).
--   3. Corrige dos codigos que no coincidian con el plan de estudios PUCP:
--        Calculo Diferencial es 1MAT06 (no 1MAT05)  y  Fisica 1 es 1FIS02 (no 1FIS01).
--   4. Agrega cursos reales de EE.GG.CC. y de Ingenieria Informatica (codigos y nombres
--      del plan de estudios PUCP), con sus temas.
--   5. Agrega postulaciones aprobadas, tarifas y sesiones SOLO con los usuarios de prueba
--      que ya existen (Ana, Diego, Sofia, Valeria y Mateo). No se crean personas nuevas.
--
-- Fuentes de los codigos y nombres de los cursos:
--   Cursos EE.GG.CC. 2023-2:  facultad.pucp.edu.pe/generales-ciencias/wp-content/uploads/2023/07/CURSOS-EEGGCC-2023-2.pdf
--   Plan de Ingenieria Informatica:  facultad-ciencias-ingenieria.pucp.edu.pe/wp-content/uploads/2025/08/INF.pdf
-- Los nombres van sin tildes, igual que los cursos que ya estaban en la base de datos.
-- =====================================================================================

use db2026lab1;
set sql_safe_updates = 0;

-- ---------------------------------------------------------------------------------------
-- 0. VISTA PREVIA: lo que se va a borrar (solo consulta)
-- ---------------------------------------------------------------------------------------
select id, codigo, nombre, activo from curso
 where codigo like 'PRB-%' or codigo like 'CRUD-%' order by id;

select id, correo_institucional, estado from usuario
 where correo_institucional regexp '^(prueba|crud|tutor)[0-9]+@pucp[.]edu[.]pe$' order by id;

start transaction;

-- ---------------------------------------------------------------------------------------
-- 1. Cursos de prueba del Main (PRB-xxxxx y CRUD-xxxxx) y todo lo que cuelga de ellos
-- ---------------------------------------------------------------------------------------
delete r from reclamo r
  join sesion s on s.id = r.id_sesion
  join curso c on c.id = s.id_curso
 where c.codigo like 'PRB-%' or c.codigo like 'CRUD-%';

-- sus filas de sesion_tema se borran solas (on delete cascade)
delete s from sesion s
  join curso c on c.id = s.id_curso
 where c.codigo like 'PRB-%' or c.codigo like 'CRUD-%';

delete t from tarifa t
  join curso c on c.id = t.id_curso
 where c.codigo like 'PRB-%' or c.codigo like 'CRUD-%';

delete p from postulacion p
  join curso c on c.id = p.id_curso
 where c.codigo like 'PRB-%' or c.codigo like 'CRUD-%';

delete te from tema te
  join curso c on c.id = te.id_curso
 where c.codigo like 'PRB-%' or c.codigo like 'CRUD-%';

delete from curso where codigo like 'PRB-%' or codigo like 'CRUD-%';

-- mensajes que deja el Main
delete from mensaje
 where contenido in ('Hola profe, gracias por la sesion!', 'Mensaje de la prueba CRUD');

-- ---------------------------------------------------------------------------------------
-- 2. Usuarios de prueba del Main: pruebaNNNNN, crudNNNNN y tutorNNNNN @pucp.edu.pe
-- ---------------------------------------------------------------------------------------
set @usuarios_main = '^(prueba|crud|tutor)[0-9]+@pucp[.]edu[.]pe$';

delete r from reclamo r
  join usuario u on u.id = r.id_usuario_reclamante
 where u.correo_institucional regexp @usuarios_main;

delete r from reclamo r
  join sesion s on s.id = r.id_sesion
  join usuario u on u.id in (s.id_tutor, s.id_estudiante)
 where u.correo_institucional regexp @usuarios_main;

delete s from sesion s
  join usuario u on u.id in (s.id_tutor, s.id_estudiante)
 where u.correo_institucional regexp @usuarios_main;

delete m from mensaje m
  join usuario u on u.id in (m.id_usuario_emisor, m.id_usuario_receptor)
 where u.correo_institucional regexp @usuarios_main;

delete t from tarifa t
  join usuario u on u.id = t.id_tutor
 where u.correo_institucional regexp @usuarios_main;

delete p from postulacion p
  join usuario u on u.id = p.id_tutor
 where u.correo_institucional regexp @usuarios_main;

-- sus perfiles de tutor y estudiante se borran solos (on delete cascade)
delete from usuario where correo_institucional regexp @usuarios_main;

-- administradores de prueba del Main que no hayan revisado nada
delete from administrador
 where correo regexp '^admin[.](prueba|crud)[0-9]+@pucp[.]edu[.]pe$'
   and id not in (select id_administrador_revisor from postulacion where id_administrador_revisor is not null)
   and id not in (select id_administrador_resuelve from reclamo where id_administrador_resuelve is not null);

-- ---------------------------------------------------------------------------------------
-- 3. Codigos corregidos segun el plan de estudios PUCP
--    (las postulaciones, tarifas y sesiones se mantienen: apuntan al id, no al codigo)
-- ---------------------------------------------------------------------------------------
update curso set codigo = '1MAT06' where codigo = '1MAT05' and nombre like 'Calculo Diferencial%';
update curso set codigo = '1FIS02' where codigo = '1FIS01' and nombre like 'Fisica 1%';

-- ---------------------------------------------------------------------------------------
-- 4. Cursos reales del plan de estudios y sus temas
-- ---------------------------------------------------------------------------------------
insert ignore into curso(codigo, nombre, activo) values
('1MAT04', 'Algebra Matricial y Geometria Analitica', true),
('1MAT05', 'Fundamentos de Calculo', true),
('1MAT07', 'Calculo Integral', true),
('1FIS01', 'Fundamentos de Fisica', true),
('1FIS04', 'Fisica 2', true),
('1QUI03', 'Quimica 2', true),
('1INF25', 'Programacion 2', true),
('1INF27', 'Algoritmia y Estructura de Datos', true),
('1INF30', 'Programacion 3', true),
('1INF33', 'Base de Datos', true);

-- los cursos base quedan activos
update curso set activo = true where codigo in ('1INF01', '1MAT06', '1FIS02', '1QUI01');

insert ignore into tema(id_curso, nombre)
select c.id, d.nombre
  from (select '1MAT04' codigo, 'Matrices y determinantes' nombre
        union all select '1MAT04', 'Sistemas de ecuaciones lineales'
        union all select '1MAT04', 'Vectores en el plano y el espacio'
        union all select '1MAT04', 'Rectas y planos'
        union all select '1MAT05', 'Numeros reales y desigualdades'
        union all select '1MAT05', 'Funciones reales'
        union all select '1MAT05', 'Funciones exponenciales y logaritmicas'
        union all select '1MAT06', 'Continuidad'
        union all select '1MAT06', 'Aplicaciones de la derivada'
        union all select '1MAT07', 'Integral indefinida'
        union all select '1MAT07', 'Tecnicas de integracion'
        union all select '1MAT07', 'Integral definida'
        union all select '1MAT07', 'Aplicaciones de la integral'
        union all select '1FIS01', 'Magnitudes fisicas y unidades'
        union all select '1FIS01', 'Vectores'
        union all select '1FIS02', 'Trabajo y energia'
        union all select '1FIS02', 'Cantidad de movimiento'
        union all select '1FIS04', 'Oscilaciones'
        union all select '1FIS04', 'Ondas mecanicas'
        union all select '1FIS04', 'Mecanica de fluidos'
        union all select '1QUI01', 'Estructura atomica'
        union all select '1QUI01', 'Enlace quimico'
        union all select '1QUI03', 'Gases'
        union all select '1QUI03', 'Soluciones'
        union all select '1QUI03', 'Equilibrio quimico'
        union all select '1INF01', 'Funciones'
        union all select '1INF01', 'Arreglos'
        union all select '1INF25', 'Punteros y memoria dinamica'
        union all select '1INF25', 'Clases y objetos'
        union all select '1INF27', 'Recursion'
        union all select '1INF27', 'Listas enlazadas'
        union all select '1INF27', 'Arboles'
        union all select '1INF30', 'Arquitectura en capas'
        union all select '1INF30', 'JDBC y procedimientos almacenados'
        union all select '1INF30', 'Servicios web REST'
        union all select '1INF30', 'Aplicaciones web con Blazor'
        union all select '1INF33', 'Modelo entidad-relacion'
        union all select '1INF33', 'SQL'
        union all select '1INF33', 'Normalizacion') d
  join curso c on c.codigo = d.codigo;

-- ---------------------------------------------------------------------------------------
-- 5. Mas datos para cada curso, con los usuarios de prueba que ya existen
--    Ana (Ing. Informatica), Diego (Ing. Industrial) y Sofia (Ing. Mecatronica) como tutores;
--    Valeria, Mateo y Sofia como estudiantes. La revisora es Carla (administrador 1).
-- ---------------------------------------------------------------------------------------
drop temporary table if exists tmp_tutorias;
create temporary table tmp_tutorias (
    correo varchar(150), curso varchar(20), monto decimal(6,2),
    fecha_postulacion date, fecha_resolucion date, nota varchar(100));
insert into tmp_tutorias values
('a20201234@pucp.edu.pe', '1INF25', 30.00, '2026-08-17', '2026-08-20', 'Nota 18 en el curso'),
('a20201234@pucp.edu.pe', '1INF27', 35.00, '2026-08-17', '2026-08-20', 'Nota 17 en el curso'),
('a20201234@pucp.edu.pe', '1INF30', 40.00, '2026-08-18', '2026-08-21', 'Nota 19 en el curso'),
('a20211567@pucp.edu.pe', '1MAT05', 25.00, '2026-08-19', '2026-08-24', 'Nota 17 en el curso'),
('a20211567@pucp.edu.pe', '1MAT07', 30.00, '2026-08-19', '2026-08-24', 'Nota 16 en el curso'),
('a20190876@pucp.edu.pe', '1FIS01', 22.00, '2026-08-20', '2026-08-25', 'Nota 18 en el curso'),
('a20190876@pucp.edu.pe', '1FIS04', 28.00, '2026-08-20', '2026-08-25', 'Nota 17 en el curso');

insert into postulacion(id_tutor, id_curso, archivo_certificado_url, estado, fecha_postulacion,
                        fecha_resolucion, resolucion, id_administrador_revisor)
select u.id, c.id, concat('certificados/', u.codigo_pucp, '_', c.codigo, '.pdf'), 'APROBADA',
       d.fecha_postulacion, d.fecha_resolucion, d.nota, 1
  from tmp_tutorias d
  join usuario u on u.correo_institucional = d.correo
  join tutor tu on tu.id_usuario = u.id
  join curso c on c.codigo = d.curso
 where not exists (select 1 from postulacion p where p.id_tutor = u.id and p.id_curso = c.id);

insert ignore into tarifa(id_tutor, id_curso, monto_por_hora)
select u.id, c.id, d.monto
  from tmp_tutorias d
  join usuario u on u.correo_institucional = d.correo
  join tutor tu on tu.id_usuario = u.id
  join curso c on c.codigo = d.curso;

drop temporary table if exists tmp_sesiones;
create temporary table tmp_sesiones (
    tutor varchar(150), estudiante varchar(150), curso varchar(20),
    inicio datetime, fin datetime, estado varchar(25), hecha boolean,
    puntaje int, comentario text, tema varchar(150));
insert into tmp_sesiones values
('a20201234@pucp.edu.pe', 'a20223345@pucp.edu.pe', '1INF30', '2026-10-07 16:00:00', '2026-10-07 17:30:00', 'REALIZADA', true, 5, 'Me quedo claro como llamar a los procedimientos almacenados', 'JDBC y procedimientos almacenados'),
('a20201234@pucp.edu.pe', 'a20224410@pucp.edu.pe', '1INF27', '2026-10-14 15:00:00', '2026-10-14 16:00:00', 'REALIZADA', true, 4, 'Buen repaso de arboles', 'Arboles'),
('a20211567@pucp.edu.pe', 'a20223345@pucp.edu.pe', '1MAT07', '2026-10-09 10:00:00', '2026-10-09 11:30:00', 'REALIZADA', true, 5, 'Muy buenos ejercicios de practica', 'Tecnicas de integracion'),
('a20211567@pucp.edu.pe', 'a20190876@pucp.edu.pe', '1MAT05', '2026-10-16 18:00:00', '2026-10-16 19:00:00', 'REALIZADA', true, 4, null, 'Funciones reales'),
('a20190876@pucp.edu.pe', 'a20224410@pucp.edu.pe', '1FIS04', '2026-10-21 11:00:00', '2026-10-21 12:00:00', 'REALIZADA', true, 5, 'Explica muy bien con ejemplos', 'Oscilaciones'),
('a20190876@pucp.edu.pe', 'a20223345@pucp.edu.pe', '1FIS01', '2026-10-23 09:00:00', '2026-10-23 10:00:00', 'REALIZADA', true, 4, null, 'Vectores'),
('a20201234@pucp.edu.pe', 'a20224410@pucp.edu.pe', '1INF30', '2026-11-28 16:00:00', null, 'PROGRAMADA', false, null, null, 'Servicios web REST'),
('a20211567@pucp.edu.pe', 'a20223345@pucp.edu.pe', '1MAT07', '2026-12-01 18:00:00', null, 'PROGRAMADA', false, null, null, 'Integral definida');

insert into sesion(id_tutor, id_estudiante, id_curso, fecha_hora_inicio, fecha_hora_fin, estado,
                   check_in_tutor, check_in_estudiante, check_out_tutor, check_out_estudiante,
                   puntaje, comentario)
select t.id, e.id, c.id, d.inicio, d.fin, d.estado, d.hecha, d.hecha, d.hecha, d.hecha, d.puntaje, d.comentario
  from tmp_sesiones d
  join usuario t on t.correo_institucional = d.tutor
  join tutor tu on tu.id_usuario = t.id
  join usuario e on e.correo_institucional = d.estudiante
  join estudiante es on es.id_usuario = e.id
  join curso c on c.codigo = d.curso
 where not exists (select 1 from sesion s
                    where s.id_tutor = t.id and s.id_estudiante = e.id and s.fecha_hora_inicio = d.inicio);

insert ignore into sesion_tema(id_sesion, id_tema)
select s.id, te.id
  from tmp_sesiones d
  join usuario t on t.correo_institucional = d.tutor
  join usuario e on e.correo_institucional = d.estudiante
  join sesion s on s.id_tutor = t.id and s.id_estudiante = e.id and s.fecha_hora_inicio = d.inicio
  join tema te on te.id_curso = s.id_curso and te.nombre = d.tema;

insert into mensaje(id_usuario_emisor, id_usuario_receptor, contenido, fecha_envio, leido)
select e.id, t.id, 'Hola Ana, el sabado podemos repasar servicios REST?', '2026-11-21 20:00:00', false
  from usuario e join usuario t
    on e.correo_institucional = 'a20224410@pucp.edu.pe' and t.correo_institucional = 'a20201234@pucp.edu.pe'
 where not exists (select 1 from mensaje m
                    where m.id_usuario_emisor = e.id and m.id_usuario_receptor = t.id
                      and m.fecha_envio = '2026-11-21 20:00:00');

drop temporary table if exists tmp_tutorias;
drop temporary table if exists tmp_sesiones;

commit;
set sql_safe_updates = 1;

-- ---------------------------------------------------------------------------------------
-- 6. Resumen para revisar
-- ---------------------------------------------------------------------------------------
select c.codigo, c.nombre, c.activo,
       (select count(*) from tema te where te.id_curso = c.id) as temas,
       (select count(*) from tarifa ta where ta.id_curso = c.id) as tutores_con_tarifa,
       (select count(*) from sesion s where s.id_curso = c.id) as sesiones
  from curso c
 order by c.codigo;
