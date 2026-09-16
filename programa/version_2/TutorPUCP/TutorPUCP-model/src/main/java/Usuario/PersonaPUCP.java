package Usuario;

import Tutoria.Sesion;

import java.util.List;

public abstract class PersonaPUCP {
    private Usuario usuario;

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public abstract List<Sesion> getSesiones();
}
