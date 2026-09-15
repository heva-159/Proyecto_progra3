package Usuario;

import Interfaces.IAutenticable;
import enums.EstadoCuenta;
import enums.Rol;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Usuario implements IAutenticable {
    //
    private String nombreCompleto;
    private String apellidos;
    private int codigo; //codigo pucp
    private String facultad;
    private String especialidad;
    //
    private long idUsuario;
    private String correoInstitucional;
    private String passwordHash;
    private LocalDate fechaRegistro;
    private EstadoCuenta estado;
    private boolean consentimientoDatos;
    private boolean consentimientoCalificaciones;
    private boolean verificado;
    private String codigoVerificacion;
    private LocalDateTime codigoExpiracion;
    private Estudiante perfil_estudiante;
    private Tutor perfil_tutor;
    private Set<Rol> roles = new HashSet<>();

    public Usuario(String nombreCompleto, String apellidos, int codigo, String facultad, String especialidad,
                   String correoInstitucional, String passwordHash, boolean esTutor, boolean esEstudiante) {
        if (!esTutor && !esEstudiante) {
            throw new IllegalArgumentException("Debe elegir al menos un rol");
        }
        this.nombreCompleto=nombreCompleto;
        this.apellidos=apellidos;
        this.codigo=codigo;
        this.facultad=facultad;
        this.especialidad=especialidad;
        this.idUsuario = System.currentTimeMillis();
        this.correoInstitucional = correoInstitucional;
        this.passwordHash = passwordHash;
        this.fechaRegistro = LocalDate.now();
        this.estado = EstadoCuenta.ACTIVA;
        this.consentimientoDatos = false;
        this.consentimientoCalificaciones = false;
        this.verificado = false;
        if (esTutor) {
            perfil_tutor = new Tutor(nombreCompleto, apellidos);
            roles.add(Rol.TUTOR);
        }else{
            perfil_tutor = null;
        }
        if (esEstudiante) {
            perfil_estudiante = new Estudiante(nombreCompleto, apellidos);
            roles.add(Rol.ESTUDIANTE);
        }else{
            perfil_estudiante = null;
        }
    }

    @Override
    public boolean iniciarSesion(String correo, String password) {
        return estado == EstadoCuenta.ACTIVA
                && correoInstitucional.equalsIgnoreCase(correo)
                && passwordHash.equals(password); //faltaría implementar el hash de la contraseña
    }

    public long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(long idUsuario) { this.idUsuario = idUsuario; }

    public String getCorreoInstitucional() { return correoInstitucional; }
    public void setCorreoInstitucional(String correoInstitucional) { this.correoInstitucional = correoInstitucional; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public EstadoCuenta getEstado() { return estado; }
    public void setEstado(EstadoCuenta estado) { this.estado = estado; }

    public boolean isConsentimientoDatos() { return consentimientoDatos; }
    public void setConsentimientoDatos(boolean consentimientoDatos) { this.consentimientoDatos = consentimientoDatos; }

    public boolean isConsentimientoCalificaciones() { return consentimientoCalificaciones; }
    public void setConsentimientoCalificaciones(boolean consentimientoCalificaciones) { this.consentimientoCalificaciones = consentimientoCalificaciones; }

    public boolean isVerificado() { return verificado; }
    public void setVerificado(boolean verificado) { this.verificado = verificado; }

    public String getCodigoVerificacion() { return codigoVerificacion; }
    public void setCodigoVerificacion(String codigoVerificacion) { this.codigoVerificacion = codigoVerificacion; }

    public LocalDateTime getCodigoExpiracion() { return codigoExpiracion; }
    public void setCodigoExpiracion(LocalDateTime codigoExpiracion) { this.codigoExpiracion = codigoExpiracion; }

    public Set<Rol> getRoles() { return roles; }
    public void setRoles(Set<Rol> roles) { this.roles = roles; }

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
