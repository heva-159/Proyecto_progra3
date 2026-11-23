package pe.edu.pucp.tutorPUCP.modelo.academico;

public class Tema {
    private int idTema;
    private Curso curso;
    private String nombre;

    public Tema() {
    }

    public Tema(String nombre) {
        this.nombre = nombre;
    }

    public int getIdTema() { return idTema; }
    public void setIdTema(int idTema) { this.idTema = idTema; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
