package Usuario;

import Academico.Postulacion;
import Tutoria.Sesion;
import Tutoria.Tarifa;

import java.util.ArrayList;
import java.util.List;

public class Tutor extends PersonaPUCP {
    private List<Postulacion> postulaciones;
    private List<Tarifa> tarifas;
    private List<Sesion> sesiones;
    private int calificacion_promedio;
    private int numero_sesiones;

    public Tutor() {
        postulaciones = new ArrayList<>();
        tarifas = new ArrayList<>();
        sesiones = new ArrayList<>();
        numero_sesiones = 0;
        calificacion_promedio = 0;
    }

    public List<Postulacion> getPostulaciones() {
        return postulaciones;
    }

    public List<Tarifa> getTarifas() {
        return tarifas;
    }

    @Override
    public List<Sesion> getSesiones() {
        return sesiones;
    }

    public int getCalificacion_promedio() {
        return calificacion_promedio;
    }

    public void setCalificacion_promedio(int calificacion_promedio) {
        this.calificacion_promedio = calificacion_promedio;
    }

    public int getNumero_sesiones() {
        return numero_sesiones;
    }

    public void setNumero_sesiones(int numero_sesiones) {
        this.numero_sesiones = numero_sesiones;
    }
}
