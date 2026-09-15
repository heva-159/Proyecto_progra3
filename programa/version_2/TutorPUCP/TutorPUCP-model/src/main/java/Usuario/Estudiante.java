package Usuario;

import Tutoria.Sesion;

import java.util.ArrayList;
import java.util.List;

public class Estudiante {
    private List<Sesion> sesiones;
    //
    public Estudiante(String nombreCompleto, String apellidos) {
        sesiones = new ArrayList<>();
    }
    public List<Sesion> getSesiones() {
        return sesiones;
    }
}
