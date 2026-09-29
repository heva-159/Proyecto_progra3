package pe.edu.pucp.tutorPUCP.bl.Impl;

import pe.edu.pucp.tutorPUCP.bl.DAOs.CursoDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.DBManager;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.academico.Tema;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CursoDAOImpl implements CursoDAO {

    @Override
    public Curso load(Integer id) {
        Curso curso = null;
        String sql = "{CALL buscar_curso_por_id(?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                curso = leerCurso(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener curso: " + e.getMessage());
        }
        // los temas se cargan despues de cerrar la consulta del curso
        if (curso != null) {
            for (Tema tema : new TemaDAOImpl().listarPorCurso(curso)) {
                curso.agregarTema(tema);
            }
        }
        return curso;
    }

    // para validar que el codigo del curso no este repetido; no carga los temas
    @Override
    public Curso buscarPorCodigo(String codigo) {
        Curso curso = null;
        String sql = "{CALL buscar_curso_por_codigo(?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                curso = leerCurso(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar curso por codigo: " + e.getMessage());
        }
        return curso;
    }

    @Override
    public List<Curso> listAll() {
        List<Curso> cursos = new ArrayList<>();
        String sql = "{CALL listar_cursos()}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cursos.add(leerCurso(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar cursos: " + e.getMessage());
        }
        return cursos;
    }

    @Override
    public Curso save(Curso curso) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return saveConConnection(curso, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Curso save(Curso curso, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return saveConConnection(curso, connection);
    }

    private Curso saveConConnection(Curso curso, Connection connection) {
        String sql = "{CALL insertar_curso(?,?,?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setString(1, curso.getCodigo());
            stmt.setString(2, curso.getNombre());
            stmt.registerOutParameter(3, Types.INTEGER);
            stmt.execute();
            curso.setIdCurso(stmt.getInt(3));
        } catch (SQLException e) {
            System.out.println("Error al insertar curso: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return curso;
    }

    @Override
    public Curso update(Curso curso) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return updateConConnection(curso, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Curso update(Curso curso, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return updateConConnection(curso, connection);
    }

    private Curso updateConConnection(Curso curso, Connection connection) {
        String sql = "{CALL modificar_curso(?,?,?,?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, curso.getIdCurso());
            stmt.setString(2, curso.getCodigo());
            stmt.setString(3, curso.getNombre());
            stmt.setBoolean(4, curso.isActivo());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al modificar curso: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return curso;
    }

    // eliminacion logica: el SP pone activo = false
    @Override
    public void remove(Curso curso) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            removeConConnection(curso, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(Curso curso, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        removeConConnection(curso, connection);
    }

    private void removeConConnection(Curso curso, Connection connection) {
        String sql = "{CALL eliminar_curso(?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, curso.getIdCurso());
            stmt.execute();
            curso.setActivo(false);
        } catch (SQLException e) {
            System.out.println("Error al eliminar curso: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Curso leerCurso(ResultSet rs) throws SQLException {
        Curso curso = new Curso();
        curso.setIdCurso(rs.getInt("id"));
        curso.setCodigo(rs.getString("codigo"));
        curso.setNombre(rs.getString("nombre"));
        curso.setActivo(rs.getBoolean("activo"));
        return curso;
    }
}
