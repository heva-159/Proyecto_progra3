package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.academico.Tema;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Lo que viaja en JSON de una sesión: tutor, estudiante y curso ya "aplanados" (id y nombre),
// así no se forman ciclos (Sesion -> Tutor -> sesiones -> Sesion...).
// No lleva correos ni teléfonos (RNF004: no exponer datos de contacto).
// Las fechas van como texto ISO (2026-11-12T16:00).
public class SesionDTO {
    private int idSesion;
    private int idTutor;
    private String tutor;
    private int idEstudiante;
    private String estudiante;
    private int idCurso;
    private String codigoCurso;
    private String curso;
    private List<String> temas = new ArrayList<>();
    private String fechaHoraInicio;
    private String fechaHoraFin;
    private String estado;
    private boolean checkInTutor;
    private boolean checkInEstudiante;
    private boolean checkOutTutor;
    private boolean checkOutEstudiante;
    private Integer puntaje;
    private String comentario;
    private String motivoCancelacion;
    private String cancelo;
    private BigDecimal tarifa;

    public SesionDTO() {
    }

    public SesionDTO(Sesion sesion) {
        this.idSesion = sesion.getIdSesion();
        Usuario usuarioTutor = sesion.getTutor() != null ? sesion.getTutor().getUsuario() : null;
        if (usuarioTutor != null) {
            this.idTutor = usuarioTutor.getIdUsuario();
            this.tutor = nombreCompleto(usuarioTutor);
        }
        Usuario usuarioEstudiante = sesion.getEstudiante() != null ? sesion.getEstudiante().getUsuario() : null;
        if (usuarioEstudiante != null) {
            this.idEstudiante = usuarioEstudiante.getIdUsuario();
            this.estudiante = nombreCompleto(usuarioEstudiante);
        }
        Curso c = sesion.getCurso();
        if (c != null) {
            this.idCurso = c.getIdCurso();
            this.codigoCurso = c.getCodigo();
            this.curso = c.getNombre();
        }
        for (Tema tema : sesion.getTemas()) {
            this.temas.add(tema.getNombre());
        }
        this.fechaHoraInicio = sesion.getFechaHoraInicio() != null ? sesion.getFechaHoraInicio().toString() : null;
        this.fechaHoraFin = sesion.getFechaHoraFin() != null ? sesion.getFechaHoraFin().toString() : null;
        this.estado = sesion.getEstado() != null ? sesion.getEstado().name() : null;
        this.checkInTutor = sesion.isCheckInTutor();
        this.checkInEstudiante = sesion.isCheckInEstudiante();
        this.checkOutTutor = sesion.isCheckOutTutor();
        this.checkOutEstudiante = sesion.isCheckOutEstudiante();
        this.puntaje = sesion.getPuntaje();
        this.comentario = sesion.getComentario();
        this.motivoCancelacion = sesion.getMotivoCancelacion();
        this.cancelo = sesion.getUsuarioCancelo() != null ? nombreCompleto(sesion.getUsuarioCancelo()) : null;
    }

    private static String nombreCompleto(Usuario usuario) {
        return usuario.getNombres() + " " + usuario.getApellidos();
    }

    // getters y setters
    public int getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(int idSesion) {
        this.idSesion = idSesion;
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

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public String getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(String estudiante) {
        this.estudiante = estudiante;
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

    public List<String> getTemas() {
        return temas;
    }

    public void setTemas(List<String> temas) {
        this.temas = temas;
    }

    public String getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(String fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public String getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(String fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isCheckInTutor() {
        return checkInTutor;
    }

    public void setCheckInTutor(boolean checkInTutor) {
        this.checkInTutor = checkInTutor;
    }

    public boolean isCheckInEstudiante() {
        return checkInEstudiante;
    }

    public void setCheckInEstudiante(boolean checkInEstudiante) {
        this.checkInEstudiante = checkInEstudiante;
    }

    public boolean isCheckOutTutor() {
        return checkOutTutor;
    }

    public void setCheckOutTutor(boolean checkOutTutor) {
        this.checkOutTutor = checkOutTutor;
    }

    public boolean isCheckOutEstudiante() {
        return checkOutEstudiante;
    }

    public void setCheckOutEstudiante(boolean checkOutEstudiante) {
        this.checkOutEstudiante = checkOutEstudiante;
    }

    public Integer getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(Integer puntaje) {
        this.puntaje = puntaje;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    public String getCancelo() {
        return cancelo;
    }

    public void setCancelo(String cancelo) {
        this.cancelo = cancelo;
    }

    public BigDecimal getTarifa() {
        return tarifa;
    }

    public void setTarifa(BigDecimal tarifa) {
        this.tarifa = tarifa;
    }
}
