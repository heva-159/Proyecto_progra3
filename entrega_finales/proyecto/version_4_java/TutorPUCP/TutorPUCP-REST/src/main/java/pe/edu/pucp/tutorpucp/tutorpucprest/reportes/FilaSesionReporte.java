package pe.edu.pucp.tutorpucp.tutorpucprest.reportes;

import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.time.format.DateTimeFormatter;

// Una fila de los reportes de sesiones (RF017 tutorías del tutor y RF018 sesiones del administrador).
// Jasper lee los campos por su getter: los $F{...} del .jrxml se llaman igual que estas propiedades.
public class FilaSesionReporte {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Integer idSesion;
    private final String fecha;
    private final String curso;
    private final String tutor;
    private final String estudiante;
    private final String estado;
    private final Integer puntaje;

    public FilaSesionReporte(Sesion sesion) {
        this.idSesion = sesion.getIdSesion();
        this.fecha = sesion.getFechaHoraInicio() == null ? "" : sesion.getFechaHoraInicio().format(FORMATO);
        this.curso = sesion.getCurso() == null ? "" : sesion.getCurso().getCodigo() + " " + sesion.getCurso().getNombre();
        this.tutor = sesion.getTutor() == null ? "" : nombre(sesion.getTutor().getUsuario());
        this.estudiante = sesion.getEstudiante() == null ? "" : nombre(sesion.getEstudiante().getUsuario());
        this.estado = sesion.getEstado() == null ? "" : estadoTexto(sesion.getEstado().name());
        this.puntaje = sesion.getPuntaje();
    }

    static String nombre(Usuario usuario) {
        return usuario == null ? "" : usuario.getNombres() + " " + usuario.getApellidos();
    }

    // PROGRAMADA -> Programada, CANCELADA_INASISTENCIA -> Cancelada inasistencia
    static String estadoTexto(String estado) {
        String texto = estado.replace('_', ' ').toLowerCase();
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    public Integer getIdSesion() { return idSesion; }
    public String getFecha() { return fecha; }
    public String getCurso() { return curso; }
    public String getTutor() { return tutor; }
    public String getEstudiante() { return estudiante; }
    public String getEstado() { return estado; }
    public Integer getPuntaje() { return puntaje; }
}
