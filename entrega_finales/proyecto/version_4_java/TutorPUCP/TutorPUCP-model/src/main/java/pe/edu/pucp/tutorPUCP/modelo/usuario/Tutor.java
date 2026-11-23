package pe.edu.pucp.tutorPUCP.modelo.usuario;

import pe.edu.pucp.tutorPUCP.modelo.academico.Postulacion;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoSesion;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Tarifa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tutor extends PersonaPUCP {
    private List<Postulacion> postulaciones = new ArrayList<>();
    private List<Tarifa> tarifas = new ArrayList<>();

    public void agregarPostulacion(Postulacion postulacion) {
        postulacion.setTutor(this);
        postulaciones.add(postulacion);
    }

    public void agregarTarifa(Tarifa tarifa) {
        tarifa.setTutor(this);
        tarifas.add(tarifa);
    }

    // Datos derivados: se calculan a partir de las sesiones, no se guardan.
    public int getNumeroSesiones() {
        int numeroSesiones = 0;
        for (Sesion sesion : getSesiones()) {
            if (sesion.getEstado() == EstadoSesion.REALIZADA) numeroSesiones++;
        }
        return numeroSesiones;
    }

    public double getCalificacionPromedio() {
        int suma = 0;
        int calificadas = 0;
        for (Sesion sesion : getSesiones()) {
            if (sesion.getPuntaje() != null) {
                suma += sesion.getPuntaje();
                calificadas++;
            }
        }
        return calificadas == 0 ? 0.0 : (double) suma / calificadas;
    }

    // Solo lectura: se agregan con agregarPostulacion() y agregarTarifa().
    public List<Postulacion> getPostulaciones() { return Collections.unmodifiableList(postulaciones); }
    public void setPostulaciones(List<Postulacion> postulaciones) {
        this.postulaciones = new ArrayList<>();
        for (Postulacion postulacion : postulaciones) agregarPostulacion(postulacion);
    }

    public List<Tarifa> getTarifas() { return Collections.unmodifiableList(tarifas); }
    public void setTarifas(List<Tarifa> tarifas) {
        this.tarifas = new ArrayList<>();
        for (Tarifa tarifa : tarifas) agregarTarifa(tarifa);
    }
}
