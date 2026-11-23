namespace TutorPUCP.Modelos;

// Lo que se envía al REST para aprobar o rechazar una postulación (ResolucionRequest en Java).
public class ResolucionFormulario
{
    public bool Aprobar { get; set; }
    public string? Resolucion { get; set; }    // motivo: obligatorio solo al rechazar
    public int IdAdministrador { get; set; }   // quién la revisa
}
