package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

public class ResolverReclamoRequest {
    private int idAdministrador;
    private boolean aprobar;
    private String resolucion;

    // getters y setters

    public int getIdAdministrador() { return idAdministrador; }
    public void setIdAdministrador(int idAdministrador) { this.idAdministrador = idAdministrador; }

    public boolean isAprobar() { return aprobar; }
    public void setAprobar(boolean aprobar) { this.aprobar = aprobar; }

    public String getResolucion() { return resolucion; }
    public void setResolucion(String resolucion) { this.resolucion = resolucion; }
}
