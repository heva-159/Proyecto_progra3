import java.util.ArrayList;
import java.util.List;

public class PerfilTutor {
    private Usuario usuario;
    private List<Postulacion> postulaciones = new ArrayList<>();
    private List<Tarifa> tarifas = new ArrayList<>();
    private List<Sesion> sesiones = new ArrayList<>();

    public PerfilTutor(Usuario usuario) {
        this.usuario = usuario;
    }
}
