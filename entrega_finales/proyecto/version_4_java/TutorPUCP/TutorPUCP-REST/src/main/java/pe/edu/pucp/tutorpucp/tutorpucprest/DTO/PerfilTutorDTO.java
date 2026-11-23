package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import pe.edu.pucp.tutorPUCP.modelo.usuario.PerfilTutor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// El perfil publico de un tutor para el estudiante que lo esta evaluando (RF009).
// No lleva correo ni telefono (RNF005) y las calificaciones van sin el nombre de quien califico.
// Las fechas van como texto ISO (2026-11-12T16:00).
public class PerfilTutorDTO {
    private int idTutor;
    private String nombre;
    private String facultad;
    private String especialidad;
    private boolean correoVerificado;
    private Double calificacion;          // null si todavia no lo calificaron
    private int cantidadCalificaciones;
    private int sesionesDictadas;
    private List<CursoItem> cursos = new ArrayList<>();
    private List<ResenaItem> resenas = new ArrayList<>();

    public PerfilTutorDTO() {
    }

    public PerfilTutorDTO(PerfilTutor perfil) {
        this.idTutor = perfil.getIdTutor();
        this.nombre = perfil.getNombre();
        this.facultad = perfil.getFacultad();
        this.especialidad = perfil.getEspecialidad();
        this.correoVerificado = perfil.isCorreoVerificado();
        this.calificacion = perfil.getCalificacion();
        this.cantidadCalificaciones = perfil.getCantidadCalificaciones();
        this.sesionesDictadas = perfil.getSesionesDictadas();
        for (PerfilTutor.CursoDelTutor curso : perfil.getCursos()) {
            CursoItem item = new CursoItem();
            item.setIdCurso(curso.getIdCurso());
            item.setCodigo(curso.getCodigo());
            item.setNombre(curso.getNombre());
            item.setTarifaPorHora(curso.getTarifaPorHora());
            item.getTemas().addAll(curso.getTemas());
            this.cursos.add(item);
        }
        for (PerfilTutor.ResenaDelTutor resena : perfil.getResenas()) {
            ResenaItem item = new ResenaItem();
            item.setCodigoCurso(resena.getCodigoCurso());
            item.setCurso(resena.getCurso());
            item.setPuntaje(resena.getPuntaje());
            item.setComentario(resena.getComentario());
            item.setFecha(resena.getFecha() != null ? resena.getFecha().toString() : null);
            this.resenas.add(item);
        }
    }

    public int getIdTutor() { return idTutor; }
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getFacultad() { return facultad; }
    public void setFacultad(String facultad) { this.facultad = facultad; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public boolean isCorreoVerificado() { return correoVerificado; }
    public void setCorreoVerificado(boolean correoVerificado) { this.correoVerificado = correoVerificado; }

    public Double getCalificacion() { return calificacion; }
    public void setCalificacion(Double calificacion) { this.calificacion = calificacion; }

    public int getCantidadCalificaciones() { return cantidadCalificaciones; }
    public void setCantidadCalificaciones(int cantidadCalificaciones) { this.cantidadCalificaciones = cantidadCalificaciones; }

    public int getSesionesDictadas() { return sesionesDictadas; }
    public void setSesionesDictadas(int sesionesDictadas) { this.sesionesDictadas = sesionesDictadas; }

    public List<CursoItem> getCursos() { return cursos; }
    public void setCursos(List<CursoItem> cursos) { this.cursos = cursos; }

    public List<ResenaItem> getResenas() { return resenas; }
    public void setResenas(List<ResenaItem> resenas) { this.resenas = resenas; }

    // Un curso en el que el tutor esta aprobado; tarifaPorHora es null si todavia no la registro.
    public static class CursoItem {
        private int idCurso;
        private String codigo;
        private String nombre;
        private BigDecimal tarifaPorHora;
        private List<String> temas = new ArrayList<>();

        public int getIdCurso() { return idCurso; }
        public void setIdCurso(int idCurso) { this.idCurso = idCurso; }

        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public BigDecimal getTarifaPorHora() { return tarifaPorHora; }
        public void setTarifaPorHora(BigDecimal tarifaPorHora) { this.tarifaPorHora = tarifaPorHora; }

        public List<String> getTemas() { return temas; }
        public void setTemas(List<String> temas) { this.temas = temas; }
    }

    // Una calificacion recibida: curso, puntaje del 1 al 5 y comentario (puede no haber).
    public static class ResenaItem {
        private String codigoCurso;
        private String curso;
        private int puntaje;
        private String comentario;
        private String fecha;

        public String getCodigoCurso() { return codigoCurso; }
        public void setCodigoCurso(String codigoCurso) { this.codigoCurso = codigoCurso; }

        public String getCurso() { return curso; }
        public void setCurso(String curso) { this.curso = curso; }

        public int getPuntaje() { return puntaje; }
        public void setPuntaje(int puntaje) { this.puntaje = puntaje; }

        public String getComentario() { return comentario; }
        public void setComentario(String comentario) { this.comentario = comentario; }

        public String getFecha() { return fecha; }
        public void setFecha(String fecha) { this.fecha = fecha; }
    }
}
