package pe.edu.pucp.tutorPUCP.bl.Clases;

import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Estudiante;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.util.List;

public interface GestorUsuarios {
    // Recibe la contrasena sin cifrar y guarda su hash. El usuario y sus perfiles
    // (tutor / estudiante) se guardan en una sola transaccion.
    Usuario registrar(String nombres, String apellidos, String codigoPucp, String facultad, String especialidad,
                       String correoInstitucional, String password, Rol rol);

    // Modifica el usuario y deja en la BD los mismos perfiles que tiene el objeto (una sola transaccion).
    Usuario modificarUsuario(Usuario usuario);

    // Eliminacion logica: la cuenta queda BLOQUEADA.
    void eliminarUsuario(Usuario usuario);

    // Devuelve null si no existe.
    Usuario obtenerUsuarioPorId(int idUsuario);

    List<Usuario> listarUsuarios();

    // Devuelve el usuario si el correo y la contrasena coinciden y la cuenta esta activa; si no, null.
    Usuario iniciarSesion(String correo, String password);

    // Cambia la contrasena si la actual coincide (la cuenta debe estar activa). La nueva se guarda como hash.
    void cambiarPassword(int idUsuario, String passwordActual, String passwordNueva);

    // ---- Perfil Tutor ----

    // Le agrega el perfil de tutor a un usuario que ya existe.
    Tutor registrarTutor(Usuario usuario);
    Tutor modificarTutor(Tutor tutor);
    void eliminarTutor(Tutor tutor);
    Tutor obtenerTutorPorId(int idUsuario);
    List<Tutor> listarTutores();

    // ---- Perfil Estudiante ----

    // Le agrega el perfil de estudiante a un usuario que ya existe.
    Estudiante registrarEstudiante(Usuario usuario);
    Estudiante modificarEstudiante(Estudiante estudiante);
    void eliminarEstudiante(Estudiante estudiante);
    Estudiante obtenerEstudiantePorId(int idUsuario);
    List<Estudiante> listarEstudiantes();

    // ---- Administrador ----

    // Recibe la contrasena sin cifrar y guarda su hash.
    Administrador registrarAdministrador(String nombre, String correo, String password, String nivelPermiso);
    Administrador modificarAdministrador(Administrador administrador);
    void eliminarAdministrador(Administrador administrador);
    Administrador obtenerAdministradorPorId(int idAdministrador);
    List<Administrador> listarAdministradores();

    Administrador iniciarSesionAdministrador(String correo, String password);

    // Igual que cambiarPassword, para el administrador.
    void cambiarPasswordAdministrador(int idAdministrador, String passwordActual, String passwordNueva);
}
