using System.Globalization;

namespace TutorPUCP.Modelos;

// Sesión tal como llega del servicio REST (SesionDTO en Java).
// Tutor, estudiante y curso ya vienen "aplanados"; sin correos ni teléfonos (RNF004).
public class SesionRestDTO
{
    public int IdSesion { get; set; }
    public int IdTutor { get; set; }
    public string Tutor { get; set; } = "";
    public int IdEstudiante { get; set; }
    public string Estudiante { get; set; } = "";
    public int IdCurso { get; set; }
    public string CodigoCurso { get; set; } = "";
    public string Curso { get; set; } = "";
    public List<string> Temas { get; set; } = [];
    public string? FechaHoraInicio { get; set; }   // "2026-11-12T16:00"
    public string? FechaHoraFin { get; set; }
    public string Estado { get; set; } = "";       // PROGRAMADA, EN_CURSO, REALIZADA, CANCELADA o CANCELADA_INASISTENCIA
    public bool CheckInTutor { get; set; }
    public bool CheckInEstudiante { get; set; }
    public bool CheckOutTutor { get; set; }
    public bool CheckOutEstudiante { get; set; }
    public int? Puntaje { get; set; }
    public string? Comentario { get; set; }
    public string? MotivoCancelacion { get; set; }
    public string? Cancelo { get; set; }
    public decimal? Tarifa { get; set; }

    // ---- Solo para mostrar en pantalla ----

    // "EN_CURSO" -> "En curso"
    public string EstadoTexto => Estado switch
    {
        "EN_CURSO" => "En curso",
        "REALIZADA" => "Realizada",
        "CANCELADA" => "Cancelada",
        "CANCELADA_INASISTENCIA" => "Cancelada por inasistencia",
        _ => "Programada",
    };

    public DateTime? Inicio => LeerFecha(FechaHoraInicio);

    public string TemasTexto => Temas.Count == 0 ? "Sin temas" : string.Join(", ", Temas);

    public string TarifaTexto => Tarifa is decimal t ? $"S/ {t:0.00} por hora" : "Sin tarifa registrada";

    private static DateTime? LeerFecha(string? texto) =>
        DateTime.TryParse(texto, CultureInfo.InvariantCulture, DateTimeStyles.None, out var fecha) ? fecha : null;
}
