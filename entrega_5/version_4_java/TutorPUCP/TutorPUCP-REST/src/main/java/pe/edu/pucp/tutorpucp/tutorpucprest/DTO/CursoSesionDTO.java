package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Un curso que el tutor puede dictar (para el formulario de registrar sesión): con sus temas y la tarifa del tutor.
public class CursoSesionDTO {
    private int idCurso;
    private String codigo;
    private String nombre;
    private BigDecimal tarifa;      // null si el tutor todavía no registró tarifa para este curso
    private List<TemaDTO> temas = new ArrayList<>();

    public CursoSesionDTO() {
    }

    public CursoSesionDTO(Curso curso, BigDecimal tarifa, List<TemaDTO> temas) {
        this.idCurso = curso.getIdCurso();
        this.codigo = curso.getCodigo();
        this.nombre = curso.getNombre();
        this.tarifa = tarifa;
        this.temas = temas;
    }

    // getters y setters
    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getTarifa() {
        return tarifa;
    }

    public void setTarifa(BigDecimal tarifa) {
        this.tarifa = tarifa;
    }

    public List<TemaDTO> getTemas() {
        return temas;
    }

    public void setTemas(List<TemaDTO> temas) {
        this.temas = temas;
    }
}
