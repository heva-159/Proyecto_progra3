package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;

// Lo que viaja en JSON de un curso. No se manda la lista de temas (cada Tema apunta a su Curso
// y se formaría un ciclo infinito al convertirlo a JSON): solo cuántos tiene.
public class CursoDTO {
    private int idCurso;
    private String codigo;
    private String nombre;
    private boolean activo;
    private int cantidadTemas;

    public CursoDTO() {
    }

    public CursoDTO(Curso curso, int cantidadTemas) {
        this.idCurso = curso.getIdCurso();
        this.codigo = curso.getCodigo();
        this.nombre = curso.getNombre();
        this.activo = curso.isActivo();
        this.cantidadTemas = cantidadTemas;
    }

    // getters y setters
    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public int getCantidadTemas() {
        return cantidadTemas;
    }

    public void setCantidadTemas(int cantidadTemas) {
        this.cantidadTemas = cantidadTemas;
    }
}
