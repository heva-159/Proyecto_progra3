package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import java.util.ArrayList;
import java.util.List;

// Lo que necesita el formulario de RF011 en una sola llamada:
// los cursos que el tutor puede dictar y los estudiantes a los que puede agendar.
public class OpcionesSesionDTO {
    private List<CursoSesionDTO> cursos = new ArrayList<>();
    private List<PersonaSesionDTO> estudiantes = new ArrayList<>();

    public OpcionesSesionDTO() {
    }

    public OpcionesSesionDTO(List<CursoSesionDTO> cursos, List<PersonaSesionDTO> estudiantes) {
        this.cursos = cursos;
        this.estudiantes = estudiantes;
    }

    // getters y setters
    public List<CursoSesionDTO> getCursos() {
        return cursos;
    }

    public void setCursos(List<CursoSesionDTO> cursos) {
        this.cursos = cursos;
    }

    public List<PersonaSesionDTO> getEstudiantes() {
        return estudiantes;
    }

    public void setEstudiantes(List<PersonaSesionDTO> estudiantes) {
        this.estudiantes = estudiantes;
    }
}
