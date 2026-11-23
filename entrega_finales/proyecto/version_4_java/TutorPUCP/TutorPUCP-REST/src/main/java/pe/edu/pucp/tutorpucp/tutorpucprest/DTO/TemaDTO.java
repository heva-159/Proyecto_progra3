package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import pe.edu.pucp.tutorPUCP.modelo.academico.Tema;

// Lo que viaja en JSON de un tema: del curso solo va su id (para no formar un ciclo Tema -> Curso -> temas).
public class TemaDTO {
    private int idTema;
    private int idCurso;
    private String nombre;

    public TemaDTO() {
    }

    public TemaDTO(Tema tema) {
        this.idTema = tema.getIdTema();
        this.idCurso = tema.getCurso() != null ? tema.getCurso().getIdCurso() : 0;
        this.nombre = tema.getNombre();
    }

    // getters y setters
    public int getIdTema() {
        return idTema;
    }

    public void setIdTema(int idTema) {
        this.idTema = idTema;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
