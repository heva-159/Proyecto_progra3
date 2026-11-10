using System.Globalization;

namespace TutorPUCP.Modelos;

// Postulación tal como llega del servicio REST (PostulacionDTO en Java).
// Los datos del tutor, del curso y del administrador que la revisó ya vienen "aplanados".
public class PostulacionRestDTO
{
    public int IdPostulacion { get; set; }
    public int IdTutor { get; set; }
    public string Tutor { get; set; } = "";
    public string CodigoPucp { get; set; } = "";
    public string Correo { get; set; } = "";
    public string Facultad { get; set; } = "";
    public string Especialidad { get; set; } = "";
    public int IdCurso { get; set; }
    public string CodigoCurso { get; set; } = "";
    public string Curso { get; set; } = "";
    public string ArchivoCertificadoUrl { get; set; } = "";
    public string? FechaPostulacion { get; set; }   // "2026-09-20"
    public string Estado { get; set; } = "";        // "PENDIENTE", "APROBADA" o "RECHAZADA"
    public string? FechaResolucion { get; set; }
    public string? Resolucion { get; set; }
    public string? Revisor { get; set; }

    // ---- Solo para mostrar en pantalla ----

    public bool EsPendiente => Estado == "PENDIENTE";

    // "APROBADA" -> "Aprobada"
    public string EstadoTexto => Estado switch
    {
        "APROBADA" => "Aprobada",
        "RECHAZADA" => "Rechazada",
        _ => "Pendiente",
    };

    // "2026-09-20" -> "20/09/2026"
    public string FechaPostulacionTexto => FormatearFecha(FechaPostulacion);
    public string FechaResolucionTexto => FormatearFecha(FechaResolucion);

    // "certificados/20201234_1MAT05.pdf" -> "20201234_1MAT05.pdf"
    public string NombreCertificado => Path.GetFileName(ArchivoCertificadoUrl ?? "");

    private static string FormatearFecha(string? fecha) =>
        DateOnly.TryParseExact(fecha, "yyyy-MM-dd", CultureInfo.InvariantCulture, DateTimeStyles.None, out var dia)
            ? dia.ToString("dd/MM/yyyy")
            : fecha ?? "";
}
