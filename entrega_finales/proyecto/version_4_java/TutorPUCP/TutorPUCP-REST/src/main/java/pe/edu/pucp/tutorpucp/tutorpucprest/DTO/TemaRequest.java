package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

// Lo que llega al registrar o modificar un tema.
public class TemaRequest {
    private String nombre;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
