import java.util.ArrayList;
import java.util.List;

public class Curso {
    private static long contador = 0;

    private long idCurso;
    private String codigo;
    private String nombre;
    private boolean activo;
    private List<Tema> temas = new ArrayList<>();

    public Curso(String nombre, String codigo) {
        this.idCurso = ++contador;
        this.nombre = nombre;
        this.codigo = codigo;
        this.activo = true;
    }
}
