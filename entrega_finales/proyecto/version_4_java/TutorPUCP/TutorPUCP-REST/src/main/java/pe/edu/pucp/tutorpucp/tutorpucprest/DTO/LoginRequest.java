package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

public class LoginRequest {
    private String correo;
    private String password;

    // getters y setters

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}