package pe.edu.pucp.tutorPUCP.modelo.tutoria;

import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;

import java.math.BigDecimal;

public class Tarifa {
    private int idTarifa;
    private Tutor tutor;
    private Curso curso;
    // BigDecimal y no double: es un monto de dinero (en la BD es decimal(6,2))
    private BigDecimal montoPorHora;

    public Tarifa() {
    }

    public Tarifa(Curso curso, BigDecimal montoPorHora) {
        this.curso = curso;
        this.montoPorHora = montoPorHora;
    }

    public int getIdTarifa() { return idTarifa; }
    public void setIdTarifa(int idTarifa) { this.idTarifa = idTarifa; }

    public Tutor getTutor() { return tutor; }
    public void setTutor(Tutor tutor) { this.tutor = tutor; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public BigDecimal getMontoPorHora() { return montoPorHora; }
    public void setMontoPorHora(BigDecimal montoPorHora) { this.montoPorHora = montoPorHora; }
}
