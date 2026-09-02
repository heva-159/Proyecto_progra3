import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GestorUsuarios {
    private List<Usuario> usuarios = new ArrayList<>();
    private ServicioCorreo servicioCorreo;

    public GestorUsuarios(ServicioCorreo servicioCorreo) {
        this.servicioCorreo = servicioCorreo;
    }

    public Usuario registrar(String nombreCompleto, String apellidos, String correoInstitucional,
                              String passwordHash, boolean esTutor, boolean esEstudiante) {
        Usuario usuario = new Usuario(nombreCompleto, apellidos, correoInstitucional, passwordHash, esTutor, esEstudiante);
        usuarios.add(usuario);
        enviarCodigoVerificacion(usuario);
        return usuario;
    }

    public void enviarCodigoVerificacion(Usuario usuario) {
        String codigo = String.format("%06d", new Random().nextInt(1_000_000));
        usuario.setCodigoVerificacion(codigo);
        usuario.setCodigoExpiracion(LocalDateTime.now().plusMinutes(10));
        servicioCorreo.enviar(usuario.getCorreoInstitucional(), "Verifica tu cuenta",
                "Tu código de verificación es: " + codigo + " (vence en 10 minutos)");
    }

    public boolean verificarCuenta(Usuario usuario, String codigo) {
        if (usuario.getCodigoVerificacion() == null || usuario.getCodigoExpiracion() == null) return false;
        if (LocalDateTime.now().isAfter(usuario.getCodigoExpiracion())) return false;
        if (!usuario.getCodigoVerificacion().equals(codigo)) return false;
        usuario.setVerificado(true);
        return true;
    }

    public Usuario iniciarSesion(String correo, String password) {
        for (Usuario usuario : usuarios) {
            if (usuario.iniciarSesion(correo, password)) {
                return usuario;
            }
        }
        return null;
    }

    public List<Tutor> buscarTutoresPorCurso(Curso curso) {
        List<Tutor> resultado = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            for (PersonaPUCP perfil : usuario.getPerfiles()) {
                if (perfil instanceof Tutor) {
                    Tutor tutor = (Tutor) perfil;
                    for (Postulacion postulacion : tutor.getPostulaciones()) {
                        if (postulacion.getCurso() == curso && postulacion.getEstado() == EstadoAprobacion.APROBADA) {
                            resultado.add(tutor);
                            break;
                        }
                    }
                }
            }
        }
        return resultado;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }
}
