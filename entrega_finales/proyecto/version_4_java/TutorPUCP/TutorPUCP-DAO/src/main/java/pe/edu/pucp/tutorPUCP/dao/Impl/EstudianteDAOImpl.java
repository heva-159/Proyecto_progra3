package pe.edu.pucp.tutorPUCP.dao.Impl;

import pe.edu.pucp.tutorPUCP.dao.DAOs.EstudianteDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.DBManager;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Estudiante;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAOImpl implements EstudianteDAO {

    // el perfil se lee a traves de su usuario: buscar_usuario_por_id indica si es_estudiante
    @Override
    public Estudiante load(Integer id) {
        Usuario usuario = new UsuarioDAOImpl().load(id);
        return usuario != null ? usuario.getPerfilEstudiante() : null;
    }

    @Override
    public List<Estudiante> listAll() {
        List<Estudiante> estudiantes = new ArrayList<>();
        for (Usuario usuario : new UsuarioDAOImpl().listAll()) {
            if (usuario.getPerfilEstudiante() != null) {
                estudiantes.add(usuario.getPerfilEstudiante());
            }
        }
        return estudiantes;
    }

    // el usuario del estudiante ya debe estar guardado: la fila de estudiante solo guarda su id
    @Override
    public Estudiante save(Estudiante estudiante) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return saveConConnection(estudiante, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Estudiante save(Estudiante estudiante, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return saveConConnection(estudiante, connection);
    }

    private Estudiante saveConConnection(Estudiante estudiante, Connection connection) {
        String sql = "{CALL insertar_estudiante(?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, estudiante.getUsuario().getIdUsuario());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al insertar estudiante: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return estudiante;
    }

    // la tabla estudiante solo tiene la clave del usuario: no hay columnas que modificar
    @Override
    public Estudiante update(Estudiante estudiante) {
        return estudiante;
    }

    @Override
    public Estudiante update(Estudiante estudiante, TransactionContext transactionContext) {
        return estudiante;
    }

    // falla si el estudiante ya tiene sesiones (clave foranea)
    @Override
    public void remove(Estudiante estudiante) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            removeConConnection(estudiante, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(Estudiante estudiante, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        removeConConnection(estudiante, connection);
    }

    private void removeConConnection(Estudiante estudiante, Connection connection) {
        String sql = "{CALL eliminar_estudiante(?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, estudiante.getUsuario().getIdUsuario());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al eliminar estudiante: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
