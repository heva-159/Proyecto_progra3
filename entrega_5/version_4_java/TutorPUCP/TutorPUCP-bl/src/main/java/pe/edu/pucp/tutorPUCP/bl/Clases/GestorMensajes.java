package pe.edu.pucp.tutorPUCP.bl.Clases;

import pe.edu.pucp.tutorPUCP.modelo.tutoria.Mensaje;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.util.List;

public interface GestorMensajes {
    Mensaje enviarMensaje(Usuario emisor, Usuario receptor, String contenido);
    Mensaje modificarMensaje(Mensaje mensaje);
    void eliminarMensaje(Mensaje mensaje);
    // Devuelve null si no existe.
    Mensaje obtenerMensajePorId(int idMensaje);
    List<Mensaje> listarMensajes();
    List<Mensaje> obtenerConversacion(Usuario usuario1, Usuario usuario2);
    // Mensajes enviados o recibidos por el usuario (para armar su bandeja), en orden de envio.
    List<Mensaje> listarMensajesDeUsuario(Usuario usuario);
    // Solo el receptor puede marcar un mensaje como leido.
    void marcarComoLeido(Mensaje mensaje, Usuario lector);
    // Marca como leidos los mensajes que "otro" le envio a "lector". Devuelve cuantos se marcaron.
    int marcarConversacionComoLeida(Usuario lector, Usuario otro);
}
