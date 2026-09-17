package Tutoria;

import Interfaces.IAprobable;
import Usuario.Usuario;
import enums.EstadoAprobacion;
import enums.Rol;

import java.time.LocalDate;

public class Reclamo implements IAprobable {
    private static long contador = 0;

    private long idReclamo;
    private Sesion sesion;
    private Usuario reclamante;
    private Rol rolReclamante;
    private String motivo;
    private LocalDate fechaReclamo;
    private EstadoAprobacion estado;
    private String resolucion;

    public Reclamo(Sesion sesion, Usuario reclamante, Rol rolReclamante, String motivo) {
        this.idReclamo = ++contador;
        this.sesion = sesion;
        this.reclamante = reclamante;
        this.rolReclamante = rolReclamante;
        this.motivo = motivo;
        this.fechaReclamo = LocalDate.now();
        this.estado = EstadoAprobacion.PENDIENTE;
    }

    @Override
    public void aprobar() {
        this.estado = EstadoAprobacion.APROBADA;
    }

    @Override
    public void rechazar(String motivo) {
        this.estado = EstadoAprobacion.RECHAZADA;
        this.resolucion = motivo;
    }

    @Override
    public EstadoAprobacion getEstado() { return estado; }
    public void setEstado(EstadoAprobacion estado) { this.estado = estado; }

    public long getIdReclamo() { return idReclamo; }
    public void setIdReclamo(long idReclamo) { this.idReclamo = idReclamo; }

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
}
