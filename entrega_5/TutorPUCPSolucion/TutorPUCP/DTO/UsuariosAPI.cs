using System.Net;
using TutorPUCP.DTO;

namespace TutorPUCP.Servicios;

// Cliente del REST de Java para el gestor de usuarios: una función por cada función del BL.
// Uso:  var (usuario, error) = await Api.IniciarSesionAsync(correo, clave);
//       if (error is not null) { mensaje = error; return; }
public class UsuariosApi(IHttpClientFactory fabrica)
{
    private readonly HttpClient http = fabrica.CreateClient("api");

    // ---- Usuario ----
    public Task<(UsuarioDto? Valor, string? Error)> RegistrarAsync(RegistroUsuario datos) =>
        Enviar<UsuarioDto>(HttpMethod.Post, "usuario/registrar", datos);
    public Task<(UsuarioDto? Valor, string? Error)> ModificarUsuarioAsync(int id, DatosUsuario datos) =>
        Enviar<UsuarioDto>(HttpMethod.Put, $"usuario/{id}", datos);
    public Task<string?> EliminarUsuarioAsync(int id) => EnviarSinValor(HttpMethod.Delete, $"usuario/{id}");
    public Task<(UsuarioDto? Valor, string? Error)> ObtenerUsuarioAsync(int id) =>
        Enviar<UsuarioDto>(HttpMethod.Get, $"usuario/{id}");
    public Task<(List<UsuarioDto>? Valor, string? Error)> ListarUsuariosAsync() =>
        Enviar<List<UsuarioDto>>(HttpMethod.Get, "usuario");
    public Task<(UsuarioDto? Valor, string? Error)> IniciarSesionAsync(string correo, string password) =>
        Enviar<UsuarioDto>(HttpMethod.Post, "usuario/login", new Credenciales(correo, password));
    public Task<string?> CambiarPasswordAsync(int idUsuario, string passwordActual, string passwordNueva) =>
        EnviarSinValor(HttpMethod.Put, $"usuario/{idUsuario}/password", new CambioPassword(passwordActual, passwordNueva));

    // ---- Perfil tutor ----
    public Task<(PerfilDto? Valor, string? Error)> RegistrarTutorAsync(int idUsuario) =>
        Enviar<PerfilDto>(HttpMethod.Post, $"usuario/{idUsuario}/tutor");
    public Task<(PerfilDto? Valor, string? Error)> ModificarTutorAsync(int idUsuario) =>
        Enviar<PerfilDto>(HttpMethod.Put, $"usuario/{idUsuario}/tutor");
    public Task<string?> EliminarTutorAsync(int idUsuario) => EnviarSinValor(HttpMethod.Delete, $"usuario/{idUsuario}/tutor");
    public Task<(PerfilDto? Valor, string? Error)> ObtenerTutorAsync(int idUsuario) =>
        Enviar<PerfilDto>(HttpMethod.Get, $"usuario/{idUsuario}/tutor");
    public Task<(List<PerfilDto>? Valor, string? Error)> ListarTutoresAsync() =>
        Enviar<List<PerfilDto>>(HttpMethod.Get, "usuario/tutores");

    // ---- Perfil estudiante ----
    public Task<(PerfilDto? Valor, string? Error)> RegistrarEstudianteAsync(int idUsuario) =>
        Enviar<PerfilDto>(HttpMethod.Post, $"usuario/{idUsuario}/estudiante");
    public Task<(PerfilDto? Valor, string? Error)> ModificarEstudianteAsync(int idUsuario) =>
        Enviar<PerfilDto>(HttpMethod.Put, $"usuario/{idUsuario}/estudiante");
    public Task<string?> EliminarEstudianteAsync(int idUsuario) => EnviarSinValor(HttpMethod.Delete, $"usuario/{idUsuario}/estudiante");
    public Task<(PerfilDto? Valor, string? Error)> ObtenerEstudianteAsync(int idUsuario) =>
        Enviar<PerfilDto>(HttpMethod.Get, $"usuario/{idUsuario}/estudiante");
    public Task<(List<PerfilDto>? Valor, string? Error)> ListarEstudiantesAsync() =>
        Enviar<List<PerfilDto>>(HttpMethod.Get, "usuario/estudiantes");

    // ---- Administrador ----
    public Task<(AdministradorDto? Valor, string? Error)> RegistrarAdministradorAsync(RegistroAdministrador datos) =>
        Enviar<AdministradorDto>(HttpMethod.Post, "usuario/administradores", datos);
    public Task<(AdministradorDto? Valor, string? Error)> ModificarAdministradorAsync(int id, RegistroAdministrador datos) =>
        Enviar<AdministradorDto>(HttpMethod.Put, $"usuario/administradores/{id}", datos);
    public Task<string?> EliminarAdministradorAsync(int id) => EnviarSinValor(HttpMethod.Delete, $"usuario/administradores/{id}");
    public Task<(AdministradorDto? Valor, string? Error)> ObtenerAdministradorAsync(int id) =>
        Enviar<AdministradorDto>(HttpMethod.Get, $"usuario/administradores/{id}");
    public Task<(List<AdministradorDto>? Valor, string? Error)> ListarAdministradoresAsync() =>
        Enviar<List<AdministradorDto>>(HttpMethod.Get, "usuario/administradores");
    public Task<(AdministradorDto? Valor, string? Error)> IniciarSesionAdministradorAsync(string correo, string password) =>
        Enviar<AdministradorDto>(HttpMethod.Post, "usuario/administradores/login", new Credenciales(correo, password));
    public Task<string?> CambiarPasswordAdministradorAsync(int id, string passwordActual, string passwordNueva) =>
        EnviarSinValor(HttpMethod.Put, $"usuario/administradores/{id}/password", new CambioPassword(passwordActual, passwordNueva));

    // ---- Lo común: enviar, leer el JSON y traducir los errores ----
    private async Task<(T? Valor, string? Error)> Enviar<T>(HttpMethod metodo, string ruta, object? cuerpo = null)
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

    private async Task<string?> EnviarSinValor(HttpMethod metodo, string ruta, object? cuerpo = null) =>
        (await Enviar<object>(metodo, ruta, cuerpo)).Error;
}

// Lo que se envía al REST (los nombres se convierten a camelCase, como espera Java)
public record Credenciales(string Correo, string Password);
public record RegistroUsuario(string Nombres, string Apellidos, string CodigoPucp, string Facultad,
                              string? Especialidad, string CorreoInstitucional, string Password, string Rol);
public record DatosUsuario(string Nombres, string Apellidos, string CodigoPucp, string Facultad,
                           string? Especialidad, string CorreoInstitucional);
public record RegistroAdministrador(string Nombre, string Correo, string Password, string? NivelPermiso);
public record CambioPassword(string PasswordActual, string PasswordNueva);