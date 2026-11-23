package pe.edu.pucp.tutorPUCP.dao.Impl;

import pe.edu.pucp.tutorPUCP.dao.DAOs.PostulacionDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.DBManager;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.academico.Postulacion;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoAprobacion;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostulacionDAOImpl implements PostulacionDAO {

    // Las tres consultas leen primero todas las filas y cierran la conexion; recien despues se cargan
    // el tutor, el curso y el administrador de cada postulacion (ver completarRelaciones).
    @Override
    public Postulacion load(Integer id) {
        List<FilaPostulacion> filas = new ArrayList<>();
        String sql = "{CALL buscar_postulacion_por_id(?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                filas.add(leerFila(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener postulacion: " + e.getMessage());
        }
        List<Postulacion> postulaciones = completarRelaciones(filas, null);
        return postulaciones.isEmpty() ? null : postulaciones.get(0);
    }

    @Override
    public List<Postulacion> listAll() {
        List<FilaPostulacion> filas = new ArrayList<>();
        String sql = "{CALL listar_postulaciones()}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                filas.add(leerFila(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar postulaciones: " + e.getMessage());
        }
        return completarRelaciones(filas, null);
    }

    // el tutor que se pasa queda asignado a cada postulacion
    @Override
    public List<Postulacion> listarPorTutor(Tutor tutor) {
        List<FilaPostulacion> filas = new ArrayList<>();
        String sql = "{CALL listar_postulaciones_por_tutor(?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, tutor.getUsuario().getIdUsuario());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                filas.add(leerFila(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar postulaciones del tutor: " + e.getMessage());
        }
        return completarRelaciones(filas, tutor);
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

    // Una fila de la consulta: la postulacion con sus datos propios y los ids de lo relacionado.
    private static class FilaPostulacion {
        Postulacion postulacion;
        int idTutor;
        int idCurso;
        Integer idAdministrador;
    }

    private FilaPostulacion leerFila(ResultSet rs) throws SQLException {
        Postulacion postulacion = new Postulacion();
        postulacion.setIdPostulacion(rs.getInt("id"));
        postulacion.setArchivoCertificadoUrl(rs.getString("archivo_certificado_url"));
        postulacion.setEstado(EstadoAprobacion.valueOf(rs.getString("estado")));
        postulacion.setFechaPostulacion(rs.getObject("fecha_postulacion", LocalDate.class));
        postulacion.setFechaResolucion(rs.getObject("fecha_resolucion", LocalDate.class));
        postulacion.setResolucion(rs.getString("resolucion"));

        FilaPostulacion fila = new FilaPostulacion();
        fila.postulacion = postulacion;
        fila.idTutor = rs.getInt("id_tutor");
        fila.idCurso = rs.getInt("id_curso");
        fila.idAdministrador = rs.getObject("id_administrador_revisor", Integer.class);
        return fila;
    }

    // Carga el tutor, el curso y el administrador UNA sola vez por id y con la primera conexion ya cerrada.
    // Antes se abrian 3 conexiones mas por cada fila mientras la primera seguia abierta, y con la BD
    // en AWS eso terminaba en "Communications link failure" al listar las postulaciones.
    // Si tutor no es null, se usa ese para todas las filas (listarPorTutor).
    private List<Postulacion> completarRelaciones(List<FilaPostulacion> filas, Tutor tutor) {
        Map<Integer, Tutor> tutores = new HashMap<>();
        Map<Integer, Curso> cursos = new HashMap<>();
        Map<Integer, Administrador> administradores = new HashMap<>();
        List<Postulacion> postulaciones = new ArrayList<>();
        for (FilaPostulacion fila : filas) {
            Postulacion postulacion = fila.postulacion;
            if (tutor != null) {
                postulacion.setTutor(tutor);
            } else {
                if (!tutores.containsKey(fila.idTutor)) {
                    Usuario usuario = new UsuarioDAOImpl().load(fila.idTutor);
                    tutores.put(fila.idTutor, usuario != null ? usuario.getPerfilTutor() : null);
                }
                postulacion.setTutor(tutores.get(fila.idTutor));
            }
            if (!cursos.containsKey(fila.idCurso)) {
                cursos.put(fila.idCurso, new CursoDAOImpl().load(fila.idCurso));
            }
            postulacion.setCurso(cursos.get(fila.idCurso));
            if (fila.idAdministrador != null) {
                if (!administradores.containsKey(fila.idAdministrador)) {
                    administradores.put(fila.idAdministrador, new AdministradorDAOImpl().load(fila.idAdministrador));
                }
                postulacion.setAdministradorRevisor(administradores.get(fila.idAdministrador));
            }
            postulaciones.add(postulacion);
        }
        return postulaciones;
    }
}
