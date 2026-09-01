import java.time.LocalDate;
import java.time.LocalDateTime;

public class Usuario {
    private static long contadorUsuarios = 0;

    private long idUsuario;
    private String nombres;
    private String apellidos;
    private String correoInstitucional;
    private String codigoEstudiante;
    private String passwordHash;
    private LocalDate fechaRegistro;
    private boolean verificado;
    private String codigoVerificacion;
    private LocalDateTime codigoExpiracion;
    private PerfilTutor perfilTutor;
    private PerfilEstudiante perfilEstudiante;

    public Usuario(String nombres, String apellidos, String correoInstitucional,
                   String codigoEstudiante, String passwordHash,
                   boolean esTutor, boolean esEstudiante) {
        if (!esTutor && !esEstudiante) {
            throw new IllegalArgumentException("Debe elegir al menos un rol");
        }
        this.idUsuario = ++contadorUsuarios;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correoInstitucional = correoInstitucional;
        this.codigoEstudiante = codigoEstudiante;
        this.passwordHash = passwordHash;
        this.fechaRegistro = LocalDate.now();
        this.verificado = false;
        if (esTutor) this.perfilTutor = new PerfilTutor(this);
        if (esEstudiante) this.perfilEstudiante = new PerfilEstudiante(this);
    }
}
