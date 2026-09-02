import java.util.ArrayList;
import java.util.List;

public class Tutor extends PersonaPUCP {
    private List<Postulacion> postulaciones = new ArrayList<>();
    private List<Tarifa> tarifas = new ArrayList<>();
    private List<Sesion> sesiones = new ArrayList<>();

    public Tutor(Usuario usuario) {
        super(usuario);
    }

    public List<Postulacion> getPostulaciones() { return postulaciones; }
    public void setPostulaciones(List<Postulacion> postulaciones) { this.postulaciones = postulaciones; }

    public List<Tarifa> getTarifas() { return tarifas; }
    public void setTarifas(List<Tarifa> tarifas) { this.tarifas = tarifas; }

    public List<Sesion> getSesiones() { return sesiones; }
    public void setSesiones(List<Sesion> sesiones) { this.sesiones = sesiones; }
}
