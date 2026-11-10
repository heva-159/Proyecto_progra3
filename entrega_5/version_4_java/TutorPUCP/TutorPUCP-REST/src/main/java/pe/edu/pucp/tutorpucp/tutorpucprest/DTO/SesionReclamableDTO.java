package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;

// Una sesión del usuario sobre la que puede registrar un reclamo.
public class SesionReclamableDTO {
    private int idSesion;
    private String curso;
    private String fechaHoraInicio;
    private String estado;
    private String rolUsuario;      // TUTOR o ESTUDIANTE: lo que el usuario fue en esa sesión
    private String nombreContraparte;
    private boolean yaReclamada;

    public SesionReclamableDTO() {}

    public SesionReclamableDTO(Sesion sesion, boolean esTutor, boolean yaReclamada) {
        this.idSesion = sesion.getIdSesion();
        this.curso = sesion.getCurso() != null ? sesion.getCurso().getNombre() : null;
        this.fechaHoraInicio = sesion.getFechaHoraInicio() != null ? sesion.getFechaHoraInicio().toString() : null;
        this.estado = sesion.getEstado() != null ? sesion.getEstado().name() : null;
        this.rolUsuario = esTutor ? "TUTOR" : "ESTUDIANTE";
        this.nombreContraparte = ReclamoDTO.nombre(esTutor ? sesion.getEstudiante() : sesion.getTutor());
        this.yaReclamada = yaReclamada;
    }

    // getters y setters

    public int getIdSesion() { return idSesion; }
    public void setIdSesion(int idSesion) { this.idSesion = idSesion; }

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }

    public String getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(String fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getRolUsuario() { return rolUsuario; }
    public void setRolUsuario(String rolUsuario) { this.rolUsuario = rolUsuario; }

    public String getNombreContraparte() { return nombreContraparte; }
    public void setNombreContraparte(String nombreContraparte) { this.nombreContraparte = nombreContraparte; }

    public boolean isYaReclamada() { return yaReclamada; }
    public void setYaReclamada(boolean yaReclamada) { this.yaReclamada = yaReclamada; }
}
