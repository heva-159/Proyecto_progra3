package pe.edu.pucp.tutorPUCP.modelo.usuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Lo que ve un estudiante al elegir un tutor (RF009): sus datos publicos, los cursos en los que esta aprobado con su
// tarifa y las calificaciones que recibio. No lleva correo ni telefono (RNF005). Es una vista de lectura armada con
// pocas consultas, no la clase Tutor completa (que arrastra sus postulaciones, tarifas y sesiones).
public class PerfilTutor {
    private int idTutor;
    private String nombre;
    private String facultad;
    private String especialidad;
    private boolean correoVerificado;
    private Double calificacion;          // promedio de los puntajes; null si todavia no lo calificaron
    private int cantidadCalificaciones;
    private int sesionesDictadas;
    private List<CursoDelTutor> cursos = new ArrayList<>();
    private List<ResenaDelTutor> resenas = new ArrayList<>();

    public PerfilTutor() {
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

    // los cursos con postulacion aprobada (y activos)
    public List<CursoDelTutor> getCursos() { return cursos; }
    public void setCursos(List<CursoDelTutor> cursos) { this.cursos = cursos; }

    // las calificaciones mas recientes, sin el nombre de quien califico
    public List<ResenaDelTutor> getResenas() { return resenas; }
    public void setResenas(List<ResenaDelTutor> resenas) { this.resenas = resenas; }

    // Un curso en el que el tutor esta aprobado, con su tarifa por hora (null si todavia no la registro) y los temas del curso.
    public static class CursoDelTutor {
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

    // Una calificacion de una sesion: el curso, el puntaje del 1 al 5 y el comentario (opcional).
    public static class ResenaDelTutor {
        private String codigoCurso;
        private String curso;
        private int puntaje;
        private String comentario;
        private LocalDateTime fecha;

        public String getCodigoCurso() { return codigoCurso; }
        public void setCodigoCurso(String codigoCurso) { this.codigoCurso = codigoCurso; }

        public String getCurso() { return curso; }
        public void setCurso(String curso) { this.curso = curso; }

        public int getPuntaje() { return puntaje; }
        public void setPuntaje(int puntaje) { this.puntaje = puntaje; }

        public String getComentario() { return comentario; }
        public void setComentario(String comentario) { this.comentario = comentario; }

        public LocalDateTime getFecha() { return fecha; }
        public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    }
}
