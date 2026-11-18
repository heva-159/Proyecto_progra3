package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

// RF001: verificar el correo con el codigo (para reenviar el codigo solo se usa el correo)
public class VerificarCorreoRequest {
    private String correo;
    private String codigo;

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
