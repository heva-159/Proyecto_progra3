package pe.edu.pucp.tutorPUCP.dao.Impl;

import pe.edu.pucp.tutorPUCP.dao.DAOs.SesionTemaDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.DBManager;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.academico.Tema;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SesionTemaDAOImpl implements SesionTemaDAO {

    // la sesion y el tema ya deben estar guardados; siempre dentro de una transaccion de la capa de negocio
    @Override
    public void save(Sesion sesion, Tema tema, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        String sql = "{CALL insertar_sesion_tema(?,?,?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, sesion.getIdSesion());
            stmt.setInt(2, tema.getIdTema());
            stmt.registerOutParameter(3, Types.INTEGER);
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al asociar el tema a la sesion: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // quita todos los temas de la sesion (se usa antes de volver a asociarlos)
    @Override
    public void eliminarPorSesion(Sesion sesion, TransactionContext transactionContext) {
        Connection connection = transactionContext.getConnection();
        String sql = "{CALL eliminar_temas_por_sesion(?)}";
        try {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, sesion.getIdSesion());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("Error al quitar los temas de la sesion: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Tema> listarPorSesion(Sesion sesion) {
        List<Tema> temas = new ArrayList<>();
        String sql = "{CALL listar_temas_por_sesion(?)}";
        try (Connection connection = DBManager.getInstance().getConnection()) {
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setInt(1, sesion.getIdSesion());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Tema tema = new Tema();
                tema.setIdTema(rs.getInt("id"));
                tema.setNombre(rs.getString("nombre"));
                tema.setCurso(sesion.getCurso());
                temas.add(tema);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar temas de la sesion: " + e.getMessage());
        }
        return temas;
    }
}
