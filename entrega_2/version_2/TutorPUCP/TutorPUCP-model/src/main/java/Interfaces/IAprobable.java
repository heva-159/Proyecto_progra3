package Interfaces;

import enums.EstadoAprobacion;

public interface IAprobable {
    void aprobar();
    void rechazar(String motivo);
    EstadoAprobacion getEstado();
}
