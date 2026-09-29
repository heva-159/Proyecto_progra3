package pe.edu.pucp.tutorPUCP.bl.Impl;

import pe.edu.pucp.tutorPUCP.bl.DAOs.PostulacionDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.DBManager;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.academico.Postulacion;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoAprobacion;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PostulacionDAOImpl implements PostulacionDAO {

    @Override
    public Postulacion load(Integer id) {
        Postulacion postulacion = null;
        String sql = "{CALL buscar_postulacion_por_id(?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                postulacion = leerPostulacion(rs, null);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener postulacion: " + e.getMessage());
        }
        return postulacion;
    }

    @Override
    public List<Postulacion> listAll() {
        List<Postulacion> postulaciones = new ArrayList<>();
        String sql = "{CALL listar_postulaciones()}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                postulaciones.add(leerPostulacion(rs, null));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar postulaciones: " + e.getMessage());
        }
        return postulaciones;
    }

    // el tutor que se pasa queda asignado a cada postulacion
    @Override
    public List<Postulacion> listarPorTutor(Tutor tutor) {
        List<Postulacion> postulaciones = new ArrayList<>();
        String sql = "{CALL listar_postulaciones_por_tutor(?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, tutor.getUsuario().getIdUsuario());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                postulaciones.add(leerPostulacion(rs, tutor));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar postulaciones del tutor: " + e.getMessage());
        }
        return postulaciones;
    }

    @Override
    public Postulacion save(Postulacion postulacion) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return saveConConnection(postulacion, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Postulacion save(Postulacion postulacion, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return saveConConnection(postulacion, connection);
    }

    private Postulacion saveConConnection(Postulacion postulacion, Connection connection) {
        String sql = "{CALL insertar_postulacion(?,?,?,?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, postulacion.getTutor().getUsuario().getIdUsuario());
            stmt.setInt(2, postulacion.getCurso().getIdCurso());
            stmt.setString(3, postulacion.getArchivoCertificadoUrl());
            stmt.registerOutParameter(4, Types.INTEGER);
            stmt.execute();
            postulacion.setIdPostulacion(stmt.getInt(4));
        } catch (SQLException e) {
            System.out.println("Error al insertar postulacion: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return postulacion;
    }

    @Override
    public Postulacion update(Postulacion postulacion) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return updateConConnection(postulacion, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Postulacion update(Postulacion postulacion, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return updateConConnection(postulacion, connection);
    }

    // tambien sirve para aprobar o rechazar la postulacion
    private Postulacion updateConConnection(Postulacion postulacion, Connection connection) {
        String sql = "{CALL modificar_postulacion(?,?,?,?,?,?,?,?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, postulacion.getIdPostulacion());
            stmt.setInt(2, postulacion.getTutor().getUsuario().getIdUsuario());
            stmt.setInt(3, postulacion.getCurso().getIdCurso());
            stmt.setString(4, postulacion.getArchivoCertificadoUrl());
            stmt.setString(5, postulacion.getEstado().name());
            stmt.setObject(6, postulacion.getFechaResolucion(), Types.DATE);
            stmt.setString(7, postulacion.getResolucion());
            if (postulacion.getAdministradorRevisor() != null) {
                stmt.setInt(8, postulacion.getAdministradorRevisor().getIdAdministrador());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al modificar postulacion: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return postulacion;
    }

    @Override
    public void remove(Postulacion postulacion) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            removeConConnection(postulacion, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(Postulacion postulacion, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        removeConConnection(postulacion, connection);
    }

    private void removeConConnection(Postulacion postulacion, Connection connection) {
        String sql = "{CALL eliminar_postulacion(?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, postulacion.getIdPostulacion());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al eliminar postulacion: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // si tutor es null se carga desde la base con el id_tutor de la fila
    private Postulacion leerPostulacion(ResultSet rs, Tutor tutor) throws SQLException {
        Postulacion postulacion = new Postulacion();
        postulacion.setIdPostulacion(rs.getInt("id"));
        postulacion.setArchivoCertificadoUrl(rs.getString("archivo_certificado_url"));
        postulacion.setEstado(EstadoAprobacion.valueOf(rs.getString("estado")));
        postulacion.setFechaPostulacion(rs.getObject("fecha_postulacion", LocalDate.class));
        postulacion.setFechaResolucion(rs.getObject("fecha_resolucion", LocalDate.class));
        postulacion.setResolucion(rs.getString("resolucion"));

        if (tutor == null) {
            Usuario usuario = new UsuarioDAOImpl().load(rs.getInt("id_tutor"));
            tutor = usuario != null ? usuario.getPerfilTutor() : null;
        }
        postulacion.setTutor(tutor);
        postulacion.setCurso(new CursoDAOImpl().load(rs.getInt("id_curso")));
        Integer idAdministrador = rs.getObject("id_administrador_revisor", Integer.class);
        if (idAdministrador != null) {
            postulacion.setAdministradorRevisor(new AdministradorDAOImpl().load(idAdministrador));
        }
        return postulacion;
    }
}
