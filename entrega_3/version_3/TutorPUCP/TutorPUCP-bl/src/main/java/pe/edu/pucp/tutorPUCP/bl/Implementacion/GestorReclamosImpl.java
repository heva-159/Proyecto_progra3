package pe.edu.pucp.tutorPUCP.bl.Implementacion;

import pe.edu.pucp.tutorPUCP.bl.DAOs.ReclamoDAO;
import pe.edu.pucp.tutorPUCP.bl.Exeptions.BL_exeption;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorReclamos;
import pe.edu.pucp.tutorPUCP.bl.Impl.ReclamoDAOImpl;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoAprobacion;
import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Reclamo;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.util.List;

public class GestorReclamosImpl implements GestorReclamos {
    private final ReclamoDAO reclamoDAO = new ReclamoDAOImpl();

    @Override
    public Reclamo registrarReclamo(Sesion sesion, Usuario reclamante, Rol rolReclamante, String motivo) {
        Reclamo reclamo = new Reclamo(reclamante, rolReclamante, motivo);
        sesion.agregarReclamo(reclamo);
        return reclamoDAO.save(reclamo);
    }

    @Override
    public List<Reclamo> listarPendientes() {
        return reclamoDAO.listarPendientes();
    }

    @Override
    public List<Reclamo> listarPorSesion(Sesion sesion) {
        return reclamoDAO.listarPorSesion(sesion);
    }

    @Override
    public void resolverReclamo(Reclamo reclamo, Administrador administrador, boolean aprobar, String resolucion) {
        if (reclamo.getEstado() != EstadoAprobacion.PENDIENTE) {
            throw new BL_exeption("Solo se pueden resolver reclamos pendientes");
        }
        if (aprobar) reclamo.aprobar(administrador, resolucion);
        else reclamo.rechazar(administrador, resolucion);
        reclamoDAO.update(reclamo);
    }
}
