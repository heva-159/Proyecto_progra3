package Servicio;

import Academico.Curso;
import Academico.Postulacion;

import java.util.ArrayList;
import java.util.List;

public class GestorAcademico {
    private List<Curso> cursos = new ArrayList<>();
    private List<Postulacion> postulaciones = new ArrayList<>();

    public Curso registrarCurso(String nombre, String codigo) {
        Curso curso = new Curso(nombre, codigo);
        cursos.add(curso);
        return curso;
    }

    public List<Curso> getCursos() {
        return cursos;
    }
}
