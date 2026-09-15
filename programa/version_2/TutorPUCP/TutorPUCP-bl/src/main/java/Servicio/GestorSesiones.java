package Servicio;

import Academico.Curso;
import Academico.Postulacion;
import Academico.Tema;
import Usuario.Usuario;
import enums.EstadoAprobacion;
import enums.EstadoSesion;
import Tutoria.Sesion;
import Usuario.Estudiante;
import Usuario.Tutor;

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
        sesion.getTemas().addAll(temas);
        sesiones.add(sesion);
        tutor.getSesiones().add(sesion);
        estudiante.getSesiones().add(sesion);
        return sesion;
    }

    public void confirmarCheckIn(Sesion sesion, Usuario persona) {
        if (sesion.getEstado() != EstadoSesion.PROGRAMADA) { //todo: error de lógica
            throw new IllegalStateException("Solo se puede hacer check-in de una sesión programada");
        }
        if (persona == sesion.getTutor().getUsuario()) sesion.setCheckInTutor(true); //si es tutor
        else if (persona == sesion.getEstudiante().getUsuario()) sesion.setCheckInEstudiante(true); //si es estudiante
        if (sesion.isCheckInTutor() && sesion.isCheckInEstudiante()) {
            sesion.setEstado(EstadoSesion.EN_CURSO);
        }
    }

    public void confirmarCheckOut(Sesion sesion, Usuario persona) {
        if (sesion.getEstado() != EstadoSesion.EN_CURSO) {
            throw new IllegalStateException("Solo se puede hacer check-out de una sesión en curso");
        }
        if (persona == sesion.getTutor().getUsuario()) sesion.setCheckOutTutor(true);
        else if (persona == sesion.getEstudiante().getUsuario()) sesion.setCheckOutEstudiante(true);
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

    public void cancelarSesion(Sesion sesion, String motivo) {
        if (sesion.getEstado() != EstadoSesion.PROGRAMADA) {
            throw new IllegalStateException("Solo se puede cancelar una sesión programada");
        }
        sesion.setEstado(EstadoSesion.CANCELADA);
        sesion.setMotivoCancelacion(motivo);
    }

    public void verificarInasistencia(Sesion sesion, LocalDateTime ahora) {
        if (sesion.getEstado() == EstadoSesion.PROGRAMADA
                && !(sesion.isCheckInTutor() && sesion.isCheckInEstudiante())
                && ahora.isAfter(sesion.getFechaHoraInicio().plusMinutes(15))) {
            sesion.setEstado(EstadoSesion.CANCELADA);
            sesion.setMotivoCancelacion("Inasistencia");
        }
    }

    public List<Sesion> getSesiones() {
        return sesiones;
    }
}
