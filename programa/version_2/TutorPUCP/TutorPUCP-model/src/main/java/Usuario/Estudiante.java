package Usuario;

import Tutoria.Sesion;

import java.util.ArrayList;
import java.util.List;

public class Estudiante {
    private Usuario usuario;
    private List<Sesion> sesiones;
    //
    public Estudiante(Usuario usuario) {
        this.usuario=usuario;
        sesiones = new ArrayList<>();
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
