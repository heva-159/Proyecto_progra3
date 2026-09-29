package pe.edu.pucp.tutorPUCP.dao.Impl;

import pe.edu.pucp.tutorPUCP.dao.DAOs.AdministradorDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.DBManager;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdministradorDAOImpl implements AdministradorDAO {

    @Override
    public Administrador load(Integer id) {
        Administrador administrador = null;
        String sql = "{CALL buscar_administrador_por_id(?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                administrador = leerAdministrador(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener administrador: " + e.getMessage());
        }
        return administrador;
    }

    // para el login del administrador
    public Administrador buscarPorCorreo(String correo) {
        Administrador administrador = null;
        String sql = "{CALL buscar_administrador_por_correo(?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setString(1, correo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                administrador = leerAdministrador(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar administrador por correo: " + e.getMessage());
        }
        return administrador;
    }

    @Override
    public List<Administrador> listAll() {
        List<Administrador> administradores = new ArrayList<>();
        String sql = "{CALL listar_administradores()}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                administradores.add(leerAdministrador(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar administradores: " + e.getMessage());
        }
        return administradores;
    }

    @Override
    public Administrador save(Administrador administrador) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return saveConConnection(administrador, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Administrador save(Administrador administrador, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return saveConConnection(administrador, connection);
    }

    private Administrador saveConConnection(Administrador administrador, Connection connection) {
        String sql = "{CALL insertar_administrador(?,?,?,?,?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setString(1, administrador.getNombre());
            stmt.setString(2, administrador.getCorreo());
            stmt.setString(3, administrador.getPasswordHash());
            stmt.setString(4, administrador.getNivelPermiso());
            stmt.registerOutParameter(5, Types.INTEGER);
            stmt.execute();
            administrador.setIdAdministrador(stmt.getInt(5));
        } catch (SQLException e) {
            System.out.println("Error al insertar administrador: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return administrador;
    }

    @Override
    public Administrador update(Administrador administrador) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return updateConConnection(administrador, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Administrador update(Administrador administrador, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return updateConConnection(administrador, connection);
    }

    private Administrador updateConConnection(Administrador administrador, Connection connection) {
        String sql = "{CALL modificar_administrador(?,?,?,?,?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, administrador.getIdAdministrador());
            stmt.setString(2, administrador.getNombre());
            stmt.setString(3, administrador.getCorreo());
            stmt.setString(4, administrador.getPasswordHash());
            stmt.setString(5, administrador.getNivelPermiso());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al modificar administrador: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return administrador;
    }

    @Override
    public void remove(Administrador administrador) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            removeConConnection(administrador, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(Administrador administrador, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        removeConConnection(administrador, connection);
    }

    private void removeConConnection(Administrador administrador, Connection connection) {
        String sql = "{CALL eliminar_administrador(?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, administrador.getIdAdministrador());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al eliminar administrador: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Administrador leerAdministrador(ResultSet rs) throws SQLException {
        Administrador administrador = new Administrador();
        administrador.setIdAdministrador(rs.getInt("id"));
        administrador.setNombre(rs.getString("nombre"));
        administrador.setCorreo(rs.getString("correo"));
        administrador.setPasswordHash(rs.getString("password_hash"));
        administrador.setFechaCreacion(rs.getObject("fecha_creacion", LocalDate.class));
        administrador.setNivelPermiso(rs.getString("nivel_permiso"));
        return administrador;
    }
}
