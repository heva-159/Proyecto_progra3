-- ============================================
-- TutorPUCP - Consultas de verificacion (con JOIN, para ver nombres
-- en vez de solo IDs). Correr cada una por separado o todas juntas
-- con Ctrl+Shift+Enter para revisar los datos de prueba.
-- ============================================

-- 1) Usuarios y que roles tienen activos
SELECT u.id_usuario, u.nombres, u.apellidos, u.correo_institucional,
       CASE WHEN t.id_usuario IS NOT NULL THEN 'SI' ELSE 'NO' END AS es_tutor,
       CASE WHEN e.id_usuario IS NOT NULL THEN 'SI' ELSE 'NO' END AS es_estudiante
FROM usuario u
LEFT JOIN tutor t ON u.id_usuario = t.id_usuario
LEFT JOIN estudiante e ON u.id_usuario = e.id_usuario;

-- 2) Tutores con su nombre real
SELECT u.id_usuario, u.nombres, u.apellidos, u.correo_institucional
FROM tutor t
JOIN usuario u ON t.id_usuario = u.id_usuario;

-- 3) Estudiantes con su nombre real
SELECT u.id_usuario, u.nombres, u.apellidos, u.correo_institucional
FROM estudiante e
JOIN usuario u ON e.id_usuario = u.id_usuario;

-- 4) Cursos con sus temas
SELECT c.nombre AS curso, tm.nombre AS tema
FROM curso c
JOIN tema tm ON tm.id_curso = c.id_curso
ORDER BY c.nombre;

-- 5) Postulaciones con nombre del tutor, curso y quien la reviso
SELECT p.id_postulacion,
       CONCAT(u.nombres, ' ', u.apellidos) AS tutor,
       c.nombre AS curso,
       p.estado,
       p.fecha_postulacion,
       p.fecha_resolucion,
       a.nombre AS revisado_por
FROM postulacion p
JOIN tutor t ON p.id_tutor = t.id_usuario
JOIN usuario u ON t.id_usuario = u.id_usuario
JOIN curso c ON p.id_curso = c.id_curso
LEFT JOIN administrador a ON p.id_administrador_revisor = a.id_administrador;

-- 6) Tarifas con nombre del tutor y del curso
SELECT CONCAT(u.nombres, ' ', u.apellidos) AS tutor,
       c.nombre AS curso,
       tf.monto_por_hora
FROM tarifa tf
JOIN tutor t ON tf.id_tutor = t.id_usuario
JOIN usuario u ON t.id_usuario = u.id_usuario
JOIN curso c ON tf.id_curso = c.id_curso;

-- 7) Sesiones con nombre de tutor, estudiante y curso
SELECT s.id_sesion,
       CONCAT(ut.nombres, ' ', ut.apellidos) AS tutor,
       CONCAT(ue.nombres, ' ', ue.apellidos) AS estudiante,
       c.nombre AS curso,
       s.fecha_hora_inicio,
       s.estado,
       s.puntaje,
       s.comentario
FROM sesion s
JOIN usuario ut ON s.id_tutor = ut.id_usuario
JOIN usuario ue ON s.id_estudiante = ue.id_usuario
JOIN curso c ON s.id_curso = c.id_curso
ORDER BY s.id_sesion;

-- 8) Temas tratados en cada sesion
SELECT st.id_sesion, s.estado, tm.nombre AS tema
FROM sesion_tema st
JOIN sesion s ON st.id_sesion = s.id_sesion
JOIN tema tm ON st.id_tema = tm.id_tema
ORDER BY st.id_sesion;

-- 9) Reclamos con nombre de quien reclama y quien lo resolvio
SELECT r.id_reclamo,
       CONCAT(u.nombres, ' ', u.apellidos) AS reclamante,
       r.rol_reclamante,
       r.motivo,
       r.estado,
       r.resolucion,
       a.nombre AS resuelto_por
FROM reclamo r
JOIN usuario u ON r.id_usuario_reclamante = u.id_usuario
LEFT JOIN administrador a ON r.id_administrador_resuelve = a.id_administrador
ORDER BY r.id_reclamo;

-- 10) Mensajes con nombre de emisor y receptor
SELECT CONCAT(ue.nombres, ' ', ue.apellidos) AS emisor,
       CONCAT(ur.nombres, ' ', ur.apellidos) AS receptor,
       m.contenido,
       m.fecha_envio,
       m.leido
FROM mensaje m
JOIN usuario ue ON m.id_usuario_emisor = ue.id_usuario
JOIN usuario ur ON m.id_usuario_receptor = ur.id_usuario
ORDER BY m.fecha_envio;
