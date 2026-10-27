package pe.edu.pucp.tutorPUCP.bl.Implementacion;

import pe.edu.pucp.tutorPUCP.dao.DAOs.CursoDAO;
import pe.edu.pucp.tutorPUCP.dao.DAOs.PostulacionDAO;
import pe.edu.pucp.tutorPUCP.dao.DAOs.TarifaDAO;
import pe.edu.pucp.tutorPUCP.dao.DAOs.TemaDAO;
import pe.edu.pucp.tutorPUCP.bl.Exeptions.BL_exeption;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorAcademico;
import pe.edu.pucp.tutorPUCP.dao.Impl.CursoDAOImpl;
import pe.edu.pucp.tutorPUCP.dao.Impl.PostulacionDAOImpl;
import pe.edu.pucp.tutorPUCP.dao.Impl.TarifaDAOImpl;
import pe.edu.pucp.tutorPUCP.dao.Impl.TemaDAOImpl;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.academico.Postulacion;
import pe.edu.pucp.tutorPUCP.modelo.academico.Tema;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoAprobacion;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Tarifa;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GestorAcademicoImpl implements GestorAcademico {
    // tarifa.monto_por_hora es decimal(6,2)
    private static final BigDecimal MONTO_MAXIMO = new BigDecimal("9999.99");

    private final CursoDAO cursoDAO = new CursoDAOImpl();
    private final TemaDAO temaDAO = new TemaDAOImpl();
    private final PostulacionDAO postulacionDAO = new PostulacionDAOImpl();
    private final TarifaDAO tarifaDAO = new TarifaDAOImpl();

    // ---- Curso ----

    @Override
    public Curso registrarCurso(String nombre, String codigo) {
        validarDatosCurso(codigo, nombre);
        if (cursoDAO.buscarPorCodigo(codigo) != null) {
            throw new BL_exeption("Ya existe un curso con ese codigo");
        }
        Curso curso = new Curso(nombre, codigo);
        return cursoDAO.save(curso);
    }

    @Override
    public Curso modificarCurso(Curso curso) {
        if (cursoDAO.load(curso.getIdCurso()) == null) {
            throw new BL_exeption("El curso no existe");
        }
        if (curso.getNombre() == null || curso.getNombre().isBlank()) {
            throw new BL_exeption("El nombre del curso es obligatorio");
        }
        Curso conMismoCodigo = cursoDAO.buscarPorCodigo(curso.getCodigo());
        if (conMismoCodigo != null && conMismoCodigo.getIdCurso() != curso.getIdCurso()) {
            throw new BL_exeption("Ya existe otro curso con ese codigo");
        }
        return cursoDAO.update(curso);
    }

    // eliminacion logica (el SP deja el curso inactivo)
    @Override
    public void eliminarCurso(Curso curso) {
        if (cursoDAO.load(curso.getIdCurso()) == null) {
            throw new BL_exeption("El curso no existe");
        }
        cursoDAO.remove(curso);
        curso.setActivo(false);
    }

    @Override
    public Curso obtenerCursoPorId(int idCurso) {
        return cursoDAO.load(idCurso);
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
    public void modificarCurso(Curso curso, String codigo, String nombre) {
        validarDatosCurso(codigo, nombre);
        Curso otro = cursoDAO.buscarPorCodigo(codigo);
        if (otro != null && otro.getIdCurso() != curso.getIdCurso()) {
            throw new BL_exeption("Ya existe otro curso con ese codigo");
        }
        curso.setCodigo(codigo);
        curso.setNombre(nombre);
        cursoDAO.update(curso);
    }

    @Override
    public Curso registrarCursoConTemas(String nombre, String codigo, List<String> nombresTemas) {
        validarDatosCurso(codigo, nombre);
        if (cursoDAO.buscarPorCodigo(codigo) != null) {
            throw new BL_exeption("Ya existe un curso con ese codigo");
        }
        try (TransactionContext tx = new TransactionContext()) {
            Curso curso = new Curso(nombre, codigo);
            Curso cursoGuardado = cursoDAO.save(curso, tx);
            for (String nombreTema : nombresTemas) {
                Tema tema = new Tema(nombreTema);
                cursoGuardado.agregarTema(tema);
                temaDAO.save(tema, tx);
            }
            tx.commit();
            return cursoGuardado;
        } catch (SQLException e) {
            throw new BL_exeption("No se pudo registrar el curso con sus temas: " + e.getMessage());
        }
    }

    // ---- Tema ----

    @Override
    public Tema registrarTema(Curso curso, String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new BL_exeption("El nombre del tema es obligatorio");
        }
        if (!curso.isActivo()) {
            throw new BL_exeption("No se pueden agregar temas a un curso inactivo");
        }
        for (Tema existente : temaDAO.listarPorCurso(curso)) {
            if (existente.getNombre().equalsIgnoreCase(nombre)) {
                throw new BL_exeption("El curso ya tiene un tema con ese nombre");
            }
        }
        Tema tema = new Tema(nombre);
        curso.agregarTema(tema);
        return temaDAO.save(tema);
    }

    @Override
    public Tema modificarTema(Tema tema) {
        Tema guardado = temaDAO.load(tema.getIdTema());
        if (guardado == null) {
            throw new BL_exeption("El tema no existe");
        }
        if (tema.getNombre() == null || tema.getNombre().isBlank()) {
            throw new BL_exeption("El nombre del tema es obligatorio");
        }
        // el tema no cambia de curso: se valida el nombre dentro del curso que ya tiene guardado
        if (tema.getCurso() == null) {
            tema.setCurso(guardado.getCurso());
        }
        for (Tema existente : temaDAO.listarPorCurso(guardado.getCurso())) {
            if (existente.getIdTema() != tema.getIdTema() && existente.getNombre().equalsIgnoreCase(tema.getNombre())) {
                throw new BL_exeption("El curso ya tiene un tema con ese nombre");
            }
        }
        return temaDAO.update(tema);
    }

    @Override
    public void eliminarTema(Tema tema) {
        if (temaDAO.load(tema.getIdTema()) == null) {
            throw new BL_exeption("El tema no existe");
        }
        try {
            temaDAO.remove(tema);
        } catch (RuntimeException e) {
            throw new BL_exeption("No se puede eliminar el tema: ya se uso en alguna sesion");
        }
        if (tema.getCurso() != null) {
            tema.getCurso().quitarTema(tema);
        }
    }

    @Override
    public Tema obtenerTemaPorId(int idTema) {
        return temaDAO.load(idTema);
    }

    @Override
    public List<Tema> listarTemas() {
        return temaDAO.listAll();
    }

    @Override
    public List<Tema> listarTemasPorCurso(Curso curso) {
        return temaDAO.listarPorCurso(curso);
    }

    // el tema debe traer su curso (los de listarTemasPorCurso ya lo traen)
    @Override
    public void modificarTema(Tema tema, String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new BL_exeption("El nombre del tema es obligatorio");
        }
        for (Tema existente : temaDAO.listarPorCurso(tema.getCurso())) {
            if (existente.getIdTema() != tema.getIdTema()
                    && existente.getNombre().equalsIgnoreCase(nombre)) {
                throw new BL_exeption("El curso ya tiene un tema con ese nombre");
            }
        }
        tema.setNombre(nombre);
        temaDAO.update(tema);
    }

    // un solo DELETE: no necesita transaccion. Si el tema ya se uso en una sesion,
    // la BD no deja borrarlo (fk_sesion_tema_tema) y se avisa con un mensaje claro.
    @Override
    public void eliminarTema(Tema tema) {
        try {
            temaDAO.remove(tema);
        } catch (RuntimeException e) {
            throw new BL_exeption("No se pudo eliminar el tema (puede estar usado en una sesion): " + e.getMessage());
        }
    }
    // ---- Postulacion ----

    @Override
    public Postulacion postular(Tutor tutor, Curso curso, String archivoCertificadoUrl) {
        if (!curso.isActivo()) {
            throw new BL_exeption("No se puede postular a un curso inactivo");
        }
        if (archivoCertificadoUrl == null || !archivoCertificadoUrl.toLowerCase().endsWith(".pdf")) {
            throw new BL_exeption("El certificado debe ser un archivo .pdf");
        }
        boolean yaPostulo = postulacionDAO.listarPorTutor(tutor).stream()
                .anyMatch(p -> p.getCurso().getIdCurso() == curso.getIdCurso()
                        && (p.getEstado() == EstadoAprobacion.PENDIENTE || p.getEstado() == EstadoAprobacion.APROBADA));
        if (yaPostulo) {
            throw new BL_exeption("El tutor ya tiene una postulacion pendiente o aprobada para este curso");
        }
        Postulacion postulacion = new Postulacion(curso, archivoCertificadoUrl);
        tutor.agregarPostulacion(postulacion);
        return postulacionDAO.save(postulacion);
    }

    @Override
    public Postulacion modificarPostulacion(Postulacion postulacion) {
        Postulacion guardada = postulacionDAO.load(postulacion.getIdPostulacion());
        if (guardada == null) {
            throw new BL_exeption("La postulacion no existe");
        }
        if (guardada.getEstado() != EstadoAprobacion.PENDIENTE) {
            throw new BL_exeption("Solo se puede modificar una postulacion pendiente");
        }
        String certificado = postulacion.getArchivoCertificadoUrl();
        if (certificado == null || !certificado.toLowerCase().endsWith(".pdf")) {
            throw new BL_exeption("El certificado debe ser un archivo .pdf");
        }
        return postulacionDAO.update(postulacion);
    }

    @Override
    public void eliminarPostulacion(Postulacion postulacion) {
        Postulacion guardada = postulacionDAO.load(postulacion.getIdPostulacion());
        if (guardada == null) {
            throw new BL_exeption("La postulacion no existe");
        }
        if (guardada.getEstado() == EstadoAprobacion.APROBADA) {
            throw new BL_exeption("No se puede eliminar una postulacion aprobada");
        }
        postulacionDAO.remove(postulacion);
    }

    @Override
    public Postulacion obtenerPostulacionPorId(int idPostulacion) {
        return postulacionDAO.load(idPostulacion);
    }

    @Override
    public List<Postulacion> listarPostulaciones() {
        return postulacionDAO.listAll();
    }

    @Override
    public List<Postulacion> listarPostulacionesPorTutor(Tutor tutor) {
        return postulacionDAO.listarPorTutor(tutor);
    }

    // PostulacionDAO no tiene listarPendientes: se filtra en memoria con listAll().
    @Override
    public List<Postulacion> listarPostulacionesPendientes() {
        List<Postulacion> pendientes = new ArrayList<>();
        for (Postulacion postulacion : postulacionDAO.listAll()) {
            if (postulacion.getEstado() == EstadoAprobacion.PENDIENTE) {
                pendientes.add(postulacion);
            }
        }
        return pendientes;
    }

    @Override
    public void resolverPostulacion(Postulacion postulacion, Administrador administrador, boolean aprobar, String resolucion) {
        if (postulacion.getEstado() != EstadoAprobacion.PENDIENTE) {
            throw new BL_exeption("Solo se pueden resolver postulaciones pendientes");
        }
        // ck_postulacion_motivo en la BD solo exige motivo al rechazar, no al aprobar.
        if (!aprobar && (resolucion == null || resolucion.isBlank())) {
            throw new BL_exeption("Debe indicar un motivo al rechazar la postulacion");
        }
        if (aprobar) postulacion.aprobar(administrador, resolucion);
        else postulacion.rechazar(administrador, resolucion);
        postulacionDAO.update(postulacion);
    }

    // ---- Tarifa ----

    @Override
    public Tarifa registrarTarifa(Tutor tutor, Curso curso, BigDecimal montoPorHora) {
        BigDecimal monto = validarMonto(montoPorHora);
        // se consulta el DAO en vez de tutor.getPostulaciones(): tutor puede venir
        // de un load() que no trae sus postulaciones cargadas en memoria.
        boolean tienePostulacionAprobada = postulacionDAO.listarPorTutor(tutor).stream()
                .anyMatch(p -> p.getCurso().getIdCurso() == curso.getIdCurso() && p.getEstado() == EstadoAprobacion.APROBADA);
        if (!tienePostulacionAprobada) {
            throw new BL_exeption("El tutor no tiene una postulacion aprobada para este curso");
        }
        boolean yaTieneTarifa = tarifaDAO.listarPorTutor(tutor).stream()
                .anyMatch(t -> t.getCurso().getIdCurso() == curso.getIdCurso());
        if (yaTieneTarifa) {
            throw new BL_exeption("El tutor ya tiene una tarifa registrada para este curso");
        }
        Tarifa tarifa = new Tarifa(curso, monto);
        tutor.agregarTarifa(tarifa);
        return tarifaDAO.save(tarifa);
    }

    @Override
    public Tarifa modificarTarifa(Tarifa tarifa) {
        if (tarifaDAO.load(tarifa.getIdTarifa()) == null) {
            throw new BL_exeption("La tarifa no existe");
        }
        tarifa.setMontoPorHora(validarMonto(tarifa.getMontoPorHora()));
        return tarifaDAO.update(tarifa);
    }

    @Override
    public void eliminarTarifa(Tarifa tarifa) {
        if (tarifaDAO.load(tarifa.getIdTarifa()) == null) {
            throw new BL_exeption("La tarifa no existe");
        }
        tarifaDAO.remove(tarifa);
    }

    @Override
    public Tarifa obtenerTarifaPorId(int idTarifa) {
        return tarifaDAO.load(idTarifa);
    }

    @Override
    public List<Tarifa> listarTarifas() {
        return tarifaDAO.listAll();
    }

    @Override
    public List<Tarifa> listarTarifasPorTutor(Tutor tutor) {
        return tarifaDAO.listarPorTutor(tutor);
    }

    // el monto debe ser positivo y caber en decimal(6,2); se devuelve con 2 decimales
    private BigDecimal validarMonto(BigDecimal montoPorHora) {
        if (montoPorHora == null || montoPorHora.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BL_exeption("El monto por hora debe ser mayor que cero");
        }
        BigDecimal monto = montoPorHora.setScale(2, RoundingMode.HALF_UP);
        if (monto.compareTo(MONTO_MAXIMO) > 0) {
            throw new BL_exeption("El monto por hora no puede superar " + MONTO_MAXIMO);
        }
        return monto;
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

    private void validarDatosCurso(String codigo, String nombre) {
        if (codigo == null || codigo.isBlank() || nombre == null || nombre.isBlank()) {
            throw new BL_exeption("El codigo y el nombre del curso son obligatorios");
        }
    }
}
