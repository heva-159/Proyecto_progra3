package pe.edu.pucp.tutorPUCP.bl;

import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.academico.Postulacion;
import pe.edu.pucp.tutorPUCP.modelo.academico.Tema;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoAprobacion;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoSesion;
import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Estudiante;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GestorSesiones {
    private List<Sesion> sesiones = new ArrayList<>();

    public Sesion registrarSesion(Tutor tutor, Estudiante estudiante, Curso curso,
                                  List<Tema> temas, LocalDateTime fechaHoraInicio) {
        if (!curso.isActivo()) {
            throw new IllegalStateException("El curso no esta activo");
        }
        boolean tienePostulacionAprobada = false;
        for (Postulacion postulacion : tutor.getPostulaciones()) {
            if (postulacion.getCurso() == curso && postulacion.getEstado() == EstadoAprobacion.APROBADA) {
                tienePostulacionAprobada = true;
            }
        }
        if (!tienePostulacionAprobada) {
            throw new IllegalStateException("El tutor no tiene una postulacion aprobada para este curso");
        }
        Sesion sesion = new Sesion(tutor, estudiante, curso, fechaHoraInicio);
        for (Tema tema : temas) sesion.agregarTema(tema);
        sesiones.add(sesion);
        tutor.agregarSesion(sesion);
        estudiante.agregarSesion(sesion);
        return sesion;
    }

    public void confirmarCheckIn(Sesion sesion, Rol rol) {
        if (sesion.getEstado() != EstadoSesion.PROGRAMADA) {
            throw new IllegalStateException("Solo se puede hacer check-in de una sesión programada");
        }
        if (rol == Rol.TUTOR) sesion.setCheckInTutor(true);
        else if (rol == Rol.ESTUDIANTE) sesion.setCheckInEstudiante(true);
        if (sesion.isCheckInTutor() && sesion.isCheckInEstudiante()) {
            sesion.setEstado(EstadoSesion.EN_CURSO);
        }
    }

    public void confirmarCheckOut(Sesion sesion, Rol rol) {
        if (sesion.getEstado() != EstadoSesion.EN_CURSO) {
            throw new IllegalStateException("Solo se puede hacer check-out de una sesión en curso");
        }
        if (rol == Rol.TUTOR) sesion.setCheckOutTutor(true);
        else if (rol == Rol.ESTUDIANTE) sesion.setCheckOutEstudiante(true);
        if (sesion.isCheckOutTutor() && sesion.isCheckOutEstudiante()) {
            sesion.setEstado(EstadoSesion.REALIZADA);
            sesion.setFechaHoraFin(LocalDateTime.now());
        }
    }

    public void calificarSesion(Sesion sesion, int puntaje, String comentario) {
        if (sesion.getEstado() != EstadoSesion.REALIZADA) {
            throw new IllegalStateException("Solo se puede calificar una sesión ya realizada");
        }
        if (puntaje < 1 || puntaje > 5) {
            throw new IllegalArgumentException("El puntaje debe estar entre 1 y 5");
        }
        sesion.setPuntaje(puntaje);
        sesion.setComentario(comentario);
    }

    public void cancelarSesion(Sesion sesion, Usuario quienCancela, String motivo) {
        if (sesion.getEstado() != EstadoSesion.PROGRAMADA) {
            throw new IllegalStateException("Solo se puede cancelar una sesión programada");
        }
        if (quienCancela != sesion.getTutor().getUsuario() && quienCancela != sesion.getEstudiante().getUsuario()) {
            throw new IllegalArgumentException("Solo el tutor o el estudiante de la sesión pueden cancelarla");
        }
        sesion.setEstado(EstadoSesion.CANCELADA);
        sesion.setMotivoCancelacion(motivo);
        sesion.setUsuarioCancelo(quienCancela);
    }

    public void verificarInasistencia(Sesion sesion, LocalDateTime ahora) {
        if (sesion.getEstado() == EstadoSesion.PROGRAMADA
                && !(sesion.isCheckInTutor() && sesion.isCheckInEstudiante())
                && ahora.isAfter(sesion.getFechaHoraInicio().plusMinutes(15))) {
            sesion.setEstado(EstadoSesion.CANCELADA_INASISTENCIA);
        }
    }

    public List<Sesion> getSesiones() {
        return sesiones;
    }
}
