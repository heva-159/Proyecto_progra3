package pe.edu.pucp.tutorPUCP.dao.Impl;

import pe.edu.pucp.tutorPUCP.dao.DAOs.MensajeDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.DBManager;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Mensaje;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MensajeDAOImpl implements MensajeDAO {

    @Override
    public Mensaje load(Integer id) {
        Mensaje mensaje = null;
        String sql = "{CALL buscar_mensaje_por_id(?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                mensaje = leerMensaje(rs, new HashMap<>());
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener mensaje: " + e.getMessage());
        }
        return mensaje;
    }

    @Override
    public List<Mensaje> listAll() {
        List<Mensaje> mensajes = new ArrayList<>();
        String sql = "{CALL listar_mensajes()}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            ResultSet rs = stmt.executeQuery();
            // cada usuario se busca una sola vez aunque aparezca en muchos mensajes
            Map<Integer, Usuario> usuarios = new HashMap<>();
            while (rs.next()) {
                mensajes.add(leerMensaje(rs, usuarios));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar mensajes: " + e.getMessage());
        }
        return mensajes;
    }

    // conversacion entre dos usuarios, en orden de envio
    @Override
    public List<Mensaje> listarEntreUsuarios(Usuario usuario1, Usuario usuario2) {
        List<Mensaje> mensajes = new ArrayList<>();
        String sql = "{CALL listar_mensajes_entre_usuarios(?,?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, usuario1.getIdUsuario());
            stmt.setInt(2, usuario2.getIdUsuario());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Mensaje mensaje = new Mensaje();
                mensaje.setIdMensaje(rs.getInt("id"));
                mensaje.setContenido(rs.getString("contenido"));
                mensaje.setFechaEnvio(rs.getObject("fecha_envio", LocalDateTime.class));
                mensaje.setLeido(rs.getBoolean("leido"));
                // emisor y receptor son los mismos dos usuarios que se pasaron
                if (rs.getInt("id_usuario_emisor") == usuario1.getIdUsuario()) {
                    mensaje.setEmisor(usuario1);
                    mensaje.setReceptor(usuario2);
                } else {
                    mensaje.setEmisor(usuario2);
                    mensaje.setReceptor(usuario1);
                }
                mensajes.add(mensaje);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar mensajes entre usuarios: " + e.getMessage());
        }
        return mensajes;
    }

    @Override
    public Mensaje save(Mensaje mensaje) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return saveConConnection(mensaje, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mensaje save(Mensaje mensaje, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return saveConConnection(mensaje, connection);
    }

    private Mensaje saveConConnection(Mensaje mensaje, Connection connection) {
        String sql = "{CALL insertar_mensaje(?,?,?,?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, mensaje.getEmisor().getIdUsuario());
            stmt.setInt(2, mensaje.getReceptor().getIdUsuario());
            stmt.setString(3, mensaje.getContenido());
            stmt.registerOutParameter(4, Types.INTEGER);
            stmt.execute();
            mensaje.setIdMensaje(stmt.getInt(4));
        } catch (SQLException e) {
            System.out.println("Error al insertar mensaje: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return mensaje;
    }

    @Override
    public Mensaje update(Mensaje mensaje) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return updateConConnection(mensaje, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mensaje update(Mensaje mensaje, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return updateConConnection(mensaje, connection);
    }

    // solo cambian el contenido y si fue leido
    private Mensaje updateConConnection(Mensaje mensaje, Connection connection) {
        String sql = "{CALL modificar_mensaje(?,?,?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, mensaje.getIdMensaje());
            stmt.setString(2, mensaje.getContenido());
            stmt.setBoolean(3, mensaje.isLeido());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al modificar mensaje: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return mensaje;
    }

    @Override
    public void remove(Mensaje mensaje) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            removeConConnection(mensaje, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(Mensaje mensaje, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        removeConConnection(mensaje, connection);
    }

    private void removeConConnection(Mensaje mensaje, Connection connection) {
        String sql = "{CALL eliminar_mensaje(?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, mensaje.getIdMensaje());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al eliminar mensaje: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Mensaje leerMensaje(ResultSet rs, Map<Integer, Usuario> usuarios) throws SQLException {
        UsuarioDAOImpl usuarioDAO = new UsuarioDAOImpl();
        Mensaje mensaje = new Mensaje();
        mensaje.setIdMensaje(rs.getInt("id"));
        mensaje.setEmisor(usuarios.computeIfAbsent(rs.getInt("id_usuario_emisor"), usuarioDAO::load));
        mensaje.setReceptor(usuarios.computeIfAbsent(rs.getInt("id_usuario_receptor"), usuarioDAO::load));
        mensaje.setContenido(rs.getString("contenido"));
        mensaje.setFechaEnvio(rs.getObject("fecha_envio", LocalDateTime.class));
        mensaje.setLeido(rs.getBoolean("leido"));
        return mensaje;
    }
}
