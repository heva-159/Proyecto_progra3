package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import pe.edu.pucp.tutorPUCP.modelo.academico.Postulacion;


public class PostulacionDTO {
    private int idPostulacion;
    private int idCurso;
    private String cursoCodigo;
    private String cursoNombre;
    private String archivoCertificadoUrl;
    private String estado;
    private String fechaPostulacion;
    private String fechaResolucion;
    private String resolucion;

    public PostulacionDTO() {}

    public PostulacionDTO(Postulacion p) {
        if (p != null) {
            this.idPostulacion = p.getIdPostulacion();
            if (p.getCurso() != null) {
                this.idCurso = p.getCurso().getIdCurso();
                this.cursoCodigo = p.getCurso().getCodigo();
                this.cursoNombre = p.getCurso().getNombre();
            }
            this.archivoCertificadoUrl = p.getArchivoCertificadoUrl();
            this.estado = p.getEstado() != null ? p.getEstado().name() : "PENDIENTE";
            this.fechaPostulacion = p.getFechaPostulacion() != null ? p.getFechaPostulacion().toString() : "";
            this.fechaResolucion = p.getFechaResolucion() != null ? p.getFechaResolucion().toString() : "";
            this.resolucion = p.getResolucion() != null ? p.getResolucion() : "";
        }
    }

    public int getIdPostulacion() { return idPostulacion; }
    public void setIdPostulacion(int idPostulacion) { this.idPostulacion = idPostulacion; }

    public int getIdCurso() { return idCurso; }
    public void setIdCurso(int idCurso) { this.idCurso = idCurso; }

    public String getCursoCodigo() { return cursoCodigo; }
    public void setCursoCodigo(String cursoCodigo) { this.cursoCodigo = cursoCodigo; }

    public String getCursoNombre() { return cursoNombre; }
    public void setCursoNombre(String cursoNombre) { this.cursoNombre = cursoNombre; }

    public String getArchivoCertificadoUrl() { return archivoCertificadoUrl; }
    public void setArchivoCertificadoUrl(String archivoCertificadoUrl) { this.archivoCertificadoUrl = archivoCertificadoUrl; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFechaPostulacion() { return fechaPostulacion; }
    public void setFechaPostulacion(String fechaPostulacion) { this.fechaPostulacion = fechaPostulacion; }

    public String getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(String fechaResolucion) { this.fechaResolucion = fechaResolucion; }

    public String getResolucion() { return resolucion; }
    public void setResolucion(String resolucion) { this.resolucion = resolucion; }
}
