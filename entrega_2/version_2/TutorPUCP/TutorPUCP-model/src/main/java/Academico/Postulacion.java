package Academico;

import enums.EstadoAprobacion;
import Usuario.Tutor;
import Interfaces.IAprobable;

import java.time.LocalDate;

public class Postulacion implements IAprobable {
    private static long contador = 0;

    private long idPostulacion;
    private Tutor tutor;
    private Curso curso;
    private String archivoCertificadoUrl;
    private LocalDate fechaPostulacion;
    private EstadoAprobacion estado;
    private LocalDate fechaResolucion;
    private String resolucion;

    public Postulacion(Tutor tutor, Curso curso, String archivoCertificadoUrl) {
        this.idPostulacion = ++contador;
        this.tutor = tutor;
        this.curso = curso;
        this.archivoCertificadoUrl = archivoCertificadoUrl;
        this.fechaPostulacion = LocalDate.now();
        this.estado = EstadoAprobacion.PENDIENTE;
    }

    @Override
    public void aprobar() {
        this.estado = EstadoAprobacion.APROBADA;
        this.fechaResolucion = LocalDate.now();
    }

    @Override
    public void rechazar(String motivo) {
        this.estado = EstadoAprobacion.RECHAZADA;
        this.fechaResolucion = LocalDate.now();
        this.resolucion = motivo;
    }

    @Override
    public EstadoAprobacion getEstado() { return estado; }
    public void setEstado(EstadoAprobacion estado) { this.estado = estado; }

    public long getIdPostulacion() { return idPostulacion; }
    public void setIdPostulacion(long idPostulacion) { this.idPostulacion = idPostulacion; }

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
}
