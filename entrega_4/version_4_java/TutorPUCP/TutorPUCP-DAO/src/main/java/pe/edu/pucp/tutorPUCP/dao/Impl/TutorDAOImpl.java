package pe.edu.pucp.tutorPUCP.dao.Impl;

import pe.edu.pucp.tutorPUCP.dao.DAOs.TutorDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.DBManager;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TutorDAOImpl implements TutorDAO {

    // el perfil se lee a traves de su usuario: buscar_usuario_por_id indica si es_tutor
    @Override
    public Tutor load(Integer id) {
        Usuario usuario = new UsuarioDAOImpl().load(id);
        return usuario != null ? usuario.getPerfilTutor() : null;
    }

    @Override
    public List<Tutor> listAll() {
        List<Tutor> tutores = new ArrayList<>();
        for (Usuario usuario : new UsuarioDAOImpl().listAll()) {
            if (usuario.getPerfilTutor() != null) {
                tutores.add(usuario.getPerfilTutor());
            }
        }
        return tutores;
    }

    // el usuario del tutor ya debe estar guardado: la fila de tutor solo guarda su id
    @Override
    public Tutor save(Tutor tutor) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return saveConConnection(tutor, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tutor save(Tutor tutor, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return saveConConnection(tutor, connection);
    }

    private Tutor saveConConnection(Tutor tutor, Connection connection) {
        String sql = "{CALL insertar_tutor(?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, tutor.getUsuario().getIdUsuario());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al insertar tutor: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return tutor;
    }

    // la tabla tutor solo tiene la clave del usuario: no hay columnas que modificar
    @Override
    public Tutor update(Tutor tutor) {
        return tutor;
    }

    @Override
    public Tutor update(Tutor tutor, TransactionContext transactionContext) {
        return tutor;
    }

    // falla si el tutor ya tiene postulaciones, tarifas o sesiones (claves foraneas)
    @Override
    public void remove(Tutor tutor) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            removeConConnection(tutor, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(Tutor tutor, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        removeConConnection(tutor, connection);
    }

    private void removeConConnection(Tutor tutor, Connection connection) {
        String sql = "{CALL eliminar_tutor(?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, tutor.getUsuario().getIdUsuario());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al eliminar tutor: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
