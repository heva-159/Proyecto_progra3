package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

public class RegistrarReclamoRequest {
    private int idSesion;
    private int idUsuario;
    private String rol;     // TUTOR o ESTUDIANTE
    private String motivo;

    // getters y setters

    public int getIdSesion() { return idSesion; }
    public void setIdSesion(int idSesion) { this.idSesion = idSesion; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
