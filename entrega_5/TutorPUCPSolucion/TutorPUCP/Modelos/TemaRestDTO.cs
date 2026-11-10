namespace TutorPUCP.Modelos;

// Tema tal como llega del servicio REST (TemaDTO en Java).
public class TemaRestDTO
{
    public int IdTema { get; set; }
    public int IdCurso { get; set; }
    public string Nombre { get; set; } = "";
}
