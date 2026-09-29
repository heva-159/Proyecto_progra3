package pe.edu.pucp.tutorPUCP.modelo.academico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Curso {
    private int idCurso;
    private String codigo;
    private String nombre;
    private boolean activo;
    private List<Tema> temas = new ArrayList<>();

    public Curso() {
        this.activo = true;
    }

    public Curso(String nombre, String codigo) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.activo = true;
    }

    public void agregarTema(Tema tema) {
        tema.setCurso(this);
        temas.add(tema);
    }

    public void quitarTema(Tema tema) {
        temas.remove(tema);
    }

    public int getIdCurso() { return idCurso; }
    public void setIdCurso(int idCurso) { this.idCurso = idCurso; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    // Solo lectura: los temas se agregan con agregarTema().
    public List<Tema> getTemas() { return Collections.unmodifiableList(temas); }
    public void setTemas(List<Tema> temas) {
        this.temas = new ArrayList<>();
        for (Tema tema : temas) agregarTema(tema);
    }
}
