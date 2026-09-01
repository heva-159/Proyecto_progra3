import java.time.LocalDate;

public class Reclamo {
    private static long contador = 0;

    private long idReclamo;
    private Sesion sesion;
    private Usuario reclamante;
    private String motivo;
    private LocalDate fechaReclamo;
    private EstadoAprobacion estado;
    private String resolucion;

    public Reclamo(Sesion sesion, Usuario reclamante, String motivo) {
        this.idReclamo = ++contador;
        this.sesion = sesion;
        this.reclamante = reclamante;
        this.motivo = motivo;
        this.fechaReclamo = LocalDate.now();
        this.estado = EstadoAprobacion.PENDIENTE;
    }
}
