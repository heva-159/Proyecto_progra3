package pe.edu.pucp.tutorPUCP.bl.Implementacion;

import pe.edu.pucp.tutorPUCP.bl.DAOs.CursoDAO;
import pe.edu.pucp.tutorPUCP.bl.DAOs.PostulacionDAO;
import pe.edu.pucp.tutorPUCP.bl.DAOs.TarifaDAO;
import pe.edu.pucp.tutorPUCP.bl.DAOs.TemaDAO;
import pe.edu.pucp.tutorPUCP.bl.Exeptions.BL_exeption;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorAcademico;
import pe.edu.pucp.tutorPUCP.bl.Impl.CursoDAOImpl;
import pe.edu.pucp.tutorPUCP.bl.Impl.PostulacionDAOImpl;
import pe.edu.pucp.tutorPUCP.bl.Impl.TarifaDAOImpl;
import pe.edu.pucp.tutorPUCP.bl.Impl.TemaDAOImpl;
import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.academico.Postulacion;
import pe.edu.pucp.tutorPUCP.modelo.academico.Tema;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoAprobacion;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Tarifa;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;

import java.util.ArrayList;
import java.util.List;

public class GestorAcademicoImpl implements GestorAcademico {
    private final CursoDAO cursoDAO = new CursoDAOImpl();
    private final TemaDAO temaDAO = new TemaDAOImpl();
    private final PostulacionDAO postulacionDAO = new PostulacionDAOImpl();
    private final TarifaDAO tarifaDAO = new TarifaDAOImpl();

    @Override
    public Curso registrarCurso(String nombre, String codigo) {
        if (cursoDAO.buscarPorCodigo(codigo) != null) {
            throw new BL_exeption("Ya existe un curso con ese codigo");
        }
        Curso curso = new Curso(nombre, codigo);
        return cursoDAO.save(curso);
    }

    @Override
    public List<Curso> listarCursos() {
        return cursoDAO.listAll();
    }

    @Override
    public Curso buscarCursoPorCodigo(String codigo) {
        return cursoDAO.buscarPorCodigo(codigo);
    }

    @Override
    public void desactivarCurso(Curso curso) {
        curso.setActivo(false);
        cursoDAO.update(curso);
    }

    @Override
    public void reactivarCurso(Curso curso) {
        curso.setActivo(true);
        cursoDAO.update(curso);
    }

    @Override
    public Tema registrarTema(Curso curso, String nombre) {
        Tema tema = new Tema(nombre);
        curso.agregarTema(tema);
        return temaDAO.save(tema);
    }

    @Override
    public List<Tema> listarTemasPorCurso(Curso curso) {
        return temaDAO.listarPorCurso(curso);
    }

    @Override
    public Postulacion postular(Tutor tutor, Curso curso, String archivoCertificadoUrl) {
        Postulacion postulacion = new Postulacion(curso, archivoCertificadoUrl);
        tutor.agregarPostulacion(postulacion);
        return postulacionDAO.save(postulacion);
    }

    @Override
    public List<Postulacion> listarPostulacionesPorTutor(Tutor tutor) {
        return postulacionDAO.listarPorTutor(tutor);
    }

    @Override
    public void resolverPostulacion(Postulacion postulacion, Administrador administrador, boolean aprobar, String resolucion) {
        if (postulacion.getEstado() != EstadoAprobacion.PENDIENTE) {
            throw new BL_exeption("Solo se pueden resolver postulaciones pendientes");
        }
        if (aprobar) postulacion.aprobar(administrador, resolucion);
        else postulacion.rechazar(administrador, resolucion);
        postulacionDAO.update(postulacion);
    }

    @Override
    public Tarifa registrarTarifa(Tutor tutor, Curso curso, double montoPorHora) {
        // se consulta el DAO en vez de tutor.getPostulaciones(): tutor puede venir
        // de un load() que no trae sus postulaciones cargadas en memoria.
        boolean tienePostulacionAprobada = postulacionDAO.listarPorTutor(tutor).stream()
                .anyMatch(p -> p.getCurso().getIdCurso() == curso.getIdCurso() && p.getEstado() == EstadoAprobacion.APROBADA);
        if (!tienePostulacionAprobada) {
            throw new BL_exeption("El tutor no tiene una postulacion aprobada para este curso");
        }
        Tarifa tarifa = new Tarifa(curso, montoPorHora);
        tutor.agregarTarifa(tarifa);
        return tarifaDAO.save(tarifa);
    }

    @Override
    public List<Tarifa> listarTarifasPorTutor(Tutor tutor) {
        return tarifaDAO.listarPorTutor(tutor);
    }

    // PostulacionDAO no tiene listarPorCurso todavia: se filtra en memoria con listAll().
    // Si esta busqueda se usa seguido, conviene agregar ese metodo al DAO para no traer todo.
    @Override
    public List<Tutor> buscarTutoresPorCurso(Curso curso) {
        List<Tutor> tutores = new ArrayList<>();
        for (Postulacion postulacion : postulacionDAO.listAll()) {
            if (postulacion.getCurso().getIdCurso() == curso.getIdCurso()
                    && postulacion.getEstado() == EstadoAprobacion.APROBADA) {
                tutores.add(postulacion.getTutor());
            }
        }
        return tutores;
    }
}
