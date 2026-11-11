package pe.edu.pucp.tutorpucp.tutorpucprest.Implementacion;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorAcademico;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorUsuarios;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorAcademicoImpl;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorUsuariosImpl;
import pe.edu.pucp.tutorPUCP.dbmanager.DBManager;
import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.academico.Postulacion;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Tarifa;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/academico")
public class GestorAcademicoRecurso {

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

    // =========================================================================
    // REQUERIMIENTO RF009: BÚSQUEDA Y FILTRADO DE TUTORES POR CURSO Y TARIFA
    // =========================================================================

    @GET
    @Path("/cursos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarCursos() {
        try {
            List<Curso> cursos = gestorAcademicoBL.listarCursos();
            List<CursoDTO> resultado = new ArrayList<>();
            for (Curso c : cursos) {
                resultado.add(new CursoDTO(c));
            }
            return Response.ok(resultado).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

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
}
