public class Tema {
    private static long contador = 0;

    private long idTema;
    private String nombre;

    public Tema(String nombre) {
        this.idTema = ++contador;
        this.nombre = nombre;
    }
}
