package Servicio;

import Interfaces.IAprobable;
import Tutoria.Reclamo;
import Tutoria.Sesion;
import Usuario.Usuario;
import enums.EstadoAprobacion;

import java.util.ArrayList;
import java.util.List;

public class GestorReclamos {
    private List<Reclamo> reclamos = new ArrayList<>();

    public Reclamo registrarReclamo(Sesion sesion, Usuario reclamante, String motivo) {
        Reclamo reclamo = new Reclamo(sesion, reclamante, motivo);
        reclamos.add(reclamo);
        sesion.getReclamos().add(reclamo);
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

    public void resolverReclamo(IAprobable reclamo, boolean aprobar, String motivo) {
        if (aprobar) reclamo.aprobar();
        else reclamo.rechazar(motivo);
    }
}
