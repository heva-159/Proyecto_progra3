package pe.edu.pucp.tutorPUCP.modelo.interfaces;

public interface IServicioCorreo {
    void enviar(String destinatario, String asunto, String contenido);
}
