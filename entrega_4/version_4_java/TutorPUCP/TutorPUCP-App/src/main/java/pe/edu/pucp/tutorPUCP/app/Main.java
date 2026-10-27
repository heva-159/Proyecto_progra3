package pe.edu.pucp.tutorPUCP.app;

import pe.edu.pucp.tutorPUCP.bl.Clases.GestorAcademico;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorMensajes;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorReclamos;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorSesiones;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorUsuarios;
import pe.edu.pucp.tutorPUCP.dao.DAOs.AdministradorDAO;
import pe.edu.pucp.tutorPUCP.dao.DAOs.UsuarioDAO;
import pe.edu.pucp.tutorPUCP.bl.Exeptions.BL_exeption;
import pe.edu.pucp.tutorPUCP.dao.Impl.AdministradorDAOImpl;
import pe.edu.pucp.tutorPUCP.dao.Impl.UsuarioDAOImpl;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorAcademicoImpl;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorMensajesImpl;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorReclamosImpl;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorSesionesImpl;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorUsuariosImpl;
import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.academico.Postulacion;
import pe.edu.pucp.tutorPUCP.modelo.academico.Tema;
import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Mensaje;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Reclamo;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Tarifa;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Estudiante;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Main de prueba para el modulo TutorPUCP-app.
 *
 * No registra usuarios/cursos de cero: reutiliza filas que ya existen en la
 * base de datos (las sembradas por script_tutorPUCP.sql), para no llenar la
 * BD de duplicados cada vez que se corre. Solo el curso de esta prueba se
 * crea nuevo en cada corrida (con un codigo unico basado en la hora actual),
 * porque asi se puede probar registrarCursoConTemas sin chocar con datos
 * existentes.
 *
 * Requiere que la BD (configurada en TutorPUCP-DBManager/src/main/resources/db.properties)
 * tenga la data sembrada por script_tutorPUCP.sql, en particular:
 *  - un usuario con perfil de Tutor en el id indicado en ID_USUARIO_TUTOR
 *  - un usuario con perfil de Estudiante en el id indicado en ID_USUARIO_ESTUDIANTE
 *  - un administrador en el id indicado en ID_ADMINISTRADOR
 */
public class Main {

    // IDs ya sembrados en la BD: se reusan en vez de registrar usuarios nuevos.
    private static final int ID_USUARIO_TUTOR = 1;
    private static final int ID_USUARIO_ESTUDIANTE = 3;
    private static final int ID_ADMINISTRADOR = 1;

    public static void main(String[] args) {
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
        AdministradorDAO administradorDAO = new AdministradorDAOImpl();

        GestorUsuarios gestorUsuarios = new GestorUsuariosImpl();
        GestorAcademico gestorAcademico = new GestorAcademicoImpl();
        GestorSesiones gestorSesiones = new GestorSesionesImpl();
        GestorReclamos gestorReclamos = new GestorReclamosImpl();
        GestorMensajes gestorMensajes = new GestorMensajesImpl();

        try {
            System.out.println("== Cargando datos ya sembrados en la BD ==");
            Usuario usuarioTutor = usuarioDAO.load(ID_USUARIO_TUTOR);
            Usuario usuarioEstudiante = usuarioDAO.load(ID_USUARIO_ESTUDIANTE);
            Administrador administrador = administradorDAO.load(ID_ADMINISTRADOR);

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

            // 1) Curso + temas nuevos, en una sola transaccion (registrarCursoConTemas)
            System.out.println("\n== 1. Registrando curso con temas (transaccional) ==");
            String codigoCurso = "PRB-" + (System.currentTimeMillis() % 100000); //para que cuando se ejecute varias veces no falle y registre uno nuevo
            Curso curso = gestorAcademico.registrarCursoConTemas(
                    "Curso de prueba Main",
                    codigoCurso,
                    List.of("Tema de prueba 1", "Tema de prueba 2"));
            List<Tema> temasDelCurso = gestorAcademico.listarTemasPorCurso(curso);
            System.out.println("Curso creado: " + curso.getCodigo() + " (" + curso.getNombre() + "), "
                    + temasDelCurso.size() + " temas");

            // 1b) Gestion del catalogo: modificar curso, registrar/modificar/eliminar un tema (RF006, RF007)
            System.out.println("\n== 1b. Gestion de cursos y temas ==");
            gestorAcademico.modificarCurso(curso, codigoCurso, "Curso de prueba Main (modificado)");
            System.out.println("Curso modificado: " + gestorAcademico.buscarCursoPorCodigo(codigoCurso).getNombre());

            Tema temaExtra = gestorAcademico.registrarTema(curso, "Tema extra");
            gestorAcademico.modificarTema(temaExtra, "Tema extra (modificado)");
            System.out.println("Temas tras registrar y modificar: " + gestorAcademico.listarTemasPorCurso(curso).size());
            gestorAcademico.eliminarTema(temaExtra);
            System.out.println("Temas tras eliminar el tema extra: " + gestorAcademico.listarTemasPorCurso(curso).size());

            // 2) El tutor postula al curso y el administrador aprueba
            System.out.println("\n== 2. Postulacion del tutor al curso ==");
            Postulacion postulacion = gestorAcademico.postular(tutor, curso, "certificado_tutor.pdf");
            System.out.println("Postulacion registrada, estado=" + postulacion.getEstado());

            // RF005: el administrador consulta las pendientes antes de resolver
            List<Postulacion> pendientes = gestorAcademico.listarPostulacionesPendientes();
            System.out.println("Postulaciones pendientes: " + pendientes.size());

            gestorAcademico.resolverPostulacion(postulacion, administrador, true, "Certificado valido");
            System.out.println("Postulacion resuelta, estado=" + postulacion.getEstado());

            // 3) Tarifa del tutor para el curso
            System.out.println("\n== 3. Registrando tarifa ==");
            Tarifa tarifa = gestorAcademico.registrarTarifa(tutor, curso, 45.0);
            System.out.println("Tarifa registrada: S/ " + tarifa.getMontoPorHora() + " por hora");

            // 4) Sesion de tutoria, con check-in/check-out de ambos y calificacion
            System.out.println("\n== 4. Registrando sesion ==");
            // Se programa solo un par de segundos en el futuro (pasa la validacion de
            // "fecha futura") y se espera un momento antes del check-out, porque
            // confirmarCheckOut pone fecha_hora_fin = ahora, y la BD exige
            // (ck_sesion_fechas) que fecha_hora_fin sea posterior a fecha_hora_inicio.
            LocalDateTime fechaSesion = LocalDateTime.now().plusSeconds(5);
            System.out.println("Fecha de inicio programada: " + fechaSesion);
            Sesion sesion = gestorSesiones.registrarSesion(tutor, estudiante, curso, temasDelCurso, fechaSesion);
            System.out.println("Sesion registrada, id=" + sesion.getIdSesion() + ", estado=" + sesion.getEstado());

            gestorSesiones.confirmarCheckIn(sesion, Rol.TUTOR);
            gestorSesiones.confirmarCheckIn(sesion, Rol.ESTUDIANTE);
            System.out.println("Check-in de tutor y estudiante hecho, estado=" + sesion.getEstado());

            Thread.sleep(8000); // asegura que ya paso la fecha_hora_inicio de la sesion
            System.out.println("Hora actual antes del check-out: " + LocalDateTime.now());
            System.out.println("Fecha de inicio de la sesion (en memoria): " + sesion.getFechaHoraInicio());

            gestorSesiones.confirmarCheckOut(sesion, Rol.TUTOR);
            gestorSesiones.confirmarCheckOut(sesion, Rol.ESTUDIANTE);
            System.out.println("Check-out de tutor y estudiante hecho, estado=" + sesion.getEstado());

            gestorSesiones.calificarSesion(sesion, 5, "Muy buena sesion de prueba");
            System.out.println("Sesion calificada, puntaje=" + sesion.getPuntaje());

            // 5) Mensaje del estudiante al tutor
            System.out.println("\n== 5. Enviando mensaje ==");
            Mensaje mensaje = gestorMensajes.enviarMensaje(usuarioEstudiante, usuarioTutor,
                    "Hola profe, gracias por la sesion!");
            System.out.println("Mensaje enviado: \"" + mensaje.getContenido() + "\"");

            // 6) Reclamo del estudiante sobre la sesion, resuelto por el administrador
            System.out.println("\n== 6. Registrando y resolviendo un reclamo ==");
            Reclamo reclamo = gestorReclamos.registrarReclamo(sesion, usuarioEstudiante, Rol.ESTUDIANTE,
                    "El tutor llego tarde a la sesion");
            System.out.println("Reclamo registrado, estado=" + reclamo.getEstado());

            gestorReclamos.resolverReclamo(reclamo, administrador, false,
                    "Se reviso el registro de asistencia y el tutor llego a tiempo");
            System.out.println("Reclamo resuelto, estado=" + reclamo.getEstado());

            // 7) Registro de un usuario y un administrador nuevos (datos unicos, no chocan con la data sembrada)
            System.out.println("\n== 7. Registrando un usuario y un administrador nuevos ==");
            String sufijo = String.valueOf(System.currentTimeMillis() % 100000);
            // codigo_pucp es char(8) y exige regexp '^[0-9]{8}$': exactamente 8 digitos.
            String codigoPucp = "20" + String.format("%06d", System.currentTimeMillis() % 1000000);
            Usuario nuevoUsuario = gestorUsuarios.registrar(
                    "Usuario", "DePrueba" + sufijo, codigoPucp, "FIA", "Ingenieria Informatica",
                    "prueba" + sufijo + "@pucp.edu.pe", "clave123", Rol.ESTUDIANTE);
            System.out.println("Usuario nuevo registrado: " + nuevoUsuario.getCorreoInstitucional());

            Administrador nuevoAdministrador = gestorUsuarios.registrarAdministrador(
                    "Admin De Prueba", "admin.prueba" + sufijo + "@pucp.edu.pe", "clave123", "SOPORTE");
            System.out.println("Administrador nuevo registrado: " + nuevoAdministrador.getCorreo());

            System.out.println("\n== Flujo de prueba completo, sin errores ==");

        } catch (BL_exeption e) {
            System.out.println("\n[ERROR DE VALIDACION] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n[ERROR INESPERADO] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
