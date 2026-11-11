namespace TutorPUCP.Modelos;

// Datos para el formulario de registrar sesión (OpcionesSesionDTO en Java):
// los cursos que el tutor puede dictar y los estudiantes a los que puede agendar.
public class OpcionesSesionRestDTO
{
    public List<CursoSesionRestDTO> Cursos { get; set; } = [];
    public List<PersonaSesionRestDTO> Estudiantes { get; set; } = [];
}

// Curso con postulación aprobada del tutor, con sus temas y la tarifa (null si no registró tarifa).
public class CursoSesionRestDTO
{
    public int IdCurso { get; set; }
    public string Codigo { get; set; } = "";
    public string Nombre { get; set; } = "";
    public decimal? Tarifa { get; set; }
    public List<TemaRestDTO> Temas { get; set; } = [];
}

// Estudiante que se puede elegir: solo id y nombre.
public class PersonaSesionRestDTO
{
    public int IdUsuario { get; set; }
    public string Nombre { get; set; } = "";
}
