namespace TutorPUCP.Servicios;

// Facultades y especialidades de la PUCP (pucp.edu.pe/pregrado/carreras/por-facultad).
// Las usa la pantalla de Perfil; Registro tiene su propia copia de esta misma lista.
public static class Carreras
{
    public static readonly Dictionary<string, string[]> PorFacultad = new()
    {
        ["Arquitectura y Urbanismo"] = new[] { "Arquitectura" },
        ["Arte y Diseño"] = new[]
        {
            "Educación Artística",
            "Diseño Gráfico",
            "Diseño Industrial",
            "Escultura",
            "Pintura",
            "Arte, moda y diseño textil",
            "Grabado"
        },
        ["Artes Escénicas"] = new[] { "Danza", "Teatro", "Música", "Creación y Producción Escénica" },
        ["Ciencias Contables"] = new[] { "Contabilidad" },
        ["Ciencias e Ingeniería"] = new[]
        {
            "Estadística",
            "Física",
            "Matemáticas",
            "Química",
            "Ingeniería Ambiental y Sostenible",
            "Ingeniería Biomédica PUCP",
            "Ingeniería Civil",
            "Ingeniería de las Telecomunicaciones",
            "Ingeniería de Minas",
            "Ingeniería Electrónica",
            "Ingeniería Geológica",
            "Ingeniería Industrial",
            "Ingeniería Informática",
            "Ingeniería Mecánica",
            "Ingeniería Mecatrónica",
            "Ingeniería Química"
        },
        ["Ciencias Sociales"] = new[]
        {
            "Antropología",
            "Ciencia Política y Gobierno",
            "Economía",
            "Finanzas",
            "Relaciones Internacionales",
            "Sociología"
        },
        ["Ciencias y Artes de la Comunicación"] = new[]
        {
            "Comunicación Audiovisual",
            "Comunicación para el desarrollo",
            "Publicidad",
            "Periodismo"
        },
        ["Derecho"] = new[] { "Derecho" },
        ["Educación"] = new[] { "Educación Primaria", "Educación Inicial", "Educación Secundaria" },
        ["Estudios Generales Ciencias"] = new[] { "Sin especialidad (Estudios Generales)" },
        ["Estudios Generales Letras"] = new[] { "Sin especialidad (Estudios Generales)" },
        ["Gastronomía, Hotelería y Turismo"] = new[] { "Gastronomía", "Hotelería", "Turismo" },
        ["Gestión y Alta Dirección"] = new[] { "Gestión" },
        ["Letras y Ciencias Humanas"] = new[]
        {
            "Arqueología",
            "Ciencias de la Información",
            "Filosofía",
            "Geografía y Medio Ambiente",
            "Historia",
            "Humanidades",
            "Lingüística y Literatura"
        },
        ["Psicología"] = new[] { "Psicología" },
    };
}