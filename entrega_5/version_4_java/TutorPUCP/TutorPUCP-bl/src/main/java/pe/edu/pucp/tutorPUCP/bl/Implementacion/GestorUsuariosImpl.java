package pe.edu.pucp.tutorPUCP.bl.Implementacion;

import pe.edu.pucp.tutorPUCP.bl.Clases.GestorUsuarios;
import pe.edu.pucp.tutorPUCP.bl.EnviadorCorreo;
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

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GestorUsuariosImpl implements GestorUsuarios {
    private static final Logger LOG = Logger.getLogger(GestorUsuariosImpl.class.getName());

    // Verificacion del correo: el codigo de 6 digitos vence a los 15 minutos, se puede pedir otro cada 30 segundos
    // y se permiten 5 intentos fallidos por codigo.
    private static final int MINUTOS_VIGENCIA_CODIGO = 15;
    private static final int SEGUNDOS_ESPERA_REENVIO = 30;
    private static final int MAX_INTENTOS_FALLIDOS = 5;
    private static final SecureRandom ALEATORIO = new SecureRandom();
    // Intentos fallidos por usuario (id -> cantidad). Es static porque el REST crea un gestor nuevo en cada
    // peticion y el conteo tiene que sobrevivir entre peticiones (se pierde si se reinicia el servidor).
    private static final Map<Integer, Integer> intentosFallidos = new ConcurrentHashMap<>();

    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private final TutorDAO tutorDAO = new TutorDAOImpl();
    private final EstudianteDAO estudianteDAO = new EstudianteDAOImpl();
    private final AdministradorDAO administradorDAO = new AdministradorDAOImpl();
    private final EnviadorCorreo enviadorCorreo = new EnviadorCorreo();

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
        // el codigo de verificacion se guarda junto con el usuario y se envia al correo apenas se registra
        asignarCodigoNuevo(usuario);

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
        } catch (SQLException e) {
            throw new BL_exeption("No se pudo registrar el usuario con sus perfiles: " + e.getMessage());
        }

        // se envia despues del commit y fuera de la transaccion: el usuario ya esta registrado, asi que si el
        // correo no sale no se deshace el registro (puede pedir otro codigo con "Reenviar codigo")
        try {
            enviarCodigo(usuario);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo enviar el codigo de verificacion a " + usuario.getCorreoInstitucional(), e);
        }
        return usuario;
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

    @Override
    public void cambiarPassword(int idUsuario, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioDAO.load(idUsuario);
        if (usuario == null) {
            throw new BL_exeption("El usuario no existe");
        }
        if (usuario.getEstado() != EstadoCuenta.ACTIVA) {
            throw new BL_exeption("La cuenta no esta activa");
        }
        validarCambioDePassword(passwordActual, passwordNueva, usuario.getPasswordHash());
        usuario.setPasswordHash(PasswordHasher.hashear(passwordNueva));
        usuarioDAO.update(usuario);
    }

    // ---- Verificacion del correo ----

    @Override
    public Usuario verificarCorreo(String correo, String codigo) {
        if (codigo == null || !codigo.matches("[0-9]{6}")) {
            throw new BL_exeption("El codigo debe tener 6 digitos");
        }
        Usuario usuario = buscarCuentaActiva(correo);
        if (usuario.isCorreoVerificado()) {
            return usuario;
        }
        if (usuario.getCodigoVerificacion() == null || usuario.getFechaExpiracionCodigo() == null) {
            throw new BL_exeption("No hay un codigo pendiente. Pide uno nuevo con Reenviar codigo");
        }
        if (ahora().isAfter(usuario.getFechaExpiracionCodigo())) {
            throw new BL_exeption("El codigo vencio. Pide uno nuevo con Reenviar codigo");
        }
        if (!usuario.getCodigoVerificacion().equals(codigo)) {
            int fallidos = intentosFallidos.merge(usuario.getIdUsuario(), 1, Integer::sum);
            if (fallidos >= MAX_INTENTOS_FALLIDOS) {
                // se acabaron los intentos: este codigo ya no sirve y hay que pedir otro
                intentosFallidos.remove(usuario.getIdUsuario());
                usuario.setCodigoVerificacion(null);
                usuario.setFechaExpiracionCodigo(null);
                usuarioDAO.update(usuario);
                throw new BL_exeption("Demasiados intentos fallidos. Pide un codigo nuevo con Reenviar codigo");
            }
            int quedan = MAX_INTENTOS_FALLIDOS - fallidos;
            throw new BL_exeption("Codigo incorrecto. " + (quedan == 1 ? "Te queda 1 intento" : "Te quedan " + quedan + " intentos"));
        }
        // la tabla exige que codigo y fecha de vencimiento queden los dos en null
        usuario.setCorreoVerificado(true);
        usuario.setCodigoVerificacion(null);
        usuario.setFechaExpiracionCodigo(null);
        usuarioDAO.update(usuario);
        intentosFallidos.remove(usuario.getIdUsuario());
        return usuario;
    }

    @Override
    public void reenviarCodigo(String correo) {
        Usuario usuario = buscarCuentaActiva(correo);
        if (usuario.isCorreoVerificado()) {
            throw new BL_exeption("Esa cuenta ya tiene el correo verificado");
        }
        // el ultimo envio fue 15 minutos antes del vencimiento del codigo vigente: no hace falta guardar la hora aparte
        if (usuario.getFechaExpiracionCodigo() != null) {
            LocalDateTime puedeReenviarDesde = usuario.getFechaExpiracionCodigo()
                    .minusMinutes(MINUTOS_VIGENCIA_CODIGO).plusSeconds(SEGUNDOS_ESPERA_REENVIO);
            long faltan = (long) Math.ceil(Duration.between(ahora(), puedeReenviarDesde).toMillis() / 1000.0);
            if (faltan > 0) {
                throw new BL_exeption("Espera " + faltan + " segundos para pedir otro codigo");
            }
        }
        asignarCodigoNuevo(usuario);
        usuarioDAO.update(usuario);
        intentosFallidos.remove(usuario.getIdUsuario());
        try {
            enviarCodigo(usuario);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo reenviar el codigo de verificacion a " + usuario.getCorreoInstitucional(), e);
            // si el correo no salio, ese codigo no le sirve a nadie: se invalida para poder volver a pedirlo enseguida
            usuario.setCodigoVerificacion(null);
            usuario.setFechaExpiracionCodigo(null);
            usuarioDAO.update(usuario);
            throw new BL_exeption("No se pudo enviar el correo. Intenta de nuevo en unos minutos");
        }
    }

    // La cuenta que tiene ese correo; falla si no existe o no esta activa.
    private Usuario buscarCuentaActiva(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new BL_exeption("El correo es obligatorio");
        }
        Usuario usuario = usuarioDAO.buscarPorCorreo(correo.trim());
        if (usuario == null) {
            throw new BL_exeption("No existe una cuenta con ese correo");
        }
        if (usuario.getEstado() != EstadoCuenta.ACTIVA) {
            throw new BL_exeption("La cuenta no esta activa");
        }
        return usuario;
    }

    // Un codigo de 6 digitos al azar (con ceros a la izquierda) que vence en 15 minutos. No guarda nada en la BD.
    private void asignarCodigoNuevo(Usuario usuario) {
        usuario.setCodigoVerificacion(String.format("%06d", ALEATORIO.nextInt(1_000_000)));
        usuario.setFechaExpiracionCodigo(ahora().plusMinutes(MINUTOS_VIGENCIA_CODIGO));
    }

    // La hora del vencimiento se guarda en UTC, igual que la hora del servidor de la BD (que va 5 horas adelantada
    // respecto a Lima): asi no depende de la zona horaria de la maquina donde corra GlassFish.
    private static LocalDateTime ahora() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    private void enviarCodigo(Usuario usuario) {
        enviadorCorreo.enviarCodigoVerificacion(usuario.getCorreoInstitucional(), usuario.getNombres(),
                usuario.getCodigoVerificacion(), MINUTOS_VIGENCIA_CODIGO);
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

    @Override
    public void cambiarPasswordAdministrador(int idAdministrador, String passwordActual, String passwordNueva) {
        Administrador administrador = administradorDAO.load(idAdministrador);
        if (administrador == null) {
            throw new BL_exeption("El administrador no existe");
        }
        validarCambioDePassword(passwordActual, passwordNueva, administrador.getPasswordHash());
        administrador.setPasswordHash(PasswordHasher.hashear(passwordNueva));
        administradorDAO.update(administrador);
    }

    // La actual debe coincidir con la guardada; la nueva, tener 8 caracteres o mas y ser distinta de la actual.
    private void validarCambioDePassword(String passwordActual, String passwordNueva, String hashGuardado) {
        if (!PasswordHasher.coincide(passwordActual, hashGuardado)) {
            throw new BL_exeption("La contrasena actual no es correcta");
        }
        if (passwordNueva == null || passwordNueva.length() < 8) {
            throw new BL_exeption("La contrasena nueva debe tener al menos 8 caracteres");
        }
        if (passwordNueva.equals(passwordActual)) {
            throw new BL_exeption("La contrasena nueva debe ser distinta de la actual");
        }
    }
}
