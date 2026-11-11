package pe.edu.pucp.tutorpucp.tutorpucprest.Implementacion;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorAcademico;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorSesiones;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorUsuarios;
import pe.edu.pucp.tutorPUCP.bl.Exeptions.BL_exeption;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorAcademicoImpl;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorSesionesImpl;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorUsuariosImpl;
import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.academico.Postulacion;
import pe.edu.pucp.tutorPUCP.modelo.academico.Tema;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoAprobacion;
import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Tarifa;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Estudiante;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.AsistenciaRequest;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.CancelarSesionRequest;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.CursoSesionDTO;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.OpcionesSesionDTO;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.PersonaSesionDTO;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.RegistrarSesionRequest;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.SesionDTO;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.TemaDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio REST de las sesiones de tutoría (RF011, RF012, RF013).
 * Base: /api/sesion
 *
 *  RF011  GET  /opciones?idTutor=1           cursos que el tutor puede dictar (con temas y tarifa) y estudiantes
 *         POST /                              body: {idTutor, idEstudiante, idCurso, idsTemas[], fechaHoraInicio}
 *         GET  /{id}                          detalle de una sesión
 *  RF012  PUT  /{id}/checkin                  body: {rol: TUTOR | ESTUDIANTE}
 *         PUT  /{id}/checkout                 body: {rol:   TUTOR | ESTUDIANTE}
 *  RF013  PUT  /{id}/cancelacion              body: {idUsuario, motivo}
 *
 * Las reglas del BL que no se cumplen se responden con 400 y {"error": "..."};
 * lo que no existe, con 404; y cualquier otra falla, con 500 (el detalle queda en server.log).
 */
@Path("/sesion")
@Produces(MediaType.APPLICATION_JSON)
public class GestorSesionesRecurso {
    private static final Logger LOG = Logger.getLogger(GestorSesionesRecurso.class.getName());

    private final GestorSesiones gestorSesionesBL = new GestorSesionesImpl();
    private final GestorUsuarios gestorUsuariosBL = new GestorUsuariosImpl();
    private final GestorAcademico gestorAcademicoBL = new GestorAcademicoImpl();

    // ======================== RF011: registrar sesión ========================

    // Datos para el formulario: cursos activos con postulación aprobada del tutor (con sus temas y su tarifa)
    // y los estudiantes a los que puede agendar (todos menos él mismo).
    @GET
    @Path("/opciones")
    public Response opcionesParaRegistrar(@QueryParam("idTutor") int idTutor) {
        try {
            Tutor tutor = gestorUsuariosBL.obtenerTutorPorId(idTutor);
            if (tutor == null) {
                return error(Response.Status.NOT_FOUND, "No existe el tutor " + idTutor);
            }
            List<Tarifa> tarifas = gestorAcademicoBL.listarTarifasPorTutor(tutor);
            List<CursoSesionDTO> cursos = new ArrayList<>();
            Set<Integer> agregados = new HashSet<>();
            for (Postulacion postulacion : gestorAcademicoBL.listarPostulacionesPorTutor(tutor)) {
                Curso curso = postulacion.getCurso();
                if (postulacion.getEstado() != EstadoAprobacion.APROBADA || curso == null || !curso.isActivo()
                        || !agregados.add(curso.getIdCurso())) {
                    continue;
                }
                List<TemaDTO> temas = gestorAcademicoBL.listarTemasPorCurso(curso).stream().map(TemaDTO::new).toList();
                cursos.add(new CursoSesionDTO(curso, tarifaDelCurso(tarifas, curso.getIdCurso()), temas));
            }
            List<PersonaSesionDTO> estudiantes = new ArrayList<>();
            for (Estudiante estudiante : gestorUsuariosBL.listarEstudiantes()) {
                Usuario usuario = estudiante.getUsuario();
                if (usuario != null && usuario.getIdUsuario() != idTutor) {
                    estudiantes.add(new PersonaSesionDTO(usuario));
                }
            }
            return Response.ok(new OpcionesSesionDTO(cursos, estudiantes)).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al cargar las opciones para registrar sesion", e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo cargar los cursos y estudiantes");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrarSesion(RegistrarSesionRequest datos) {
        if (datos == null) {
            return error(Response.Status.BAD_REQUEST, "Faltan los datos de la sesión");
        }
        if (datos.getFechaHoraInicio() == null || datos.getFechaHoraInicio().isBlank()) {
            return error(Response.Status.BAD_REQUEST, "Falta la fecha y hora de inicio");
        }
        try {
            LocalDateTime inicio = LocalDateTime.parse(datos.getFechaHoraInicio().trim());
            Tutor tutor = gestorUsuariosBL.obtenerTutorPorId(datos.getIdTutor());
            if (tutor == null) {
                return error(Response.Status.NOT_FOUND, "No existe el tutor " + datos.getIdTutor());
            }
            Estudiante estudiante = gestorUsuariosBL.obtenerEstudiantePorId(datos.getIdEstudiante());
            if (estudiante == null) {
                return error(Response.Status.NOT_FOUND, "No existe el estudiante " + datos.getIdEstudiante());
            }
            Curso curso = gestorAcademicoBL.obtenerCursoPorId(datos.getIdCurso());
            if (curso == null) {
                return error(Response.Status.NOT_FOUND, "No existe el curso " + datos.getIdCurso());
            }
            List<Tema> temas = new ArrayList<>();
            if (datos.getIdsTemas() != null) {
                for (Integer idTema : datos.getIdsTemas()) {
                    Tema tema = idTema != null ? gestorAcademicoBL.obtenerTemaPorId(idTema) : null;
                    if (tema == null) {
                        return error(Response.Status.NOT_FOUND, "No existe el tema " + idTema);
                    }
                    temas.add(tema);
                }
            }
            Sesion sesion = gestorSesionesBL.registrarSesion(tutor, estudiante, curso, temas, inicio);
            return Response.status(Response.Status.CREATED).entity(aDTO(sesion)).build();
        } catch (DateTimeParseException e) {
            return error(Response.Status.BAD_REQUEST, "La fecha y hora de inicio no es válida");
        } catch (BL_exeption e) {
            return error(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al registrar la sesion", e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo registrar la sesión");
        }
    }

    @GET
    @Path("/{id}")
    public Response obtenerSesion(@PathParam("id") int id) {
        try {
            Sesion sesion = gestorSesionesBL.obtenerSesionPorId(id);
            if (sesion == null) {
                return error(Response.Status.NOT_FOUND, "No existe la sesión " + id);
            }
            return Response.ok(aDTO(sesion)).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al obtener la sesion " + id, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo obtener la sesión");
        }
    }

    // ===================== RF012: check-in y check-out =====================

    // Cada uno (tutor o estudiante) marca el suyo. Con los dos check-in la sesión pasa a EN_CURSO.
    @PUT
    @Path("/{id}/checkin")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response confirmarCheckIn(@PathParam("id") int id, AsistenciaRequest datos) {
        return registrarAsistencia(id, datos, true);
    }

    // Con los dos check-out la sesión queda REALIZADA y se guarda la hora de fin.
    @PUT
    @Path("/{id}/checkout")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response confirmarCheckOut(@PathParam("id") int id, AsistenciaRequest datos) {
        return registrarAsistencia(id, datos, false);
    }

    private Response registrarAsistencia(int id, AsistenciaRequest datos, boolean esCheckIn) {
        Rol rol = leerRol(datos);
        if (rol == null) {
            return error(Response.Status.BAD_REQUEST, "Indica quién marca la asistencia: TUTOR o ESTUDIANTE");
        }
        try {
            Sesion sesion = gestorSesionesBL.obtenerSesionPorId(id);
            if (sesion == null) {
                return error(Response.Status.NOT_FOUND, "No existe la sesión " + id);
            }
            if (esCheckIn) {
                gestorSesionesBL.confirmarCheckIn(sesion, rol);
            } else {
                // la BD exige que la hora de fin sea posterior a la de inicio (ck_sesion_fechas)
                if (!LocalDateTime.now().isAfter(sesion.getFechaHoraInicio())) {
                    return error(Response.Status.BAD_REQUEST,
                            "La sesión todavía no empieza: el check-out se registra después de la hora de inicio");
                }
                gestorSesionesBL.confirmarCheckOut(sesion, rol);
            }
            return Response.ok(aDTO(sesion)).build();
        } catch (BL_exeption e) {
            return error(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al registrar la asistencia de la sesion " + id, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR,
                    esCheckIn ? "No se pudo registrar el check-in" : "No se pudo registrar el check-out");
        }
    }

    // ========================= RF013: cancelar sesión =========================

    // Solo una sesión programada; el motivo es obligatorio y quien cancela debe ser su tutor o su estudiante (lo valida el BL).
    @PUT
    @Path("/{id}/cancelacion")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cancelarSesion(@PathParam("id") int id, CancelarSesionRequest datos) {
        if (datos == null || datos.getMotivo() == null || datos.getMotivo().isBlank()) {
            return error(Response.Status.BAD_REQUEST, "El motivo de la cancelación es obligatorio");
        }
        try {
            Sesion sesion = gestorSesionesBL.obtenerSesionPorId(id);
            if (sesion == null) {
                return error(Response.Status.NOT_FOUND, "No existe la sesión " + id);
            }
            Usuario quienCancela = gestorUsuariosBL.obtenerUsuarioPorId(datos.getIdUsuario());
            if (quienCancela == null) {
                return error(Response.Status.NOT_FOUND, "No existe el usuario " + datos.getIdUsuario());
            }
            gestorSesionesBL.cancelarSesion(sesion, quienCancela, datos.getMotivo().trim());
            return Response.ok(aDTO(sesion)).build();
        } catch (BL_exeption e) {
            return error(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al cancelar la sesion " + id, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo cancelar la sesión");
        }
    }

    // ================================ ayudas ================================

    // DTO de la sesión con la tarifa del tutor para ese curso. Si la tarifa no se puede leer, la sesión igual se devuelve.
    private SesionDTO aDTO(Sesion sesion) {
        SesionDTO dto = new SesionDTO(sesion);
        try {
            if (sesion.getTutor() != null && sesion.getCurso() != null) {
                dto.setTarifa(tarifaDelCurso(gestorAcademicoBL.listarTarifasPorTutor(sesion.getTutor()),
                        sesion.getCurso().getIdCurso()));
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "No se pudo leer la tarifa de la sesion " + sesion.getIdSesion(), e);
        }
        return dto;
    }

    private static BigDecimal tarifaDelCurso(List<Tarifa> tarifas, int idCurso) {
        for (Tarifa tarifa : tarifas) {
            if (tarifa.getCurso() != null && tarifa.getCurso().getIdCurso() == idCurso) {
                return tarifa.getMontoPorHora();
            }
        }
        return null;
    }

    // Solo TUTOR o ESTUDIANTE (AMBOS no sirve para marcar asistencia).
    private static Rol leerRol(AsistenciaRequest datos) {
        if (datos == null || datos.getRol() == null) return null;
        try {
            Rol rol = Rol.valueOf(datos.getRol().trim().toUpperCase());
            return rol == Rol.AMBOS ? null : rol;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(Map.of("error", mensaje)).type(MediaType.APPLICATION_JSON).build();
    }
}
