package pe.edu.pucp.tutorPUCP.bl.Implementacion;

import pe.edu.pucp.tutorPUCP.dao.DAOs.ReclamoDAO;
import pe.edu.pucp.tutorPUCP.bl.Exeptions.BL_exeption;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorReclamos;
import pe.edu.pucp.tutorPUCP.dao.Impl.ReclamoDAOImpl;
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
        if (rolReclamante == Rol.TUTOR) {
            if (sesion.getTutor().getUsuario().getIdUsuario() != reclamante.getIdUsuario()) {
                throw new BL_exeption("El reclamante no es el tutor de esta sesion");
            }
        } else if (rolReclamante == Rol.ESTUDIANTE) {
            if (sesion.getEstudiante().getUsuario().getIdUsuario() != reclamante.getIdUsuario()) {
                throw new BL_exeption("El reclamante no es el estudiante de esta sesion");
            }
        } else {
            throw new BL_exeption("El rol del reclamante debe ser TUTOR o ESTUDIANTE, nunca AMBOS");
        }
        boolean yaReclamo = reclamoDAO.listarPorSesion(sesion).stream()
                .anyMatch(r -> r.getReclamante().getIdUsuario() == reclamante.getIdUsuario());
        if (yaReclamo) {
            throw new BL_exeption("Este usuario ya registro un reclamo para esta sesion");
        }
        Reclamo reclamo = new Reclamo(reclamante, rolReclamante, motivo);
        sesion.agregarReclamo(reclamo);
        return reclamoDAO.save(reclamo);
    }

    @Override
    public Reclamo modificarReclamo(Reclamo reclamo) {
        Reclamo guardado = reclamoDAO.load(reclamo.getIdReclamo());
        if (guardado == null) {
            throw new BL_exeption("El reclamo no existe");
        }
        if (guardado.getEstado() != EstadoAprobacion.PENDIENTE) {
            throw new BL_exeption("Solo se puede modificar un reclamo pendiente");
        }
        if (reclamo.getMotivo() == null || reclamo.getMotivo().isBlank()) {
            throw new BL_exeption("El motivo del reclamo es obligatorio");
        }
        return reclamoDAO.update(reclamo);
    }

    @Override
    public void eliminarReclamo(Reclamo reclamo) {
        Reclamo guardado = reclamoDAO.load(reclamo.getIdReclamo());
        if (guardado == null) {
            throw new BL_exeption("El reclamo no existe");
        }
        if (guardado.getEstado() != EstadoAprobacion.PENDIENTE) {
            throw new BL_exeption("Solo se puede eliminar un reclamo pendiente");
        }
        reclamoDAO.remove(reclamo);
    }

    @Override
    public Reclamo obtenerReclamoPorId(int idReclamo) {
        return reclamoDAO.load(idReclamo);
    }

    @Override
    public List<Reclamo> listarReclamos() {
        return reclamoDAO.listAll();
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
        // a diferencia de postulacion, ck_reclamo_resolucion exige motivo tanto al aprobar como al rechazar.
        if (resolucion == null || resolucion.isBlank()) {
            throw new BL_exeption("Debe indicar una resolucion, tanto para aprobar como para rechazar el reclamo");
        }
        if (aprobar) reclamo.aprobar(administrador, resolucion);
        else reclamo.rechazar(administrador, resolucion);
        reclamoDAO.update(reclamo);
    }
}
