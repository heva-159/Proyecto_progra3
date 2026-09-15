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

    private Usuario usuario;

    public Tutor(Usuario usuario) {
        postulaciones = new ArrayList<>();
        tarifas = new ArrayList<>();
        sesiones = new ArrayList<>();
        this.usuario = usuario;
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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
