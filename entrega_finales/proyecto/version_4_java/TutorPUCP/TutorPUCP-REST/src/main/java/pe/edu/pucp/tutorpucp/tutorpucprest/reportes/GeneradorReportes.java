package pe.edu.pucp.tutorpucp.tutorpucprest.reportes;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Arma los PDF de RF017 y RF018 con JasperReports (igual que GeneradorReportes de la clase de reportes).
// Las plantillas .jrxml están en src/main/resources/reportes y se compilan una sola vez.
public class GeneradorReportes {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Map<String, JasperReport> plantillas = new HashMap<>();

    private static synchronized JasperReport plantillaCompilada(String nombre) throws JRException {
        JasperReport plantilla = plantillas.get(nombre);
        if (plantilla == null) {
            String ruta = "/reportes/" + nombre + ".jrxml";
            try (InputStream entrada = GeneradorReportes.class.getResourceAsStream(ruta)) {
                if (entrada == null) {
                    throw new JRException("No se encontró " + ruta);
                }
                plantilla = JasperCompileManager.compileReport(entrada);
                plantillas.put(nombre, plantilla);
            } catch (java.io.IOException ex) {
                throw new JRException("No se pudo cerrar la plantilla " + ruta, ex);
            }
        }
        return plantilla;
    }

    // RF017: tutorías que dictó el tutor en el rango
    public byte[] generarTutoriasTutor(List<FilaSesionReporte> filas, String tutor, LocalDate desde, LocalDate hasta,
                                       long realizadas, String promedio) throws JRException {
        Map<String, Object> parametros = parametrosDelRango(desde, hasta);
        parametros.put("TUTOR", tutor);
        parametros.put("REALIZADAS", realizadas);
        parametros.put("PROMEDIO", promedio);
        return generar("tutorias_tutor", filas, parametros);
    }

    // RF018: todas las sesiones del rango
    public byte[] generarSesiones(List<FilaSesionReporte> filas, LocalDate desde, LocalDate hasta) throws JRException {
        return generar("sesiones", filas, parametrosDelRango(desde, hasta));
    }

    // RF018: todos los reclamos del rango
    public byte[] generarReclamos(List<FilaReclamoReporte> filas, LocalDate desde, LocalDate hasta) throws JRException {
        return generar("reclamos", filas, parametrosDelRango(desde, hasta));
    }

    private Map<String, Object> parametrosDelRango(LocalDate desde, LocalDate hasta) {
        return new HashMap<>(Map.of(
                "DESDE", desde.format(FORMATO),
                "HASTA", hasta.format(FORMATO),
                JRParameter.REPORT_LOCALE, Locale.forLanguageTag("es-PE")));
    }

    private byte[] generar(String plantilla, List<?> filas, Map<String, Object> parametros) throws JRException {
        var datos = new JRBeanCollectionDataSource(filas, false);
        JasperPrint documento = JasperFillManager.fillReport(plantillaCompilada(plantilla), parametros, datos);
        return JasperExportManager.exportReportToPdf(documento);
    }

    // Lee las fechas del query (?desde=2026-10-01&hasta=2026-10-31). Devuelve el mensaje de error o null si están bien.
    public static String validarRango(String desde, String hasta) {
        if (desde == null || desde.isBlank() || hasta == null || hasta.isBlank()) {
            return "Elige la fecha de inicio y la fecha de fin";
        }
        try {
            if (LocalDate.parse(desde.trim()).isAfter(LocalDate.parse(hasta.trim()))) {
                return "La fecha de inicio no puede ser posterior a la fecha de fin";
            }
        } catch (DateTimeParseException e) {
            return "Las fechas deben tener el formato aaaa-mm-dd";
        }
        return null;
    }
}
