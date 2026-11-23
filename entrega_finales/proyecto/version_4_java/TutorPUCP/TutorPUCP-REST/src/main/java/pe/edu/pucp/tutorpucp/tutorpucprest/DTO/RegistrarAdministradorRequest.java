package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

public class RegistrarAdministradorRequest {
    private String nombre;
    private String correo;
    private String password;
    private String nivelPermiso;

    public RegistrarAdministradorRequest() {
    }

    // getters y setters

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNivelPermiso() {
        return nivelPermiso;
    }

    public void setNivelPermiso(String nivelPermiso) {
        this.nivelPermiso = nivelPermiso;
    }
}
