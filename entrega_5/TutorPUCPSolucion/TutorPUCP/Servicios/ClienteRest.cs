using System.Net;
using TutorPUCP.DTO;

namespace TutorPUCP.Servicios;

// Lo común a todos los clientes del REST de Java: enviar, leer el JSON y traducir los errores.
// Cada gestor (UsuariosApi, MensajesApi, ...) hereda de aquí y solo declara sus rutas.
public abstract class ClienteRest(IHttpClientFactory fabrica)
{
    private readonly HttpClient http = fabrica.CreateClient("api");

    protected async Task<(T? Valor, string? Error)> Enviar<T>(HttpMethod metodo, string ruta, object? cuerpo = null)
    {
        try
        {
            using var peticion = new HttpRequestMessage(metodo, ruta);
            if (cuerpo is not null) peticion.Content = JsonContent.Create(cuerpo);
            using var respuesta = await http.SendAsync(peticion);

            if (respuesta.IsSuccessStatusCode)
            {
                var valor = respuesta.StatusCode == HttpStatusCode.NoContent
                    ? default
                    : await respuesta.Content.ReadFromJsonAsync<T>();
                return (valor, null);
            }
            if (respuesta.StatusCode == HttpStatusCode.Unauthorized) // solo los dos login responden 401
                return (default, "Correo o contraseña incorrectos.");
            return (default, await respuesta.LeerErrorAsync() ?? $"El servidor respondió {(int)respuesta.StatusCode}.");
        }
        catch (HttpRequestException)
        {
            return (default, "No se pudo conectar con el servidor. Revisa que GlassFish esté encendido.");
        }
    }

    protected async Task<string?> EnviarSinValor(HttpMethod metodo, string ruta, object? cuerpo = null) =>
        (await Enviar<object>(metodo, ruta, cuerpo)).Error;
}
