package Servicio;

import Usuario.Usuario;

import java.util.ArrayList;
import java.util.List;

public class GestorUsuarios {
    private List<Usuario> usuarios = new ArrayList<>();

    public Usuario registrar(String nombreCompleto, String apellidos, int codigo, String facultad, String especialidad,
                             String correoInstitucional, String passwordHash, boolean esTutor, boolean esEstudiante) {
        Usuario usuario = new Usuario(nombreCompleto, apellidos, codigo, facultad, especialidad,
                correoInstitucional, passwordHash, esTutor, esEstudiante);
        usuarios.add(usuario);
        return usuario;
    }

    public Usuario iniciarSesion(String correo, String password) {
        for (Usuario usuario : usuarios) {
            if (usuario.iniciarSesion(correo, password)) {
                return usuario;
            }
        }
        return null;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }
}
