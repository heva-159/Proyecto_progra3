package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

// Lo que llega al aprobar o rechazar una postulación.
// resolucion = motivo (obligatorio solo al rechazar). idAdministrador = quién la revisa.
public class ResolucionRequest {
    private boolean aprobar;
    private String resolucion;
    private int idAdministrador;

    public boolean isAprobar() {
        return aprobar;
    }

    public void setAprobar(boolean aprobar) {
        this.aprobar = aprobar;
    }

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }

    public int getIdAdministrador() {
        return idAdministrador;
    }

    public void setIdAdministrador(int idAdministrador) {
        this.idAdministrador = idAdministrador;
    }
}
