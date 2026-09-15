package Servicio;

import Academico.Curso;
import Academico.Tema;
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

    public void confirmarCheckIn(Sesion sesion, PersonaPUCP persona) {
        if (sesion.getEstado() != EstadoSesion.PROGRAMADA) {
            throw new IllegalStateException("Solo se puede hacer check-in de una sesión programada");
        }
        if (persona == sesion.getTutor()) sesion.setCheckInTutor(true);
        else if (persona == sesion.getAlumno()) sesion.setCheckInAlumno(true);
        if (sesion.isCheckInTutor() && sesion.isCheckInAlumno()) {
            sesion.setEstado(EstadoSesion.EN_CURSO);
        }
    }

    public void confirmarCheckOut(Sesion sesion, PersonaPUCP persona) {
        if (persona == sesion.getTutor()) sesion.setCheckOutTutor(true);
        else if (persona == sesion.getAlumno()) sesion.setCheckOutAlumno(true);
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

    public List<Sesion> getSesiones() {
        return sesiones;
    }
}
