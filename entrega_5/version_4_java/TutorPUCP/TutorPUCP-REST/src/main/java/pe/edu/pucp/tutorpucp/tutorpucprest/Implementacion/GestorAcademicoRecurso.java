package pe.edu.pucp.tutorpucp.tutorpucprest.Implementacion;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorAcademico;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorUsuarios;
import pe.edu.pucp.tutorPUCP.bl.Exeptions.BL_exeption;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorAcademicoImpl;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorUsuariosImpl;
import pe.edu.pucp.tutorPUCP.dbmanager.DBManager;
import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.academico.Postulacion;
import pe.edu.pucp.tutorPUCP.modelo.academico.Tema;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoAprobacion;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Tarifa;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;
import pe.edu.pucp.tutorPUCP.modelo.usuario.PerfilTutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio REST del catálogo académico. Base: /api/academico
 *
 * Administrador (RF005, RF006, RF007):
 *  RF005  GET  /postulaciones[?estado=PENDIENTE|APROBADA|RECHAZADA]
 *         GET  /postulaciones/{id}
 *         PUT  /postulaciones/{id}/resolucion        body: {aprobar, resolucion, idAdministrador}
 *         GET  /postulaciones/{id}/certificado       responde el PDF (application/pdf) para verificarlo
 *  RF006  GET  /cursos
 *         GET  /cursos/{id}
 *         POST /cursos                               body: {codigo, nombre, temas[]}
 *         PUT  /cursos/{id}                          body: {codigo, nombre}
 *         PUT  /cursos/{id}/desactivar
 *         PUT  /cursos/{id}/reactivar
 *  RF007  GET  /cursos/{id}/temas
 *         POST /cursos/{id}/temas                    body: {nombre}
 *         PUT  /temas/{id}                           body: {nombre}
 *         DELETE /temas/{id}
 *
 * Tutor y estudiante (RF004, RF008, RF009):
 *  RF004  POST /postulaciones                        body: {idTutor, idCurso, certificadoBase64 (el PDF) o archivoCertificadoUrl}
 *         GET  /postulaciones/tutor/{idTutor}
 *  RF008  GET  /tarifas/tutor/{idTutor}             cursos aprobados del tutor, con su tarifa o sin ella
 *         POST /tarifas                              body: {idTutor, idCurso, montoPorHora} (registra o modifica)
 *  RF009  GET  /tutores/buscar[?idCurso=N]
 *         GET  /tutores/{idTutor}/perfil             datos del tutor, cursos aprobados con tarifa y calificaciones (404 si no existe)
 *
 * Las reglas del BL que no se cumplen se responden con 400 y {"error": "..."};
 * lo que no existe, con 404; y cualquier otra falla, con 500 (el detalle queda en server.log).
 */
@Path("/academico")
@Produces(MediaType.APPLICATION_JSON)
public class GestorAcademicoRecurso {
    private static final Logger LOG = Logger.getLogger(GestorAcademicoRecurso.class.getName());

    private final GestorAcademico gestorAcademicoBL = new GestorAcademicoImpl();
    private final GestorUsuarios gestorUsuariosBL = new GestorUsuariosImpl();

    // =========================================================================
    // REQUERIMIENTO RF004: POSTULACIÓN DE TUTOR A UN CURSO CON CERTIFICADO PDF
    // =========================================================================

    @POST
    @Path("/postulaciones")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postular(PostularRequest req) {
        boolean traePdf = req != null && req.getCertificadoBase64() != null && !req.getCertificadoBase64().isBlank();
        boolean traeNombre = req != null && req.getArchivoCertificadoUrl() != null && !req.getArchivoCertificadoUrl().isBlank();
        if (!traePdf && !traeNombre) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Debe adjuntar el certificado de notas en formato PDF")).build();
        }
        // El PDF llega en Base64 dentro del JSON: se vuelve a convertir en bytes.
        byte[] pdf = null;
        if (traePdf) {
            try {
                pdf = java.util.Base64.getMimeDecoder().decode(req.getCertificadoBase64());
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "El certificado no llegó bien codificado en Base64")).build();
            }
        }
        try {
            Tutor tutor = gestorUsuariosBL.obtenerTutorPorId(req.getIdTutor());
            Curso curso = gestorAcademicoBL.obtenerCursoPorId(req.getIdCurso());

            if (tutor == null) {
                return error(Response.Status.NOT_FOUND, "Tutor no encontrado");
            }
            if (curso == null) {
                return error(Response.Status.NOT_FOUND, "Curso no encontrado");
            }

            // Con PDF: el BL valida que sea un PDF de hasta 5 MB, lo guarda en el servidor y registra su ruta.
            // Solo con el nombre: se registra como antes.
            Postulacion postulacion = pdf != null
                    ? gestorAcademicoBL.postularConCertificado(tutor, curso, pdf)
                    : gestorAcademicoBL.postular(tutor, curso, req.getArchivoCertificadoUrl());
            return Response.status(Response.Status.CREATED).entity(aDTO(postulacion)).build();
        } catch (BL_exeption e) {
            return error(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al registrar la postulacion del tutor " + req.getIdTutor(), e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo registrar la postulación");
        }
    }

    @GET
    @Path("/postulaciones/tutor/{idTutor}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarPostulacionesPorTutor(@PathParam("idTutor") Integer idTutor) {
        try {
            Tutor tutor = gestorUsuariosBL.obtenerTutorPorId(idTutor);
            if (tutor == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "No existe el tutor con ID: " + idTutor)).build();
            }

            List<Postulacion> postulaciones = gestorAcademicoBL.listarPostulacionesPorTutor(tutor);
            List<PostulacionDTO> resultado = new ArrayList<>();
            for (Postulacion p : postulaciones) {
                resultado.add(aDTO(p));
            }
            return Response.ok(resultado).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al listar las postulaciones del tutor " + idTutor, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo listar las postulaciones");
        }
    }

    // =========================================================================
    // REQUERIMIENTO RF008: GESTIÓN DE TARIFAS POR HORA
    // =========================================================================

    @GET
    @Path("/tarifas/tutor/{idTutor}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarTarifasPorTutor(@PathParam("idTutor") Integer idTutor) {
        try {
            Tutor tutor = gestorUsuariosBL.obtenerTutorPorId(idTutor);
            if (tutor == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "No existe el tutor con ID: " + idTutor)).build();
            }

            // Una fila por cada curso con postulación APROBADA: con su tarifa, o sin tarifa todavía
            // (idTarifa = 0 y montoPorHora = null) para que el tutor pueda registrarla.
            Map<Integer, TarifaDTO> filas = new java.util.LinkedHashMap<>();
            for (Postulacion p : gestorAcademicoBL.listarPostulacionesPorTutor(tutor)) {
                if (p.getEstado() == EstadoAprobacion.APROBADA && p.getCurso() != null) {
                    filas.putIfAbsent(p.getCurso().getIdCurso(), new TarifaDTO(p.getCurso()));
                }
            }
            for (Tarifa t : gestorAcademicoBL.listarTarifasPorTutor(tutor)) {
                if (t.getCurso() != null) {
                    filas.put(t.getCurso().getIdCurso(), new TarifaDTO(t));
                }
            }
            List<TarifaDTO> resultado = new ArrayList<>(filas.values());
            resultado.sort(java.util.Comparator.comparing(TarifaDTO::getCursoCodigo,
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
            return Response.ok(resultado).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al listar las tarifas del tutor " + idTutor, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo listar las tarifas");
        }
    }

    @POST
    @Path("/tarifas")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarOModificarTarifa(GuardarTarifaRequest req) {
        if (req == null || req.getMontoPorHora() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Datos de tarifa incompletos")).build();
        }
        try {
            Tutor tutor = gestorUsuariosBL.obtenerTutorPorId(req.getIdTutor());
            Curso curso = gestorAcademicoBL.obtenerCursoPorId(req.getIdCurso());
            if (tutor == null || curso == null) {
                return error(Response.Status.NOT_FOUND, "Tutor o curso no encontrado");
            }

            // RF008: el BL decide si registra (primera vez en ese curso) o modifica (ya cobraba ahí),
            // y valida el monto (mayor que 0, máximo 9999.99) y que la postulación al curso esté aprobada.
            Tarifa guardada = gestorAcademicoBL.guardarTarifa(tutor, curso, req.getMontoPorHora());
            return Response.ok(new TarifaDTO(guardada)).build();
        } catch (BL_exeption e) {
            return error(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al guardar la tarifa del tutor " + req.getIdTutor(), e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo guardar la tarifa");
        }
    }

    // ======================== RF005: postulaciones ========================

    @GET
    @Path("/postulaciones")
    public Response listarPostulaciones(@QueryParam("estado") String estado) {
        try {
            List<Postulacion> postulaciones;
            if (estado == null || estado.isBlank()) {
                postulaciones = gestorAcademicoBL.listarPostulaciones();
            } else {
                EstadoAprobacion filtro = EstadoAprobacion.valueOf(estado.trim().toUpperCase());
                postulaciones = filtro == EstadoAprobacion.PENDIENTE
                        ? gestorAcademicoBL.listarPostulacionesPendientes()
                        : gestorAcademicoBL.listarPostulaciones().stream().filter(p -> p.getEstado() == filtro).toList();
            }
            return Response.ok(postulaciones.stream().map(this::aDTO).toList()).build();
        } catch (IllegalArgumentException e) {
            return error(Response.Status.BAD_REQUEST, "Estado no válido: usa PENDIENTE, APROBADA o RECHAZADA");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al listar postulaciones", e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo listar las postulaciones");
        }
    }

    @GET
    @Path("/postulaciones/{id}")
    public Response obtenerPostulacion(@PathParam("id") int id) {
        try {
            Postulacion postulacion = gestorAcademicoBL.obtenerPostulacionPorId(id);
            if (postulacion == null) {
                return error(Response.Status.NOT_FOUND, "No existe la postulación " + id);
            }
            return Response.ok(aDTO(postulacion)).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al obtener la postulacion " + id, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo obtener la postulación");
        }
    }

    // RF005: el administrador abre el PDF del certificado para verificarlo antes de aprobar o rechazar.
    // Responde el archivo tal cual (application/pdf) e "inline" para que el navegador lo muestre.
    @GET
    @Path("/postulaciones/{id}/certificado")
    @Produces({"application/pdf", MediaType.APPLICATION_JSON})
    public Response obtenerCertificado(@PathParam("id") int id) {
        try {
            Postulacion postulacion = gestorAcademicoBL.obtenerPostulacionPorId(id);
            if (postulacion == null) {
                return error(Response.Status.NOT_FOUND, "No existe la postulación " + id);
            }
            byte[] pdf = gestorAcademicoBL.obtenerCertificado(postulacion);
            if (pdf == null) {
                return error(Response.Status.NOT_FOUND,
                        "El archivo del certificado (" + postulacion.getArchivoCertificadoUrl() + ") no está en el servidor");
            }
            String ruta = postulacion.getArchivoCertificadoUrl().replace('\\', '/');
            String nombre = ruta.substring(ruta.lastIndexOf('/') + 1);
            return Response.ok(pdf, "application/pdf")
                    .header("Content-Disposition", "inline; filename=\"" + nombre + "\"")
                    .build();
        } catch (BL_exeption e) {
            return error(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al obtener el certificado de la postulacion " + id, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo obtener el certificado");
        }
    }

    // Aprobar o rechazar: el motivo solo es obligatorio al rechazar (lo valida el BL).
    @PUT
    @Path("/postulaciones/{id}/resolucion")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response resolverPostulacion(@PathParam("id") int id, ResolucionRequest datos) {
        if (datos == null) {
            return error(Response.Status.BAD_REQUEST, "Faltan los datos de la resolución");
        }
        try {
            Postulacion postulacion = gestorAcademicoBL.obtenerPostulacionPorId(id);
            if (postulacion == null) {
                return error(Response.Status.NOT_FOUND, "No existe la postulación " + id);
            }
            Administrador administrador = gestorUsuariosBL.obtenerAdministradorPorId(datos.getIdAdministrador());
            if (administrador == null) {
                return error(Response.Status.NOT_FOUND, "No existe el administrador " + datos.getIdAdministrador());
            }
            gestorAcademicoBL.resolverPostulacion(postulacion, administrador, datos.isAprobar(), datos.getResolucion());
            return Response.ok(aDTO(postulacion)).build();
        } catch (BL_exeption e) {
            return error(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al resolver la postulacion " + id, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo resolver la postulación");
        }
    }

    // ============================ RF006: cursos ============================

    @GET
    @Path("/cursos")
    public Response listarCursos() {
        try {
            // 2 consultas en total (los cursos y el conteo de temas); antes era 1 + una por cada curso
            Map<Integer, Integer> temasPorCurso = gestorAcademicoBL.contarTemasPorCurso();
            List<CursoDTO> cursos = gestorAcademicoBL.listarCursos().stream()
                    .map(c -> new CursoDTO(c, temasPorCurso.getOrDefault(c.getIdCurso(), 0)))
                    .toList();
            return Response.ok(cursos).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al listar cursos", e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo listar los cursos");
        }
    }

    // =========================================================================
    // REQUERIMIENTO RF009: BÚSQUEDA Y FILTRADO DE TUTORES POR CURSO Y TARIFA
    // =========================================================================

    @GET
    @Path("/tutores/buscar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarTutores(@QueryParam("idCurso") Integer idCurso) {
        List<TutorBusquedaDTO> resultado = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT DISTINCT " +
            "COALESCE(p.id_tutor, t.id_tutor) AS id_tutor, " +
            "u.nombres, u.apellidos, u.especialidad, " +
            "c.id AS id_curso, c.codigo AS curso_codigo, c.nombre AS curso_nombre, " +
            "COALESCE(t.monto_por_hora, 35.00) AS tarifa_por_hora, " +
            // RF014: promedio de las calificaciones que recibió el tutor y cuántas sesiones realizó
            "(SELECT AVG(s.puntaje) FROM sesion s WHERE s.id_tutor = u.id AND s.puntaje IS NOT NULL) AS calificacion, " +
            "(SELECT COUNT(*) FROM sesion s WHERE s.id_tutor = u.id AND s.estado = 'REALIZADA') AS sesiones_dictadas " +
            "FROM usuario u " +
            "JOIN tutor tu ON tu.id_usuario = u.id " +
            "JOIN curso c ON c.activo = 1 " +
            "LEFT JOIN postulacion p ON p.id_tutor = u.id AND p.id_curso = c.id AND p.estado = 'APROBADA' " +
            "LEFT JOIN tarifa t ON t.id_tutor = u.id AND t.id_curso = c.id " +
            "WHERE (p.id IS NOT NULL OR t.id IS NOT NULL)"
        );
        if (idCurso != null && idCurso > 0) {
            sql.append(" AND c.id = ?");
        }
        try (Connection conn = DBManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (idCurso != null && idCurso > 0) {
                ps.setInt(1, idCurso);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TutorBusquedaDTO dto = new TutorBusquedaDTO();
                    dto.setIdTutor(rs.getInt("id_tutor"));
                    String nom = rs.getString("nombres");
                    String ape = rs.getString("apellidos");
                    dto.setNombre(((nom != null ? nom : "") + " " + (ape != null ? ape : "")).trim());
                    String esp = rs.getString("especialidad");
                    dto.setEspecialidad(esp != null && !esp.isBlank() ? esp : "Estudiante PUCP");
                    // sin calificaciones todavia, AVG devuelve null y queda en 0
                    dto.setCalificacion(Math.round(rs.getDouble("calificacion") * 10) / 10.0);
                    dto.setSesionesDictadas(rs.getInt("sesiones_dictadas"));
                    dto.setIdCurso(rs.getInt("id_curso"));
                    dto.setCursoCodigo(rs.getString("curso_codigo"));
                    dto.setCursoNombre(rs.getString("curso_nombre"));
                    dto.setTarifaPorHora(rs.getBigDecimal("tarifa_por_hora"));
                    resultado.add(dto);
                }
            }
            return Response.ok(resultado).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    // El perfil del tutor que el estudiante eligió en la búsqueda: sus datos públicos, los cursos en los que está
    // aprobado con su tarifa y las calificaciones que recibió (sin correo ni teléfono, RNF005).
    @GET
    @Path("/tutores/{idTutor}/perfil")
    public Response perfilDelTutor(@PathParam("idTutor") int idTutor) {
        try {
            PerfilTutor perfil = gestorAcademicoBL.obtenerPerfilTutor(idTutor);
            if (perfil == null) {
                return error(Response.Status.NOT_FOUND, "No existe el tutor " + idTutor);
            }
            return Response.ok(new PerfilTutorDTO(perfil)).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al obtener el perfil del tutor " + idTutor, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo cargar el perfil del tutor");
        }
    }

    @GET
    @Path("/cursos/{id}")
    public Response obtenerCurso(@PathParam("id") int id) {
        try {
            Curso curso = gestorAcademicoBL.obtenerCursoPorId(id);
            if (curso == null) {
                return error(Response.Status.NOT_FOUND, "No existe el curso " + id);
            }
            return Response.ok(new CursoDTO(curso, curso.getTemas().size())).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al obtener el curso " + id, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo obtener el curso");
        }
    }

    // Registra el curso con sus temas iniciales en una sola transacción (los temas son opcionales).
    @POST
    @Path("/cursos")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrarCurso(CursoRequest datos) {
        if (datos == null) {
            return error(Response.Status.BAD_REQUEST, "Faltan los datos del curso");
        }
        try {
            List<String> temas = datos.getTemas() == null ? List.of()
                    : datos.getTemas().stream().filter(t -> t != null && !t.isBlank()).map(String::trim).toList();
            Curso curso = gestorAcademicoBL.registrarCursoConTemas(datos.getNombre(), datos.getCodigo(), temas);
            return Response.status(Response.Status.CREATED).entity(new CursoDTO(curso, temas.size())).build();
        } catch (BL_exeption e) {
            return error(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al registrar el curso", e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo registrar el curso");
        }
    }

    @PUT
    @Path("/cursos/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modificarCurso(@PathParam("id") int id, CursoRequest datos) {
        if (datos == null) {
            return error(Response.Status.BAD_REQUEST, "Faltan los datos del curso");
        }
        try {
            Curso curso = gestorAcademicoBL.obtenerCursoPorId(id);
            if (curso == null) {
                return error(Response.Status.NOT_FOUND, "No existe el curso " + id);
            }
                gestorAcademicoBL.modificarCurso(curso, datos.getCodigo(), datos.getNombre());
            return Response.ok(new CursoDTO(curso, curso.getTemas().size())).build();
        } catch (BL_exeption e) {
            return error(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al modificar el curso " + id, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo modificar el curso");
        }
    }

    // Desactivar = eliminación lógica, por eso es PUT y no DELETE.
    @PUT
    @Path("/cursos/{id}/desactivar")
    public Response desactivarCurso(@PathParam("id") int id) {
        return cambiarEstadoCurso(id, false);
    }

    @PUT
    @Path("/cursos/{id}/reactivar")
    public Response reactivarCurso(@PathParam("id") int id) {
        return cambiarEstadoCurso(id, true);
    }

    private Response cambiarEstadoCurso(int id, boolean activar) {
        try {
            Curso curso = gestorAcademicoBL.obtenerCursoPorId(id);
            if (curso == null) {
                return error(Response.Status.NOT_FOUND, "No existe el curso " + id);
            }
            if (activar) gestorAcademicoBL.reactivarCurso(curso);
            else gestorAcademicoBL.desactivarCurso(curso);
            return Response.ok(new CursoDTO(curso, curso.getTemas().size())).build();
        } catch (BL_exeption e) {
            return error(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al cambiar el estado del curso " + id, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR,
                    activar ? "No se pudo reactivar el curso" : "No se pudo desactivar el curso");
        }
    }

    // ============================ RF007: temas ============================

    @GET
    @Path("/cursos/{id}/temas")
    public Response listarTemasPorCurso(@PathParam("id") int idCurso) {
        try {
            Curso curso = gestorAcademicoBL.obtenerCursoPorId(idCurso);
            if (curso == null) {
                return error(Response.Status.NOT_FOUND, "No existe el curso " + idCurso);
            }
            List<TemaDTO> temas = gestorAcademicoBL.listarTemasPorCurso(curso).stream().map(TemaDTO::new).toList();
            return Response.ok(temas).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al listar los temas del curso " + idCurso, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo listar los temas");
        }
    }

    @POST
    @Path("/cursos/{id}/temas")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrarTema(@PathParam("id") int idCurso, TemaRequest datos) {
        if (datos == null) {
            return error(Response.Status.BAD_REQUEST, "Faltan los datos del tema");
        }
        try {
            Curso curso = gestorAcademicoBL.obtenerCursoPorId(idCurso);
            if (curso == null) {
                return error(Response.Status.NOT_FOUND, "No existe el curso " + idCurso);
            }
            Tema tema = gestorAcademicoBL.registrarTema(curso, datos.getNombre());
            return Response.status(Response.Status.CREATED).entity(new TemaDTO(tema)).build();
        } catch (BL_exeption e) {
            return error(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al registrar un tema en el curso " + idCurso, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo registrar el tema");
        }
    }

    @PUT
    @Path("/temas/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modificarTema(@PathParam("id") int id, TemaRequest datos) {
        if (datos == null) {
            return error(Response.Status.BAD_REQUEST, "Faltan los datos del tema");
        }
        try {
            Tema tema = gestorAcademicoBL.obtenerTemaPorId(id);   // ya trae su curso
            if (tema == null) {
                return error(Response.Status.NOT_FOUND, "No existe el tema " + id);
            }
            gestorAcademicoBL.modificarTema(tema, datos.getNombre());
            return Response.ok(new TemaDTO(tema)).build();
        } catch (BL_exeption e) {
            return error(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al modificar el tema " + id, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo modificar el tema");
        }
    }

    // Eliminación física: si el tema ya se usó en una sesión, el BL lo impide y se responde 400.
    @DELETE
    @Path("/temas/{id}")
    public Response eliminarTema(@PathParam("id") int id) {
        try {
            Tema tema = gestorAcademicoBL.obtenerTemaPorId(id);
            if (tema == null) {
                return error(Response.Status.NOT_FOUND, "No existe el tema " + id);
            }
            gestorAcademicoBL.eliminarTema(tema);
            return Response.noContent().build();
        } catch (BL_exeption e) {
            return error(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al eliminar el tema " + id, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo eliminar el tema");
        }
    }

    // DTO de una postulación + si su PDF está en el servidor (así la pantalla no muestra enlaces rotos).
    private PostulacionDTO aDTO(Postulacion postulacion) {
        PostulacionDTO dto = new PostulacionDTO(postulacion);
        dto.setCertificadoDisponible(gestorAcademicoBL.tieneCertificado(postulacion));
        return dto;
    }

    private static Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(Map.of("error", mensaje)).type(MediaType.APPLICATION_JSON).build();
    }

    // =========================================================================
    // REQUERIMIENTO RF017: REPORTE DE TUTORÍAS DEL TUTOR POR RANGO DE FECHAS
    // =========================================================================
    @GET
    @Path("/tutores/{idTutor}/reporte")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerReporteTutor(
            @PathParam("idTutor") Integer idTutor,
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta) {
        if (idTutor == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "El ID del tutor es requerido")).build();
        }
        StringBuilder sql = new StringBuilder(
                "SELECT s.id, s.fecha_hora_inicio, s.estado, s.puntaje, s.comentario, " +
                        "c.codigo AS curso_codigo, c.nombre AS curso_nombre, " +
                        "u.nombres, u.apellidos " +
                        "FROM sesion s " +
                        "JOIN curso c ON c.id = s.id_curso " +
                        "JOIN usuario u ON u.id = s.id_estudiante " +
                        "WHERE s.id_tutor = ?"
        );
        boolean tieneDesde = (desde != null && !desde.isBlank());
        boolean tieneHasta = (hasta != null && !hasta.isBlank());
        if (tieneDesde) sql.append(" AND DATE(s.fecha_hora_inicio) >= ?");
        if (tieneHasta) sql.append(" AND DATE(s.fecha_hora_inicio) <= ?");
        sql.append(" ORDER BY s.fecha_hora_inicio DESC");


        List<SesionReporteItemDTO> lista = new ArrayList<>();
        int totalDictadas = 0;
        int sumaCalificaciones = 0;
        int countCalificadas = 0;
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try (Connection conn = DBManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            ps.setInt(idx++, idTutor);
            if (tieneDesde) ps.setString(idx++, desde);
            if (tieneHasta) ps.setString(idx++, hasta);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SesionReporteItemDTO item = new SesionReporteItemDTO();
                    item.setIdSesion(rs.getInt("id"));
                    java.sql.Timestamp ts = rs.getTimestamp("fecha_hora_inicio");
                    if (ts != null) {
                        item.setFecha(ts.toLocalDateTime().format(dtf));
                    }
                    item.setEstado(rs.getString("estado"));
                    item.setCursoCodigo(rs.getString("curso_codigo"));
                    item.setCursoNombre(rs.getString("curso_nombre"));
                    String nom = rs.getString("nombres");
                    String ape = rs.getString("apellidos");
                    item.setEstudianteNombre(((nom != null ? nom : "") + " " + (ape != null ? ape : "")).trim());
                    int puntaje = rs.getInt("puntaje");
                    if (!rs.wasNull()) {
                        item.setCalificacion(puntaje);
                        sumaCalificaciones += puntaje;
                        countCalificadas++;
                    }
                    item.setComentario(rs.getString("comentario"));
                    if ("REALIZADA".equalsIgnoreCase(item.getEstado())) {
                        totalDictadas++;
                    }
                    lista.add(item);
                }
            }
            Double calificacionPromedio = countCalificadas > 0
                    ? Math.round((double) sumaCalificaciones / countCalificadas * 10.0) / 10.0
                    : null;
            ReporteTutorDTO reporte = new ReporteTutorDTO(totalDictadas, calificacionPromedio, lista);
            return Response.ok(reporte).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al generar reporte: " + e.getMessage())).build();
        }
    }





}
