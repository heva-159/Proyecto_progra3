package pe.edu.pucp.tutorPUCP.bl;

import pe.edu.pucp.tutorPUCP.modelo.interfaces.IAprobable;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Reclamo;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoAprobacion;
import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;

import java.util.ArrayList;
import java.util.List;

public class GestorReclamos {
    private List<Reclamo> reclamos = new ArrayList<>();

    public Reclamo registrarReclamo(Sesion sesion, Usuario reclamante, Rol rolReclamante, String motivo) {
        Reclamo reclamo = new Reclamo(reclamante, rolReclamante, motivo);
        sesion.agregarReclamo(reclamo);
        reclamos.add(reclamo);
        return reclamo;
    }

    public List<Reclamo> listarPendientes() {
        List<Reclamo> pendientes = new ArrayList<>();
        for (Reclamo reclamo : reclamos) {
            if (reclamo.getEstado() == EstadoAprobacion.PENDIENTE) {
                pendientes.add(reclamo);
            }
        }
        return pendientes;
    }

    public void resolverReclamo(IAprobable reclamo, Administrador administrador, boolean aprobar, String resolucion) {
        if (aprobar) reclamo.aprobar(administrador, resolucion);
        else reclamo.rechazar(administrador, resolucion);
    }
}
