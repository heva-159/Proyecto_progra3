public abstract class PersonaPUCP {
    private String nombreCompleto;
    private String apellidos;

    public PersonaPUCP(String nombreCompleto, String apellidos) {
        this.nombreCompleto = nombreCompleto;
        this.apellidos = apellidos;
    }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
}
