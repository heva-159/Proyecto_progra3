package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

public class VerificarCorreoRequest {
    private String correo;
    private String codigo;

    public VerificarCorreoRequest() {
    }

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
