import Academico.Curso;
import Academico.Postulacion;
import Academico.Tema;
import Servicio.GestorAcademico;
import Servicio.GestorMensajes;
import Servicio.GestorReclamos;
import Servicio.GestorSesiones;
import Servicio.GestorUsuarios;
import Tutoria.Mensaje;
import Tutoria.Reclamo;
import Tutoria.Sesion;
import Tutoria.Tarifa;
import Usuario.Administrador;
import Usuario.Usuario;
import enums.Rol;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        GestorUsuarios gestorUsuarios = new GestorUsuarios();
        GestorAcademico gestorAcademico = new GestorAcademico();
        GestorSesiones gestorSesiones = new GestorSesiones();
        GestorReclamos gestorReclamos = new GestorReclamos();
        GestorMensajes gestorMensajes = new GestorMensajes();

        // ------------------------------------------------------------
        System.out.println("=== PRUEBA DE USUARIOS ===");
        Usuario ana = gestorUsuarios.registrar("Ana Lucia", "Torres Rojas", 20201234, "Ciencias e Ingenieria",
                "Ingenieria Informatica", "ana.torres@pucp.edu.pe", "clave123", true, false);
        Usuario luis = gestorUsuarios.registrar("Luis Alberto", "Quispe Mendoza", 20225678, "Estudios Generales Ciencias",
                "Ingenieria Industrial", "luis.quispe@pucp.edu.pe", "clave456", false, true);
        Usuario carla = gestorUsuarios.registrar("Carla", "Vega Salas", 20219012, "Ciencias e Ingenieria",
                "Ingenieria Mecatronica", "carla.vega@pucp.edu.pe", "clave789", true, true);

        System.out.println("Usuarios registrados: " + gestorUsuarios.getUsuarios().size());
        mostrarUsuario(ana);
        mostrarUsuario(luis);
        mostrarUsuario(carla);

        System.out.println("Intentando registrar un usuario sin ningun rol...");
        try {
            gestorUsuarios.registrar("Pedro", "Sin Rol", 20230000, "Letras", "Filosofia",
                    "pedro@pucp.edu.pe", "clave000", false, false);
            System.out.println("Error: se registro un usuario sin rol");
        } catch (IllegalArgumentException e) {
            System.out.println("Registro rechazado: " + e.getMessage());
        }
        System.out.println();

        // ------------------------------------------------------------
        System.out.println("=== PRUEBA DE INICIO DE SESIÓN ===");
        Usuario logueado = gestorUsuarios.iniciarSesion("luis.quispe@pucp.edu.pe", "clave456");
        if (logueado != null) {
            System.out.println("Login con clave correcta: correcto (" + logueado.getNombres() + ")");
        } else {
            System.out.println("Login con clave correcta: fallo");
        }
        Usuario noLogueado = gestorUsuarios.iniciarSesion("luis.quispe@pucp.edu.pe", "otraClave");
        if (noLogueado == null) {
            System.out.println("Login con clave incorrecta: rechazado");
        } else {
            System.out.println("Login con clave incorrecta: fallo (no debio ingresar)");
        }
        System.out.println();

        // ------------------------------------------------------------
        System.out.println("=== PRUEBA DE ADMINISTRADOR ===");
        Administrador admin = new Administrador("Admin TutorPUCP", "admin@pucp.edu.pe", "admin123");
        System.out.println("Administrador creado: " + admin.getNombre() + " (id " + admin.getIdAdministrador() + ")");
        System.out.println("Login administrador: " + admin.iniciarSesion("admin@pucp.edu.pe", "admin123"));
        System.out.println();

        // ------------------------------------------------------------
        System.out.println("=== PRUEBA DE CURSOS Y TEMAS ===");
        Curso curso = gestorAcademico.registrarCurso("Programacion 3", "INF282");
        Tema tema1 = new Tema("Herencia y polimorfismo");
        Tema tema2 = new Tema("Interfaces");
        curso.getTemas().add(tema1);
        curso.getTemas().add(tema2);

        System.out.println("Curso registrado: " + curso.getCodigo() + " - " + curso.getNombre()
                + " (activo: " + curso.isActivo() + ")");
        System.out.println("Temas del curso:");
        for (Tema tema : curso.getTemas()) {
            System.out.println(" - " + tema.getNombre());
        }
        System.out.println("Cursos en el gestor: " + gestorAcademico.getCursos().size());
        System.out.println();

        // ------------------------------------------------------------
        System.out.println("=== PRUEBA DE POSTULACIÓN ===");
        Postulacion postulacion = new Postulacion(ana.getPerfilTutor(), curso, "certificados/ana_inf282.pdf");
        ana.getPerfilTutor().getPostulaciones().add(postulacion);
        System.out.println("Tutor: " + postulacion.getTutor().getUsuario().getNombres()
                + " postula a " + postulacion.getCurso().getNombre());
        System.out.println("Estado inicial: " + postulacion.getEstado());
        postulacion.aprobar();
        System.out.println("Estado despues de aprobar: " + postulacion.getEstado());
        System.out.println("Fecha de resolucion: " + postulacion.getFechaResolucion());

        Postulacion postulacion2 = new Postulacion(carla.getPerfilTutor(), curso, "certificados/carla_inf282.pdf");
        carla.getPerfilTutor().getPostulaciones().add(postulacion2);
        postulacion2.rechazar("Certificado ilegible");
        System.out.println("Segunda postulacion (Carla): " + postulacion2.getEstado()
                + " - motivo: " + postulacion2.getResolucion());
        System.out.println();

        // ------------------------------------------------------------
        System.out.println("=== PRUEBA DE TARIFA ===");
        Tarifa tarifa = new Tarifa(curso, 35.0);
        ana.getPerfilTutor().getTarifas().add(tarifa);
        System.out.println("Tarifa registrada para " + tarifa.getCurso().getNombre()
                + ": S/ " + tarifa.getMontoPorHora() + " por hora");
        System.out.println("Tarifas del tutor Ana: " + ana.getPerfilTutor().getTarifas().size());
        System.out.println();

        // ------------------------------------------------------------
        System.out.println("=== PRUEBA DE SESIÓN ===");
        List<Tema> temasSesion = new ArrayList<>();
        temasSesion.add(tema1);
        temasSesion.add(tema2);
        Sesion sesion = gestorSesiones.registrarSesion(ana.getPerfilTutor(), luis.getPerfilEstudiante(),
                curso, temasSesion, LocalDateTime.now().plusDays(1));

        System.out.println("Sesion creada: id " + sesion.getIdSesion() + ", curso " + sesion.getCurso().getNombre());
        System.out.println("Tutor: " + sesion.getTutor().getUsuario().getNombres()
                + " | Estudiante: " + sesion.getEstudiante().getUsuario().getNombres());
        System.out.println("Temas de la sesion: " + sesion.getTemas().size());
        System.out.println("Estado inicial: " + sesion.getEstado());

        System.out.println("Intentando calificar antes de terminar la sesion...");
        try {
            gestorSesiones.calificarSesion(sesion, 5, "Muy buena");
            System.out.println("Error: se califico una sesion no realizada");
        } catch (IllegalStateException e) {
            System.out.println("Calificacion rechazada: " + e.getMessage());
        }

        gestorSesiones.confirmarCheckIn(sesion, ana);
        System.out.println("Check-in Tutor: " + (sesion.isCheckInTutor() ? "correcto" : "fallo"));
        System.out.println("Estado despues del check-in del tutor: " + sesion.getEstado());

        gestorSesiones.confirmarCheckIn(sesion, luis);
        System.out.println("Check-in Estudiante: " + (sesion.isCheckInEstudiante() ? "correcto" : "fallo"));
        System.out.println("Estado actual: " + sesion.getEstado());

        gestorSesiones.confirmarCheckOut(sesion, ana);
        System.out.println("Check-out Tutor: " + (sesion.isCheckOutTutor() ? "correcto" : "fallo"));
        gestorSesiones.confirmarCheckOut(sesion, luis);
        System.out.println("Check-out Estudiante: " + (sesion.isCheckOutEstudiante() ? "correcto" : "fallo"));
        System.out.println("Estado final: " + sesion.getEstado());
        System.out.println("Fecha y hora de fin registrada: " + (sesion.getFechaHoraFin() != null));

        System.out.println("Sesiones del tutor Ana: " + ana.getPerfilTutor().getSesiones().size());
        System.out.println("Sesiones del estudiante Luis: " + luis.getPerfilEstudiante().getSesiones().size());

        System.out.println("Sesion programada hace 30 minutos sin check-in...");
        Sesion sesionPasada = gestorSesiones.registrarSesion(ana.getPerfilTutor(), luis.getPerfilEstudiante(),
                curso, temasSesion, LocalDateTime.now().minusMinutes(30));
        gestorSesiones.verificarInasistencia(sesionPasada, LocalDateTime.now());
        System.out.println("Estado final: " + sesionPasada.getEstado());
        System.out.println("Motivo: " + sesionPasada.getMotivoCancelacion());
        System.out.println();

        // ------------------------------------------------------------
        System.out.println("=== PRUEBA DE CALIFICACIÓN ===");
        gestorSesiones.calificarSesion(sesion, 5, "Explico muy claro los temas");
        System.out.println("Puntaje: " + sesion.getPuntaje());
        System.out.println("Comentario: " + sesion.getComentario());
        System.out.println();

        // ------------------------------------------------------------
        System.out.println("=== PRUEBA DE CANCELACIÓN DE SESIÓN ===");
        Sesion sesion2 = gestorSesiones.registrarSesion(ana.getPerfilTutor(), luis.getPerfilEstudiante(),
                curso, temasSesion, LocalDateTime.now().plusDays(2));
        System.out.println("Estado inicial: " + sesion2.getEstado());
        gestorSesiones.cancelarSesion(sesion2, "El estudiante tuvo un cruce de horario");
        System.out.println("Estado despues de cancelar: " + sesion2.getEstado());
        System.out.println("Motivo: " + sesion2.getMotivoCancelacion());
        System.out.println("Sesiones registradas en el gestor: " + gestorSesiones.getSesiones().size());
        System.out.println();

        // ------------------------------------------------------------
        System.out.println("=== PRUEBA DE RECLAMO ===");
        Reclamo reclamo = gestorReclamos.registrarReclamo(sesion, luis, Rol.ESTUDIANTE, "El tutor llego 10 minutos tarde");
        System.out.println("Reclamo registrado por " + reclamo.getReclamante().getNombres()
                + ": " + reclamo.getMotivo());
        System.out.println("Rol del reclamante: " + reclamo.getRolReclamante());
        System.out.println("Estado inicial: " + reclamo.getEstado());
        System.out.println("Reclamos pendientes: " + gestorReclamos.listarPendientes().size());
        System.out.println("Reclamos asociados a la sesion: " + sesion.getReclamos().size());

        gestorReclamos.resolverReclamo(reclamo, false, "Se verifico que la sesion se dicto completa");
        System.out.println("Estado despues de resolver: " + reclamo.getEstado());
        System.out.println("Resolucion: " + reclamo.getResolucion());
        System.out.println("Reclamos pendientes: " + gestorReclamos.listarPendientes().size());
        System.out.println();

        // ------------------------------------------------------------
        System.out.println("=== PRUEBA DE MENSAJES ===");
        gestorMensajes.enviarMensaje(luis, ana, "Hola, tengo dudas sobre interfaces", LocalDateTime.now(), false);
        gestorMensajes.enviarMensaje(ana, luis, "Claro, lo vemos en la sesion", LocalDateTime.now(), false);
        gestorMensajes.enviarMensaje(carla, ana, "Hola Ana, ¿compartes material?", LocalDateTime.now(), false);

        List<Mensaje> conversacion = gestorMensajes.obtenerConversacion(luis, ana);
        System.out.println("Mensajes entre Luis y Ana: " + conversacion.size());
        for (Mensaje mensaje : conversacion) {
            System.out.println(" " + mensaje.getEmisor().getNombres() + " -> "
                    + mensaje.getReceptor().getNombres() + ": " + mensaje.getContenido());
        }
        System.out.println();

        System.out.println("=== PRUEBAS FINALIZADAS ===");
    }

    private static void mostrarUsuario(Usuario usuario) {
        System.out.println(usuario.getNombres() + " " + usuario.getApellidos()
                + " | codigo " + usuario.getCodigo() + " | roles " + usuario.getRoles());
        System.out.println("  Puede actuar como Tutor: " + (usuario.getPerfilTutor() != null));
        System.out.println("  Puede actuar como Estudiante: " + (usuario.getPerfilEstudiante() != null));
    }
}
