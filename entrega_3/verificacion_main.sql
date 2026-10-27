-- ================= PARTE 1: Curso + Temas (registrarCursoConTemas) =================
SELECT * FROM curso ORDER BY 1 DESC LIMIT 3;
SELECT * FROM tema  ORDER BY 1 DESC LIMIT 3;

-- ================= PARTE 2: Postulacion (postular + resolverPostulacion) ===========
SELECT * FROM postulacion ORDER BY 1 DESC LIMIT 3;

-- ================= PARTE 3: Tarifa (registrarTarifa) ================================
SELECT * FROM tarifa ORDER BY 1 DESC LIMIT 3;

-- ================= PARTE 4: Sesion (registro + check-in + check-out + calificar) ====
SELECT * FROM sesion      ORDER BY 1 DESC LIMIT 3;
SELECT * FROM sesion_tema ORDER BY 1 DESC LIMIT 5;

-- ================= PARTE 5: Mensaje (enviarMensaje) ==================================
SELECT * FROM mensaje ORDER BY 1 DESC LIMIT 3;

-- ================= PARTE 6: Reclamo (registrarReclamo + resolverReclamo) ============
SELECT * FROM reclamo ORDER BY 1 DESC LIMIT 3;

-- ================= PARTE 7: Usuario nuevo + Administrador nuevo =====================
SELECT * FROM usuario      ORDER BY 1 DESC LIMIT 3;
SELECT * FROM estudiante   ORDER BY 1 DESC LIMIT 3;
SELECT * FROM administrador ORDER BY 1 DESC LIMIT 3;
