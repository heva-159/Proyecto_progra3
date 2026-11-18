package pe.edu.pucp.tutorPUCP.bl.Clases;

import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.academico.Postulacion;
import pe.edu.pucp.tutorPUCP.modelo.academico.Tema;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Tarifa;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface GestorAcademico {
    // ---- Curso ----
    Curso registrarCurso(String nombre, String codigo);
    // Falla si el curso no existe o si otro curso ya usa ese codigo.
    void modificarCurso(Curso curso, String codigo, String nombre);
    // Eliminacion logica: el curso queda inactivo y conserva sus temas, postulaciones y sesiones.
    void eliminarCurso(Curso curso);
    // Devuelve null si no existe.
    Curso obtenerCursoPorId(int idCurso);
    List<Curso> listarCursos();
    Curso buscarCursoPorCodigo(String codigo);
    void desactivarCurso(Curso curso);
    void reactivarCurso(Curso curso);

    // Crea el curso y todos sus temas iniciales en una sola transaccion:
    // si falla cualquier tema, no queda el curso guardado sin ellos.
    Curso registrarCursoConTemas(String nombre, String codigo, List<String> nombresTemas);

    // ---- Tema ----
    Tema registrarTema(Curso curso, String nombre);
    // El tema debe traer su curso (los de listarTemasPorCurso ya lo traen). Falla si el tema no existe.
    void modificarTema(Tema tema, String nombre);
    // Falla si el tema no existe o si ya se uso en una sesion. Tambien lo saca de la coleccion de temas de su curso.
    void eliminarTema(Tema tema);
    Tema obtenerTemaPorId(int idTema);
    List<Tema> listarTemas();
    List<Tema> listarTemasPorCurso(Curso curso);

    // cuantos temas tiene cada curso (id del curso -> cantidad) en una sola consulta; los cursos sin temas no aparecen
    Map<Integer, Integer> contarTemasPorCurso();

    // ---- Postulacion ----
    Postulacion postular(Tutor tutor, Curso curso, String archivoCertificadoUrl);
    // RF004: postula adjuntando el PDF del certificado (maximo 5 MB). El PDF se guarda en el servidor
    // y en la BD queda su ruta (archivo_certificado_url).
    Postulacion postularConCertificado(Tutor tutor, Curso curso, byte[] certificadoPdf);
    // RF005: el PDF del certificado, para que el administrador lo revise. null si el archivo no esta en el servidor.
    byte[] obtenerCertificado(Postulacion postulacion);
    // true si el PDF del certificado esta en el servidor (algunas postulaciones solo guardaron un nombre).
    boolean tieneCertificado(Postulacion postulacion);
    // Solo se puede modificar una postulacion pendiente.
    Postulacion modificarPostulacion(Postulacion postulacion);
    // No se puede eliminar una postulacion aprobada.
    void eliminarPostulacion(Postulacion postulacion);
    Postulacion obtenerPostulacionPorId(int idPostulacion);
    List<Postulacion> listarPostulaciones();
    List<Postulacion> listarPostulacionesPorTutor(Tutor tutor);
    List<Postulacion> listarPostulacionesPendientes();
    void resolverPostulacion(Postulacion postulacion, Administrador administrador, boolean aprobar, String resolucion);

    // ---- Tarifa ----
    Tarifa registrarTarifa(Tutor tutor, Curso curso, BigDecimal montoPorHora);
    Tarifa modificarTarifa(Tarifa tarifa);
    // RF008: si el tutor ya cobra en ese curso, cambia el monto; si no, registra la tarifa.
    Tarifa guardarTarifa(Tutor tutor, Curso curso, BigDecimal montoPorHora);
    void eliminarTarifa(Tarifa tarifa);
    Tarifa obtenerTarifaPorId(int idTarifa);
    List<Tarifa> listarTarifas();
    List<Tarifa> listarTarifasPorTutor(Tutor tutor);

    List<Tutor> buscarTutoresPorCurso(Curso curso);
}
