import java.time.LocalDateTime;

public class Mensaje {
    private static long contador = 0;

    private long idMensaje;
    private Usuario emisor;
    private Usuario receptor;
    private String contenido;
    private LocalDateTime fechaEnvio;

    public Mensaje(Usuario emisor, Usuario receptor, String contenido) {
        this.idMensaje = ++contador;
        this.emisor = emisor;
        this.receptor = receptor;
        this.contenido = contenido;
        this.fechaEnvio = LocalDateTime.now();
    }
}
