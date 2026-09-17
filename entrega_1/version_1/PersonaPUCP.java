public abstract class PersonaPUCP {
    private String nombreCompleto;
    private String apellidos;
    private int codigo; //codigo pucp
    private String facultad;
    private String especialidad;
    //
    public PersonaPUCP(String nombreCompleto, String apellidos) {
        this.nombreCompleto = nombreCompleto;
        this.apellidos = apellidos;
    }
    // Getters and Setters
    public String getNombreCompleto() {
        return nombreCompleto;
    }
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
    public String getApellidos() {
        return apellidos;
    }
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }
    public int getCodigo() {
        return codigo;
    }
    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }
    public String getFacultad() {
        return facultad;
    }
    public void setFacultad(String facultad) {
        this.facultad = facultad;
    }
    public String getEspecialidad() {
        return especialidad;
    }
    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }
    
}
