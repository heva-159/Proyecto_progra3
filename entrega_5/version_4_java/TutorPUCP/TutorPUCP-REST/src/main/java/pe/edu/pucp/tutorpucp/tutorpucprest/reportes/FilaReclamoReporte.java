package pe.edu.pucp.tutorpucp.tutorpucprest.reportes;

import pe.edu.pucp.tutorPUCP.modelo.tutoria.Reclamo;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;

import java.time.format.DateTimeFormatter;

// Una fila del reporte de reclamos (RF018). Los $F{...} de reclamos.jrxml se llaman igual que estas propiedades.
public class FilaReclamoReporte {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Integer idReclamo;
    private final String fecha;
    private final String sesion;
    private final String reclamante;
    private final String rol;
    private final String estado;

    public FilaReclamoReporte(Reclamo reclamo) {
        this.idReclamo = reclamo.getIdReclamo();
        this.fecha = reclamo.getFechaReclamo() == null ? "" : reclamo.getFechaReclamo().format(FORMATO);
        Sesion s = reclamo.getSesion();
        this.sesion = s == null ? "" : "N.º " + s.getIdSesion() + (s.getCurso() == null ? "" : " · " + s.getCurso().getNombre());
        this.reclamante = FilaSesionReporte.nombre(reclamo.getReclamante());
        this.rol = reclamo.getRolReclamante() == null ? "" : FilaSesionReporte.estadoTexto(reclamo.getRolReclamante().name());
        this.estado = reclamo.getEstado() == null ? "" : FilaSesionReporte.estadoTexto(reclamo.getEstado().name());
    }

    public Integer getIdReclamo() { return idReclamo; }
    public String getFecha() { return fecha; }
    public String getSesion() { return sesion; }
    public String getReclamante() { return reclamante; }
    public String getRol() { return rol; }
    public String getEstado() { return estado; }
}
