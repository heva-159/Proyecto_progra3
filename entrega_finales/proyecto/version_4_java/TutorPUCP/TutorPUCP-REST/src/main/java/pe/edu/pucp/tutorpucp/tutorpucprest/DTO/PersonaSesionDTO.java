package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

// Un estudiante que se puede elegir al registrar una sesión: solo id y nombre (sin datos de contacto, RNF004).
public class PersonaSesionDTO {
    private int idUsuario;
    private String nombre;

    public PersonaSesionDTO() {
    }

    public PersonaSesionDTO(Usuario usuario) {
        this.idUsuario = usuario.getIdUsuario();
        this.nombre = usuario.getNombres() + " " + usuario.getApellidos();
    }

    // getters y setters
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
