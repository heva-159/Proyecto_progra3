package pe.edu.pucp.tutorPUCP.modelo.tutoria;

import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoSesion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Una sesion "resumida" para los listados: el nombre del tutor, el del estudiante y el del curso ya vienen en la
// misma fila. No arma los objetos Tutor, Estudiante y Curso completos (eso cuesta varias consultas por sesion).
public class SesionResumen {
    private int idSesion;
    private int idTutor;
    private String tutor;
    private int idEstudiante;
    private String estudiante;
    private int idCurso;
    private String codigoCurso;
    private String curso;
    private List<String> temas = new ArrayList<>();
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private EstadoSesion estado;
    private boolean checkInTutor;
    private boolean checkInEstudiante;
    private boolean checkOutTutor;
    private boolean checkOutEstudiante;
    private Integer puntaje;

    public SesionResumen() {
    }

    public int getIdSesion() { return idSesion; }
    public void setIdSesion(int idSesion) { this.idSesion = idSesion; }

    public int getIdTutor() { return idTutor; }
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }

    public String getTutor() { return tutor; }
    public void setTutor(String tutor) { this.tutor = tutor; }

    public int getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(int idEstudiante) { this.idEstudiante = idEstudiante; }

    public String getEstudiante() { return estudiante; }
    public void setEstudiante(String estudiante) { this.estudiante = estudiante; }

    public int getIdCurso() { return idCurso; }
    public void setIdCurso(int idCurso) { this.idCurso = idCurso; }

    public String getCodigoCurso() { return codigoCurso; }
    public void setCodigoCurso(String codigoCurso) { this.codigoCurso = codigoCurso; }

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }

    // los nombres de los temas de la sesion
    public List<String> getTemas() { return temas; }
    public void setTemas(List<String> temas) { this.temas = temas; }

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

    public Integer getPuntaje() { return puntaje; }
    public void setPuntaje(Integer puntaje) { this.puntaje = puntaje; }
}
