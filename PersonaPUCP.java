public abstract class PersonaPUCP {
    private Usuario usuario;

    public PersonaPUCP(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
