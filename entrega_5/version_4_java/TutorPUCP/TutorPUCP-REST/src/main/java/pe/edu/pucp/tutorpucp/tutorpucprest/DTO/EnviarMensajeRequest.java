package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

public class EnviarMensajeRequest {
    private int idEmisor;
    private int idReceptor;
    private String contenido;

    // getters y setters

    public int getIdEmisor() { return idEmisor; }
    public void setIdEmisor(int idEmisor) { this.idEmisor = idEmisor; }

    public int getIdReceptor() { return idReceptor; }
    public void setIdReceptor(int idReceptor) { this.idReceptor = idReceptor; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
}
