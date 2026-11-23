package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;

import java.time.LocalDate;

public class AdministradorDTO {
    private int idAdministrador;
    private String nombre;
    private String correo;
    private String nivelPermiso;
    private LocalDate fechaCreacion;   // import java.time.LocalDate

    public AdministradorDTO() {
    }

    public AdministradorDTO(Administrador administrador) {
        this.idAdministrador = administrador.getIdAdministrador();
        this.nombre = administrador.getNombre();
        this.correo = administrador.getCorreo();
        this.nivelPermiso = administrador.getNivelPermiso();
        this.fechaCreacion = administrador.getFechaCreacion();
    }
    // getters y setters (sin passwordHash)

    public int getIdAdministrador() {
        return idAdministrador;
    }

    public void setIdAdministrador(int idAdministrador) {
        this.idAdministrador = idAdministrador;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNivelPermiso() {
        return nivelPermiso;
    }

    public void setNivelPermiso(String nivelPermiso) {
        this.nivelPermiso = nivelPermiso;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
