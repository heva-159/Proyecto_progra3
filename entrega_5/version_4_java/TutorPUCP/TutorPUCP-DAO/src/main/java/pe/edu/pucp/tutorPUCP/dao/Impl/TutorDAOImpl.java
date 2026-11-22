package pe.edu.pucp.tutorPUCP.dao.Impl;

import pe.edu.pucp.tutorPUCP.dao.DAOs.TutorDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.DBManager;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.usuario.PerfilTutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    // El perfil publico (RF009): datos del usuario con su promedio, los cursos con postulacion aprobada y su tarifa, los
    // temas de esos cursos y las ultimas calificaciones. Consultas simples (no procedimientos), como en SesionDAOImpl.
    @Override
    public PerfilTutor obtenerPerfil(int idTutor) {
        String sqlTutor = "SELECT u.nombres, u.apellidos, u.facultad, u.especialidad, u.correo_verificado, "
                + "(SELECT AVG(s.puntaje) FROM sesion s WHERE s.id_tutor = u.id AND s.puntaje IS NOT NULL) AS calificacion, "
                + "(SELECT COUNT(*) FROM sesion s WHERE s.id_tutor = u.id AND s.puntaje IS NOT NULL) AS cantidad_calificaciones, "
                + "(SELECT COUNT(*) FROM sesion s WHERE s.id_tutor = u.id AND s.estado = 'REALIZADA') AS sesiones_dictadas "
                + "FROM usuario u JOIN tutor t ON t.id_usuario = u.id WHERE u.id = ?";
        String sqlCursos = "SELECT DISTINCT c.id, c.codigo, c.nombre, ta.monto_por_hora "
                + "FROM postulacion p "
                + "JOIN curso c ON c.id = p.id_curso AND c.activo = 1 "
                + "LEFT JOIN tarifa ta ON ta.id_tutor = p.id_tutor AND ta.id_curso = p.id_curso "
                + "WHERE p.id_tutor = ? AND p.estado = 'APROBADA' ORDER BY c.codigo";
        String sqlTemas = "SELECT tm.id_curso, tm.nombre FROM tema tm "
                + "WHERE tm.id_curso IN (SELECT p.id_curso FROM postulacion p WHERE p.id_tutor = ? AND p.estado = 'APROBADA') "
                + "ORDER BY tm.id_curso, tm.nombre";
        String sqlResenas = "SELECT c.codigo, c.nombre, s.puntaje, s.comentario, s.fecha_hora_inicio "
                + "FROM sesion s JOIN curso c ON c.id = s.id_curso "
                + "WHERE s.id_tutor = ? AND s.puntaje IS NOT NULL "
                + "ORDER BY s.fecha_hora_inicio DESC, s.id DESC LIMIT 10";

        try (Connection connection = DBManager.getInstance().getConnection()) {
            PerfilTutor perfil = new PerfilTutor();
            perfil.setIdTutor(idTutor);
            try (PreparedStatement stmt = connection.prepareStatement(sqlTutor)) {
                stmt.setInt(1, idTutor);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    String nombres = rs.getString("nombres");
                    String apellidos = rs.getString("apellidos");
                    perfil.setNombre(((nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "")).trim());
                    perfil.setFacultad(rs.getString("facultad"));
                    perfil.setEspecialidad(rs.getString("especialidad"));
                    perfil.setCorreoVerificado(rs.getBoolean("correo_verificado"));
                    double promedio = rs.getDouble("calificacion");
                    perfil.setCalificacion(rs.wasNull() ? null : Math.round(promedio * 10) / 10.0);
                    perfil.setCantidadCalificaciones(rs.getInt("cantidad_calificaciones"));
                    perfil.setSesionesDictadas(rs.getInt("sesiones_dictadas"));
                }
            }

            Map<Integer, PerfilTutor.CursoDelTutor> cursos = new LinkedHashMap<>();
            try (PreparedStatement stmt = connection.prepareStatement(sqlCursos)) {
                stmt.setInt(1, idTutor);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        PerfilTutor.CursoDelTutor curso = new PerfilTutor.CursoDelTutor();
                        curso.setIdCurso(rs.getInt("id"));
                        curso.setCodigo(rs.getString("codigo"));
                        curso.setNombre(rs.getString("nombre"));
                        curso.setTarifaPorHora(rs.getBigDecimal("monto_por_hora"));
                        cursos.put(curso.getIdCurso(), curso);
                    }
                }
            }
            try (PreparedStatement stmt = connection.prepareStatement(sqlTemas)) {
                stmt.setInt(1, idTutor);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        PerfilTutor.CursoDelTutor curso = cursos.get(rs.getInt("id_curso"));
                        if (curso != null) {
                            curso.getTemas().add(rs.getString("nombre"));
                        }
                    }
                }
            }
            perfil.setCursos(new ArrayList<>(cursos.values()));

            try (PreparedStatement stmt = connection.prepareStatement(sqlResenas)) {
                stmt.setInt(1, idTutor);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        PerfilTutor.ResenaDelTutor resena = new PerfilTutor.ResenaDelTutor();
                        resena.setCodigoCurso(rs.getString("codigo"));
                        resena.setCurso(rs.getString("nombre"));
                        resena.setPuntaje(rs.getInt("puntaje"));
                        resena.setComentario(rs.getString("comentario"));
                        resena.setFecha(rs.getObject("fecha_hora_inicio", LocalDateTime.class));
                        perfil.getResenas().add(resena);
                    }
                }
            }
            return perfil;
        } catch (SQLException e) {
            System.out.println("Error al obtener el perfil del tutor: " + e.getMessage());
            throw new RuntimeException(e);
        }
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
