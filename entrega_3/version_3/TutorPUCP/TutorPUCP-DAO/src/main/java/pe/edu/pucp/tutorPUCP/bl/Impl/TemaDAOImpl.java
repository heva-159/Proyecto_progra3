package pe.edu.pucp.tutorPUCP.bl.Impl;

import pe.edu.pucp.tutorPUCP.bl.DAOs.TemaDAO;
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

public class TemaDAOImpl implements TemaDAO {

    @Override
    public Tema load(Integer id) {
        Tema tema = null;
        int idCurso = 0;
        String sql = "{CALL buscar_tema_por_id(?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                tema = new Tema();
                tema.setIdTema(rs.getInt("id"));
                tema.setNombre(rs.getString("nombre"));
                idCurso = rs.getInt("id_curso");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener tema: " + e.getMessage());
        }
        if (tema != null) {
            tema.setCurso(new CursoDAOImpl().load(idCurso));
        }
        return tema;
    }

    @Override
    public List<Tema> listAll() {
        List<Tema> temas = new ArrayList<>();
        List<Integer> idsCurso = new ArrayList<>();
        String sql = "{CALL listar_temas()}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Tema tema = new Tema();
                tema.setIdTema(rs.getInt("id"));
                tema.setNombre(rs.getString("nombre"));
                temas.add(tema);
                idsCurso.add(rs.getInt("id_curso"));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar temas: " + e.getMessage());
        }
        // cada tema recibe su curso (sin volver a cargar sus temas)
        CursoDAOImpl cursoDAO = new CursoDAOImpl();
        for (int i = 0; i < temas.size(); i++) {
            temas.get(i).setCurso(cursoDAO.load(idsCurso.get(i)));
        }
        return temas;
    }

    // el curso que se pasa queda asignado a cada tema
    @Override
    public List<Tema> listarPorCurso(Curso curso) {
        List<Tema> temas = new ArrayList<>();
        String sql = "{CALL listar_temas_por_curso(?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, curso.getIdCurso());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Tema tema = new Tema();
                tema.setIdTema(rs.getInt("id"));
                tema.setNombre(rs.getString("nombre"));
                tema.setCurso(curso);
                temas.add(tema);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar temas del curso: " + e.getMessage());
        }
        return temas;
    }

    @Override
    public Tema save(Tema tema) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return saveConConnection(tema, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tema save(Tema tema, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return saveConConnection(tema, connection);
    }

    private Tema saveConConnection(Tema tema, Connection connection) {
        String sql = "{CALL insertar_tema(?,?,?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, tema.getCurso().getIdCurso());
            stmt.setString(2, tema.getNombre());
            stmt.registerOutParameter(3, Types.INTEGER);
            stmt.execute();
            tema.setIdTema(stmt.getInt(3));
        } catch (SQLException e) {
            System.out.println("Error al insertar tema: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return tema;
    }

    @Override
    public Tema update(Tema tema) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return updateConConnection(tema, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tema update(Tema tema, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return updateConConnection(tema, connection);
    }

    private Tema updateConConnection(Tema tema, Connection connection) {
        String sql = "{CALL modificar_tema(?,?,?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, tema.getIdTema());
            stmt.setInt(2, tema.getCurso().getIdCurso());
            stmt.setString(3, tema.getNombre());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al modificar tema: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return tema;
    }

    @Override
    public void remove(Tema tema) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            removeConConnection(tema, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(Tema tema, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        removeConConnection(tema, connection);
    }

    private void removeConConnection(Tema tema, Connection connection) {
        String sql = "{CALL eliminar_tema(?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, tema.getIdTema());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al eliminar tema: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
