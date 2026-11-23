using TutorPUCP.DTO;

namespace TutorPUCP.Servicios;

// Cliente del REST de Java para el gestor de reclamos (RF015 y RF016).
// Uso:  var (pendientes, error) = await Api.ListarPendientesAsync();
public class ReclamosApi(IHttpClientFactory fabrica) : ClienteRest(fabrica)
{
    // rol: "TUTOR" o "ESTUDIANTE" (el que tuvo el usuario en esa sesión)
    public Task<(ReclamoDto? Valor, string? Error)> RegistrarAsync(int idSesion, int idUsuario, string rol, string motivo) =>
        Enviar<ReclamoDto>(HttpMethod.Post, "reclamo", new RegistrarReclamo(idSesion, idUsuario, rol, motivo));

    public Task<(List<ReclamoDto>? Valor, string? Error)> ListarAsync() =>
        Enviar<List<ReclamoDto>>(HttpMethod.Get, "reclamo");

    public Task<(List<ReclamoDto>? Valor, string? Error)> ListarPendientesAsync() =>
        Enviar<List<ReclamoDto>>(HttpMethod.Get, "reclamo/pendientes");

    public Task<(ReclamoDto? Valor, string? Error)> ObtenerAsync(int idReclamo) =>
        Enviar<ReclamoDto>(HttpMethod.Get, $"reclamo/{idReclamo}");

    public Task<(List<ReclamoDto>? Valor, string? Error)> ListarDeUsuarioAsync(int idUsuario) =>
        Enviar<List<ReclamoDto>>(HttpMethod.Get, $"reclamo/usuario/{idUsuario}");

    // Sesiones en las que participó el usuario, para elegir sobre cuál reclamar.
    public Task<(List<SesionReclamableDto>? Valor, string? Error)> SesionesDeUsuarioAsync(int idUsuario) =>
        Enviar<List<SesionReclamableDto>>(HttpMethod.Get, $"reclamo/usuario/{idUsuario}/sesiones");

    public Task<(ReclamoDto? Valor, string? Error)> ResolverAsync(int idReclamo, int idAdministrador, bool aprobar, string resolucion) =>
        Enviar<ReclamoDto>(HttpMethod.Put, $"reclamo/{idReclamo}/resolver", new ResolverReclamo(idAdministrador, aprobar, resolucion));
}
