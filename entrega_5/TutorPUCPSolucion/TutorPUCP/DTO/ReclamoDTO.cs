namespace TutorPUCP.DTO
{
    // Lo que devuelve el REST en /reclamo (mismos nombres que ReclamoDTO.java)
    public class ReclamoDto
    {
        public int IdReclamo { get; set; }
        public int IdSesion { get; set; }
        public string? Curso { get; set; }
        public string? FechaSesion { get; set; }
        public string? NombreTutor { get; set; }
        public string? NombreEstudiante { get; set; }
        public int IdReclamante { get; set; }
        public string? NombreReclamante { get; set; }
        public string RolReclamante { get; set; } = "";   // TUTOR o ESTUDIANTE
        public string Motivo { get; set; } = "";
        public string? FechaReclamo { get; set; }
        public string Estado { get; set; } = "";          // PENDIENTE, APROBADA o RECHAZADA
        public string? Resolucion { get; set; }
        public string? NombreAdministrador { get; set; }
        public string? FechaResolucion { get; set; }

        public DateTime Sesion => FechaServidor.Leer(FechaSesion);
        public DateTime Registrado => FechaServidor.Leer(FechaReclamo);
        public DateTime Resuelto => FechaServidor.Leer(FechaResolucion);

        public bool EsTutor => RolReclamante == "TUTOR";
        public string Rol => EsTutor ? "Tutor" : "Estudiante";
        // La otra persona de la sesión, vista desde quien reclama
        public string? Reclamado => EsTutor ? NombreEstudiante : NombreTutor;
        public string EstadoTexto => TextoEstado.Reclamo(Estado);
    }

    // Una sesión en la que participó el usuario (SesionReclamableDTO.java)
    public class SesionReclamableDto
    {
        public int IdSesion { get; set; }
        public string? Curso { get; set; }
        public string? FechaHoraInicio { get; set; }
        public string Estado { get; set; } = "";
        public string RolUsuario { get; set; } = "";      // TUTOR o ESTUDIANTE
        public string? NombreContraparte { get; set; }
        public bool YaReclamada { get; set; }

        public DateTime Inicio => FechaServidor.Leer(FechaHoraInicio);
        public string Rol => RolUsuario == "TUTOR" ? "Tutor" : "Estudiante";
        public string EstadoTexto => TextoEstado.Sesion(Estado);
    }

    // Lo que se envía al REST
    public record RegistrarReclamo(int IdSesion, int IdUsuario, string Rol, string Motivo);
    public record ResolverReclamo(int IdAdministrador, bool Aprobar, string Resolucion);

    // Los enums de Java en texto para la pantalla
    public static class TextoEstado
    {
        public static string Reclamo(string estado) => estado switch
        {
            "APROBADA" => "Aprobado",
            "RECHAZADA" => "Rechazado",
            _ => "Pendiente",
        };

        public static string Sesion(string estado) => estado switch
        {
            "PROGRAMADA" => "Programada",
            "EN_CURSO" => "En curso",
            "REALIZADA" => "Realizada",
            "CANCELADA" => "Cancelada",
            "CANCELADA_INASISTENCIA" => "Cancelada por inasistencia",
            _ => estado,
        };
    }
}
