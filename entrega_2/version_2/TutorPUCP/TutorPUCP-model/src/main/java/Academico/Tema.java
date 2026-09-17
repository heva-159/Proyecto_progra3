package Academico;

public class Tema {
    private static long contador = 0;

    private long idTema;
    private String nombre;

    public Tema(String nombre) {
        this.idTema = ++contador;
        this.nombre = nombre;
    }

    public long getIdTema() { return idTema; }
    public void setIdTema(long idTema) { this.idTema = idTema; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
