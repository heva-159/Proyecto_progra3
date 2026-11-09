package pe.edu.pucp.tutorPUCP.modelo.usuario;

import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoCuenta;
import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Usuario {
    private int idUsuario;
    private String nombres;
    private String apellidos;
    private String codigoPucp;
    private String facultad;
    private String especialidad;
    private String correoInstitucional;
    private String passwordHash;
    private LocalDate fechaRegistro;
    private EstadoCuenta estado;
    private boolean correoVerificado;
    private String codigoVerificacion;
    private LocalDateTime fechaExpiracionCodigo;
    private boolean consentimientoDatos;
    private boolean consentimientoCalificaciones;
    private List<PersonaPUCP> personas = new ArrayList<>();

    public Usuario() {
        this.estado = EstadoCuenta.ACTIVA;
    }

    public Usuario(String nombres, String apellidos, String codigoPucp, String facultad, String especialidad,
                   String correoInstitucional, String passwordHash, Rol rol) {
        if (rol == null) {
            throw new IllegalArgumentException("Debe elegir al menos un rol");
        }
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.codigoPucp = codigoPucp;
        this.facultad = facultad;
        this.especialidad = especialidad;
        this.correoInstitucional = correoInstitucional;
        this.passwordHash = passwordHash;
        this.fechaRegistro = LocalDate.now();
        this.estado = EstadoCuenta.ACTIVA;
        this.correoVerificado = false;
        if (rol == Rol.TUTOR || rol == Rol.AMBOS) agregarPerfil(new Tutor());
        if (rol == Rol.ESTUDIANTE || rol == Rol.AMBOS) agregarPerfil(new Estudiante());
    }

    public void agregarPerfil(PersonaPUCP persona) {
        persona.setUsuario(this);
        personas.add(persona);
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

    // El rol no se guarda: se deduce de los perfiles que tiene el usuario.
    public Rol getRol() {
        boolean esTutor = getPerfilTutor() != null;
        boolean esEstudiante = getPerfilEstudiante() != null;
        if (esTutor && esEstudiante) return Rol.AMBOS;
        if (esTutor) return Rol.TUTOR;
        if (esEstudiante) return Rol.ESTUDIANTE;
        return null;
    }

    // Solo lectura: los perfiles se agregan con agregarPerfil().
    public List<PersonaPUCP> getPersonas() { return Collections.unmodifiableList(personas); }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCodigoPucp() { return codigoPucp; }
    public void setCodigoPucp(String codigoPucp) { this.codigoPucp = codigoPucp; }

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

    public boolean isCorreoVerificado() { return correoVerificado; }
    public void setCorreoVerificado(boolean correoVerificado) { this.correoVerificado = correoVerificado; }

    public String getCodigoVerificacion() { return codigoVerificacion; }
    public void setCodigoVerificacion(String codigoVerificacion) { this.codigoVerificacion = codigoVerificacion; }

    public LocalDateTime getFechaExpiracionCodigo() { return fechaExpiracionCodigo; }
    public void setFechaExpiracionCodigo(LocalDateTime fechaExpiracionCodigo) { this.fechaExpiracionCodigo = fechaExpiracionCodigo; }

    public boolean isConsentimientoDatos() { return consentimientoDatos; }
    public void setConsentimientoDatos(boolean consentimientoDatos) { this.consentimientoDatos = consentimientoDatos; }

    public boolean isConsentimientoCalificaciones() { return consentimientoCalificaciones; }
    public void setConsentimientoCalificaciones(boolean consentimientoCalificaciones) { this.consentimientoCalificaciones = consentimientoCalificaciones; }
}
