package pe.edu.pucp.tutorPUCP.modelo.usuario;

import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PersonaPUCP {
    private Usuario usuario;
    private List<Sesion> sesiones = new ArrayList<>();

    // no repite la sesion si ya estaba: Sesion se agrega sola al crearse o al asignarle participantes
    public void agregarSesion(Sesion sesion) {
        if (!sesiones.contains(sesion)) sesiones.add(sesion);
    }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    // Solo lectura: las sesiones se agregan con agregarSesion().
    public List<Sesion> getSesiones() { return Collections.unmodifiableList(sesiones); }
    public void setSesiones(List<Sesion> sesiones) { this.sesiones = new ArrayList<>(sesiones); }
}
