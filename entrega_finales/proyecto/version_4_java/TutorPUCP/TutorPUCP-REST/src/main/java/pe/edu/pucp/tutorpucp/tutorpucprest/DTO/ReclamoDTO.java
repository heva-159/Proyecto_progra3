package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import pe.edu.pucp.tutorPUCP.modelo.tutoria.Reclamo;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;
import pe.edu.pucp.tutorPUCP.modelo.usuario.PersonaPUCP;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

// Lo que devuelve /reclamo. Las fechas van como texto ISO ("2026-11-10").
public class ReclamoDTO {
    private int idReclamo;
    private int idSesion;
    private String curso;
    private String fechaSesion;
    private String nombreTutor;
    private String nombreEstudiante;
    private int idReclamante;
    private String nombreReclamante;
    private String rolReclamante;
    private String motivo;
    private String fechaReclamo;
    private String estado;
    private String resolucion;
    private String nombreAdministrador;
    private String fechaResolucion;

    public ReclamoDTO() {}

    public ReclamoDTO(Reclamo reclamo) {
        this.idReclamo = reclamo.getIdReclamo();
        Sesion sesion = reclamo.getSesion();
        if (sesion != null) {
            this.idSesion = sesion.getIdSesion();
            this.curso = sesion.getCurso() != null ? sesion.getCurso().getNombre() : null;
            this.fechaSesion = sesion.getFechaHoraInicio() != null ? sesion.getFechaHoraInicio().toString() : null;
            this.nombreTutor = nombre(sesion.getTutor());
            this.nombreEstudiante = nombre(sesion.getEstudiante());
        }
        if (reclamo.getReclamante() != null) {
            this.idReclamante = reclamo.getReclamante().getIdUsuario();
            this.nombreReclamante = nombre(reclamo.getReclamante());
        }
        this.rolReclamante = reclamo.getRolReclamante() != null ? reclamo.getRolReclamante().name() : null;
        this.motivo = reclamo.getMotivo();
        this.fechaReclamo = reclamo.getFechaReclamo() != null ? reclamo.getFechaReclamo().toString() : null;
        this.estado = reclamo.getEstado() != null ? reclamo.getEstado().name() : null;
        this.resolucion = reclamo.getResolucion();
        this.nombreAdministrador = reclamo.getAdministradorResuelve() != null ? reclamo.getAdministradorResuelve().getNombre() : null;
        this.fechaResolucion = reclamo.getFechaResolucion() != null ? reclamo.getFechaResolucion().toString() : null;
    }

    static String nombre(PersonaPUCP persona) {
        return persona != null ? nombre(persona.getUsuario()) : null;
    }

    static String nombre(Usuario usuario) {
        return usuario != null ? usuario.getNombres() + " " + usuario.getApellidos() : null;
    }

    // getters y setters

    public int getIdReclamo() { return idReclamo; }
    public void setIdReclamo(int idReclamo) { this.idReclamo = idReclamo; }

    public int getIdSesion() { return idSesion; }
    public void setIdSesion(int idSesion) { this.idSesion = idSesion; }

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }

    public String getFechaSesion() { return fechaSesion; }
    public void setFechaSesion(String fechaSesion) { this.fechaSesion = fechaSesion; }

    public String getNombreTutor() { return nombreTutor; }
    public void setNombreTutor(String nombreTutor) { this.nombreTutor = nombreTutor; }

    public String getNombreEstudiante() { return nombreEstudiante; }
    public void setNombreEstudiante(String nombreEstudiante) { this.nombreEstudiante = nombreEstudiante; }

    public int getIdReclamante() { return idReclamante; }
    public void setIdReclamante(int idReclamante) { this.idReclamante = idReclamante; }

    public String getNombreReclamante() { return nombreReclamante; }
    public void setNombreReclamante(String nombreReclamante) { this.nombreReclamante = nombreReclamante; }

    public String getRolReclamante() { return rolReclamante; }
    public void setRolReclamante(String rolReclamante) { this.rolReclamante = rolReclamante; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getFechaReclamo() { return fechaReclamo; }
    public void setFechaReclamo(String fechaReclamo) { this.fechaReclamo = fechaReclamo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getResolucion() { return resolucion; }
    public void setResolucion(String resolucion) { this.resolucion = resolucion; }

    public String getNombreAdministrador() { return nombreAdministrador; }
    public void setNombreAdministrador(String nombreAdministrador) { this.nombreAdministrador = nombreAdministrador; }

    public String getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(String fechaResolucion) { this.fechaResolucion = fechaResolucion; }
}
