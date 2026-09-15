package Servicio;

import Academico.Curso;
import Academico.Tema;
import Usuario.Usuario;
import enums.EstadoSesion;
import Tutoria.Sesion;
import Usuario.Estudiante;
import Usuario.Tutor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GestorSesiones {
    private List<Sesion> sesiones = new ArrayList<>();

    public Sesion registrarSesion(Tutor tutor, Estudiante alumno, Curso curso,
                                  List<Tema> temas, LocalDateTime fechaHoraInicio) {
        Sesion sesion = new Sesion(tutor, alumno, curso, fechaHoraInicio);
        sesion.getTemas().addAll(temas);
        sesiones.add(sesion);
        tutor.getSesiones().add(sesion);
        alumno.getSesiones().add(sesion);
        return sesion;
    }

    public void confirmarCheckIn(Sesion sesion, Usuario persona) {
        if (sesion.getEstado() != EstadoSesion.PROGRAMADA) { //todo: error de lógica
            throw new IllegalStateException("Solo se puede hacer check-in de una sesión programada");
        }
        if (persona == sesion.getTutor().getUsuario()) sesion.setCheckInTutor(true); //si es tutor
        else if (persona == sesion.getAlumno().getUsuario()) sesion.setCheckInAlumno(true); //si es alumno
        if (sesion.isCheckInTutor() && sesion.isCheckInAlumno()) {
            sesion.setEstado(EstadoSesion.EN_CURSO);
        }
    }

    public void confirmarCheckOut(Sesion sesion, Usuario persona) {
        if (persona == sesion.getTutor().getUsuario()) sesion.setCheckOutTutor(true);
        else if (persona == sesion.getAlumno().getUsuario()) sesion.setCheckOutAlumno(true);
        if (sesion.isCheckOutTutor() && sesion.isCheckOutAlumno()) {
            sesion.setEstado(EstadoSesion.REALIZADA);
            sesion.setFechaHoraFin(LocalDateTime.now());
        }
    }

    public void calificarSesion(Sesion sesion, int puntaje, String comentario) {
        if (sesion.getEstado() != EstadoSesion.REALIZADA) {
            throw new IllegalStateException("Solo se puede calificar una sesión ya realizada");
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
                && !(sesion.isCheckInTutor() && sesion.isCheckInAlumno())
                && ahora.isAfter(sesion.getFechaHoraInicio().plusMinutes(15))) {
            sesion.setEstado(EstadoSesion.CANCELADA);
            sesion.setMotivoCancelacion("Inasistencia");
        }
    }

    public List<Sesion> getSesiones() {
        return sesiones;
    }
}
