package pe.edu.pucp.tutorPUCP.bl.Implementacion;

import pe.edu.pucp.tutorPUCP.bl.DAOs.MensajeDAO;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorMensajes;
import pe.edu.pucp.tutorPUCP.bl.Impl.MensajeDAOImpl;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Mensaje;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.util.List;

public class GestorMensajesImpl implements GestorMensajes {
    private final MensajeDAO mensajeDAO = new MensajeDAOImpl();

    @Override
    public Mensaje enviarMensaje(Usuario emisor, Usuario receptor, String contenido) {
        Mensaje mensaje = new Mensaje(emisor, receptor, contenido);
        return mensajeDAO.save(mensaje);
    }

    @Override
    public List<Mensaje> obtenerConversacion(Usuario usuario1, Usuario usuario2) {
        // el filtro "entre ambos usuarios" lo hace el DAO por SQL, no hace falta comparar en memoria.
        return mensajeDAO.listarEntreUsuarios(usuario1, usuario2);
    }
}
