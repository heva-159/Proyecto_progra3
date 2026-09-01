import java.time.LocalDate;

public class Usuario implements Autenticable {
    private long idUsuario;
    private String nombreCompleto;
    private String apellidos;
    private String correoInstitucional;
    private String passwordHash;
    private LocalDate fechaRegistro;
    private EstadoCuenta estado;
    private boolean consentimientoDatos;
    private boolean consentimientoCalificaciones;
    private PerfilTutor perfilTutor;
    private PerfilEstudiante perfilEstudiante;

    public Usuario(String nombreCompleto, String apellidos, String correoInstitucional, String passwordHash,
                   boolean esTutor, boolean esEstudiante) {
        if (!esTutor && !esEstudiante) {
            throw new IllegalArgumentException("Debe elegir al menos un rol");
        }
        this.idUsuario = generarIdUsuario();
        this.nombreCompleto = nombreCompleto;
        this.apellidos = apellidos;
        this.correoInstitucional = correoInstitucional;
        this.passwordHash = passwordHash;
        this.fechaRegistro = LocalDate.now();
        this.estado = EstadoCuenta.ACTIVA;
        this.consentimientoDatos = false;
        this.consentimientoCalificaciones = false;
        if (esTutor) this.perfilTutor = new PerfilTutor(this);
        if (esEstudiante) this.perfilEstudiante = new PerfilEstudiante(this);
    }

    private long generarIdUsuario() {
        return System.currentTimeMillis();
    }

    @Override
    public boolean iniciarSesion(String correo, String password) {
        return estado == EstadoCuenta.ACTIVA
                && correoInstitucional.equalsIgnoreCase(correo)
                && passwordHash.equals(password); //faltaría implementar el hash de la contraseña
    }
}
