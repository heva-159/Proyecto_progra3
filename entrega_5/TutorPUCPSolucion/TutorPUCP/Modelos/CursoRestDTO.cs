namespace TutorPUCP.Modelos;

// Curso tal como llega del servicio REST (CursoDTO en Java).
// No trae la lista de temas, solo cuántos tiene.
public class CursoRestDTO
{
    public int IdCurso { get; set; }
    public string Codigo { get; set; } = "";
    public string Nombre { get; set; } = "";
    public bool Activo { get; set; }
    public int CantidadTemas { get; set; }
}
