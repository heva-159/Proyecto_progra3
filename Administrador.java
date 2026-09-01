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
}
