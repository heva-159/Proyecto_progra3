using System.Net.Http.Json;
using System.Text.Json;
using TutorPUCP.Modelos;

namespace TutorPUCP.Servicios;

// Cliente del servicio REST del administrador (RF005, RF006, RF007).
// Llama a GestorAcademicoRecurso en GlassFish: {ApiRest:BaseUrl}academico/...
// Mismo estilo que PlatoRestService del ejemplo del profesor.
public class AcademicoRestService(HttpClient http)
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

    // Texto para mostrar en pantalla cuando algo falla.
    public static string MensajeDeError(Exception ex) =>
        ex is InvalidOperationException
            ? ex.Message
            : "No se pudo conectar con el servicio REST. Revisa que GlassFish esté corriendo.";

    // ======================== RF005: postulaciones ========================

    // estado: null (todas), "PENDIENTE", "APROBADA" o "RECHAZADA"
    public async Task<List<PostulacionRestDTO>> ListarPostulacionesAsync(string? estado = null)
    {
        string ruta = "academico/postulaciones";
        if (!string.IsNullOrWhiteSpace(estado)) ruta += "?estado=" + Uri.EscapeDataString(estado);
        using var r = await http.GetAsync(ruta);
        return await Leer<List<PostulacionRestDTO>>(r);
    }

    public async Task<PostulacionRestDTO> ObtenerPostulacionAsync(int idPostulacion)
    {
        using var r = await http.GetAsync($"academico/postulaciones/{idPostulacion}");
        return await Leer<PostulacionRestDTO>(r);
    }

    public async Task<PostulacionRestDTO> ResolverPostulacionAsync(int idPostulacion, ResolucionFormulario f)
    {
        using var r = await http.PutAsJsonAsync($"academico/postulaciones/{idPostulacion}/resolucion", f);
        return await Leer<PostulacionRestDTO>(r);
    }

    // ============================ RF006: cursos ============================

    public async Task<List<CursoRestDTO>> ListarCursosAsync()
    {
        using var r = await http.GetAsync("academico/cursos");
        return await Leer<List<CursoRestDTO>>(r);
    }

    public async Task<CursoRestDTO> RegistrarCursoAsync(CursoFormulario f)
    {
        using var r = await http.PostAsJsonAsync("academico/cursos", f);
        return await Leer<CursoRestDTO>(r);
    }

    public async Task<CursoRestDTO> ModificarCursoAsync(int idCurso, CursoFormulario f)
    {
        using var r = await http.PutAsJsonAsync($"academico/cursos/{idCurso}", f);
        return await Leer<CursoRestDTO>(r);
    }

    // Desactivar es eliminación lógica: por eso es PUT y no DELETE.
    public async Task DesactivarCursoAsync(int idCurso)
    {
        using var r = await http.PutAsync($"academico/cursos/{idCurso}/desactivar", null);
        await Verificar(r);
    }

    public async Task ReactivarCursoAsync(int idCurso)
    {
        using var r = await http.PutAsync($"academico/cursos/{idCurso}/reactivar", null);
        await Verificar(r);
    }

    // ============================ RF007: temas ============================

    public async Task<List<TemaRestDTO>> ListarTemasAsync(int idCurso)
    {
        using var r = await http.GetAsync($"academico/cursos/{idCurso}/temas");
        return await Leer<List<TemaRestDTO>>(r);
    }

    public async Task<TemaRestDTO> RegistrarTemaAsync(int idCurso, TemaFormulario f)
    {
        using var r = await http.PostAsJsonAsync($"academico/cursos/{idCurso}/temas", f);
        return await Leer<TemaRestDTO>(r);
    }

    public async Task<TemaRestDTO> ModificarTemaAsync(int idTema, TemaFormulario f)
    {
        using var r = await http.PutAsJsonAsync($"academico/temas/{idTema}", f);
        return await Leer<TemaRestDTO>(r);
    }

    // Eliminación física. Si el tema ya se usó en una sesión, el REST responde 400 con el motivo.
    public async Task EliminarTemaAsync(int idTema)
    {
        using var r = await http.DeleteAsync($"academico/temas/{idTema}");
        await Verificar(r);
    }
}
