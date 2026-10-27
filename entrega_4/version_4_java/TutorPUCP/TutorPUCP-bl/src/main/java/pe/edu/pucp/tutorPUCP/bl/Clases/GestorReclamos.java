package pe.edu.pucp.tutorPUCP.bl.Clases;

import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Reclamo;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.util.List;

public interface GestorReclamos {
    Reclamo registrarReclamo(Sesion sesion, Usuario reclamante, Rol rolReclamante, String motivo);
    // Solo se puede modificar un reclamo pendiente.
    Reclamo modificarReclamo(Reclamo reclamo);
    // Solo se puede eliminar un reclamo pendiente: los resueltos quedan como historial.
    void eliminarReclamo(Reclamo reclamo);
    // Devuelve null si no existe.
    Reclamo obtenerReclamoPorId(int idReclamo);
    List<Reclamo> listarReclamos();
    List<Reclamo> listarPendientes();
    List<Reclamo> listarPorSesion(Sesion sesion);
    void resolverReclamo(Reclamo reclamo, Administrador administrador, boolean aprobar, String resolucion);
}
