package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;


public class CursoDTO {
    private int idCurso;
    private String codigo;
    private String nombre;

    public CursoDTO() {}

    public CursoDTO(Curso curso) {
        if (curso != null) {
            this.idCurso = curso.getIdCurso();
            this.codigo = curso.getCodigo();
            this.nombre = curso.getNombre();
        }
    }

    public int getIdCurso() { return idCurso; }
    public void setIdCurso(int idCurso) { this.idCurso = idCurso; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
