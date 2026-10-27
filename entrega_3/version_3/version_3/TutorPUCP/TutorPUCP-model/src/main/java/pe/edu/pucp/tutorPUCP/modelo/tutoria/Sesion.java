package pe.edu.pucp.tutorPUCP.modelo.tutoria;

import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.academico.Tema;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Estudiante;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoSesion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sesion {
    private int idSesion;
    private Tutor tutor;
    private Estudiante estudiante;
    private Curso curso;
    private List<Tema> temas = new ArrayList<>();
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private EstadoSesion estado;
    private boolean checkInTutor;
    private boolean checkInEstudiante;
    private boolean checkOutTutor;
    private boolean checkOutEstudiante;
    private Integer puntaje;
    private String comentario;
    private String motivoCancelacion;
    private Usuario usuarioCancelo;
    private List<Reclamo> reclamos = new ArrayList<>();

    public Sesion() {
        this.estado = EstadoSesion.PROGRAMADA;
    }

    public Sesion(Tutor tutor, Estudiante estudiante, Curso curso, LocalDateTime fechaHoraInicio) {
        this.tutor = tutor;
        this.estudiante = estudiante;
        this.curso = curso;
        this.fechaHoraInicio = fechaHoraInicio;
        this.estado = EstadoSesion.PROGRAMADA;
    }

    public void agregarTema(Tema tema) {
        temas.add(tema);
    }

    public void agregarReclamo(Reclamo reclamo) {
        reclamo.setSesion(this);
        reclamos.add(reclamo);
    }

    public int getIdSesion() { return idSesion; }
    public void setIdSesion(int idSesion) { this.idSesion = idSesion; }

    public Tutor getTutor() { return tutor; }
    public void setTutor(Tutor tutor) { this.tutor = tutor; }

    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    // Solo lectura: los temas se agregan con agregarTema().
    public List<Tema> getTemas() { return Collections.unmodifiableList(temas); }
    public void setTemas(List<Tema> temas) { this.temas = new ArrayList<>(temas); }

    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }

    public LocalDateTime getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }

    public EstadoSesion getEstado() { return estado; }
    public void setEstado(EstadoSesion estado) { this.estado = estado; }

    public boolean isCheckInTutor() { return checkInTutor; }
    public void setCheckInTutor(boolean checkInTutor) { this.checkInTutor = checkInTutor; }

    public boolean isCheckInEstudiante() { return checkInEstudiante; }
    public void setCheckInEstudiante(boolean checkInEstudiante) { this.checkInEstudiante = checkInEstudiante; }

    public boolean isCheckOutTutor() { return checkOutTutor; }
    public void setCheckOutTutor(boolean checkOutTutor) { this.checkOutTutor = checkOutTutor; }

    public boolean isCheckOutEstudiante() { return checkOutEstudiante; }
    public void setCheckOutEstudiante(boolean checkOutEstudiante) { this.checkOutEstudiante = checkOutEstudiante; }

    // null mientras la sesion no haya sido calificada.
    public Integer getPuntaje() { return puntaje; }
    public void setPuntaje(Integer puntaje) { this.puntaje = puntaje; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

    public Usuario getUsuarioCancelo() { return usuarioCancelo; }
    public void setUsuarioCancelo(Usuario usuarioCancelo) { this.usuarioCancelo = usuarioCancelo; }

    // Solo lectura: los reclamos se agregan con agregarReclamo().
    public List<Reclamo> getReclamos() { return Collections.unmodifiableList(reclamos); }
    public void setReclamos(List<Reclamo> reclamos) {
        this.reclamos = new ArrayList<>();
        for (Reclamo reclamo : reclamos) agregarReclamo(reclamo);
    }
}
