package pe.edu.pucp.tutorPUCP.modelo.tutoria;

import pe.edu.pucp.tutorPUCP.modelo.interfaces.IAprobable;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoAprobacion;
import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;

import java.time.LocalDate;

public class Reclamo implements IAprobable {
    private int idReclamo;
    private Sesion sesion;
    private Usuario reclamante;
    private Rol rolReclamante;
    private String motivo;
    private LocalDate fechaReclamo;
    private EstadoAprobacion estado;
    private String resolucion;
    private Administrador administradorResuelve;
    private LocalDate fechaResolucion;

    public Reclamo() {
        this.estado = EstadoAprobacion.PENDIENTE;
    }

    public Reclamo(Usuario reclamante, Rol rolReclamante, String motivo) {
        this.reclamante = reclamante;
        this.rolReclamante = rolReclamante;
        this.motivo = motivo;
        this.fechaReclamo = LocalDate.now();
        this.estado = EstadoAprobacion.PENDIENTE;
    }

    @Override
    public void aprobar(Administrador administrador, String resolucion) {
        this.estado = EstadoAprobacion.APROBADA;
        this.administradorResuelve = administrador;
        this.fechaResolucion = fechaDeResolucion();
        this.resolucion = resolucion;
    }

    @Override
    public void rechazar(Administrador administrador, String motivo) {
        this.estado = EstadoAprobacion.RECHAZADA;
        this.administradorResuelve = administrador;
        this.fechaResolucion = fechaDeResolucion();
        this.resolucion = motivo;
    }

    // La BD (RDS en UTC) pone fecha_reclamo con curdate() y Java usa la hora de Peru: de noche la BD ya va un dia
    // adelante y ck_reclamo_fechas (fecha_resolucion >= fecha_reclamo) fallaba. Nunca se resuelve antes del reclamo.
    private LocalDate fechaDeResolucion() {
        LocalDate hoy = LocalDate.now();
        return fechaReclamo != null && hoy.isBefore(fechaReclamo) ? fechaReclamo : hoy;
    }

    @Override
    public EstadoAprobacion getEstado() { return estado; }
    public void setEstado(EstadoAprobacion estado) { this.estado = estado; }

    public int getIdReclamo() { return idReclamo; }
    public void setIdReclamo(int idReclamo) { this.idReclamo = idReclamo; }

    public Sesion getSesion() { return sesion; }
    public void setSesion(Sesion sesion) { this.sesion = sesion; }

    public Usuario getReclamante() { return reclamante; }
    public void setReclamante(Usuario reclamante) { this.reclamante = reclamante; }

    public Rol getRolReclamante() { return rolReclamante; }
    public void setRolReclamante(Rol rolReclamante) { this.rolReclamante = rolReclamante; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public LocalDate getFechaReclamo() { return fechaReclamo; }
    public void setFechaReclamo(LocalDate fechaReclamo) { this.fechaReclamo = fechaReclamo; }

    public String getResolucion() { return resolucion; }
    public void setResolucion(String resolucion) { this.resolucion = resolucion; }

    public Administrador getAdministradorResuelve() { return administradorResuelve; }
    public void setAdministradorResuelve(Administrador administradorResuelve) { this.administradorResuelve = administradorResuelve; }

    public LocalDate getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDate fechaResolucion) { this.fechaResolucion = fechaResolucion; }
}
