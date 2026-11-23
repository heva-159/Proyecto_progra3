package pe.edu.pucp.tutorPUCP.bl.Implementacion;

import pe.edu.pucp.tutorPUCP.dao.DAOs.MensajeDAO;
import pe.edu.pucp.tutorPUCP.bl.Exeptions.BL_exeption;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorMensajes;
import pe.edu.pucp.tutorPUCP.dao.Impl.MensajeDAOImpl;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoCuenta;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Mensaje;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.util.List;

public class GestorMensajesImpl implements GestorMensajes {
    private final MensajeDAO mensajeDAO = new MensajeDAOImpl();

    @Override
    public Mensaje enviarMensaje(Usuario emisor, Usuario receptor, String contenido) {
        if (emisor.getIdUsuario() == receptor.getIdUsuario()) {
            throw new BL_exeption("El emisor y el receptor no pueden ser el mismo usuario");
        }
        if (emisor.getEstado() != EstadoCuenta.ACTIVA || receptor.getEstado() != EstadoCuenta.ACTIVA) {
            throw new BL_exeption("Ambos usuarios deben tener la cuenta activa");
        }
        if (contenido == null || contenido.isBlank()) {
            throw new BL_exeption("El mensaje no puede estar vacio");
        }
        Mensaje mensaje = new Mensaje(emisor, receptor, contenido);
        return mensajeDAO.save(mensaje);
    }

    @Override
    public Mensaje modificarMensaje(Mensaje mensaje) {
        if (mensajeDAO.load(mensaje.getIdMensaje()) == null) {
            throw new BL_exeption("El mensaje no existe");
        }
        if (mensaje.getContenido() == null || mensaje.getContenido().isBlank()) {
            throw new BL_exeption("El mensaje no puede estar vacio");
        }
        return mensajeDAO.update(mensaje);
    }

    @Override
    public void eliminarMensaje(Mensaje mensaje) {
        if (mensajeDAO.load(mensaje.getIdMensaje()) == null) {
            throw new BL_exeption("El mensaje no existe");
        }
        mensajeDAO.remove(mensaje);
    }

    @Override
    public Mensaje obtenerMensajePorId(int idMensaje) {
        return mensajeDAO.load(idMensaje);
    }

    @Override
    public List<Mensaje> listarMensajes() {
        return mensajeDAO.listAll();
    }

    @Override
    public List<Mensaje> obtenerConversacion(Usuario usuario1, Usuario usuario2) {
        // el filtro "entre ambos usuarios" lo hace el DAO por SQL, no hace falta comparar en memoria.
        return mensajeDAO.listarEntreUsuarios(usuario1, usuario2);
    }

    @Override
    public List<Mensaje> listarMensajesDeUsuario(Usuario usuario) {
        int id = usuario.getIdUsuario();
        return mensajeDAO.listAll().stream()
                .filter(m -> m.getEmisor().getIdUsuario() == id || m.getReceptor().getIdUsuario() == id)
                .toList();
    }

    @Override
    public void marcarComoLeido(Mensaje mensaje, Usuario lector) {
        if (mensaje.getReceptor().getIdUsuario() != lector.getIdUsuario()) {
            throw new BL_exeption("Solo el receptor puede marcar el mensaje como leido");
        }
        if (mensaje.isLeido()) return;
        mensaje.marcarComoLeido();
        mensajeDAO.update(mensaje);
    }

    @Override
    public int marcarConversacionComoLeida(Usuario lector, Usuario otro) {
        int marcados = 0;
        for (Mensaje mensaje : mensajeDAO.listarEntreUsuarios(lector, otro)) {
            if (mensaje.getReceptor().getIdUsuario() == lector.getIdUsuario() && !mensaje.isLeido()) {
                mensaje.marcarComoLeido();
                mensajeDAO.update(mensaje);
                marcados++;
            }
        }
        return marcados;
    }
}
