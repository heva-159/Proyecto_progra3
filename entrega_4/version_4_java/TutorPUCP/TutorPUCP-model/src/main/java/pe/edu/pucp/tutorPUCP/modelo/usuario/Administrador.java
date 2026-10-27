package pe.edu.pucp.tutorPUCP.modelo.usuario;

import pe.edu.pucp.tutorPUCP.modelo.interfaces.IAutenticable;

import java.time.LocalDate;

public class Administrador implements IAutenticable {
    private int idAdministrador;
    private String nombre;
    private String correo;
    private String passwordHash;
    private LocalDate fechaCreacion;
    private String nivelPermiso;

    public Administrador() {
    }

    public Administrador(String nombre, String correo, String passwordHash, String nivelPermiso) {
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.nivelPermiso = nivelPermiso;
        this.fechaCreacion = LocalDate.now();
    }

    @Override
    public boolean iniciarSesion(String correo, String password) {
        return this.correo.equalsIgnoreCase(correo) && passwordHash.equals(password); //faltaría implementar el hash de la contraseña
    }

    public int getIdAdministrador() { return idAdministrador; }
    public void setIdAdministrador(int idAdministrador) { this.idAdministrador = idAdministrador; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getNivelPermiso() { return nivelPermiso; }
    public void setNivelPermiso(String nivelPermiso) { this.nivelPermiso = nivelPermiso; }
}
