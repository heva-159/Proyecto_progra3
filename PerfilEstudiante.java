import java.util.ArrayList;
import java.util.List;

public class PerfilEstudiante {
    private Usuario usuario;
    private List<Sesion> sesiones = new ArrayList<>();

    public PerfilEstudiante(Usuario usuario) {
        this.usuario = usuario;
    }
}
