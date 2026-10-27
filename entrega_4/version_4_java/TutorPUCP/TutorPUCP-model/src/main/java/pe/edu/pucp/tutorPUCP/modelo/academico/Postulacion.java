package pe.edu.pucp.tutorPUCP.modelo.academico;

import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoAprobacion;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorPUCP.modelo.interfaces.IAprobable;

import java.time.LocalDate;

public class Postulacion implements IAprobable {
    private int idPostulacion;
    private Tutor tutor;
    private Curso curso;
    private String archivoCertificadoUrl;
    private LocalDate fechaPostulacion;
    private EstadoAprobacion estado;
    private LocalDate fechaResolucion;
    private String resolucion;
    private Administrador administradorRevisor;

    public Postulacion() {
        this.estado = EstadoAprobacion.PENDIENTE;
    }

    public Postulacion(Curso curso, String archivoCertificadoUrl) {
        this.curso = curso;
        this.archivoCertificadoUrl = archivoCertificadoUrl;
        this.fechaPostulacion = LocalDate.now();
        this.estado = EstadoAprobacion.PENDIENTE;
    }

    @Override
    public void aprobar(Administrador administrador, String resolucion) {
        this.estado = EstadoAprobacion.APROBADA;
        this.administradorRevisor = administrador;
        this.fechaResolucion = LocalDate.now();
        this.resolucion = resolucion;
    }

    @Override
    public void rechazar(Administrador administrador, String motivo) {
        this.estado = EstadoAprobacion.RECHAZADA;
        this.administradorRevisor = administrador;
        this.fechaResolucion = LocalDate.now();
        this.resolucion = motivo;
    }

    @Override
    public EstadoAprobacion getEstado() { return estado; }
    public void setEstado(EstadoAprobacion estado) { this.estado = estado; }

    public int getIdPostulacion() { return idPostulacion; }
    public void setIdPostulacion(int idPostulacion) { this.idPostulacion = idPostulacion; }

    public Tutor getTutor() { return tutor; }
    public void setTutor(Tutor tutor) { this.tutor = tutor; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public String getArchivoCertificadoUrl() { return archivoCertificadoUrl; }
    public void setArchivoCertificadoUrl(String archivoCertificadoUrl) { this.archivoCertificadoUrl = archivoCertificadoUrl; }

    public LocalDate getFechaPostulacion() { return fechaPostulacion; }
    public void setFechaPostulacion(LocalDate fechaPostulacion) { this.fechaPostulacion = fechaPostulacion; }

    public LocalDate getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDate fechaResolucion) { this.fechaResolucion = fechaResolucion; }

    public String getResolucion() { return resolucion; }
    public void setResolucion(String resolucion) { this.resolucion = resolucion; }

    public Administrador getAdministradorRevisor() { return administradorRevisor; }
    public void setAdministradorRevisor(Administrador administradorRevisor) { this.administradorRevisor = administradorRevisor; }
}
