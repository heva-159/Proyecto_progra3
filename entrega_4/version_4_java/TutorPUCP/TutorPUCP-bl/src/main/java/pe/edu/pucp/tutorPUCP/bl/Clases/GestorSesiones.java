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
    Sesion registrarSesion(Tutor tutor, Estudiante estudiante, Curso curso,
                            List<Tema> temas, LocalDateTime fechaHoraInicio);

    void confirmarCheckIn(Sesion sesion, Rol rol);
    void confirmarCheckOut(Sesion sesion, Rol rol);
    void calificarSesion(Sesion sesion, int puntaje, String comentario);
    void cancelarSesion(Sesion sesion, Usuario quienCancela, String motivo);
    void verificarInasistencia(Sesion sesion, LocalDateTime ahora);

    List<Sesion> listarSesiones();
}
