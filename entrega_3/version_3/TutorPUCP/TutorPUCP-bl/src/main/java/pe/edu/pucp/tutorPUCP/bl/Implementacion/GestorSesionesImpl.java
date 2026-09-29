package pe.edu.pucp.tutorPUCP.bl.Implementacion;

import pe.edu.pucp.tutorPUCP.bl.DAOs.PostulacionDAO;
import pe.edu.pucp.tutorPUCP.bl.DAOs.SesionDAO;
import pe.edu.pucp.tutorPUCP.bl.Exeptions.BL_exeption;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorSesiones;
import pe.edu.pucp.tutorPUCP.bl.Impl.PostulacionDAOImpl;
import pe.edu.pucp.tutorPUCP.bl.Impl.SesionDAOImpl;
import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.academico.Tema;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoAprobacion;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoSesion;
import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Estudiante;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.time.LocalDateTime;
import java.util.List;

public class GestorSesionesImpl implements GestorSesiones {
    private final SesionDAO sesionDAO = new SesionDAOImpl();
    private final PostulacionDAO postulacionDAO = new PostulacionDAOImpl();

    @Override
    public Sesion registrarSesion(Tutor tutor, Estudiante estudiante, Curso curso,
                                   List<Tema> temas, LocalDateTime fechaHoraInicio) {
        if (!curso.isActivo()) {
            throw new BL_exeption("El curso no esta activo");
        }
        boolean tienePostulacionAprobada = postulacionDAO.listarPorTutor(tutor).stream()
                .anyMatch(p -> p.getCurso().getIdCurso() == curso.getIdCurso() && p.getEstado() == EstadoAprobacion.APROBADA);
        if (!tienePostulacionAprobada) {
            throw new BL_exeption("El tutor no tiene una postulacion aprobada para este curso");
        }
        Sesion sesion = new Sesion(tutor, estudiante, curso, fechaHoraInicio);
        for (Tema tema : temas) sesion.agregarTema(tema);
        return sesionDAO.save(sesion);
    }

    @Override
    public void confirmarCheckIn(Sesion sesion, Rol rol) {
        if (sesion.getEstado() != EstadoSesion.PROGRAMADA) {
            throw new BL_exeption("Solo se puede hacer check-in de una sesion programada");
        }
        if (rol == Rol.TUTOR) sesion.setCheckInTutor(true);
        else if (rol == Rol.ESTUDIANTE) sesion.setCheckInEstudiante(true);
        if (sesion.isCheckInTutor() && sesion.isCheckInEstudiante()) {
            sesion.setEstado(EstadoSesion.EN_CURSO);
        }
        sesionDAO.update(sesion);
    }

    @Override
    public void confirmarCheckOut(Sesion sesion, Rol rol) {
        if (sesion.getEstado() != EstadoSesion.EN_CURSO) {
            throw new BL_exeption("Solo se puede hacer check-out de una sesion en curso");
        }
        if (rol == Rol.TUTOR) sesion.setCheckOutTutor(true);
        else if (rol == Rol.ESTUDIANTE) sesion.setCheckOutEstudiante(true);
        if (sesion.isCheckOutTutor() && sesion.isCheckOutEstudiante()) {
            sesion.setEstado(EstadoSesion.REALIZADA);
            sesion.setFechaHoraFin(LocalDateTime.now());
        }
        sesionDAO.update(sesion);
    }

    @Override
    public void calificarSesion(Sesion sesion, int puntaje, String comentario) {
        if (sesion.getEstado() != EstadoSesion.REALIZADA) {
            throw new BL_exeption("Solo se puede calificar una sesion ya realizada");
        }
        if (puntaje < 1 || puntaje > 5) {
            throw new BL_exeption("El puntaje debe estar entre 1 y 5");
        }
        sesion.setPuntaje(puntaje);
        sesion.setComentario(comentario);
        sesionDAO.update(sesion);
    }

    @Override
    public void cancelarSesion(Sesion sesion, Usuario quienCancela, String motivo) {
        if (sesion.getEstado() != EstadoSesion.PROGRAMADA) {
            throw new BL_exeption("Solo se puede cancelar una sesion programada");
        }
        // se compara por id: quienCancela y el usuario del tutor/estudiante de la sesion
        // vienen de loads independientes, no son el mismo objeto en memoria.
        Usuario usuarioTutor = sesion.getTutor().getUsuario();
        Usuario usuarioEstudiante = sesion.getEstudiante().getUsuario();
        boolean esTutor = usuarioTutor != null && usuarioTutor.getIdUsuario() == quienCancela.getIdUsuario();
        boolean esEstudiante = usuarioEstudiante != null && usuarioEstudiante.getIdUsuario() == quienCancela.getIdUsuario();
        if (!esTutor && !esEstudiante) {
            throw new BL_exeption("Solo el tutor o el estudiante de la sesion pueden cancelarla");
        }
        sesion.setEstado(EstadoSesion.CANCELADA);
        sesion.setMotivoCancelacion(motivo);
        sesion.setUsuarioCancelo(quienCancela);
        sesionDAO.update(sesion);
    }

    @Override
    public void verificarInasistencia(Sesion sesion, LocalDateTime ahora) {
        if (sesion.getEstado() == EstadoSesion.PROGRAMADA
                && !(sesion.isCheckInTutor() && sesion.isCheckInEstudiante())
                && ahora.isAfter(sesion.getFechaHoraInicio().plusMinutes(15))) {
            sesion.setEstado(EstadoSesion.CANCELADA_INASISTENCIA);
            sesionDAO.update(sesion);
        }
    }

    @Override
    public List<Sesion> listarSesiones() {
        return sesionDAO.listAll();
    }
}
