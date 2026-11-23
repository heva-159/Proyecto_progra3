package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

// Lo que llega al registrar check-in o check-out (RF012): quién lo marca, TUTOR o ESTUDIANTE.
public class AsistenciaRequest {
    private String rol;

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
