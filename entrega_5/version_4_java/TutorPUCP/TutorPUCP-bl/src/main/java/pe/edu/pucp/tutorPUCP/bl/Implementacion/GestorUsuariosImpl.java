package pe.edu.pucp.tutorPUCP.bl.Implementacion;

import pe.edu.pucp.tutorPUCP.bl.Clases.GestorUsuarios;
import pe.edu.pucp.tutorPUCP.bl.Exeptions.BL_exeption;
import pe.edu.pucp.tutorPUCP.bl.Seguridad.PasswordHasher;
import pe.edu.pucp.tutorPUCP.dao.DAOs.AdministradorDAO;
import pe.edu.pucp.tutorPUCP.dao.DAOs.EstudianteDAO;
import pe.edu.pucp.tutorPUCP.dao.DAOs.TutorDAO;
import pe.edu.pucp.tutorPUCP.dao.DAOs.UsuarioDAO;
import pe.edu.pucp.tutorPUCP.dao.Impl.AdministradorDAOImpl;
import pe.edu.pucp.tutorPUCP.dao.Impl.EstudianteDAOImpl;
import pe.edu.pucp.tutorPUCP.dao.Impl.TutorDAOImpl;
import pe.edu.pucp.tutorPUCP.dao.Impl.UsuarioDAOImpl;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoCuenta;
import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Estudiante;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.sql.SQLException;
import java.util.List;

public class GestorUsuariosImpl implements GestorUsuarios {
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private final TutorDAO tutorDAO = new TutorDAOImpl();
    private final EstudianteDAO estudianteDAO = new EstudianteDAOImpl();
    private final AdministradorDAO administradorDAO = new AdministradorDAOImpl();

    // ---- Usuario ----

    @Override
    public Usuario registrar(String nombres, String apellidos, String codigoPucp, String facultad, String especialidad,
                              String correoInstitucional, String password, Rol rol) {
        if (rol == null) {
            throw new BL_exeption("Debe elegir al menos un rol");
        }
        if (password == null || password.isBlank()) {
            throw new BL_exeption("La contrasena es obligatoria");
        }
        if (correoInstitucional == null || !correoInstitucional.toLowerCase().endsWith("@pucp.edu.pe")) {
            throw new BL_exeption("El correo debe ser institucional (@pucp.edu.pe)");
        }
        if (codigoPucp == null || !codigoPucp.matches("[0-9]{8}")) {
            throw new BL_exeption("El codigo PUCP debe tener exactamente 8 digitos");
        }
        if (usuarioDAO.buscarPorCorreo(correoInstitucional) != null) {
            throw new BL_exeption("Ya existe un usuario registrado con ese correo");
        }
        if (usuarioDAO.buscarPorCodigoPucp(codigoPucp) != null) {
            throw new BL_exeption("Ya existe un usuario registrado con ese codigo PUCP");
        }
        Usuario usuario = new Usuario(nombres, apellidos, codigoPucp, facultad, especialidad,
                correoInstitucional, PasswordHasher.hashear(password), rol);

        // la transaccion la abre esta capa: si falla un perfil, no queda el usuario guardado sin el
        try (TransactionContext tx = new TransactionContext()) {
            usuarioDAO.save(usuario, tx);
            if (usuario.getPerfilTutor() != null) {
                tutorDAO.save(usuario.getPerfilTutor(), tx);
            }
            if (usuario.getPerfilEstudiante() != null) {
                estudianteDAO.save(usuario.getPerfilEstudiante(), tx);
            }
            tx.commit();
            return usuario;
        } catch (SQLException e) {
            throw new BL_exeption("No se pudo registrar el usuario con sus perfiles: " + e.getMessage());
        }
    }

    @Override
    public Usuario modificarUsuario(Usuario usuario) {
        Usuario guardado = usuarioDAO.load(usuario.getIdUsuario());
        if (guardado == null) {
            throw new BL_exeption("El usuario no existe");
        }
        if (usuario.getPerfilTutor() == null && usuario.getPerfilEstudiante() == null) {
            throw new BL_exeption("El usuario debe tener al menos un perfil (tutor o estudiante)");
        }
        Usuario conMismoCorreo = usuarioDAO.buscarPorCorreo(usuario.getCorreoInstitucional());
        if (conMismoCorreo != null && conMismoCorreo.getIdUsuario() != usuario.getIdUsuario()) {
            throw new BL_exeption("Ya existe otro usuario con ese correo");
        }
        Usuario conMismoCodigo = usuarioDAO.buscarPorCodigoPucp(usuario.getCodigoPucp());
        if (conMismoCodigo != null && conMismoCodigo.getIdUsuario() != usuario.getIdUsuario()) {
            throw new BL_exeption("Ya existe otro usuario con ese codigo PUCP");
        }

        try (TransactionContext tx = new TransactionContext()) {
            usuarioDAO.update(usuario, tx);
            sincronizarPerfiles(usuario, guardado, tx);
            tx.commit();
            return usuario;
        } catch (SQLException e) {
            throw new BL_exeption("No se pudo modificar el usuario: " + e.getMessage());
        }
    }

    // agrega los perfiles nuevos y quita los que el objeto ya no tiene, comparando con lo guardado en la BD
    private void sincronizarPerfiles(Usuario usuario, Usuario guardado, TransactionContext tx) {
        boolean esTutor = usuario.getPerfilTutor() != null;
        boolean eraTutor = guardado.getPerfilTutor() != null;
        if (esTutor && !eraTutor) tutorDAO.save(usuario.getPerfilTutor(), tx);
        if (!esTutor && eraTutor) tutorDAO.remove(guardado.getPerfilTutor(), tx);

        boolean esEstudiante = usuario.getPerfilEstudiante() != null;
        boolean eraEstudiante = guardado.getPerfilEstudiante() != null;
        if (esEstudiante && !eraEstudiante) estudianteDAO.save(usuario.getPerfilEstudiante(), tx);
        if (!esEstudiante && eraEstudiante) estudianteDAO.remove(guardado.getPerfilEstudiante(), tx);
    }

    // eliminacion logica: el DAO deja la cuenta BLOQUEADA
    @Override
    public void eliminarUsuario(Usuario usuario) {
        if (usuarioDAO.load(usuario.getIdUsuario()) == null) {
            throw new BL_exeption("El usuario no existe");
        }
        usuarioDAO.remove(usuario);
    }

    @Override
    public Usuario obtenerUsuarioPorId(int idUsuario) {
        return usuarioDAO.load(idUsuario);
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioDAO.listAll();
    }

    @Override
    public Usuario iniciarSesion(String correo, String password) {
        Usuario usuario = usuarioDAO.buscarPorCorreo(correo);
        if (usuario == null
                || usuario.getEstado() != EstadoCuenta.ACTIVA
                || !PasswordHasher.coincide(password, usuario.getPasswordHash())) {
            return null;
        }
        return usuario;
    }

    // ---- Perfil Tutor ----

    @Override
    public Tutor registrarTutor(Usuario usuario) {
        if (usuarioDAO.load(usuario.getIdUsuario()) == null) {
            throw new BL_exeption("El usuario no existe");
        }
        if (tutorDAO.load(usuario.getIdUsuario()) != null) {
            throw new BL_exeption("El usuario ya tiene perfil de tutor");
        }
        Tutor tutor = usuario.getPerfilTutor();
        if (tutor == null) {
            tutor = new Tutor();
            usuario.agregarPerfil(tutor);
        }
        return tutorDAO.save(tutor);
    }

    @Override
    public Tutor modificarTutor(Tutor tutor) {
        if (tutorDAO.load(tutor.getUsuario().getIdUsuario()) == null) {
            throw new BL_exeption("El perfil de tutor no existe");
        }
        return tutorDAO.update(tutor);
    }

    @Override
    public void eliminarTutor(Tutor tutor) {
        int idUsuario = tutor.getUsuario().getIdUsuario();
        if (tutorDAO.load(idUsuario) == null) {
            throw new BL_exeption("El perfil de tutor no existe");
        }
        if (estudianteDAO.load(idUsuario) == null) {
            throw new BL_exeption("No se puede quitar el unico perfil del usuario");
        }
        try {
            tutorDAO.remove(tutor);
        } catch (RuntimeException e) {
            throw new BL_exeption("No se puede eliminar el perfil de tutor: ya tiene postulaciones, tarifas o sesiones");
        }
    }

    @Override
    public Tutor obtenerTutorPorId(int idUsuario) {
        return tutorDAO.load(idUsuario);
    }

    @Override
    public List<Tutor> listarTutores() {
        return tutorDAO.listAll();
    }

    // ---- Perfil Estudiante ----

    @Override
    public Estudiante registrarEstudiante(Usuario usuario) {
        if (usuarioDAO.load(usuario.getIdUsuario()) == null) {
            throw new BL_exeption("El usuario no existe");
        }
        if (estudianteDAO.load(usuario.getIdUsuario()) != null) {
            throw new BL_exeption("El usuario ya tiene perfil de estudiante");
        }
        Estudiante estudiante = usuario.getPerfilEstudiante();
        if (estudiante == null) {
            estudiante = new Estudiante();
            usuario.agregarPerfil(estudiante);
        }
        return estudianteDAO.save(estudiante);
    }

    @Override
    public Estudiante modificarEstudiante(Estudiante estudiante) {
        if (estudianteDAO.load(estudiante.getUsuario().getIdUsuario()) == null) {
            throw new BL_exeption("El perfil de estudiante no existe");
        }
        return estudianteDAO.update(estudiante);
    }

    @Override
    public void eliminarEstudiante(Estudiante estudiante) {
        int idUsuario = estudiante.getUsuario().getIdUsuario();
        if (estudianteDAO.load(idUsuario) == null) {
            throw new BL_exeption("El perfil de estudiante no existe");
        }
        if (tutorDAO.load(idUsuario) == null) {
            throw new BL_exeption("No se puede quitar el unico perfil del usuario");
        }
        try {
            estudianteDAO.remove(estudiante);
        } catch (RuntimeException e) {
            throw new BL_exeption("No se puede eliminar el perfil de estudiante: ya tiene sesiones registradas");
        }
    }

    @Override
    public Estudiante obtenerEstudiantePorId(int idUsuario) {
        return estudianteDAO.load(idUsuario);
    }

    @Override
    public List<Estudiante> listarEstudiantes() {
        return estudianteDAO.listAll();
    }

    // ---- Administrador ----

    @Override
    public Administrador registrarAdministrador(String nombre, String correo, String password, String nivelPermiso) {
        if (correo == null || correo.isBlank()) {
            throw new BL_exeption("El correo del administrador es obligatorio");
        }
        if (password == null || password.isBlank()) {
            throw new BL_exeption("La contrasena es obligatoria");
        }
        if (administradorDAO.buscarPorCorreo(correo) != null) {
            throw new BL_exeption("Ya existe un administrador registrado con ese correo");
        }
        Administrador administrador = new Administrador(nombre, correo, PasswordHasher.hashear(password), nivelPermiso);
        return administradorDAO.save(administrador);
    }

    @Override
    public Administrador modificarAdministrador(Administrador administrador) {
        if (administradorDAO.load(administrador.getIdAdministrador()) == null) {
            throw new BL_exeption("El administrador no existe");
        }
        if (administrador.getCorreo() == null || administrador.getCorreo().isBlank()) {
            throw new BL_exeption("El correo del administrador es obligatorio");
        }
        Administrador conMismoCorreo = administradorDAO.buscarPorCorreo(administrador.getCorreo());
        if (conMismoCorreo != null && conMismoCorreo.getIdAdministrador() != administrador.getIdAdministrador()) {
            throw new BL_exeption("Ya existe otro administrador con ese correo");
        }
        return administradorDAO.update(administrador);
    }

    @Override
    public void eliminarAdministrador(Administrador administrador) {
        if (administradorDAO.load(administrador.getIdAdministrador()) == null) {
            throw new BL_exeption("El administrador no existe");
        }
        try {
            administradorDAO.remove(administrador);
        } catch (RuntimeException e) {
            throw new BL_exeption("No se puede eliminar el administrador: ya resolvio postulaciones o reclamos");
        }
    }

    @Override
    public Administrador obtenerAdministradorPorId(int idAdministrador) {
        return administradorDAO.load(idAdministrador);
    }

    @Override
    public List<Administrador> listarAdministradores() {
        return administradorDAO.listAll();
    }

    @Override
    public Administrador iniciarSesionAdministrador(String correo, String password) {
        Administrador administrador = administradorDAO.buscarPorCorreo(correo);
        if (administrador == null || !PasswordHasher.coincide(password, administrador.getPasswordHash())) {
            return null;
        }
        return administrador;
    }
}
