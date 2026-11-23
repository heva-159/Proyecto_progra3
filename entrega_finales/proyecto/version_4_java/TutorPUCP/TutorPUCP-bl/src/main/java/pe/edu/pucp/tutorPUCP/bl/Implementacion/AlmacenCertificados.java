package pe.edu.pucp.tutorPUCP.bl.Implementacion;

import pe.edu.pucp.tutorPUCP.bl.Exeptions.BL_exeption;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Guarda y lee los PDF de los certificados de notas (RF004 y RF005).
// En la BD (postulacion.archivo_certificado_url) solo se guarda la ruta, por ejemplo
// "certificados/20201234_1MAT05_20261115-173012.pdf". El archivo queda en el disco del servidor,
// en la carpeta <carpeta del usuario>/TutorPUCP (en Windows: C:\Users\<usuario>\TutorPUCP\certificados),
// fuera del .war para que no se borre al volver a desplegar en GlassFish.
// Los certificados de los datos de prueba del script vienen dentro del proyecto (src/main/resources/certificados).
class AlmacenCertificados {
    static final int TAMANO_MAXIMO = 5 * 1024 * 1024;   // 5 MB (RF004)

    private static final String CARPETA = "certificados";
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final Path BASE = Path.of(System.getProperty("user.home"), "TutorPUCP").toAbsolutePath().normalize();

    private AlmacenCertificados() {
    }

    // Reglas del RF004: tiene que venir un PDF de verdad y pesar como maximo 5 MB.
    static void validarPdf(byte[] contenido) {
        if (contenido == null || contenido.length == 0) {
            throw new BL_exeption("Debes adjuntar el certificado de notas en PDF");
        }
        if (contenido.length > TAMANO_MAXIMO) {
            throw new BL_exeption("El certificado no puede pesar mas de 5 MB");
        }
        // Todo PDF empieza con "%PDF-": asi no pasa un archivo al que solo le cambiaron la extension.
        if (contenido.length < 5 || contenido[0] != '%' || contenido[1] != 'P' || contenido[2] != 'D'
                || contenido[3] != 'F' || contenido[4] != '-') {
            throw new BL_exeption("El archivo no es un PDF valido");
        }
    }

    // Guarda el PDF y devuelve la ruta que se registra en la BD.
    static String guardar(byte[] contenido, String codigoPucp, String codigoCurso) {
        String nombre = soloLetrasYNumeros(codigoPucp) + "_" + soloLetrasYNumeros(codigoCurso) + "_"
                + LocalDateTime.now().format(FORMATO) + ".pdf";
        String ruta = CARPETA + "/" + nombre;
        try {
            Path destino = BASE.resolve(ruta);
            Files.createDirectories(destino.getParent());
            Files.write(destino, contenido, StandardOpenOption.CREATE_NEW);
            return ruta;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el certificado en " + BASE, e);
        }
    }

    // Devuelve el PDF guardado en esa ruta, o null si el archivo no esta en el servidor.
    static byte[] leer(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            return null;
        }
        String relativa = ruta.replace('\\', '/');
        Path archivo = BASE.resolve(relativa).normalize();
        if (!archivo.startsWith(BASE)) {
            return null;   // no se aceptan rutas que salgan de la carpeta, como "../../otro.pdf"
        }
        try {
            if (Files.isRegularFile(archivo)) {
                return Files.readAllBytes(archivo);
            }
            // Certificados de los datos de prueba, incluidos en el proyecto.
            try (InputStream recurso = AlmacenCertificados.class.getClassLoader().getResourceAsStream(relativa)) {
                return recurso != null ? recurso.readAllBytes() : null;
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el certificado " + ruta, e);
        }
    }

    // true si el PDF de esa ruta esta en el servidor (o entre los PDF de prueba del proyecto), sin leerlo.
    static boolean existe(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            return false;
        }
        String relativa = ruta.replace('\\', '/');
        Path archivo = BASE.resolve(relativa).normalize();
        if (!archivo.startsWith(BASE)) {
            return false;
        }
        return Files.isRegularFile(archivo)
                || AlmacenCertificados.class.getClassLoader().getResource(relativa) != null;
    }

    // Se usa si la postulacion no se llega a registrar, para no dejar el PDF suelto.
    static void borrar(String ruta) {
        try {
            Files.deleteIfExists(BASE.resolve(ruta));
        } catch (IOException ignorada) {
            // si no se puede borrar, solo queda un archivo de mas en la carpeta
        }
    }

    private static String soloLetrasYNumeros(String texto) {
        String limpio = texto == null ? "" : texto.replaceAll("[^A-Za-z0-9]", "");
        return limpio.isEmpty() ? "sin-codigo" : limpio;
    }
}
