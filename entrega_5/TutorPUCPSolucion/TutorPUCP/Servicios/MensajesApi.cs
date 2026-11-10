using TutorPUCP.DTO;

namespace TutorPUCP.Servicios;

// Cliente del REST de Java para el gestor de mensajes (RF010).
// Uso:  var (bandeja, error) = await Api.BandejaAsync(idUsuario);
public class MensajesApi(IHttpClientFactory fabrica) : ClienteRest(fabrica)
{
    public Task<(MensajeDto? Valor, string? Error)> EnviarAsync(int idEmisor, int idReceptor, string contenido) =>
        Enviar<MensajeDto>(HttpMethod.Post, "mensaje", new EnviarMensaje(idEmisor, idReceptor, contenido));

    public Task<(List<ConversacionDto>? Valor, string? Error)> BandejaAsync(int idUsuario) =>
        Enviar<List<ConversacionDto>>(HttpMethod.Get, $"mensaje/usuario/{idUsuario}/bandeja");

    public Task<(List<MensajeDto>? Valor, string? Error)> ConversacionAsync(int idUsuario1, int idUsuario2) =>
        Enviar<List<MensajeDto>>(HttpMethod.Get, $"mensaje/conversacion?usuario1={idUsuario1}&usuario2={idUsuario2}");

    public Task<string?> MarcarLeidoAsync(int idMensaje, int idLector) =>
        EnviarSinValor(HttpMethod.Put, $"mensaje/{idMensaje}/leido?lector={idLector}");

    // Al abrir la conversación: todo lo que "otro" le mandó a "lector" queda leído.
    public Task<string?> MarcarConversacionLeidaAsync(int idLector, int idOtro) =>
        EnviarSinValor(HttpMethod.Put, $"mensaje/conversacion/leida?lector={idLector}&otro={idOtro}");
}
