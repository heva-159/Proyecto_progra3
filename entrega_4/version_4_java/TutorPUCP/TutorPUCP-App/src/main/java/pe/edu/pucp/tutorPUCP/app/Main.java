package pe.edu.pucp.tutorPUCP.app;

import pe.edu.pucp.tutorPUCP.bl.Clases.GestorAcademico;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorMensajes;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorReclamos;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorSesiones;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorUsuarios;
import pe.edu.pucp.tutorPUCP.bl.Exeptions.BL_exeption;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorAcademicoImpl;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorMensajesImpl;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorReclamosImpl;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorSesionesImpl;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorUsuariosImpl;
import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.academico.Postulacion;
import pe.edu.pucp.tutorPUCP.modelo.academico.Tema;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoCuenta;
import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Mensaje;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Reclamo;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Tarifa;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Estudiante;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Main de prueba (tester) del modulo TutorPUCP-app.
 *
 */

public class Main {

    // IDs ya sembrados en la BD: se reusan en vez de registrar usuarios nuevos.
    private static final int ID_USUARIO_TUTOR = 1;
    private static final int ID_USUARIO_ESTUDIANTE = 3;
    private static final int ID_ADMINISTRADOR = 1;

    private static final GestorUsuarios gestorUsuarios = new GestorUsuariosImpl();
    private static final GestorAcademico gestorAcademico = new GestorAcademicoImpl();
    private static final GestorSesiones gestorSesiones = new GestorSesionesImpl();
    private static final GestorReclamos gestorReclamos = new GestorReclamosImpl();
    private static final GestorMensajes gestorMensajes = new GestorMensajesImpl();

    private static int verificacionesOk = 0;
    private static int verificacionesFallidas = 0;

    public static void main(String[] args) {
        try {
            // para que cuando se ejecute varias veces no falle y registre datos nuevos
            String sufijo = String.valueOf(System.currentTimeMillis() % 100000);

            encabezado("1. Datos ya sembrados en la BD (se leen con BL)");
            Usuario usuarioTutor = gestorUsuarios.obtenerUsuarioPorId(ID_USUARIO_TUTOR);
            Usuario usuarioEstudiante = gestorUsuarios.obtenerUsuarioPorId(ID_USUARIO_ESTUDIANTE);
            Administrador administrador = gestorUsuarios.obtenerAdministradorPorId(ID_ADMINISTRADOR);

            if (usuarioTutor == null || usuarioEstudiante == null || administrador == null) {
                System.out.println("No se encontraron los datos sembrados esperados (usuario tutor id="
                        + ID_USUARIO_TUTOR + ", usuario estudiante id=" + ID_USUARIO_ESTUDIANTE
                        + ", administrador id=" + ID_ADMINISTRADOR + ").");
                System.out.println("Revisa que la BD conectada tenga la data de script_tutorPUCP.sql.");
                return;
            }
            Tutor tutor = usuarioTutor.getPerfilTutor();
            Estudiante estudiante = usuarioEstudiante.getPerfilEstudiante();
            if (tutor == null) {
                System.out.println("El usuario id=" + ID_USUARIO_TUTOR + " no tiene perfil de Tutor.");
                return;
            }
            if (estudiante == null) {
                System.out.println("El usuario id=" + ID_USUARIO_ESTUDIANTE + " no tiene perfil de Estudiante.");
                return;
            }
            System.out.println("Tutor: " + usuarioTutor.getNombres() + " " + usuarioTutor.getApellidos());
            System.out.println("Estudiante: " + usuarioEstudiante.getNombres() + " " + usuarioEstudiante.getApellidos());
            System.out.println("Administrador: " + administrador.getNombre());

            autenticacion(usuarioTutor, administrador);

            // ---- flujo de negocio ----
            encabezado("3. Flujo de negocio con transacciones desde BL");

            // curso + temas nuevos, en una sola transaccion (registrarCursoConTemas)
            String codigoCurso = "PRB-" + sufijo;
            Curso curso = gestorAcademico.registrarCursoConTemas(
                    "Curso de prueba Main", codigoCurso, List.of("Tema de prueba 1", "Tema de prueba 2"));
            List<Tema> temasDelCurso = gestorAcademico.listarTemasPorCurso(curso);
            verificar("Curso con temas: se guardo el curso y sus 2 temas", curso.getIdCurso() > 0 && temasDelCurso.size() == 2);

            // el tutor postula al curso y el administrador aprueba
            Postulacion postulacion = gestorAcademico.postular(tutor, curso, "certificado_tutor.pdf");
            verificar("Postulacion registrada como PENDIENTE", postulacion.getIdPostulacion() > 0);
            gestorAcademico.resolverPostulacion(postulacion, administrador, true, "Certificado valido");
            System.out.println("Postulacion resuelta, estado=" + postulacion.getEstado());

            // tarifa del tutor para el curso (BigDecimal: es dinero)
            Tarifa tarifa = gestorAcademico.registrarTarifa(tutor, curso, new BigDecimal("45.00"));
            System.out.println("Tarifa registrada: S/ " + tarifa.getMontoPorHora() + " por hora");

            // sesion de tutoria: la sesion y sus temas se guardan en una sola transaccion
            // Se programa solo un par de segundos en el futuro (pasa la validacion de
            // "fecha futura") y se espera un momento antes del check-out, porque
            // confirmarCheckOut pone fecha_hora_fin = ahora, y la BD exige
            // (ck_sesion_fechas) que fecha_hora_fin sea posterior a fecha_hora_inicio.
            LocalDateTime fechaSesion = LocalDateTime.now().plusSeconds(5);
            Sesion sesion = gestorSesiones.registrarSesion(tutor, estudiante, curso, temasDelCurso, fechaSesion);
            System.out.println("Sesion registrada, id=" + sesion.getIdSesion() + ", estado=" + sesion.getEstado());
            Sesion sesionEnBD = gestorSesiones.obtenerSesionPorId(sesion.getIdSesion());
            verificar("Sesion con temas: se guardo la sesion y sus 2 temas",
                    sesionEnBD != null && sesionEnBD.getTemas().size() == 2);
            // relacion bidireccional: la sesion se agrego sola a las colecciones de ambos participantes
            verificar("La sesion quedo en las colecciones del tutor y del estudiante",
                    tutor.getSesiones().contains(sesion) && estudiante.getSesiones().contains(sesion));

            gestorSesiones.confirmarCheckIn(sesion, Rol.TUTOR);
            gestorSesiones.confirmarCheckIn(sesion, Rol.ESTUDIANTE);
            System.out.println("Check-in de tutor y estudiante hecho, estado=" + sesion.getEstado());

            Thread.sleep(8000); // asegura que ya paso la fecha_hora_inicio de la sesion

            gestorSesiones.confirmarCheckOut(sesion, Rol.TUTOR);
            gestorSesiones.confirmarCheckOut(sesion, Rol.ESTUDIANTE);
            System.out.println("Check-out de tutor y estudiante hecho, estado=" + sesion.getEstado());

            gestorSesiones.calificarSesion(sesion, 5, "Muy buena sesion de prueba");
            System.out.println("Sesion calificada, puntaje=" + sesion.getPuntaje()
                    + ", calificacion promedio del tutor (en memoria)=" + tutor.getCalificacionPromedio());

            // mensaje del estudiante al tutor
            Mensaje mensaje = gestorMensajes.enviarMensaje(usuarioEstudiante, usuarioTutor,
                    "Hola profe, gracias por la sesion!");
            System.out.println("Mensaje enviado: \"" + mensaje.getContenido() + "\"");

            // reclamo del estudiante sobre la sesion, resuelto por el administrador
            Reclamo reclamo = gestorReclamos.registrarReclamo(sesion, usuarioEstudiante, Rol.ESTUDIANTE,
                    "El tutor llego tarde a la sesion");
            gestorReclamos.resolverReclamo(reclamo, administrador, false,
                    "Se reviso el registro de asistencia y el tutor llego a tiempo");
            System.out.println("Reclamo registrado y resuelto, estado=" + reclamo.getEstado());

            // usuario nuevo con ambos perfiles: usuario + tutor + estudiante en una sola transaccion
            Usuario usuarioNuevo = gestorUsuarios.registrar(
                    "Usuario", "DePrueba" + sufijo, codigoPucp("20"), "Ciencias e Ingenieria", "Ingenieria Informatica",
                    "prueba" + sufijo + "@pucp.edu.pe", "clave123", Rol.AMBOS);
            Usuario usuarioNuevoEnBD = gestorUsuarios.obtenerUsuarioPorId(usuarioNuevo.getIdUsuario());
            verificar("Usuario con perfiles: se guardo el usuario y sus perfiles tutor y estudiante",
                    usuarioNuevoEnBD != null && usuarioNuevoEnBD.getRol() == Rol.AMBOS);
            verificar("El usuario nuevo puede iniciar sesion con la contrasena que eligio",
                    gestorUsuarios.iniciarSesion(usuarioNuevo.getCorreoInstitucional(), "clave123") != null);

            // ---- CRUD completo mediante BL ----
            encabezado("4. CRUD completo mediante BL (insertar, modificar, obtener por id, listar, eliminar)");
            crudCursoYTema(sufijo);
            crudUsuarioYPerfiles(sufijo);
            crudAdministrador(sufijo);
            crudMensaje(usuarioEstudiante, usuarioTutor);
            crudTarifa(tarifa, tutor);

            // ---- consultas del resto de entidades y reglas de negocio ----
            encabezado("5. Consultas de Postulacion, Sesion y Reclamo, y reglas de negocio");
            verificar("Postulacion: obtener por id y listar",
                    gestorAcademico.obtenerPostulacionPorId(postulacion.getIdPostulacion()) != null
                            && !gestorAcademico.listarPostulaciones().isEmpty());
            verificar("Sesion: obtener por id y listar",
                    gestorSesiones.obtenerSesionPorId(sesion.getIdSesion()) != null
                            && !gestorSesiones.listarSesiones().isEmpty());
            verificar("Reclamo: obtener por id y listar",
                    gestorReclamos.obtenerReclamoPorId(reclamo.getIdReclamo()) != null
                            && !gestorReclamos.listarReclamos().isEmpty());

            verificarRechazo("No se puede eliminar una postulacion aprobada",
                    () -> gestorAcademico.eliminarPostulacion(postulacion));
            verificarRechazo("No se puede eliminar una sesion ya realizada",
                    () -> gestorSesiones.eliminarSesion(sesion));
            verificarRechazo("No se puede eliminar un reclamo ya resuelto",
                    () -> gestorReclamos.eliminarReclamo(reclamo));
            verificarRechazo("No se puede registrar un curso con un codigo repetido",
                    () -> gestorAcademico.registrarCurso("Otro curso", codigoCurso));
            verificarRechazo("No se puede registrar un usuario con un correo no institucional",
                    () -> gestorUsuarios.registrar("Pepe", "Gomez", "20250000", "Derecho", "Derecho",
                            "pepe@gmail.com", "clave123", Rol.ESTUDIANTE));

            System.out.println("\n== Resumen: " + verificacionesOk + " comprobaciones OK, "
                    + verificacionesFallidas + " con FALLO ==");
            if (verificacionesFallidas == 0) {
                System.out.println("== Flujo de prueba completo, sin errores ==");
            }

        } catch (BL_exeption e) {
            System.out.println("\n[ERROR DE VALIDACION] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n[ERROR INESPERADO] " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ---- autenticacion ----

    // las contrasenas sembradas estan guardadas como sha2(clave, 256); BL calcula el mismo hash para comparar
    private static void autenticacion(Usuario usuarioTutor, Administrador administrador) {
        encabezado("2. Autenticacion con contrasena cifrada (la hace BL, no el modelo)");
        verificar("Usuario sembrado con la clave correcta ingresa",
                gestorUsuarios.iniciarSesion(usuarioTutor.getCorreoInstitucional(), "clave123") != null);
        verificar("Usuario sembrado con una clave incorrecta es rechazado",
                gestorUsuarios.iniciarSesion(usuarioTutor.getCorreoInstitucional(), "clave-incorrecta") == null);
        verificar("Administrador sembrado con la clave correcta ingresa",
                gestorUsuarios.iniciarSesionAdministrador(administrador.getCorreo(), "admin123") != null);
        verificar("Administrador sembrado con una clave incorrecta es rechazado",
                gestorUsuarios.iniciarSesionAdministrador(administrador.getCorreo(), "clave-incorrecta") == null);
    }

    // ---- CRUD ----

    private static void crudCursoYTema(String sufijo) {
        System.out.println("\n-- Curso --");
        String codigo = "CRUD-" + sufijo;
        Curso curso = gestorAcademico.registrarCurso("Curso CRUD", codigo);
        verificar("Curso insertar: la BD le asigno un id", curso.getIdCurso() > 0);

        curso.setNombre("Curso CRUD (modificado)");
        gestorAcademico.modificarCurso(curso);
        Curso cursoEnBD = gestorAcademico.obtenerCursoPorId(curso.getIdCurso());
        verificar("Curso obtener por id: lo encuentra", cursoEnBD != null && codigo.equals(cursoEnBD.getCodigo()));
        verificar("Curso modificar: el nombre cambio en la BD",
                cursoEnBD != null && "Curso CRUD (modificado)".equals(cursoEnBD.getNombre()));
        verificar("Curso listar todos: incluye el nuevo",
                gestorAcademico.listarCursos().stream().anyMatch(c -> c.getIdCurso() == curso.getIdCurso()));

        System.out.println("\n-- Tema (del curso anterior) --");
        Tema tema = gestorAcademico.registrarTema(curso, "Tema CRUD");
        verificar("Tema insertar: la BD le asigno un id", tema.getIdTema() > 0);

        tema.setNombre("Tema CRUD (modificado)");
        gestorAcademico.modificarTema(tema);
        Tema temaEnBD = gestorAcademico.obtenerTemaPorId(tema.getIdTema());
        verificar("Tema obtener por id: lo encuentra", temaEnBD != null);
        verificar("Tema modificar: el nombre cambio en la BD",
                temaEnBD != null && "Tema CRUD (modificado)".equals(temaEnBD.getNombre()));
        verificar("Tema listar todos: incluye el nuevo",
                gestorAcademico.listarTemas().stream().anyMatch(t -> t.getIdTema() == tema.getIdTema()));

        gestorAcademico.eliminarTema(tema);
        verificar("Tema eliminar: ya no existe en la BD", gestorAcademico.obtenerTemaPorId(tema.getIdTema()) == null);

        // el curso se elimina al final: la eliminacion es logica (queda inactivo)
        gestorAcademico.eliminarCurso(curso);
        Curso cursoEliminado = gestorAcademico.obtenerCursoPorId(curso.getIdCurso());
        verificar("Curso eliminar: queda inactivo (eliminacion logica)",
                cursoEliminado != null && !cursoEliminado.isActivo());
    }

    private static void crudUsuarioYPerfiles(String sufijo) {
        System.out.println("\n-- Usuario --");
        Usuario usuario = gestorUsuarios.registrar("Usuario", "Crud" + sufijo, codigoPucp("21"),
                "Ciencias e Ingenieria", "Ingenieria Informatica",
                "crud" + sufijo + "@pucp.edu.pe", "clave123", Rol.ESTUDIANTE);
        verificar("Usuario insertar: la BD le asigno un id", usuario.getIdUsuario() > 0);

        usuario.setEspecialidad("Ingenieria de Software");
        gestorUsuarios.modificarUsuario(usuario);
        Usuario usuarioEnBD = gestorUsuarios.obtenerUsuarioPorId(usuario.getIdUsuario());
        verificar("Usuario obtener por id: lo encuentra", usuarioEnBD != null);
        verificar("Usuario modificar: la especialidad cambio en la BD",
                usuarioEnBD != null && "Ingenieria de Software".equals(usuarioEnBD.getEspecialidad()));
        verificar("Usuario listar todos: incluye el nuevo",
                gestorUsuarios.listarUsuarios().stream().anyMatch(u -> u.getIdUsuario() == usuario.getIdUsuario()));

        System.out.println("\n-- Perfil Tutor (del usuario anterior, que solo era estudiante) --");
        Tutor tutor = gestorUsuarios.registrarTutor(usuario);
        verificar("Tutor insertar: el usuario ahora tiene perfil de tutor",
                gestorUsuarios.obtenerTutorPorId(usuario.getIdUsuario()) != null);
        gestorUsuarios.modificarTutor(tutor);
        verificar("Tutor modificar: el perfil sigue guardado (no tiene columnas propias)",
                gestorUsuarios.obtenerTutorPorId(usuario.getIdUsuario()) != null);
        verificar("Tutor listar todos: incluye el nuevo",
                gestorUsuarios.listarTutores().stream()
                        .anyMatch(t -> t.getUsuario().getIdUsuario() == usuario.getIdUsuario()));
        gestorUsuarios.eliminarTutor(tutor);
        verificar("Tutor eliminar: el usuario ya no tiene perfil de tutor",
                gestorUsuarios.obtenerTutorPorId(usuario.getIdUsuario()) == null);

        System.out.println("\n-- Perfil Estudiante (de otro usuario, que solo es tutor) --");
        Usuario usuarioTutor = gestorUsuarios.registrar("Usuario", "Tutor" + sufijo, codigoPucp("22"),
                "Letras y Ciencias Humanas", "Historia",
                "tutor" + sufijo + "@pucp.edu.pe", "clave123", Rol.TUTOR);
        Estudiante estudiante = gestorUsuarios.registrarEstudiante(usuarioTutor);
        verificar("Estudiante insertar: el usuario ahora tiene perfil de estudiante",
                gestorUsuarios.obtenerEstudiantePorId(usuarioTutor.getIdUsuario()) != null);
        gestorUsuarios.modificarEstudiante(estudiante);
        verificar("Estudiante modificar: el perfil sigue guardado (no tiene columnas propias)",
                gestorUsuarios.obtenerEstudiantePorId(usuarioTutor.getIdUsuario()) != null);
        verificar("Estudiante listar todos: incluye el nuevo",
                gestorUsuarios.listarEstudiantes().stream()
                        .anyMatch(e -> e.getUsuario().getIdUsuario() == usuarioTutor.getIdUsuario()));
        gestorUsuarios.eliminarEstudiante(estudiante);
        verificar("Estudiante eliminar: el usuario ya no tiene perfil de estudiante",
                gestorUsuarios.obtenerEstudiantePorId(usuarioTutor.getIdUsuario()) == null);

        // la eliminacion de usuario es logica: la cuenta queda BLOQUEADA
        gestorUsuarios.eliminarUsuario(usuario);
        gestorUsuarios.eliminarUsuario(usuarioTutor);
        Usuario usuarioEliminado = gestorUsuarios.obtenerUsuarioPorId(usuario.getIdUsuario());
        verificar("Usuario eliminar: la cuenta queda BLOQUEADA (eliminacion logica)",
                usuarioEliminado != null && usuarioEliminado.getEstado() == EstadoCuenta.BLOQUEADA);
        verificar("Un usuario bloqueado ya no puede iniciar sesion",
                gestorUsuarios.iniciarSesion(usuario.getCorreoInstitucional(), "clave123") == null);
    }

    private static void crudAdministrador(String sufijo) {
        System.out.println("\n-- Administrador --");
        Administrador administrador = gestorUsuarios.registrarAdministrador(
                "Admin CRUD", "admin.crud" + sufijo + "@pucp.edu.pe", "clave123", "SOPORTE");
        verificar("Administrador insertar: la BD le asigno un id", administrador.getIdAdministrador() > 0);

        administrador.setNivelPermiso("REVISOR");
        gestorUsuarios.modificarAdministrador(administrador);
        Administrador enBD = gestorUsuarios.obtenerAdministradorPorId(administrador.getIdAdministrador());
        verificar("Administrador obtener por id: lo encuentra", enBD != null);
        verificar("Administrador modificar: el nivel de permiso cambio en la BD",
                enBD != null && "REVISOR".equals(enBD.getNivelPermiso()));
        verificar("Administrador listar todos: incluye el nuevo",
                gestorUsuarios.listarAdministradores().stream()
                        .anyMatch(a -> a.getIdAdministrador() == administrador.getIdAdministrador()));
        verificar("El administrador nuevo puede iniciar sesion con la contrasena que eligio",
                gestorUsuarios.iniciarSesionAdministrador(administrador.getCorreo(), "clave123") != null);

        gestorUsuarios.eliminarAdministrador(administrador);
        verificar("Administrador eliminar: ya no existe en la BD",
                gestorUsuarios.obtenerAdministradorPorId(administrador.getIdAdministrador()) == null);
    }

    private static void crudMensaje(Usuario emisor, Usuario receptor) {
        System.out.println("\n-- Mensaje --");
        Mensaje mensaje = gestorMensajes.enviarMensaje(emisor, receptor, "Mensaje de la prueba CRUD");
        verificar("Mensaje insertar: la BD le asigno un id", mensaje.getIdMensaje() > 0);

        mensaje.setContenido("Mensaje de la prueba CRUD (corregido)");
        gestorMensajes.modificarMensaje(mensaje);
        Mensaje enBD = gestorMensajes.obtenerMensajePorId(mensaje.getIdMensaje());
        verificar("Mensaje obtener por id: lo encuentra", enBD != null);
        verificar("Mensaje modificar: el contenido cambio en la BD",
                enBD != null && "Mensaje de la prueba CRUD (corregido)".equals(enBD.getContenido()));
        verificar("Mensaje listar todos: incluye el nuevo",
                gestorMensajes.listarMensajes().stream().anyMatch(m -> m.getIdMensaje() == mensaje.getIdMensaje()));
        verificar("Mensaje: aparece en la conversacion entre ambos usuarios",
                gestorMensajes.obtenerConversacion(emisor, receptor).stream()
                        .anyMatch(m -> m.getIdMensaje() == mensaje.getIdMensaje()));

        gestorMensajes.eliminarMensaje(mensaje);
        verificar("Mensaje eliminar: ya no existe en la BD",
                gestorMensajes.obtenerMensajePorId(mensaje.getIdMensaje()) == null);
    }

    // la tarifa ya se inserto en el flujo de negocio (hace falta una postulacion aprobada)
    private static void crudTarifa(Tarifa tarifa, Tutor tutor) {
        System.out.println("\n-- Tarifa (insertada en el flujo de negocio) --");
        verificar("Tarifa insertar: la BD le asigno un id", tarifa.getIdTarifa() > 0);

        tarifa.setMontoPorHora(new BigDecimal("50.50"));
        gestorAcademico.modificarTarifa(tarifa);
        Tarifa enBD = gestorAcademico.obtenerTarifaPorId(tarifa.getIdTarifa());
        verificar("Tarifa obtener por id: lo encuentra", enBD != null);
        verificar("Tarifa modificar: el monto cambio en la BD (BigDecimal 50.50)",
                enBD != null && new BigDecimal("50.50").compareTo(enBD.getMontoPorHora()) == 0);
        verificar("Tarifa listar todos: incluye la nueva",
                gestorAcademico.listarTarifas().stream().anyMatch(t -> t.getIdTarifa() == tarifa.getIdTarifa()));
        verificar("Tarifa listar por tutor: incluye la nueva",
                gestorAcademico.listarTarifasPorTutor(tutor).stream()
                        .anyMatch(t -> t.getIdTarifa() == tarifa.getIdTarifa()));
        verificarRechazo("No se puede modificar una tarifa con monto negativo", () -> {
            Tarifa invalida = gestorAcademico.obtenerTarifaPorId(tarifa.getIdTarifa());
            invalida.setMontoPorHora(new BigDecimal("-5"));
            gestorAcademico.modificarTarifa(invalida);
        });

        gestorAcademico.eliminarTarifa(tarifa);
        verificar("Tarifa eliminar: ya no existe en la BD",
                gestorAcademico.obtenerTarifaPorId(tarifa.getIdTarifa()) == null);
    }

    // ---- utilitarios del tester ----

    // codigo_pucp es char(8) y exige regexp '^[0-9]{8}$': prefijo de 2 digitos + 6 de la hora actual
    private static String codigoPucp(String prefijo) {
        return prefijo + String.format("%06d", System.currentTimeMillis() % 1000000);
    }

    private static void encabezado(String titulo) {
        System.out.println("\n== " + titulo + " ==");
    }

    private static void verificar(String descripcion, boolean cumple) {
        if (cumple) {
            verificacionesOk++;
            System.out.println("[OK] " + descripcion);
        } else {
            verificacionesFallidas++;
            System.out.println("[FALLO] " + descripcion);
        }
    }

    // la accion debe ser rechazada por BL con un BL_exeption (regla de negocio)
    private static void verificarRechazo(String descripcion, Runnable accion) {
        try {
            accion.run();
            verificar(descripcion + " (BL no lo rechazo)", false);
        } catch (BL_exeption e) {
            verificacionesOk++;
            System.out.println("[OK] " + descripcion + " -> BL responde: " + e.getMessage());
        }
    }
}
