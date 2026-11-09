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
}
