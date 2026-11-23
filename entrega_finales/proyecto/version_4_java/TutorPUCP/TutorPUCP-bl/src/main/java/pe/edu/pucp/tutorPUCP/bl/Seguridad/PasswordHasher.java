package pe.edu.pucp.tutorPUCP.bl.Seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

// La autenticacion vive en la capa de negocio, no en el modelo.
// Usa SHA-256 en hexadecimal: es el mismo resultado que sha2(clave, 256) de MySQL, con el que
// script_tutorPUCP.sql siembra las contrasenas de prueba (por eso los usuarios sembrados pueden iniciar sesion).
public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String hashear(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            //no deberia ocurrir
            throw new IllegalStateException("SHA-256 no esta disponible", e);
        }
    }

    public static boolean coincide(String password, String passwordHash) {
        if (password == null || password.isEmpty() || passwordHash == null) {
            return false;
        }
        // MessageDigest.isEqual compara en tiempo constante
        return MessageDigest.isEqual(
                hashear(password).getBytes(StandardCharsets.UTF_8),
                passwordHash.getBytes(StandardCharsets.UTF_8));
    }
}
