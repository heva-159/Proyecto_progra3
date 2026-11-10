namespace TutorPUCP.DTO
{
    public class UsuarioDto
    {
        public int IdUsuario { get; set; }
        public string Nombres { get; set; } = "";
        public string Apellidos { get; set; } = "";
        public string CodigoPucp { get; set; } = "";
        public string Facultad { get; set; } = "";
        public string Especialidad { get; set; } = "";
        public string CorreoInstitucional { get; set; } = "";
        public string Estado { get; set; } = "";
        public string Rol { get; set; } = "";
        public bool CorreoVerificado { get; set; }
    }
}
