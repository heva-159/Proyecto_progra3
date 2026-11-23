using System.Net.Http.Json;
using System.Text.Json;
using TutorPUCP.Modelos;

namespace TutorPUCP.Servicios;

// Cliente del servicio REST de sesiones (RF011, RF012, RF013, RF014).
// Llama a GestorSesionesRecurso en GlassFish: {ApiRest:BaseUrl}sesion/...
// Mismo estilo que AcademicoRestService (y que PlatoRestService del ejemplo del profesor).
public class SesionRestService(HttpClient http)
{
    // Si el servicio responde con error, se lanza el mensaje que manda Java en {"error": "..."}.
    private static async Task Verificar(HttpResponseMessage r)
    {
        if (r.IsSuccessStatusCode) return;
        string mensaje = $"El servicio devolvió HTTP {(int)r.StatusCode}.";
        try
        {
            using var doc = JsonDocument.Parse(await r.Content.ReadAsStringAsync());
            if (doc.RootElement.ValueKind == JsonValueKind.Object
                && doc.RootElement.TryGetProperty("error", out var valor))
            {
                mensaje = valor.GetString() ?? mensaje;
            }
        }
        catch (JsonException) { }
        throw new InvalidOperationException(mensaje);
    }

    // Verifica la respuesta y convierte el JSON al DTO.
    private static async Task<T> Leer<T>(HttpResponseMessage r)
    {
        await Verificar(r);
        var resultado = await r.Content.ReadFromJsonAsync<T>();
        if (resultado == null)
        {
            throw new InvalidOperationException("El servicio devolvió una respuesta vacía");
        }
        return resultado;
    }

    // ======================== RF011: registrar sesión ========================

    // Cursos que el tutor puede dictar (con temas y tarifa) y estudiantes.
    public async Task<OpcionesSesionRestDTO> OpcionesAsync(int idTutor)
    {
        using var r = await http.GetAsync($"sesion/opciones?idTutor={idTutor}");
        return await Leer<OpcionesSesionRestDTO>(r);
    }

    public async Task<SesionRestDTO> RegistrarAsync(RegistrarSesionFormulario f)
    {
        using var r = await http.PostAsJsonAsync("sesion", f);
        return await Leer<SesionRestDTO>(r);
    }

    public async Task<SesionRestDTO> ObtenerAsync(int idSesion)
    {
        using var r = await http.GetAsync($"sesion/{idSesion}");
        return await Leer<SesionRestDTO>(r);
    }

    // "Mis sesiones": las sesiones en las que participa la persona (como tutor o como estudiante),
    // de la más reciente a la más antigua. Es una lista resumida: no trae comentario ni motivo de cancelación
    // (eso está en ObtenerAsync).
    public async Task<List<SesionRestDTO>> ListarDeUsuarioAsync(int idUsuario)
    {
        using var r = await http.GetAsync($"sesion/usuario/{idUsuario}");
        return await Leer<List<SesionRestDTO>>(r);
    }

    // ===================== RF012: check-in y check-out =====================

    // rol: "TUTOR" o "ESTUDIANTE". Devuelve la sesión actualizada.
    public async Task<SesionRestDTO> CheckInAsync(int idSesion, string rol)
    {
        using var r = await http.PutAsJsonAsync($"sesion/{idSesion}/checkin", new AsistenciaFormulario { Rol = rol });
        return await Leer<SesionRestDTO>(r);
    }

    public async Task<SesionRestDTO> CheckOutAsync(int idSesion, string rol)
    {
        using var r = await http.PutAsJsonAsync($"sesion/{idSesion}/checkout", new AsistenciaFormulario { Rol = rol });
        return await Leer<SesionRestDTO>(r);
    }

    // ========================= RF013: cancelar sesión =========================

    public async Task<SesionRestDTO> CancelarAsync(int idSesion, CancelarSesionFormulario f)
    {
        using var r = await http.PutAsJsonAsync($"sesion/{idSesion}/cancelacion", f);
        return await Leer<SesionRestDTO>(r);
    }

    // ======================== RF014: calificar al tutor ========================

    // Solo el estudiante de una sesión Realizada, una sola vez. Devuelve la sesión con su puntaje.
    public async Task<SesionRestDTO> CalificarAsync(int idSesion, CalificarSesionFormulario f)
    {
        using var r = await http.PutAsJsonAsync($"sesion/{idSesion}/calificacion", f);
        return await Leer<SesionRestDTO>(r);
    }

    // ======================== RF017 / RF018: reportes en PDF ========================
    // Igual que DescargarAsync de PlatoRestService (ejemplo del profesor): Java arma el PDF con JasperReports.

    // RF017: tutorías del tutor entre desde y hasta.
    public Task<byte[]> DescargarReporteTutorAsync(int idUsuario, DateOnly desde, DateOnly hasta, CancellationToken ct)
        => DescargarPdfAsync($"sesion/reportes/tutor/{idUsuario}?{Rango(desde, hasta)}", ct);

    // RF018: sesiones de la plataforma entre desde y hasta.
    public Task<byte[]> DescargarReporteSesionesAsync(DateOnly desde, DateOnly hasta, CancellationToken ct)
        => DescargarPdfAsync($"sesion/reportes?{Rango(desde, hasta)}", ct);

    // RF018: reclamos registrados entre desde y hasta.
    public Task<byte[]> DescargarReporteReclamosAsync(DateOnly desde, DateOnly hasta, CancellationToken ct)
        => DescargarPdfAsync($"reclamo/reportes?{Rango(desde, hasta)}", ct);

    private static string Rango(DateOnly desde, DateOnly hasta)
        => $"desde={desde:yyyy-MM-dd}&hasta={hasta:yyyy-MM-dd}";

    private async Task<byte[]> DescargarPdfAsync(string ruta, CancellationToken ct)
    {
        using var respuesta = await http.GetAsync(ruta, ct);
        respuesta.EnsureSuccessStatusCode();
        if (respuesta.Content.Headers.ContentType?.MediaType != "application/pdf")
            throw new InvalidDataException("El backend no devolvió un PDF.");
        return await respuesta.Content.ReadAsByteArrayAsync(ct);
    }
}
