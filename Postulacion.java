import java.time.LocalDate;

public class Postulacion implements Aprobable {
    private static long contador = 0;

    private long idPostulacion;
    private PerfilTutor tutor;
    private Curso curso;
    private String archivoCertificadoUrl;
    private LocalDate fechaPostulacion;
    private EstadoAprobacion estado;
    private LocalDate fechaResolucion;
    private String resolucion;

    public Postulacion(PerfilTutor tutor, Curso curso, String archivoCertificadoUrl) {
        this.idPostulacion = ++contador;
        this.tutor = tutor;
        this.curso = curso;
        this.archivoCertificadoUrl = archivoCertificadoUrl;
        this.fechaPostulacion = LocalDate.now();
        this.estado = EstadoAprobacion.PENDIENTE;
    }

    @Override
    public void aprobar() {
        this.estado = EstadoAprobacion.APROBADA;
        this.fechaResolucion = LocalDate.now();
    }

    @Override
    public void rechazar(String motivo) {
        this.estado = EstadoAprobacion.RECHAZADA;
        this.fechaResolucion = LocalDate.now();
        this.resolucion = motivo;
    }

    @Override
    public EstadoAprobacion getEstado() {
        return estado;
    }
}
