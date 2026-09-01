import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sesion {
    private static long contador = 0;

    private long idSesion;
    private PerfilTutor tutor;
    private PerfilEstudiante alumno;
    private Curso curso;
    private List<Tema> tema = new ArrayList<>();
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private EstadoSesion estado;
    private boolean checkInTutor;
    private boolean checkInAlumno;
    private boolean checkOutTutor; //todo: revisar si es que se va a a incluir 
    private boolean checkOutAlumno;
    private int puntaje;
    private String comentario;
    private String motivoCancelacion;
    private List<Reclamo> reclamos = new ArrayList<>(); //todo: ver si es que se puede poner más de un reclamo por sesión

    public Sesion(PerfilTutor tutor, PerfilEstudiante alumno, Curso curso, LocalDateTime fechaHoraInicio) {
        this.idSesion = ++contador;
        this.tutor = tutor;
        this.alumno = alumno;
        this.curso = curso;
        this.fechaHoraInicio = fechaHoraInicio;
        this.estado = EstadoSesion.PROGRAMADA;
    }
}
