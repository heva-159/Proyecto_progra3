package Usuario;

import Tutoria.Sesion;

import java.util.ArrayList;
import java.util.List;

public class Estudiante extends PersonaPUCP {
    private List<Sesion> sesiones;

    public Estudiante() {
        sesiones = new ArrayList<>();
    }

    @Override
    public List<Sesion> getSesiones() {
        return sesiones;
    }
}
