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
 *  RF004  POST /postulaciones                        body: {idTutor, idCurso, archivoCertificadoUrl}
 *         GET  /postulaciones/tutor/{idTutor}
 *  RF008  GET  /tarifas/tutor/{idTutor}
 *         POST /tarifas                              body: {idTutor, idCurso, montoPorHora}
 *  RF009  GET  /tutores/buscar[?idCurso=N]
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
        if (req == null || req.getArchivoCertificadoUrl() == null || req.getArchivoCertificadoUrl().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Debe adjuntar el certificado de notas en formato PDF")).build();
        }
        try {
            Tutor tutor = gestorUsuariosBL.obtenerTutorPorId(req.getIdTutor());
            Curso curso = gestorAcademicoBL.obtenerCursoPorId(req.getIdCurso());

            if (tutor == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Tutor no encontrado")).build();
            }
            if (curso == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Curso no encontrado")).build();
            }

            Postulacion postulacion = gestorAcademicoBL.postular(tutor, curso, req.getArchivoCertificadoUrl());
            return Response.status(Response.Status.CREATED).entity(new PostulacionDTO(postulacion)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
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
                resultado.add(new PostulacionDTO(p));
            }
            return Response.ok(resultado).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
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

            List<Tarifa> tarifas = gestorAcademicoBL.listarTarifasPorTutor(tutor);
            List<TarifaDTO> resultado = new ArrayList<>();
            for (Tarifa t : tarifas) {
                resultado.add(new TarifaDTO(t));
            }
            return Response.ok(resultado).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
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
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Tutor o Curso no encontrado")).build();
            }

            // RF008: Registrar y Modificar
            // Verificamos si el tutor ya tiene una tarifa para este curso
            List<Tarifa> tarifasExistentes = gestorAcademicoBL.listarTarifasPorTutor(tutor);
            Tarifa tarifaExistente = null;
            for (Tarifa t : tarifasExistentes) {
                if (t.getCurso() != null && t.getCurso().getIdCurso() == curso.getIdCurso()) {
                    tarifaExistente = t;
                    break;
                }
            }

            Tarifa guardada;
            if (tarifaExistente != null) {
                // Si ya existe la tarifa, la MODIFICAMOS
                tarifaExistente.setMontoPorHora(req.getMontoPorHora());
                guardada = gestorAcademicoBL.modificarTarifa(tarifaExistente);
            } else {
                // Si no existe, la REGISTRAMOS
                guardada = gestorAcademicoBL.registrarTarifa(tutor, curso, req.getMontoPorHora());
            }
            return Response.ok(new TarifaDTO(guardada)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
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
            return Response.ok(postulaciones.stream().map(PostulacionDTO::new).toList()).build();
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
            return Response.ok(new PostulacionDTO(postulacion)).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al obtener la postulacion " + id, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo obtener la postulación");
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
            return Response.ok(new PostulacionDTO(postulacion)).build();
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
            List<CursoDTO> cursos = gestorAcademicoBL.listarCursos().stream()
                    .map(c -> new CursoDTO(c, gestorAcademicoBL.listarTemasPorCurso(c).size()))
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
            "COALESCE(t.monto_por_hora, 35.00) AS tarifa_por_hora " +
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
                    dto.setCalificacion(4.9);
                    dto.setSesionesDictadas(14);
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

    private static Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(Map.of("error", mensaje)).type(MediaType.APPLICATION_JSON).build();
    }
}
