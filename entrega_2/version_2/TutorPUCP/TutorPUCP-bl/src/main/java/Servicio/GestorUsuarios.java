package Servicio;

import Usuario.Usuario;
import enums.Rol;

import java.util.ArrayList;
import java.util.List;

public class GestorUsuarios {
    private List<Usuario> usuarios = new ArrayList<>();

    public Usuario registrar(String nombres, String apellidos, int codigo, String facultad, String especialidad,
                             String correoInstitucional, String passwordHash, Rol rol) {
        Usuario usuario = new Usuario(nombres, apellidos, codigo, facultad, especialidad,
                correoInstitucional, passwordHash, rol);
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
