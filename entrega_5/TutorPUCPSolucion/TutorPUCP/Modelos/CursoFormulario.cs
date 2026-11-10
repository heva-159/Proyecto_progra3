namespace TutorPUCP.Modelos;

// Lo que se envía al REST para registrar o modificar un curso (CursoRequest en Java).
public class CursoFormulario
{
    public string Codigo { get; set; } = "";
    public string Nombre { get; set; } = "";
    public List<string> Temas { get; set; } = [];   // temas iniciales: solo se usan al registrar
}
