package pe.edu.pucp.tutorPUCP.modelo.interfaces;

import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoAprobacion;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;

public interface IAprobable {
    void aprobar(Administrador administrador, String resolucion);
    void rechazar(Administrador administrador, String motivo);
    EstadoAprobacion getEstado();
}
