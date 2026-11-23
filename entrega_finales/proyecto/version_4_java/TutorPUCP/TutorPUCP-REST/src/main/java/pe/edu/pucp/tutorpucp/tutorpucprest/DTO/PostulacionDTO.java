package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.academico.Postulacion;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

// Lo que viaja en JSON de una postulación: los datos del tutor, del curso y del revisor ya "aplanados",
// así la pantalla no necesita otras llamadas y no se forman ciclos (Tutor -> postulaciones -> Tutor...).
// Las fechas van como texto ISO (2026-09-20).
//
// El nombre del curso viaja con dos juegos de nombres porque dos pantallas lo leen distinto:
//   codigoCurso / curso        -> pantallas del administrador (RF005)
//   cursoCodigo / cursoNombre  -> pantallas del tutor (RF004)
// Los dos juegos llevan el mismo valor.
public class PostulacionDTO {
    private int idPostulacion;
    private int idTutor;
    private String tutor;
    private String codigoPucp;
    private String correo;
    private String facultad;
    private String especialidad;
    private int idCurso;
    private String codigoCurso;
    private String curso;
    private String cursoCodigo;
    private String cursoNombre;
    private String archivoCertificadoUrl;
    private String fechaPostulacion;
    private String estado;
    private String fechaResolucion;
    private String resolucion;
    private String revisor;
    private boolean certificadoDisponible;   // false: se registró solo el nombre, sin el PDF

    public PostulacionDTO() {
    }

    public PostulacionDTO(Postulacion postulacion) {
        if (postulacion == null) {
            return;
        }
        this.idPostulacion = postulacion.getIdPostulacion();
        Usuario usuario = postulacion.getTutor() != null ? postulacion.getTutor().getUsuario() : null;
        if (usuario != null) {
            this.idTutor = usuario.getIdUsuario();
            this.tutor = usuario.getNombres() + " " + usuario.getApellidos();
            this.codigoPucp = usuario.getCodigoPucp();
            this.correo = usuario.getCorreoInstitucional();
            this.facultad = usuario.getFacultad();
            this.especialidad = usuario.getEspecialidad();
        }
        Curso c = postulacion.getCurso();
        if (c != null) {
            this.idCurso = c.getIdCurso();
            this.codigoCurso = c.getCodigo();
            this.curso = c.getNombre();
            this.cursoCodigo = c.getCodigo();
            this.cursoNombre = c.getNombre();
        }
        this.archivoCertificadoUrl = postulacion.getArchivoCertificadoUrl();
        this.fechaPostulacion = postulacion.getFechaPostulacion() != null ? postulacion.getFechaPostulacion().toString() : null;
        this.estado = postulacion.getEstado() != null ? postulacion.getEstado().name() : "PENDIENTE";
        this.fechaResolucion = postulacion.getFechaResolucion() != null ? postulacion.getFechaResolucion().toString() : null;
        this.resolucion = postulacion.getResolucion();
        this.revisor = postulacion.getAdministradorRevisor() != null ? postulacion.getAdministradorRevisor().getNombre() : null;
    }

    // getters y setters
    public int getIdPostulacion() {
        return idPostulacion;
    }

    public void setIdPostulacion(int idPostulacion) {
        this.idPostulacion = idPostulacion;
    }

    public int getIdTutor() {
        return idTutor;
    }

    public void setIdTutor(int idTutor) {
        this.idTutor = idTutor;
    }

    public String getTutor() {
        return tutor;
    }

    public void setTutor(String tutor) {
        this.tutor = tutor;
    }

    public String getCodigoPucp() {
        return codigoPucp;
    }

    public void setCodigoPucp(String codigoPucp) {
        this.codigoPucp = codigoPucp;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getFacultad() {
        return facultad;
    }

    public void setFacultad(String facultad) {
        this.facultad = facultad;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    public String getCodigoCurso() {
        return codigoCurso;
    }

    public void setCodigoCurso(String codigoCurso) {
        this.codigoCurso = codigoCurso;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public String getCursoCodigo() {
        return cursoCodigo;
    }

    public void setCursoCodigo(String cursoCodigo) {
        this.cursoCodigo = cursoCodigo;
    }

    public String getCursoNombre() {
        return cursoNombre;
    }

    public void setCursoNombre(String cursoNombre) {
        this.cursoNombre = cursoNombre;
    }

    public String getArchivoCertificadoUrl() {
        return archivoCertificadoUrl;
    }

    public void setArchivoCertificadoUrl(String archivoCertificadoUrl) {
        this.archivoCertificadoUrl = archivoCertificadoUrl;
    }

    public String getFechaPostulacion() {
        return fechaPostulacion;
    }

    public void setFechaPostulacion(String fechaPostulacion) {
        this.fechaPostulacion = fechaPostulacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(String fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }

    public String getRevisor() {
        return revisor;
    }

    public void setRevisor(String revisor) {
        this.revisor = revisor;
    }

    public boolean isCertificadoDisponible() {
        return certificadoDisponible;
    }

    public void setCertificadoDisponible(boolean certificadoDisponible) {
        this.certificadoDisponible = certificadoDisponible;
    }
}