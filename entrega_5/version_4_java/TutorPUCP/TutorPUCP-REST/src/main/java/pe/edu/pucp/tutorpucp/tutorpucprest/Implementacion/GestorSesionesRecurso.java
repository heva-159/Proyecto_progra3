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
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoSesion;
import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Tarifa;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Estudiante;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.AsistenciaRequest;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.CalificarSesionRequest;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.CancelarSesionRequest;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.CursoSesionDTO;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.OpcionesSesionDTO;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.PersonaSesionDTO;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.RegistrarSesionRequest;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.SesionDTO;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.TemaDTO;
import pe.edu.pucp.tutorpucp.tutorpucprest.reportes.FilaSesionReporte;
import pe.edu.pucp.tutorpucp.tutorpucprest.reportes.GeneradorReportes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


@Path("/sesion")
@Produces(MediaType.APPLICATION_JSON)
public class GestorSesionesRecurso {
    private static final Logger LOG = Logger.getLogger(GestorSesionesRecurso.class.getName());

    private final GestorSesiones gestorSesionesBL = new GestorSesionesImpl();
    private final GestorUsuarios gestorUsuariosBL = new GestorUsuariosImpl();
    private final GestorAcademico gestorAcademicoBL = new GestorAcademicoImpl();
    private final GeneradorReportes generador = new GeneradorReportes();

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

    // "Mis sesiones": las sesiones en las que participa la persona (como tutor o como estudiante), de la más
    // reciente a la más antigua. Es una lista resumida (sin comentario ni motivo de cancelación: eso va en el detalle).
    @GET
    @Path("/usuario/{idUsuario}")
    public Response sesionesDelUsuario(@PathParam("idUsuario") int idUsuario) {
        try {
            if (gestorUsuariosBL.obtenerUsuarioPorId(idUsuario) == null) {
                return error(Response.Status.NOT_FOUND, "No existe el usuario " + idUsuario);
            }
            List<SesionDTO> sesiones = gestorSesionesBL.listarResumenPorUsuario(idUsuario).stream()
                    .map(SesionDTO::new).toList();
            return Response.ok(sesiones).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al listar las sesiones del usuario " + idUsuario, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudieron cargar las sesiones");
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

    // ======================== RF014: calificar al tutor ========================

    // Solo el estudiante de la sesión, solo si está REALIZADA y una sola vez (lo último lo valida el BL).
    @PUT
    @Path("/{id}/calificacion")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response calificarSesion(@PathParam("id") int id, CalificarSesionRequest datos) {
        if (datos == null) {
            return error(Response.Status.BAD_REQUEST, "Faltan los datos de la calificación");
        }
        try {
            Sesion sesion = gestorSesionesBL.obtenerSesionPorId(id);
            if (sesion == null) {
                return error(Response.Status.NOT_FOUND, "No existe la sesión " + id);
            }
            // se compara por id: el usuario de la sesión y el que califica vienen de loads distintos
            Usuario usuarioEstudiante = sesion.getEstudiante() != null ? sesion.getEstudiante().getUsuario() : null;
            if (usuarioEstudiante == null || usuarioEstudiante.getIdUsuario() != datos.getIdUsuario()) {
                return error(Response.Status.BAD_REQUEST, "Solo el estudiante de la sesión puede calificar al tutor");
            }
            gestorSesionesBL.calificarSesion(sesion, datos.getPuntaje(), datos.getComentario());
            return Response.ok(aDTO(sesion)).build();
        } catch (BL_exeption e) {
            return error(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al calificar la sesion " + id, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo registrar la calificación");
        }
    }

    // ============== RF017 y RF018: reportes en PDF por rango de fechas ==============

    // RF017: tutorías que dictó el tutor entre desde y hasta (incluidas). Ej: /sesion/reportes/tutor/1?desde=2026-10-01&hasta=2026-10-31
    @GET
    @Path("/reportes/tutor/{idUsuario}")
    @Produces("application/pdf")
    public Response reporteTutoriasTutor(@PathParam("idUsuario") int idUsuario,
                                         @QueryParam("desde") String desde, @QueryParam("hasta") String hasta) {
        String problema = GeneradorReportes.validarRango(desde, hasta);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        try {
            Tutor tutor = gestorUsuariosBL.obtenerTutorPorId(idUsuario);
            if (tutor == null || tutor.getUsuario() == null) {
                return error(Response.Status.NOT_FOUND, "No existe el tutor " + idUsuario);
            }
            LocalDate inicio = LocalDate.parse(desde.trim());
            LocalDate fin = LocalDate.parse(hasta.trim());
            List<Sesion> sesiones = sesionesDelRango(inicio, fin).stream()
                    .filter(s -> s.getTutor() != null && s.getTutor().getUsuario() != null
                            && s.getTutor().getUsuario().getIdUsuario() == idUsuario)
                    .toList();

            long realizadas = sesiones.stream().filter(s -> s.getEstado() == EstadoSesion.REALIZADA).count();
            List<Integer> puntajes = sesiones.stream().map(Sesion::getPuntaje).filter(p -> p != null).toList();
            String promedio = puntajes.isEmpty()
                    ? "sin calificaciones"
                    : String.format(Locale.forLanguageTag("es-PE"), "%.1f de 5 (%d calificaciones)",
                            puntajes.stream().mapToInt(Integer::intValue).average().orElse(0), puntajes.size());

            byte[] pdf = generador.generarTutoriasTutor(filas(sesiones),
                    tutor.getUsuario().getNombres() + " " + tutor.getUsuario().getApellidos(),
                    inicio, fin, realizadas, promedio);
            return pdf(pdf, "reporte-tutorias.pdf");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al generar el reporte de tutorias del tutor " + idUsuario, e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo generar el reporte");
        }
    }

    // RF018: todas las sesiones entre desde y hasta (para el administrador). Ej: /sesion/reportes?desde=2026-10-01&hasta=2026-10-31
    @GET
    @Path("/reportes")
    @Produces("application/pdf")
    public Response reporteSesiones(@QueryParam("desde") String desde, @QueryParam("hasta") String hasta) {
        String problema = GeneradorReportes.validarRango(desde, hasta);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        try {
            LocalDate inicio = LocalDate.parse(desde.trim());
            LocalDate fin = LocalDate.parse(hasta.trim());
            byte[] pdf = generador.generarSesiones(filas(sesionesDelRango(inicio, fin)), inicio, fin);
            return pdf(pdf, "reporte-sesiones.pdf");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al generar el reporte de sesiones", e);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo generar el reporte");
        }
    }

    // No hay SP de sesiones por fecha, así que se filtra la lista completa (como el ejemplo de la clase con platos).
    private List<Sesion> sesionesDelRango(LocalDate desde, LocalDate hasta) {
        return gestorSesionesBL.listarSesiones().stream()
                .filter(s -> s.getFechaHoraInicio() != null)
                .filter(s -> {
                    LocalDate dia = s.getFechaHoraInicio().toLocalDate();
                    return !dia.isBefore(desde) && !dia.isAfter(hasta);
                })
                .sorted(Comparator.comparing(Sesion::getFechaHoraInicio))
                .toList();
    }

    private static List<FilaSesionReporte> filas(List<Sesion> sesiones) {
        return sesiones.stream().map(FilaSesionReporte::new).toList();
    }

    private static Response pdf(byte[] pdf, String nombreArchivo) {
        return Response.ok(pdf, "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"" + nombreArchivo + "\"")
                .header("Cache-Control", "no-store")
                .build();
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
