namespace TutorPUCP.Modelos;

// Lo que se envía al REST para registrar una sesión (RegistrarSesionRequest en Java).
public class RegistrarSesionFormulario
{
    public int IdTutor { get; set; }
    public int IdEstudiante { get; set; }
    public int IdCurso { get; set; }
    public List<int> IdsTemas { get; set; } = [];
    public string FechaHoraInicio { get; set; } = "";   // "2026-11-12T16:00:00"
}

// Lo que se envía al marcar check-in o check-out (AsistenciaRequest en Java).
public class AsistenciaFormulario
{
    public string Rol { get; set; } = "";   // TUTOR o ESTUDIANTE
}

// Lo que se envía al cancelar una sesión (CancelarSesionRequest en Java).
public class CancelarSesionFormulario
{
    public int IdUsuario { get; set; }      // quién cancela: el tutor o el estudiante de la sesión
    public string Motivo { get; set; } = "";
}

// Lo que se envía al calificar al tutor (CalificarSesionRequest en Java, RF014).
public class CalificarSesionFormulario
{
    public int IdUsuario { get; set; }      // quién califica: el estudiante de la sesión
    public int Puntaje { get; set; }        // de 1 a 5
    public string? Comentario { get; set; } // opcional
}
