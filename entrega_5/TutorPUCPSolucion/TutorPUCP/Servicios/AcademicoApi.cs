using TutorPUCP.DTO;

namespace TutorPUCP.Servicios;


public class AcademicoApi(IHttpClientFactory fabrica) : ClienteRest(fabrica)
{
    // =========================================================================
    // REQUERIMIENTO RF004: POSTULACIÓN A CURSO CON CERTIFICADO PDF
    // =========================================================================

    // Envía la postulación a un curso con la URL/nombre del archivo PDF
    public Task<(PostulacionDto? Valor, string? Error)> PostularAsync(PostularDto datos) =>
        Enviar<PostulacionDto>(HttpMethod.Post, "academico/postulaciones", datos);

    // Consulta el historial de postulaciones de un tutor
    public Task<(List<PostulacionDto>? Valor, string? Error)> ListarPostulacionesPorTutorAsync(int idTutor) =>
        Enviar<List<PostulacionDto>>(HttpMethod.Get, $"academico/postulaciones/tutor/{idTutor}");

    // =========================================================================
    // REQUERIMIENTO RF008: GESTIÓN DE TARIFAS POR HORA
    // =========================================================================

    public Task<(List<TarifaDto>? Valor, string? Error)> ListarTarifasPorTutorAsync(int idTutor) =>
        Enviar<List<TarifaDto>>(HttpMethod.Get, $"academico/tarifas/tutor/{idTutor}");

    public Task<(TarifaDto? Valor, string? Error)> GuardarTarifaAsync(GuardarTarifaDto datos) =>
        Enviar<TarifaDto>(HttpMethod.Post, "academico/tarifas", datos);

    // =========================================================================
    // REQUERIMIENTO RF009: BÚSQUEDA Y FILTRADO DE TUTORES POR CURSO
    // =========================================================================

    public Task<(List<CursoDto>? Valor, string? Error)> ListarCursosAsync() =>
        Enviar<List<CursoDto>>(HttpMethod.Get, "academico/cursos");

    public Task<(List<TutorBusquedaDto>? Valor, string? Error)> BuscarTutoresAsync(int? idCurso = null) =>
        Enviar<List<TutorBusquedaDto>>(HttpMethod.Get, idCurso.HasValue && idCurso.Value > 0
            ? $"academico/tutores/buscar?idCurso={idCurso.Value}"
            : "academico/tutores/buscar");
}



// RF004
public record PostulacionDto(
    int IdPostulacion,
    int IdCurso,
    string CursoCodigo,
    string CursoNombre,
    string ArchivoCertificadoUrl,
    string Estado,
    string? FechaPostulacion,
    string? FechaResolucion,
    string? Resolucion);

public record PostularDto(
    int IdTutor,
    int IdCurso,
    string ArchivoCertificadoUrl,
    string? Comentarios);

// RF008
public record TarifaDto(int IdTarifa, int IdCurso, string CursoCodigo, string CursoNombre, decimal MontoPorHora);
public record GuardarTarifaDto(int IdTutor, int IdCurso, decimal MontoPorHora);

// RF009
public record CursoDto(int IdCurso, string Codigo, string Nombre);
public record TutorBusquedaDto(
    int IdTutor,
    string Nombre,
    string Especialidad,
    double Calificacion,
    int SesionesDictadas,
    int IdCurso,
    string CursoCodigo,
    string CursoNombre,
    decimal TarifaPorHora);
