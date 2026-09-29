package pe.edu.pucp.tutorPUCP.bl.Impl;

import pe.edu.pucp.tutorPUCP.bl.DAOs.ReclamoDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.DBManager;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoAprobacion;
import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Reclamo;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReclamoDAOImpl implements ReclamoDAO {

    @Override
    public Reclamo load(Integer id) {
        Reclamo reclamo = null;
        String sql = "{CALL buscar_reclamo_por_id(?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                reclamo = leerReclamo(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener reclamo: " + e.getMessage());
        }
        return reclamo;
    }

    @Override
    public List<Reclamo> listAll() {
        return listar("{CALL listar_reclamos()}");
    }

    @Override
    public List<Reclamo> listarPendientes() {
        return listar("{CALL listar_reclamos_pendientes()}");
    }

    // para validar que una sesion no tenga dos reclamos del mismo usuario
    @Override
    public List<Reclamo> listarPorSesion(Sesion sesion) {
        return listar("{CALL listar_reclamos_por_sesion(?)}", sesion.getIdSesion());
    }

    // idSesion es null cuando el SP no recibe parametros
    private List<Reclamo> listar(String sql) {
        return listar(sql, null);
    }

    private List<Reclamo> listar(String sql, Integer idSesion) {
        List<Reclamo> reclamos = new ArrayList<>();
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            if (idSesion != null) {
                stmt.setInt(1, idSesion);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reclamos.add(leerReclamo(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar reclamos: " + e.getMessage());
        }
        return reclamos;
    }

    @Override
    public Reclamo save(Reclamo reclamo) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return saveConConnection(reclamo, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Reclamo save(Reclamo reclamo, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return saveConConnection(reclamo, connection);
    }

    // el reclamo nace PENDIENTE
    private Reclamo saveConConnection(Reclamo reclamo, Connection connection) {
        String sql = "{CALL insertar_reclamo(?,?,?,?,?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, reclamo.getSesion().getIdSesion());
            stmt.setInt(2, reclamo.getReclamante().getIdUsuario());
            stmt.setString(3, reclamo.getRolReclamante().name());
            stmt.setString(4, reclamo.getMotivo());
            stmt.registerOutParameter(5, Types.INTEGER);
            stmt.execute();
            reclamo.setIdReclamo(stmt.getInt(5));
        } catch (SQLException e) {
            System.out.println("Error al insertar reclamo: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return reclamo;
    }

    @Override
    public Reclamo update(Reclamo reclamo) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            return updateConConnection(reclamo, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Reclamo update(Reclamo reclamo, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        return updateConConnection(reclamo, connection);
    }

    // tambien sirve para resolver el reclamo
    private Reclamo updateConConnection(Reclamo reclamo, Connection connection) {
        String sql = "{CALL modificar_reclamo(?,?,?,?,?,?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, reclamo.getIdReclamo());
            stmt.setString(2, reclamo.getMotivo());
            stmt.setString(3, reclamo.getEstado().name());
            stmt.setString(4, reclamo.getResolucion());
            if (reclamo.getAdministradorResuelve() != null) {
                stmt.setInt(5, reclamo.getAdministradorResuelve().getIdAdministrador());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setObject(6, reclamo.getFechaResolucion(), Types.DATE);
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al modificar reclamo: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return reclamo;
    }

    @Override
    public void remove(Reclamo reclamo) {
        try (Connection connection = DBManager.getInstance().getConnection()) {
            removeConConnection(reclamo, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(Reclamo reclamo, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        removeConConnection(reclamo, connection);
    }

    private void removeConConnection(Reclamo reclamo, Connection connection) {
        String sql = "{CALL eliminar_reclamo(?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, reclamo.getIdReclamo());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al eliminar reclamo: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Reclamo leerReclamo(ResultSet rs) throws SQLException {
        Reclamo reclamo = new Reclamo();
        reclamo.setIdReclamo(rs.getInt("id"));
        reclamo.setSesion(new SesionDAOImpl().load(rs.getInt("id_sesion")));
        reclamo.setReclamante(new UsuarioDAOImpl().load(rs.getInt("id_usuario_reclamante")));
        reclamo.setRolReclamante(Rol.valueOf(rs.getString("rol_reclamante")));
        reclamo.setMotivo(rs.getString("motivo"));
        reclamo.setFechaReclamo(rs.getObject("fecha_reclamo", LocalDate.class));
        reclamo.setEstado(EstadoAprobacion.valueOf(rs.getString("estado")));
        reclamo.setResolucion(rs.getString("resolucion"));
        Integer idAdministrador = rs.getObject("id_administrador_resuelve", Integer.class);
        if (idAdministrador != null) {
            reclamo.setAdministradorResuelve(new AdministradorDAOImpl().load(idAdministrador));
        }
        reclamo.setFechaResolucion(rs.getObject("fecha_resolucion", LocalDate.class));
        return reclamo;
    }
}
