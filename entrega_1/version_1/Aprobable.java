public interface Aprobable {
    void aprobar();
    void rechazar(String motivo);
    EstadoAprobacion getEstado();
}
