using System.Text.RegularExpressions;

namespace TutorPUCP.Servicios;

// RNF: la coordinación se hace solo por la mensajería interna.
// Detecta si un texto trae un correo o un número de teléfono para avisar al usuario.
public static partial class DatosContacto
{
    [GeneratedRegex(@"[\w.+-]+@[\w-]+\.[\w.]+")]
    private static partial Regex Correo();

    [GeneratedRegex(@"(\+?\d[\s-]?){7,}")]
    private static partial Regex Telefono();

    public static bool Contiene(string? texto) =>
        !string.IsNullOrWhiteSpace(texto) && (Correo().IsMatch(texto) || Telefono().IsMatch(texto));
}
