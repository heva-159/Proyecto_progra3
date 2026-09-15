package Tutoria;

import Academico.Curso;
import Academico.Tema;
import Usuario.Estudiante;
import Usuario.Tutor;
import enums.EstadoSesion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sesion {
    private static long contador = 0;

    private long idSesion;
    private Tutor tutor;
    private Estudiante alumno;
    private Curso curso;
    private List<Tema> tema = new ArrayList<>();
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private EstadoSesion estado;
    private boolean checkInTutor;
    private boolean checkInAlumno;
    private boolean checkOutTutor;
    private boolean checkOutAlumno;
    private int puntaje;
    private String comentario;
    private String motivoCancelacion;
    private List<Reclamo> reclamos = new ArrayList<>();

    public Sesion(Tutor tutor, Estudiante alumno, Curso curso, LocalDateTime fechaHoraInicio) {
        this.idSesion = ++contador;
        this.tutor = tutor;
        this.alumno = alumno;
        this.curso = curso;
        this.fechaHoraInicio = fechaHoraInicio;
        this.estado = EstadoSesion.PROGRAMADA;
    }

    public long getIdSesion() { return idSesion; }
    public void setIdSesion(long idSesion) { this.idSesion = idSesion; }

    public Tutor getTutor() { return tutor; }
    public void setTutor(Tutor tutor) { this.tutor = tutor; }

    public Estudiante getAlumno() { return alumno; }
    public void setAlumno(Estudiante alumno) { this.alumno = alumno; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public List<Tema> getTemas() { return tema; }
    public void setTemas(List<Tema> tema) { this.tema = tema; }

    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }

    public LocalDateTime getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }

    public EstadoSesion getEstado() { return estado; }
    public void setEstado(EstadoSesion estado) { this.estado = estado; }

    public boolean isCheckInTutor() { return checkInTutor; }
    public void setCheckInTutor(boolean checkInTutor) { this.checkInTutor = checkInTutor; }

    public boolean isCheckInAlumno() { return checkInAlumno; }
    public void setCheckInAlumno(boolean checkInAlumno) { this.checkInAlumno = checkInAlumno; }

    public boolean isCheckOutTutor() { return checkOutTutor; }
    public void setCheckOutTutor(boolean checkOutTutor) { this.checkOutTutor = checkOutTutor; }

    public boolean isCheckOutAlumno() { return checkOutAlumno; }
    public void setCheckOutAlumno(boolean checkOutAlumno) { this.checkOutAlumno = checkOutAlumno; }

    public int getPuntaje() { return puntaje; }
    public void setPuntaje(int puntaje) { this.puntaje = puntaje; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

    public List<Reclamo> getReclamos() { return reclamos; }
    public void setReclamos(List<Reclamo> reclamos) { this.reclamos = reclamos; }
}
