using TutorPUCP.DTO;

namespace TutorPUCP.Servicios;

// Rol con el que el usuario navega (Estudiante o Tutor).
// Solo sirve para la parte visual: el menú lateral y la barra superior
// se actualizan cuando cambia.
public class RolActual
{
    public const string Estudiante = "Estudiante";
    public const string Tutor = "Tutor";

    public string Rol { get; private set; } = Estudiante;

    public event Action? Cambio;

    // Usuario que inició sesión (lo devuelve el REST); null mientras no haya entrado
    public UsuarioDto? Usuario { get; set; }
    // Administrador que inició sesión (lo devuelve el REST); null mientras no haya entrado
    public AdministradorDto? Administrador { get; set; }

    public void Cambiar(string rol)
    {
        if (rol == Rol) return;
        Rol = rol;
        Cambio?.Invoke();
    }

    // Nombre de ejemplo que se muestra según el rol.
    public string NombreUsuario => Rol == Tutor ? "Andrés Villanueva" : "Lucía Ramos";

    public string CorreoUsuario => Rol == Tutor ? "andres.villanueva@pucp.edu.pe" : "lucia.ramos@pucp.edu.pe";

    public string CodigoUsuario => Rol == Tutor ? "20191234" : "20225678";
}
