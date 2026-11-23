package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

// Lo que llega al calificar una sesión (RF014): quién califica (el estudiante), el puntaje y el comentario opcional.
public class CalificarSesionRequest {
    private int idUsuario;
    private int puntaje;
    private String comentario;

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(int puntaje) {
        this.puntaje = puntaje;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
