import java.util.ArrayList;
import java.util.List;

public class Estudiante extends PersonaPUCP {
    private List<Sesion> sesiones = new ArrayList<>();

    public Estudiante(Usuario usuario) {
        super(usuario);
    }

    public List<Sesion> getSesiones() { return sesiones; }
    public void setSesiones(List<Sesion> sesiones) { this.sesiones = sesiones; }
}
