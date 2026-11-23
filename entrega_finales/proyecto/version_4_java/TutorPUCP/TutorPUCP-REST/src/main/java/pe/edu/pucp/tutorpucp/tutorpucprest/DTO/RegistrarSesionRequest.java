package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import java.util.List;

// Lo que llega al registrar una sesión (RF011).
// fechaHoraInicio va como texto ISO: 2026-11-12T16:00:00
public class RegistrarSesionRequest {
    private int idTutor;
    private int idEstudiante;
    private int idCurso;
    private List<Integer> idsTemas;
    private String fechaHoraInicio;

    public int getIdTutor() {
        return idTutor;
    }

    public void setIdTutor(int idTutor) {
        this.idTutor = idTutor;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    public List<Integer> getIdsTemas() {
        return idsTemas;
    }

    public void setIdsTemas(List<Integer> idsTemas) {
        this.idsTemas = idsTemas;
    }

    public String getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(String fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }
}
