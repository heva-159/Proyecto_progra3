package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

// Lo que llega al cancelar una sesión (RF013): quién cancela y por qué.
public class CancelarSesionRequest {
    private int idUsuario;
    private String motivo;

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
