package pe.edu.pucp.tutorPUCP.bl;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import pe.edu.pucp.tutorPUCP.modelo.interfaces.IServicioCorreo;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

// Envia los correos por SMTP con Jakarta Mail (GlassFish ya trae la implementacion).
// Los datos de la cuenta estan en correo.properties, igual que db.properties en el DBManager.
// Si la cuenta no esta configurada, el correo se muestra en la consola (ServicioCorreoConsola).
public class ServicioCorreoSmtp implements IServicioCorreo {
    private static final String ARCHIVO = "correo.properties";

    private final Properties datos = new Properties();
    private final IServicioCorreo respaldo = new ServicioCorreoConsola();

    public ServicioCorreoSmtp() {
        try (InputStream entrada = getClass().getClassLoader().getResourceAsStream(ARCHIVO)) {
            if (entrada != null) {
                datos.load(entrada);
            }
        } catch (Exception ex) {
            System.out.println("No se pudo leer " + ARCHIVO + ": " + ex.getMessage());
        }
    }

    private boolean configurado() {
        return !leer("smtp.host").isEmpty() && !leer("smtp.usuario").isEmpty() && !leer("smtp.password").isEmpty();
    }

    @Override
    public void enviar(String destinatario, String asunto, String contenido) {
        if (!configurado()) {
            respaldo.enviar(destinatario, asunto, contenido);
            return;
        }
        String puerto = leer("smtp.puerto").isEmpty() ? "587" : leer("smtp.puerto");
        String remitente = leer("remitente").isEmpty() ? leer("smtp.usuario") : leer("remitente");
        Envio.enviar(leer("smtp.host"), puerto, leer("smtp.usuario"), leer("smtp.password"), remitente,
                destinatario, asunto, contenido);
    }

    private String leer(String clave) {
        return datos.getProperty(clave, "").trim();
    }

    // Lo que usa Jakarta Mail va aparte: esta clase solo se carga cuando hay cuenta configurada,
    // asi el programa de consola (TutorPUCP-App), que no tiene la libreria, sigue funcionando.
    private static final class Envio {
        static void enviar(String host, String puerto, String usuario, String password, String remitente,
                           String destinatario, String asunto, String contenido) {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", puerto);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");

            Session sesion = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(usuario, password);
                }
            });
            try {
                MimeMessage mensaje = new MimeMessage(sesion);
                mensaje.setFrom(new InternetAddress(remitente, "TutorPUCP", "UTF-8"));
                mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
                mensaje.setSubject(asunto, "UTF-8");
                mensaje.setText(contenido, "UTF-8");
                Transport.send(mensaje);
            } catch (MessagingException | UnsupportedEncodingException ex) {
                throw new RuntimeException("No se pudo enviar el correo a " + destinatario + ": " + ex.getMessage(), ex);
            }
        }
    }
}
