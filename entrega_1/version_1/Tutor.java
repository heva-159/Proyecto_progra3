import java.util.ArrayList;
import java.util.List;

public class Tutor extends PersonaPUCP {
    private List<Postulacion> postulaciones = new ArrayList<>();
    private List<Tarifa> tarifas = new ArrayList<>();
    private List<Sesion> sesiones = new ArrayList<>();

    public Tutor(String nombreCompleto, String apellidos) {
        super(nombreCompleto, apellidos);
    }
    
    public List<Postulacion> getPostulaciones() {
        return postulaciones;
    }

    public List<Tarifa> getTarifas() {
        return tarifas;
    }

    public List<Sesion> getSesiones() {
        return sesiones;
    }
}
