package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import java.math.BigDecimal;

// DTO para enviar la información de búsqueda de tutores acreditados con su tarifa (RF009)
public class TutorBusquedaDTO {
    private int idTutor;
    private String nombre;
    private String especialidad;
    private double calificacion;
    private int sesionesDictadas;
    private int idCurso;
    private String cursoCodigo;
    private String cursoNombre;
    private BigDecimal tarifaPorHora;

    public TutorBusquedaDTO() {}

    public int getIdTutor() { return idTutor; }
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public double getCalificacion() { return calificacion; }
    public void setCalificacion(double calificacion) { this.calificacion = calificacion; }

    public int getSesionesDictadas() { return sesionesDictadas; }
    public void setSesionesDictadas(int sesionesDictadas) { this.sesionesDictadas = sesionesDictadas; }

    public int getIdCurso() { return idCurso; }
    public void setIdCurso(int idCurso) { this.idCurso = idCurso; }

    public String getCursoCodigo() { return cursoCodigo; }
    public void setCursoCodigo(String cursoCodigo) { this.cursoCodigo = cursoCodigo; }

    public String getCursoNombre() { return cursoNombre; }
    public void setCursoNombre(String cursoNombre) { this.cursoNombre = cursoNombre; }

    public BigDecimal getTarifaPorHora() { return tarifaPorHora; }
    public void setTarifaPorHora(BigDecimal tarifaPorHora) { this.tarifaPorHora = tarifaPorHora; }
}
