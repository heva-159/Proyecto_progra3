package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

// Una fila de la bandeja: con quién se conversa, el último mensaje y cuántos faltan leer.
public class ConversacionDTO {
    private int idContacto;
    private String nombresContacto;
    private String apellidosContacto;
    private String ultimoMensaje;
    private String fechaUltimo; // ISO 8601
    private boolean ultimoEsMio;
    private int noLeidos;

    public ConversacionDTO() {
    }

    // getters y setters

    public int getIdContacto() { return idContacto; }
    public void setIdContacto(int idContacto) { this.idContacto = idContacto; }

    public String getNombresContacto() { return nombresContacto; }
    public void setNombresContacto(String nombresContacto) { this.nombresContacto = nombresContacto; }

    public String getApellidosContacto() { return apellidosContacto; }
    public void setApellidosContacto(String apellidosContacto) { this.apellidosContacto = apellidosContacto; }


    public String getUltimoMensaje() { return ultimoMensaje; }
    public void setUltimoMensaje(String ultimoMensaje) { this.ultimoMensaje = ultimoMensaje; }

    public String getFechaUltimo() { return fechaUltimo; }
    public void setFechaUltimo(String fechaUltimo) { this.fechaUltimo = fechaUltimo; }

    public boolean isUltimoEsMio() { return ultimoEsMio; }
    public void setUltimoEsMio(boolean ultimoEsMio) { this.ultimoEsMio = ultimoEsMio; }

    public int getNoLeidos() { return noLeidos; }
    public void setNoLeidos(int noLeidos) { this.noLeidos = noLeidos; }
}
