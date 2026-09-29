package pe.edu.pucp.tutorPUCP.bl.Clases;

import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.academico.Postulacion;
import pe.edu.pucp.tutorPUCP.modelo.academico.Tema;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Tarifa;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;

import java.util.List;

public interface GestorAcademico {
    Curso registrarCurso(String nombre, String codigo);
    List<Curso> listarCursos();
    Curso buscarCursoPorCodigo(String codigo);
    void desactivarCurso(Curso curso);
    void reactivarCurso(Curso curso);

    // Crea el curso y todos sus temas iniciales en una sola transaccion:
    // si falla cualquier tema, no queda el curso guardado sin ellos.
    Curso registrarCursoConTemas(String nombre, String codigo, List<String> nombresTemas);

    Tema registrarTema(Curso curso, String nombre);
    List<Tema> listarTemasPorCurso(Curso curso);

    Postulacion postular(Tutor tutor, Curso curso, String archivoCertificadoUrl);
    List<Postulacion> listarPostulacionesPorTutor(Tutor tutor);
    void resolverPostulacion(Postulacion postulacion, Administrador administrador, boolean aprobar, String resolucion);

    Tarifa registrarTarifa(Tutor tutor, Curso curso, double montoPorHora);
    List<Tarifa> listarTarifasPorTutor(Tutor tutor);

    List<Tutor> buscarTutoresPorCurso(Curso curso);
}
