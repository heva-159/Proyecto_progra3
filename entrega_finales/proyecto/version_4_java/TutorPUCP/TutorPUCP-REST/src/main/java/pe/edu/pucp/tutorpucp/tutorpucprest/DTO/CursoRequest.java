package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import java.util.List;

// Lo que llega al registrar (código, nombre y temas iniciales) o modificar (código y nombre) un curso.
public class CursoRequest {
    private String codigo;
    private String nombre;
    private List<String> temas;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<String> getTemas() {
        return temas;
    }

    public void setTemas(List<String> temas) {
        this.temas = temas;
    }
}
