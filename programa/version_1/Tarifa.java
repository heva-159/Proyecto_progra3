public class Tarifa {
    private static long contador = 0;

    private long idTarifa;
    private Curso curso;
    private double montoPorHora;

    public Tarifa(Curso curso, double montoPorHora) {
        this.idTarifa = ++contador;
        this.curso = curso;
        this.montoPorHora = montoPorHora;
    }

    public long getIdTarifa() { return idTarifa; }
    public void setIdTarifa(long idTarifa) { this.idTarifa = idTarifa; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public double getMontoPorHora() { return montoPorHora; }
    public void setMontoPorHora(double montoPorHora) { this.montoPorHora = montoPorHora; }
}
