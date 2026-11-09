package pe.edu.pucp.tutorPUCP.dao.Impl;

import pe.edu.pucp.tutorPUCP.dao.DAOs.UsuarioDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.DBManager;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoCuenta;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Estudiante;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOImpl implements UsuarioDAO {

    @Override
    public Usuario load(Integer id) {
        Usuario usuario = null;
        String sql = "{CALL buscar_usuario_por_id(?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                usuario = leerUsuario(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener usuario: " + e.getMessage());
        }
        return usuario;
    }

    @Override
    public Usuario buscarPorCorreo(String correoInstitucional) {
        Usuario usuario = null;
        String sql = "{CALL buscar_usuario_por_correo(?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setString(1, correoInstitucional);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                usuario = leerUsuario(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar usuario por correo: " + e.getMessage());
        }
        return usuario;
    }

    // para validar que el codigo PUCP no este repetido
    @Override
    public Usuario buscarPorCodigoPucp(String codigoPucp) {
        Usuario usuario = null;
        String sql = "{CALL buscar_usuario_por_codigo(?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setString(1, codigoPucp);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                usuario = leerUsuario(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar usuario por codigo: " + e.getMessage());
        }
        return usuario;
    }

    @Override
    public List<Usuario> listAll() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "{CALL listar_usuarios()}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                usuarios.add(leerUsuario(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar usuarios: " + e.getMessage());
        }
        return usuarios;
    }

    @Override
    public Usuario save(Usuario usuario) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return saveConConnection(usuario, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Usuario save(Usuario usuario, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return saveConConnection(usuario, connection);
    }

    // solo inserta la fila de usuario: sus perfiles (tutor / estudiante) los guardan TutorDAO y
    // EstudianteDAO dentro de la transaccion que abre la capa de negocio
    private Usuario saveConConnection(Usuario usuario, Connection connection) {
        String sql = "{CALL insertar_usuario(?,?,?,?,?,?,?,?,?,?,?,?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setString(1, usuario.getNombres());
            stmt.setString(2, usuario.getApellidos());
            stmt.setString(3, usuario.getCorreoInstitucional());
            stmt.setString(4, usuario.getCodigoPucp());
            stmt.setString(5, usuario.getFacultad());
            stmt.setString(6, usuario.getEspecialidad());
            stmt.setString(7, usuario.getPasswordHash());
            stmt.setString(8, usuario.getCodigoVerificacion());
            stmt.setObject(9, usuario.getFechaExpiracionCodigo(), Types.TIMESTAMP);
            stmt.setBoolean(10, usuario.isConsentimientoDatos());
            stmt.setBoolean(11, usuario.isConsentimientoCalificaciones());
            stmt.registerOutParameter(12, Types.INTEGER);
            stmt.execute();
            usuario.setIdUsuario(stmt.getInt(12));
        } catch (SQLException e) {
            System.out.println("Error al insertar usuario: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return usuario;
    }

    @Override
    public Usuario update(Usuario usuario) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return updateConConnection(usuario, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Usuario update(Usuario usuario, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return updateConConnection(usuario, connection);
    }

    // solo modifica la fila de usuario: agregar o quitar perfiles lo coordina la capa de negocio
    private Usuario updateConConnection(Usuario usuario, Connection connection) {
        String sql = "{CALL modificar_usuario(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, usuario.getIdUsuario());
            stmt.setString(2, usuario.getNombres());
            stmt.setString(3, usuario.getApellidos());
            stmt.setString(4, usuario.getCorreoInstitucional());
            stmt.setString(5, usuario.getCodigoPucp());
            stmt.setString(6, usuario.getFacultad());
            stmt.setString(7, usuario.getEspecialidad());
            stmt.setString(8, usuario.getPasswordHash());
            stmt.setString(9, usuario.getEstado().name());
            stmt.setBoolean(10, usuario.isCorreoVerificado());
            stmt.setString(11, usuario.getCodigoVerificacion());
            stmt.setObject(12, usuario.getFechaExpiracionCodigo(), Types.TIMESTAMP);
            stmt.setBoolean(13, usuario.isConsentimientoDatos());
            stmt.setBoolean(14, usuario.isConsentimientoCalificaciones());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al modificar usuario: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return usuario;
    }

    // eliminacion logica: el SP deja la cuenta BLOQUEADA
    @Override
    public void remove(Usuario usuario) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            removeConConnection(usuario, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(Usuario usuario, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        removeConConnection(usuario, connection);
    }

    private void removeConConnection(Usuario usuario, Connection connection) {
        String sql = "{CALL eliminar_usuario(?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, usuario.getIdUsuario());
            stmt.execute();
            usuario.setEstado(EstadoCuenta.BLOQUEADA);
        } catch (SQLException e) {
            System.out.println("Error al eliminar usuario: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Usuario leerUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id"));
        usuario.setNombres(rs.getString("nombres"));
        usuario.setApellidos(rs.getString("apellidos"));
        usuario.setCorreoInstitucional(rs.getString("correo_institucional"));
        usuario.setCodigoPucp(rs.getString("codigo_pucp"));
        usuario.setFacultad(rs.getString("facultad"));
        usuario.setEspecialidad(rs.getString("especialidad"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        usuario.setFechaRegistro(rs.getObject("fecha_registro", LocalDate.class));
        usuario.setEstado(EstadoCuenta.valueOf(rs.getString("estado")));
        usuario.setCorreoVerificado(rs.getBoolean("correo_verificado"));
        usuario.setCodigoVerificacion(rs.getString("codigo_verificacion"));
        usuario.setFechaExpiracionCodigo(rs.getObject("fecha_expiracion_codigo", LocalDateTime.class));
        usuario.setConsentimientoDatos(rs.getBoolean("consentimiento_datos"));
        usuario.setConsentimientoCalificaciones(rs.getBoolean("consentimiento_calificaciones"));
        if (rs.getBoolean("es_tutor")) usuario.agregarPerfil(new Tutor());
        if (rs.getBoolean("es_estudiante")) usuario.agregarPerfil(new Estudiante());
        return usuario;
    }
}
