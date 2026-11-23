namespace TutorPUCP.DTO
{
    public class PerfilDto
    {
        public int IdUsuario { get; set; }
        public string Nombres { get; set; } = "";
        public string Apellidos { get; set; } = "";
        public string Facultad { get; set; } = "";
        public string Especialidad { get; set; } = "";
    }
}