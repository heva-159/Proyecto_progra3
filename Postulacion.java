import java.time.LocalDate;

public class Postulacion {
    private static long contador = 0;

    private long idPostulacion;
    private PerfilTutor tutor;
    private Curso curso;
    private String archivoCertificadoUrl;
    private LocalDate fechaPostulacion;
    private EstadoAprobacion estado;
    private LocalDate fechaResolucion;

    public Postulacion(PerfilTutor tutor, Curso curso, String archivoCertificadoUrl) {
        this.idPostulacion = ++contador;
        this.tutor = tutor;
        this.curso = curso;
        this.archivoCertificadoUrl = archivoCertificadoUrl;
        this.fechaPostulacion = LocalDate.now();
        this.estado = EstadoAprobacion.PENDIENTE;
    }
}
