import java.time.LocalDate;

public abstract class Usuario{
    private long usuario_id;
    private String nombreCompleto;
    private String apellidos;
    private String correoInstitucional;
    private String passwordHash;
    private LocalDate fechaRegistro;
    private EstadoCuenta estado;
    private boolean ConsentimientoDatos;
    private boolean ConsentimientoCalificaiones;
    //
    public Usuario(String nombreCompleto, String apellidos, String correoInstitucional, String passwordHash) {
        this.nombreCompleto = nombreCompleto;
        this.apellidos = apellidos;
        this.correoInstitucional = correoInstitucional;
        this.passwordHash = passwordHash;
        this.fechaRegistro = LocalDate.now();
        this.estado = EstadoCuenta.ACTIVA;
        this.ConsentimientoDatos = false;
        this.ConsentimientoCalificaiones = false;
    }
    private long generarIdUsuario() {
        return System.currentTimeMillis();
    }

}