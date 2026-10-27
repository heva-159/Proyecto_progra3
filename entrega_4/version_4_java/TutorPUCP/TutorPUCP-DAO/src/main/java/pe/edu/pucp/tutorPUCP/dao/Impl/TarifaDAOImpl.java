package pe.edu.pucp.tutorPUCP.dao.Impl;

import pe.edu.pucp.tutorPUCP.dao.DAOs.TarifaDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.DBManager;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Tarifa;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class TarifaDAOImpl implements TarifaDAO {

    @Override
    public Tarifa load(Integer id) {
        Tarifa tarifa = null;
        String sql = "{CALL buscar_tarifa_por_id(?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                tarifa = leerTarifa(rs, null);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener tarifa: " + e.getMessage());
        }
        return tarifa;
    }

    @Override
    public List<Tarifa> listAll() {
        List<Tarifa> tarifas = new ArrayList<>();
        String sql = "{CALL listar_tarifas()}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tarifas.add(leerTarifa(rs, null));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar tarifas: " + e.getMessage());
        }
        return tarifas;
    }

    // el tutor que se pasa queda asignado a cada tarifa
    @Override
    public List<Tarifa> listarPorTutor(Tutor tutor) {
        List<Tarifa> tarifas = new ArrayList<>();
        String sql = "{CALL listar_tarifas_por_tutor(?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, tutor.getUsuario().getIdUsuario());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tarifas.add(leerTarifa(rs, tutor));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar tarifas del tutor: " + e.getMessage());
        }
        return tarifas;
    }

    @Override
    public Tarifa save(Tarifa tarifa) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return saveConConnection(tarifa, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tarifa save(Tarifa tarifa, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return saveConConnection(tarifa, connection);
    }

    private Tarifa saveConConnection(Tarifa tarifa, Connection connection) {
        String sql = "{CALL insertar_tarifa(?,?,?,?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, tarifa.getTutor().getUsuario().getIdUsuario());
            stmt.setInt(2, tarifa.getCurso().getIdCurso());
            stmt.setDouble(3, tarifa.getMontoPorHora());
            stmt.registerOutParameter(4, Types.INTEGER);
            stmt.execute();
            tarifa.setIdTarifa(stmt.getInt(4));
        } catch (SQLException e) {
            System.out.println("Error al insertar tarifa: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return tarifa;
    }

    @Override
    public Tarifa update(Tarifa tarifa) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return updateConConnection(tarifa, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tarifa update(Tarifa tarifa, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return updateConConnection(tarifa, connection);
    }

    private Tarifa updateConConnection(Tarifa tarifa, Connection connection) {
        String sql = "{CALL modificar_tarifa(?,?,?,?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, tarifa.getIdTarifa());
            stmt.setInt(2, tarifa.getTutor().getUsuario().getIdUsuario());
            stmt.setInt(3, tarifa.getCurso().getIdCurso());
            stmt.setDouble(4, tarifa.getMontoPorHora());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al modificar tarifa: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return tarifa;
    }

    @Override
    public void remove(Tarifa tarifa) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            removeConConnection(tarifa, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(Tarifa tarifa, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        removeConConnection(tarifa, connection);
    }

    private void removeConConnection(Tarifa tarifa, Connection connection) {
        String sql = "{CALL eliminar_tarifa(?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, tarifa.getIdTarifa());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al eliminar tarifa: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // si tutor es null se carga desde la base con el id_tutor de la fila
    private Tarifa leerTarifa(ResultSet rs, Tutor tutor) throws SQLException {
        Tarifa tarifa = new Tarifa();
        tarifa.setIdTarifa(rs.getInt("id"));
        tarifa.setMontoPorHora(rs.getDouble("monto_por_hora"));
        if (tutor == null) {
            Usuario usuario = new UsuarioDAOImpl().load(rs.getInt("id_tutor"));
            tutor = usuario != null ? usuario.getPerfilTutor() : null;
        }
        tarifa.setTutor(tutor);
        tarifa.setCurso(new CursoDAOImpl().load(rs.getInt("id_curso")));
        return tarifa;
    }
}
