package pe.edu.pucp.tutorPUCP.bl.Clases;

import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;
import pe.edu.pucp.tutorPUCP.modelo.academico.Tema;
import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Estudiante;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.time.LocalDateTime;
import java.util.List;

public interface GestorSesiones {
    // Guarda la sesion y sus temas en una sola transaccion.
    Sesion registrarSesion(Tutor tutor, Estudiante estudiante, Curso curso,
                            List<Tema> temas, LocalDateTime fechaHoraInicio);

    // Solo se puede modificar una sesion programada. Deja la sesion y sus temas en una sola transaccion.
    Sesion modificarSesion(Sesion sesion);

    // No se puede eliminar una sesion en curso o ya realizada, ni una que tenga reclamos.
    void eliminarSesion(Sesion sesion);

    // Devuelve null si no existe.
    Sesion obtenerSesionPorId(int idSesion);

    List<Sesion> listarSesiones();

    void confirmarCheckIn(Sesion sesion, Rol rol);
    void confirmarCheckOut(Sesion sesion, Rol rol);
    void calificarSesion(Sesion sesion, int puntaje, String comentario);
    void cancelarSesion(Sesion sesion, Usuario quienCancela, String motivo);
    void verificarInasistencia(Sesion sesion, LocalDateTime ahora);
}
