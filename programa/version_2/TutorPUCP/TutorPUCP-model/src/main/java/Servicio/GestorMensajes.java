package Servicio;

import Tutoria.Mensaje;
import Usuario.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class GestorMensajes {
    private List<Mensaje> mensajes = new ArrayList<>();

    public Mensaje enviarMensaje(Usuario emisor, Usuario receptor, String contenido, LocalDateTime fechaEnvio, boolean leido) {
        Mensaje mensaje = new Mensaje(emisor, receptor, contenido, fechaEnvio, leido);
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
