package pe.edu.pucp.tutorPUCP.bl.Clases;

import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.util.List;

public interface GestorUsuarios {
    Usuario registrar(String nombres, String apellidos, String codigoPucp, String facultad, String especialidad,
                       String correoInstitucional, String passwordHash, Rol rol);

    Usuario iniciarSesion(String correo, String password);

    List<Usuario> listarUsuarios();

    Administrador registrarAdministrador(String nombre, String correo, String passwordHash, String nivelPermiso);

    Administrador iniciarSesionAdministrador(String correo, String password);
}
