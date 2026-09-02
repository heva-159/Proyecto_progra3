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

    public Usuario getEmisor() {
        return emisor;
    }

    public Usuario getReceptor() {
        return receptor;
    }

    public boolean isLeido() {
        return leido;
    }
    
}
