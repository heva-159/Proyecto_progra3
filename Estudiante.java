import java.util.ArrayList;
import java.util.List;

public class Estudiante extends PersonaPUCP {
    private List<Sesion> sesiones = new ArrayList<>();

    public Estudiante(String nombreCompleto, String apellidos) {
        super(nombreCompleto, apellidos);
    }

    public List<Sesion> getSesiones() { return sesiones; }
    public void setSesiones(List<Sesion> sesiones) { this.sesiones = sesiones; }
}
