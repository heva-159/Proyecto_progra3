package pe.edu.pucp.tutorPUCP.modelo.tutoria;

import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;

public class Tarifa {
    private int idTarifa;
    private Tutor tutor;
    private Curso curso;
    private double montoPorHora;

    public Tarifa() {
    }

    public Tarifa(Curso curso, double montoPorHora) {
        this.curso = curso;
        this.montoPorHora = montoPorHora;
    }

    public int getIdTarifa() { return idTarifa; }
    public void setIdTarifa(int idTarifa) { this.idTarifa = idTarifa; }

    public Tutor getTutor() { return tutor; }
    public void setTutor(Tutor tutor) { this.tutor = tutor; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public double getMontoPorHora() { return montoPorHora; }
    public void setMontoPorHora(double montoPorHora) { this.montoPorHora = montoPorHora; }
}
