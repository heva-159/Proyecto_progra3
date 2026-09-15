import java.time.LocalDateTime;

public class Mensaje {
    private static long contador = 0;

    private long idMensaje;
    private Usuario emisor;
    private Usuario receptor;
    private String contenido;
    private LocalDateTime fechaEnvio;
    private boolean leido;

    public Mensaje(Usuario emisor, Usuario receptor, String contenido, LocalDateTime fechaEnvio, boolean leido) {
        this.idMensaje = ++contador;
        this.emisor = emisor;
        this.receptor = receptor;
        this.contenido = contenido;
        this.fechaEnvio = fechaEnvio;
        this.leido = leido;
    }

    public long getIdMensaje() { return idMensaje; }
    public void setIdMensaje(long idMensaje) { this.idMensaje = idMensaje; }

    public Usuario getEmisor() { return emisor; }
    public void setEmisor(Usuario emisor) { this.emisor = emisor; }

    public Usuario getReceptor() { return receptor; }
    public void setReceptor(Usuario receptor) { this.receptor = receptor; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }
}
