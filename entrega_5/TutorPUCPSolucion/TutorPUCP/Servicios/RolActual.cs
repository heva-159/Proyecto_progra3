using Microsoft.AspNetCore.Components.Server.ProtectedBrowserStorage;
using TutorPUCP.DTO;

namespace TutorPUCP.Servicios;

// Rol con el que el usuario navega (Estudiante o Tutor).
// El menú lateral y la barra superior se actualizan cuando cambia.
// RF003: solo una cuenta AMBOS puede cambiar entre Estudiante y Tutor, sin cerrar sesión.
public class RolActual
{
    public const string Estudiante = "Estudiante";
    public const string Tutor = "Tutor";

    // La sesión se guarda cifrada en el navegador (sessionStorage de la pestaña). Así no se pierde al recargar
    // la página, al escribir una dirección o cuando Blazor se reconecta después de cambiar de pestaña.
    private const string ClaveSesion = "tutorpucp-sesion";
    private readonly ProtectedSessionStorage? almacen;

    public RolActual(ProtectedSessionStorage? almacen = null) => this.almacen = almacen;

    public string Rol { get; private set; } = Estudiante;

    public event Action? Cambio;

    private UsuarioDto? usuario;
    private AdministradorDto? administrador;

    // Usuario que inició sesión (lo devuelve el REST); null mientras no haya entrado
    public UsuarioDto? Usuario
    {
        get => usuario;
        set { usuario = value; _ = GuardarAsync(); }
    }

    // Administrador que inició sesión (lo devuelve el REST); null mientras no haya entrado
    public AdministradorDto? Administrador
    {
        get => administrador;
        set { administrador = value; _ = GuardarAsync(); }
    }

    public bool HaySesion => usuario is not null || administrador is not null;

    // Lo que se guarda en el navegador
    public sealed class SesionGuardada
    {
        public UsuarioDto? Usuario { get; set; }
        public AdministradorDto? Administrador { get; set; }
        public string Rol { get; set; } = Estudiante;
    }

    private bool recuperada;

    // Se llama una vez por conexión (MainLayout), cuando la página ya es interactiva: si la pestaña tenía una
    // sesión guardada, la vuelve a cargar.
    public async Task RecuperarAsync()
    {
        if (recuperada) return;
        recuperada = true;
        if (HaySesion || almacen is null) return;
        try
        {
            var guardada = await almacen.GetAsync<SesionGuardada>(ClaveSesion);
            if (guardada.Success && guardada.Value is { } s)
            {
                usuario = s.Usuario;
                administrador = s.Administrador;
                Rol = TieneRol(s.Rol) ? s.Rol : RolInicial;
                Cambio?.Invoke();
            }
        }
        catch
        {
            // sin acceso al almacenamiento del navegador: se pide iniciar sesión de nuevo
        }
    }

    private async Task GuardarAsync()
    {
        if (almacen is null) return;
        try
        {
            if (HaySesion)
                await almacen.SetAsync(ClaveSesion, new SesionGuardada { Usuario = usuario, Administrador = administrador, Rol = Rol });
            else
                await almacen.DeleteAsync(ClaveSesion);
        }
        catch
        {
            // antes de que la página sea interactiva no se puede usar el navegador: no se guarda
        }
    }

    // Roles que tiene la cuenta: ESTUDIANTE, TUTOR o AMBOS (los dos).
    // Sin sesión iniciada se dejan los dos para poder recorrer las pantallas en la demo.
    public string[] RolesDisponibles => Usuario?.Rol switch
    {
        "TUTOR" => [Tutor],
        "ESTUDIANTE" => [Estudiante],
        _ => [Estudiante, Tutor],
    };

    public bool PuedeCambiarDeRol => RolesDisponibles.Length > 1;

    // Los dos roles que puede tener una cuenta, en el orden del selector
    public static readonly string[] TodosLosRoles = [Estudiante, Tutor];

    // RF003: la cuenta activó el rol que le faltaba. Se guarda el usuario que devolvió el REST (ahora AMBOS)
    // y se pasa directo al rol recién activado.
    public void RolActivado(UsuarioDto actualizado, string rol)
    {
        usuario = actualizado;
        if (TieneRol(rol)) Rol = rol;
        _ = GuardarAsync();
        Cambio?.Invoke();
    }

    public bool TieneRol(string rol) => RolesDisponibles.Contains(rol);

    // Rol con el que entra la cuenta: si es AMBOS, como estudiante
    public string RolInicial => TieneRol(Estudiante) ? Estudiante : Tutor;

    // Pantalla de inicio de cada rol
    public static string Inicio(string rol) => rol == Tutor ? "/tutor" : "/estudiante";

    public void Cambiar(string rol)
    {
        if (rol == Rol || !TieneRol(rol)) return;
        Rol = rol;
        _ = GuardarAsync();
        Cambio?.Invoke();
    }

    // Al cerrar sesión se olvida al usuario y se vuelve al rol por defecto
    public void CerrarSesion()
    {
        usuario = null;
        administrador = null;
        Rol = Estudiante;
        _ = GuardarAsync();   // sin sesión: se borra lo guardado en el navegador
        Cambio?.Invoke();
    }

    // Contadores del menú lateral y de la barra superior. Los carga MainLayout desde el REST.
    public int MensajesNoLeidos { get; private set; }
    public int ReclamosPendientes { get; private set; }
    public event Action? ContadoresCambio;

    public void ActualizarContadores(int mensajesNoLeidos, int reclamosPendientes)
    {
        if (mensajesNoLeidos == MensajesNoLeidos && reclamosPendientes == ReclamosPendientes) return;
        MensajesNoLeidos = mensajesNoLeidos;
        ReclamosPendientes = reclamosPendientes;
        ContadoresCambio?.Invoke();
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
