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

    // ---- Lo que se muestra en pantalla ----
    public string PrimerNombre => Palabras(Usuario?.Nombres ?? NombreUsuario, 1);
    // "Ana Lucia" + "Quispe Huaman" -> "Ana Quispe": cabe en el menú lateral y las iniciales salen AQ
    public string NombreCorto => Usuario is { } u ? $"{Palabras(u.Nombres, 1)} {Palabras(u.Apellidos, 1)}" : NombreUsuario;
    public string CorreoMostrado => Usuario?.CorreoInstitucional ?? CorreoUsuario;
    public string NombreAdministrador => Administrador?.Nombre ?? "Administrador";
    public string CorreoAdministrador => Administrador?.Correo ?? "admin@pucp.edu.pe";
    public string NombreCortoAdministrador => Palabras(NombreAdministrador, 2);

    private static string Palabras(string texto, int cuantas) =>
        string.Join(' ', texto.Split(' ', StringSplitOptions.RemoveEmptyEntries).Take(cuantas));
}
