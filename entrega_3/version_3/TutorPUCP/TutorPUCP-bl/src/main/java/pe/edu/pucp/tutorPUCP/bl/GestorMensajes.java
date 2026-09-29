package pe.edu.pucp.tutorPUCP.bl;

import pe.edu.pucp.tutorPUCP.modelo.tutoria.Mensaje;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.util.ArrayList;
import java.util.List;

public class GestorMensajes {
    private List<Mensaje> mensajes = new ArrayList<>();

    public Mensaje enviarMensaje(Usuario emisor, Usuario receptor, String contenido) {
        Mensaje mensaje = new Mensaje(emisor, receptor, contenido);
        mensajes.add(mensaje);
        return mensaje;
    }

    public List<Mensaje> obtenerConversacion(Usuario usuario1, Usuario usuario2) {
        List<Mensaje> conversacion = new ArrayList<>();
        for (Mensaje mensaje : mensajes) {
            boolean esEntreAmbos = (mensaje.getEmisor() == usuario1 && mensaje.getReceptor() == usuario2)
                    || (mensaje.getEmisor() == usuario2 && mensaje.getReceptor() == usuario1);
            if (esEntreAmbos) conversacion.add(mensaje);
        }
        return conversacion;
    }
}
