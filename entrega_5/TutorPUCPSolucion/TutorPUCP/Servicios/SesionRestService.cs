using System.Net.Http.Json;
using System.Text.Json;
using TutorPUCP.Modelos;

namespace TutorPUCP.Servicios;

// Cliente del servicio REST de sesiones (RF011, RF012, RF013).
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
}
