package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import pe.edu.pucp.tutorPUCP.modelo.tutoria.Mensaje;

// Mensaje plano: del emisor y receptor solo van id y nombre (nunca el hash de la contraseña).
public class MensajeDTO {
    private int idMensaje;
    private int idEmisor;
    private String nombreEmisor;
    private int idReceptor;
    private String nombreReceptor;
    private String contenido;
    private String fechaEnvio; // ISO 8601, por ejemplo 2026-11-10T10:42:00
    private boolean leido;

    public MensajeDTO() {
    }

    public MensajeDTO(Mensaje mensaje) {
        this.idMensaje = mensaje.getIdMensaje();
        this.idEmisor = mensaje.getEmisor().getIdUsuario();
        this.nombreEmisor = mensaje.getEmisor().getNombres() + " " + mensaje.getEmisor().getApellidos();
        this.idReceptor = mensaje.getReceptor().getIdUsuario();
        this.nombreReceptor = mensaje.getReceptor().getNombres() + " " + mensaje.getReceptor().getApellidos();
        this.contenido = mensaje.getContenido();
        this.fechaEnvio = mensaje.getFechaEnvio() == null ? null : mensaje.getFechaEnvio().toString();
        this.leido = mensaje.isLeido();
    }

    // getters y setters

    public int getIdMensaje() { return idMensaje; }
    public void setIdMensaje(int idMensaje) { this.idMensaje = idMensaje; }

    public int getIdEmisor() { return idEmisor; }
    public void setIdEmisor(int idEmisor) { this.idEmisor = idEmisor; }

    public String getNombreEmisor() { return nombreEmisor; }
    public void setNombreEmisor(String nombreEmisor) { this.nombreEmisor = nombreEmisor; }

    public int getIdReceptor() { return idReceptor; }
    public void setIdReceptor(int idReceptor) { this.idReceptor = idReceptor; }

    public String getNombreReceptor() { return nombreReceptor; }
    public void setNombreReceptor(String nombreReceptor) { this.nombreReceptor = nombreReceptor; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(String fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }
}
