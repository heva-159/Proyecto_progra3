package pe.edu.pucp.tutorPUCP.bl.Clases;

import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Reclamo;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.util.List;

public interface GestorReclamos {
    Reclamo registrarReclamo(Sesion sesion, Usuario reclamante, Rol rolReclamante, String motivo);
    List<Reclamo> listarPendientes();
    List<Reclamo> listarPorSesion(Sesion sesion);
    void resolverReclamo(Reclamo reclamo, Administrador administrador, boolean aprobar, String resolucion);
}
