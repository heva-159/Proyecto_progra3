package pe.edu.pucp.tutorPUCP.bl.Implementacion;

import pe.edu.pucp.tutorPUCP.bl.DAOs.UsuarioDAO;
import pe.edu.pucp.tutorPUCP.bl.Exeptions.BL_exeption;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorUsuarios;
import pe.edu.pucp.tutorPUCP.bl.Impl.UsuarioDAOImpl;
import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.util.List;

public class GestorUsuariosImpl implements GestorUsuarios {
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    @Override
    public Usuario registrar(String nombres, String apellidos, String codigoPucp, String facultad, String especialidad,
                              String correoInstitucional, String passwordHash, Rol rol) {
        if (rol == null) {
            throw new BL_exeption("Debe elegir al menos un rol");
        }
        if (usuarioDAO.buscarPorCorreo(correoInstitucional) != null) {
            throw new BL_exeption("Ya existe un usuario registrado con ese correo");
        }
        if (usuarioDAO.buscarPorCodigoPucp(codigoPucp) != null) {
            throw new BL_exeption("Ya existe un usuario registrado con ese codigo PUCP");
        }
        Usuario usuario = new Usuario(nombres, apellidos, codigoPucp, facultad, especialidad,
                correoInstitucional, passwordHash, rol);
        return usuarioDAO.save(usuario);
    }

    @Override
    public Usuario iniciarSesion(String correo, String password) {
        Usuario usuario = usuarioDAO.buscarPorCorreo(correo);
        if (usuario == null || !usuario.iniciarSesion(correo, password)) {
            return null;
        }
        return usuario;
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioDAO.listAll();
    }
}
