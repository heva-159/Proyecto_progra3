namespace TutorPUCP.DTO
{
    public class AdministradorDto
    {
        public int IdAdministrador { get; set; }
        public string Nombre { get; set; } = "";
        public string Correo { get; set; } = "";
        public string NivelPermiso { get; set; } = "";
        public DateOnly? FechaCreacion { get; set; }
    }
}
