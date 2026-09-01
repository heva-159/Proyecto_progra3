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
}
