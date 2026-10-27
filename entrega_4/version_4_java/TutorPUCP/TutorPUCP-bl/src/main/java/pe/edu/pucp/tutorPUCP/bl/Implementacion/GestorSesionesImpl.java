package pe.edu.pucp.tutorPUCP.bl.Implementacion;

import pe.edu.pucp.tutorPUCP.dao.DAOs.PostulacionDAO;
import pe.edu.pucp.tutorPUCP.dao.DAOs.SesionDAO;
import pe.edu.pucp.tutorPUCP.dao.DAOs.SesionTemaDAO;
import pe.edu.pucp.tutorPUCP.dao.DAOs.TemaDAO;
import pe.edu.pucp.tutorPUCP.bl.Exeptions.BL_exeption;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorSesiones;
import pe.edu.pucp.tutorPUCP.dao.Impl.PostulacionDAOImpl;
import pe.edu.pucp.tutorPUCP.dao.Impl.SesionDAOImpl;
import pe.edu.pucp.tutorPUCP.dao.Impl.SesionTemaDAOImpl;
import pe.edu.pucp.tutorPUCP.dao.Impl.TemaDAOImpl;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.academico.Tema;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoAprobacion;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoSesion;
import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Estudiante;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GestorSesionesImpl implements GestorSesiones {
    private final SesionDAO sesionDAO = new SesionDAOImpl();
    private final SesionTemaDAO sesionTemaDAO = new SesionTemaDAOImpl();
    private final PostulacionDAO postulacionDAO = new PostulacionDAOImpl();
    private final TemaDAO temaDAO = new TemaDAOImpl();

    @Override
    public Sesion registrarSesion(Tutor tutor, Estudiante estudiante, Curso curso,
                                   List<Tema> temas, LocalDateTime fechaHoraInicio) {
        if (!curso.isActivo()) {
            throw new BL_exeption("El curso no esta activo");
        }
        if (tutor.getUsuario().getIdUsuario() == estudiante.getUsuario().getIdUsuario()) {
            throw new BL_exeption("El tutor y el estudiante no pueden ser el mismo usuario");
        }
        if (fechaHoraInicio == null || !fechaHoraInicio.isAfter(LocalDateTime.now())) {
            throw new BL_exeption("La sesion debe programarse en una fecha y hora futura");
        }
        boolean tienePostulacionAprobada = postulacionDAO.listarPorTutor(tutor).stream()
                .anyMatch(p -> p.getCurso().getIdCurso() == curso.getIdCurso() && p.getEstado() == EstadoAprobacion.APROBADA);
        if (!tienePostulacionAprobada) {
            throw new BL_exeption("El tutor no tiene una postulacion aprobada para este curso");
        }
        validarTemasDelCurso(curso, temas);

        // al crearse, la sesion queda en las colecciones del tutor y del estudiante
        Sesion sesion = new Sesion(tutor, estudiante, curso, fechaHoraInicio);
        for (Tema tema : temas) sesion.agregarTema(tema);

        // la transaccion la abre esta capa: la sesion y sus temas se guardan juntos o no se guarda nada
        try (TransactionContext tx = new TransactionContext()) {
            sesionDAO.save(sesion, tx);
            for (Tema tema : sesion.getTemas()) {
                sesionTemaDAO.save(sesion, tema, tx);
            }
            tx.commit();
            return sesion;
        } catch (SQLException e) {
            throw new BL_exeption("No se pudo registrar la sesion con sus temas: " + e.getMessage());
        }
    }

    @Override
    public Sesion modificarSesion(Sesion sesion) {
        Sesion guardada = sesionDAO.load(sesion.getIdSesion());
        if (guardada == null) {
            throw new BL_exeption("La sesion no existe");
        }
        if (guardada.getEstado() != EstadoSesion.PROGRAMADA) {
            throw new BL_exeption("Solo se puede modificar una sesion programada");
        }
        if (sesion.getFechaHoraInicio() == null || !sesion.getFechaHoraInicio().isAfter(LocalDateTime.now())) {
            throw new BL_exeption("La sesion debe programarse en una fecha y hora futura");
        }
        validarTemasDelCurso(sesion.getCurso(), sesion.getTemas());

        try (TransactionContext tx = new TransactionContext()) {
            sesionDAO.update(sesion, tx);
            // los temas se vuelven a asociar: se quitan todos y se insertan los actuales
            sesionTemaDAO.eliminarPorSesion(sesion, tx);
            for (Tema tema : sesion.getTemas()) {
                sesionTemaDAO.save(sesion, tema, tx);
            }
            tx.commit();
            return sesion;
        } catch (SQLException e) {
            throw new BL_exeption("No se pudo modificar la sesion con sus temas: " + e.getMessage());
        }
    }

    @Override
    public void eliminarSesion(Sesion sesion) {
        Sesion guardada = sesionDAO.load(sesion.getIdSesion());
        if (guardada == null) {
            throw new BL_exeption("La sesion no existe");
        }
        if (guardada.getEstado() == EstadoSesion.EN_CURSO || guardada.getEstado() == EstadoSesion.REALIZADA) {
            throw new BL_exeption("No se puede eliminar una sesion en curso o ya realizada");
        }
        try {
            // sus temas se borran solos por el on delete cascade de sesion_tema
            sesionDAO.remove(sesion);
        } catch (RuntimeException e) {
            throw new BL_exeption("No se puede eliminar la sesion porque tiene reclamos registrados");
        }
    }

    @Override
    public Sesion obtenerSesionPorId(int idSesion) {
        return sesionDAO.load(idSesion);
    }

    @Override
    public List<Sesion> listarSesiones() {
        return sesionDAO.listAll();
    }

    // se valida contra la BD (no contra tema.getCurso() en memoria) para no depender
    // de que el caller haya poblado esa referencia correctamente.
    private void validarTemasDelCurso(Curso curso, List<Tema> temas) {
        Set<Integer> idsTemasDelCurso = new HashSet<>();
        for (Tema t : temaDAO.listarPorCurso(curso)) idsTemasDelCurso.add(t.getIdTema());
        for (Tema tema : temas) {
            if (!idsTemasDelCurso.contains(tema.getIdTema())) {
                throw new BL_exeption("El tema '" + tema.getNombre() + "' no pertenece al curso de la sesion");
            }
        }
    }

    @Override
    public void confirmarCheckIn(Sesion sesion, Rol rol) {
        if (sesion.getEstado() != EstadoSesion.PROGRAMADA) {
            throw new BL_exeption("Solo se puede hacer check-in de una sesion programada");
        }
        if (rol == Rol.TUTOR) sesion.setCheckInTutor(true);
        else if (rol == Rol.ESTUDIANTE) sesion.setCheckInEstudiante(true);
        if (sesion.isCheckInTutor() && sesion.isCheckInEstudiante()) {
            sesion.setEstado(EstadoSesion.EN_CURSO);
        }
        sesionDAO.update(sesion);
    }

    @Override
    public void confirmarCheckOut(Sesion sesion, Rol rol) {
        if (sesion.getEstado() != EstadoSesion.EN_CURSO) {
            throw new BL_exeption("Solo se puede hacer check-out de una sesion en curso");
        }
        if (rol == Rol.TUTOR) sesion.setCheckOutTutor(true);
        else if (rol == Rol.ESTUDIANTE) sesion.setCheckOutEstudiante(true);
        if (sesion.isCheckOutTutor() && sesion.isCheckOutEstudiante()) {
            sesion.setEstado(EstadoSesion.REALIZADA);
            sesion.setFechaHoraFin(LocalDateTime.now());
        }
        sesionDAO.update(sesion);
    }

    @Override
    public void calificarSesion(Sesion sesion, int puntaje, String comentario) {
        if (sesion.getEstado() != EstadoSesion.REALIZADA) {
            throw new BL_exeption("Solo se puede calificar una sesion ya realizada");
        }
        if (puntaje < 1 || puntaje > 5) {
            throw new BL_exeption("El puntaje debe estar entre 1 y 5");
        }
        sesion.setPuntaje(puntaje);
        sesion.setComentario(comentario);
        sesionDAO.update(sesion);
    }

    @Override
    public void cancelarSesion(Sesion sesion, Usuario quienCancela, String motivo) {
        if (sesion.getEstado() != EstadoSesion.PROGRAMADA) {
            throw new BL_exeption("Solo se puede cancelar una sesion programada");
        }
        // se compara por id: quienCancela y el usuario del tutor/estudiante de la sesion
        // vienen de loads independientes, no son el mismo objeto en memoria.
        Usuario usuarioTutor = sesion.getTutor().getUsuario();
        Usuario usuarioEstudiante = sesion.getEstudiante().getUsuario();
        boolean esTutor = usuarioTutor != null && usuarioTutor.getIdUsuario() == quienCancela.getIdUsuario();
        boolean esEstudiante = usuarioEstudiante != null && usuarioEstudiante.getIdUsuario() == quienCancela.getIdUsuario();
        if (!esTutor && !esEstudiante) {
            throw new BL_exeption("Solo el tutor o el estudiante de la sesion pueden cancelarla");
        }
        sesion.setEstado(EstadoSesion.CANCELADA);
        sesion.setMotivoCancelacion(motivo);
        sesion.setUsuarioCancelo(quienCancela);
        sesionDAO.update(sesion);
    }

    @Override
    public void verificarInasistencia(Sesion sesion, LocalDateTime ahora) {
        if (sesion.getEstado() == EstadoSesion.PROGRAMADA
                && !(sesion.isCheckInTutor() && sesion.isCheckInEstudiante())
                && ahora.isAfter(sesion.getFechaHoraInicio().plusMinutes(15))) {
            sesion.setEstado(EstadoSesion.CANCELADA_INASISTENCIA);
            sesionDAO.update(sesion);
        }
    }
}
