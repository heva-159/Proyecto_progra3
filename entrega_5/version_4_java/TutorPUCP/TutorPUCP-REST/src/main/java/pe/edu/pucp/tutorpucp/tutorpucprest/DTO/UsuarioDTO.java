package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoCuenta;
import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

public class UsuarioDTO {
    private int idUsuario;
    private String nombres;
    private String apellidos;
    private String codigoPucp;
    private String facultad;
    private String especialidad;
    private String correoInstitucional;
    private EstadoCuenta estado;
    private Rol rol;

    public UsuarioDTO() {
    }

    public UsuarioDTO(Usuario usuario) {
        this.idUsuario = usuario.getIdUsuario();
        this.nombres = usuario.getNombres();
        this.apellidos = usuario.getApellidos();
        this.codigoPucp = usuario.getCodigoPucp();
        this.facultad = usuario.getFacultad();
        this.especialidad = usuario.getEspecialidad();
        this.correoInstitucional = usuario.getCorreoInstitucional();
        this.estado = usuario.getEstado();
        this.rol = usuario.getRol();
    }

    // getters y setters

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCodigoPucp() {
        return codigoPucp;
    }

    public void setCodigoPucp(String codigoPucp) {
        this.codigoPucp = codigoPucp;
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

    public String getCorreoInstitucional() {
        return correoInstitucional;
    }

    public void setCorreoInstitucional(String correoInstitucional) {
        this.correoInstitucional = correoInstitucional;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    public void setEstado(EstadoCuenta estado) {
        this.estado = estado;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}