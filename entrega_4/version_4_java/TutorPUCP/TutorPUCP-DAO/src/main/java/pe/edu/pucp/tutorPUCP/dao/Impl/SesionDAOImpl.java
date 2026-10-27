package pe.edu.pucp.tutorPUCP.dao.Impl;

import pe.edu.pucp.tutorPUCP.dao.DAOs.SesionDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.DBManager;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoSesion;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SesionDAOImpl implements SesionDAO {

    @Override
    public Sesion load(Integer id) {
        Sesion sesion = null;
        String sql = "{CALL buscar_sesion_por_id(?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                sesion = leerSesion(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener sesion: " + e.getMessage());
        }
        return sesion;
    }

    @Override
    public List<Sesion> listAll() {
        List<Sesion> sesiones = new ArrayList<>();
        String sql = "{CALL listar_sesiones()}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sesiones.add(leerSesion(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar sesiones: " + e.getMessage());
        }
        return sesiones;
    }

    @Override
    public Sesion save(Sesion sesion) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return saveConConnection(sesion, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Sesion save(Sesion sesion, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return saveConConnection(sesion, connection);
    }

    // solo inserta la sesion (queda PROGRAMADA): sus temas los guarda SesionTemaDAO dentro
    // de la transaccion que abre la capa de negocio
    private Sesion saveConConnection(Sesion sesion, Connection connection) {
        String sql = "{CALL insertar_sesion(?,?,?,?,?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, sesion.getTutor().getUsuario().getIdUsuario());
            stmt.setInt(2, sesion.getEstudiante().getUsuario().getIdUsuario());
            stmt.setInt(3, sesion.getCurso().getIdCurso());
            stmt.setObject(4, sesion.getFechaHoraInicio(), Types.TIMESTAMP);
            stmt.registerOutParameter(5, Types.INTEGER);
            stmt.execute();
            sesion.setIdSesion(stmt.getInt(5));
        } catch (SQLException e) {
            System.out.println("Error al insertar sesion: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return sesion;
    }

    @Override
    public Sesion update(Sesion sesion) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return updateConConnection(sesion, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Sesion update(Sesion sesion, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return updateConConnection(sesion, connection);
    }

    // tambien sirve para check-in, check-out, cancelar y calificar;
    // no toca los temas: cambiarlos lo coordina la capa de negocio con SesionTemaDAO
    private Sesion updateConConnection(Sesion sesion, Connection connection) {
        String sql = "{CALL modificar_sesion(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, sesion.getIdSesion());
            stmt.setInt(2, sesion.getTutor().getUsuario().getIdUsuario());
            stmt.setInt(3, sesion.getEstudiante().getUsuario().getIdUsuario());
            stmt.setInt(4, sesion.getCurso().getIdCurso());
            stmt.setObject(5, sesion.getFechaHoraInicio(), Types.TIMESTAMP);
            stmt.setObject(6, sesion.getFechaHoraFin(), Types.TIMESTAMP);
            stmt.setString(7, sesion.getEstado().name());
            stmt.setBoolean(8, sesion.isCheckInTutor());
            stmt.setBoolean(9, sesion.isCheckInEstudiante());
            stmt.setBoolean(10, sesion.isCheckOutTutor());
            stmt.setBoolean(11, sesion.isCheckOutEstudiante());
            stmt.setObject(12, sesion.getPuntaje(), Types.INTEGER);
            stmt.setString(13, sesion.getComentario());
            stmt.setString(14, sesion.getMotivoCancelacion());
            if (sesion.getUsuarioCancelo() != null) {
                stmt.setInt(15, sesion.getUsuarioCancelo().getIdUsuario());
            } else {
                stmt.setNull(15, Types.INTEGER);
            }
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al modificar sesion: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return sesion;
    }

    // sus temas se borran por el on delete cascade; falla si la sesion tiene reclamos
    @Override
    public void remove(Sesion sesion) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            removeConConnection(sesion, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(Sesion sesion, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        removeConConnection(sesion, connection);
    }

    private void removeConConnection(Sesion sesion, Connection connection) {
        String sql = "{CALL eliminar_sesion(?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, sesion.getIdSesion());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al eliminar sesion: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Sesion leerSesion(ResultSet rs) throws SQLException {
        UsuarioDAOImpl usuarioDAO = new UsuarioDAOImpl();
        Sesion sesion = new Sesion();
        sesion.setIdSesion(rs.getInt("id"));

        Usuario usuarioTutor = usuarioDAO.load(rs.getInt("id_tutor"));
        Usuario usuarioEstudiante = usuarioDAO.load(rs.getInt("id_estudiante"));
        sesion.setTutor(usuarioTutor != null ? usuarioTutor.getPerfilTutor() : null);
        sesion.setEstudiante(usuarioEstudiante != null ? usuarioEstudiante.getPerfilEstudiante() : null);
        sesion.setCurso(new CursoDAOImpl().load(rs.getInt("id_curso")));

        sesion.setFechaHoraInicio(rs.getObject("fecha_hora_inicio", LocalDateTime.class));
        sesion.setFechaHoraFin(rs.getObject("fecha_hora_fin", LocalDateTime.class));
        sesion.setEstado(EstadoSesion.valueOf(rs.getString("estado")));
        sesion.setCheckInTutor(rs.getBoolean("check_in_tutor"));
        sesion.setCheckInEstudiante(rs.getBoolean("check_in_estudiante"));
        sesion.setCheckOutTutor(rs.getBoolean("check_out_tutor"));
        sesion.setCheckOutEstudiante(rs.getBoolean("check_out_estudiante"));
        sesion.setPuntaje(rs.getObject("puntaje", Integer.class));
        sesion.setComentario(rs.getString("comentario"));
        sesion.setMotivoCancelacion(rs.getString("motivo_cancelacion"));

        // quien cancelo es el tutor o el estudiante: se reutiliza el usuario ya cargado
        Integer idCancelo = rs.getObject("id_usuario_cancelo", Integer.class);
        if (idCancelo != null) {
            boolean canceloTutor = usuarioTutor != null && idCancelo == usuarioTutor.getIdUsuario();
            sesion.setUsuarioCancelo(canceloTutor ? usuarioTutor : usuarioEstudiante);
        }

        sesion.setTemas(new SesionTemaDAOImpl().listarPorSesion(sesion));
        return sesion;
    }
}
