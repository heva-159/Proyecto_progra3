package Usuario;

import Academico.Postulacion;
import Tutoria.Sesion;
import Tutoria.Tarifa;

import java.util.ArrayList;
import java.util.List;

public class Tutor {
    private List<Postulacion> postulaciones;
    private List<Tarifa> tarifas;
    private List<Sesion> sesiones;

    public Tutor(String nombreCompleto, String apellidos) {
        postulaciones = new ArrayList<>();
        tarifas = new ArrayList<>();
        sesiones = new ArrayList<>();
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
