package Usuario;

import Interfaces.IAutenticable;
import enums.EstadoCuenta;
import enums.Rol;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Usuario implements IAutenticable {
    private static long contador = 0;

    private long idUsuario;
    private String nombres;
    private String apellidos;
    private int codigo;
    private String facultad;
    private String especialidad;
    private String correoInstitucional;
    private String passwordHash;
    private LocalDate fechaRegistro;
    private EstadoCuenta estado;
    private boolean verificado;
    private String codigoVerificacion;
    private LocalDateTime codigoExpiracion;
    private List<PersonaPUCP> personas;
    private Rol rol;
    private boolean consentimientoDatos;
    private boolean consentimientoCalificaciones;

    public Usuario(String nombres, String apellidos, int codigo, String facultad, String especialidad,
                   String correoInstitucional, String passwordHash, Rol rol) {
        if (rol == null) {
            throw new IllegalArgumentException("Debe elegir al menos un rol");
        }
        this.idUsuario = ++contador;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.codigo = codigo;
        this.facultad = facultad;
        this.especialidad = especialidad;
        this.correoInstitucional = correoInstitucional;
        this.passwordHash = passwordHash;
        this.fechaRegistro = LocalDate.now();
        this.estado = EstadoCuenta.ACTIVA;
        this.verificado = false;
        this.rol = rol;
        this.personas = new ArrayList<>();
        if (rol == Rol.TUTOR || rol == Rol.AMBOS) {
            Tutor tutor = new Tutor();
            tutor.setUsuario(this);
            personas.add(tutor);
        }
        if (rol == Rol.ESTUDIANTE || rol == Rol.AMBOS) {
            Estudiante estudiante = new Estudiante();
            estudiante.setUsuario(this);
            personas.add(estudiante);
        }
    }

    @Override
    public boolean iniciarSesion(String correo, String password) {
        return estado == EstadoCuenta.ACTIVA
                && correoInstitucional.equalsIgnoreCase(correo)
                && passwordHash.equals(password); //faltaría implementar el hash de la contraseña
    }

    public Tutor getPerfilTutor() {
        for (PersonaPUCP persona : personas) {
            if (persona instanceof Tutor tutor) return tutor;
        }
        return null;
    }

    public Estudiante getPerfilEstudiante() {
        for (PersonaPUCP persona : personas) {
            if (persona instanceof Estudiante estudiante) return estudiante;
        }
        return null;
    }

    public List<PersonaPUCP> getPersonas() { return personas; }

    public long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(long idUsuario) { this.idUsuario = idUsuario; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public int getCodigo() { return codigo; }
    public void setCodigo(int codigo) { this.codigo = codigo; }

    public String getFacultad() { return facultad; }
    public void setFacultad(String facultad) { this.facultad = facultad; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

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

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
}
