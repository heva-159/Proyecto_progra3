package Academico;

import java.util.ArrayList;
import java.util.List;

public class Curso {
    private static long contador = 0;

    private long idCurso;
    private String codigo;
    private String nombre;
    private boolean activo;
    private List<Tema> temas = new ArrayList<>();

    public Curso(String nombre, String codigo) {
        this.idCurso = ++contador;
        this.nombre = nombre;
        this.codigo = codigo;
        this.activo = true;
    }

    public long getIdCurso() { return idCurso; }
    public void setIdCurso(long idCurso) { this.idCurso = idCurso; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public List<Tema> getTemas() { return temas; }
    public void setTemas(List<Tema> temas) { this.temas = temas; }
}
