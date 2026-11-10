using System.Globalization;

namespace TutorPUCP.DTO
{
    // Lo que devuelve el REST en /mensaje (mismos nombres que MensajeDTO.java)
    public class MensajeDto
    {
        public int IdMensaje { get; set; }
        public int IdEmisor { get; set; }
        public string NombreEmisor { get; set; } = "";
        public int IdReceptor { get; set; }
        public string NombreReceptor { get; set; } = "";
        public string Contenido { get; set; } = "";
        public string FechaEnvio { get; set; } = "";
        public bool Leido { get; set; }

        public DateTime Enviado => FechaServidor.Leer(FechaEnvio);
    }

    // Una fila de la bandeja (ConversacionDTO.java)
    public class ConversacionDto
    {
        public int IdContacto { get; set; }
        public string NombresContacto { get; set; } = "";
        public string ApellidosContacto { get; set; } = "";
        public string UltimoMensaje { get; set; } = "";
        public string FechaUltimo { get; set; } = "";
        public bool UltimoEsMio { get; set; }
        public int NoLeidos { get; set; }

        public string NombreContacto => $"{NombresContacto} {ApellidosContacto}".Trim();
        public DateTime Ultimo => FechaServidor.Leer(FechaUltimo);
    }

    // Lo que se envía al REST para mandar un mensaje
    public record EnviarMensaje(int IdEmisor, int IdReceptor, string Contenido);

    // Las fechas llegan del REST como texto ISO 8601 ("2026-11-10T10:42:00").
    public static class FechaServidor
    {
        public static DateTime Leer(string? texto) =>
            DateTime.TryParse(texto, CultureInfo.InvariantCulture, DateTimeStyles.None, out var fecha)
                ? fecha
                : DateTime.MinValue;
    }
}
