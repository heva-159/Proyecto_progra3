import java.time.LocalDate;

public class Administrador implements Autenticable {
    private static long contador = 0;

    private long idAdministrador;
    private String nombre;
    private String correo;
    private String passwordHash;
    private LocalDate fechaCreacion;

    public Administrador(String nombre, String correo, String passwordHash) {
        this.idAdministrador = ++contador;
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.fechaCreacion = LocalDate.now();
    }

    @Override
    public boolean iniciarSesion(String correo, String password) {
        return this.correo.equalsIgnoreCase(correo) && passwordHash.equals(password);
    }

    public long getIdAdministrador() { return idAdministrador; }
    public void setIdAdministrador(long idAdministrador) { this.idAdministrador = idAdministrador; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
