package pe.edu.pucp.tutorPUCP.bl;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import pe.edu.pucp.tutorPUCP.bl.Exeptions.BL_exeption;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

// Usa un servidor SMTP (gmail). Los datos de la cuenta van en correo.properties
// Si ese archivo no existe, trabaja en "modo consola": no envia nada y escribe el codigo en el log del servidor,
// asi se puede probar el registro sin tener ninguna cuenta de correo configurada.
public class EnviadorCorreo {
    private static final Logger LOG = Logger.getLogger(EnviadorCorreo.class.getName());
    private static final String ARCHIVO_CONFIGURACION = "correo.properties";

    // Los datos de correo.properties; null si no hay archivo (modo consola). Se leen una sola vez al cargar la clase.
    private static final Properties CONFIGURACION = cargarConfiguracion();

    // Envia el codigo al correo del usuario. Si no se pudo enviar, lanza BL_exeption.
    public void enviarCodigoVerificacion(String destino, String nombres, String codigo, int minutosVigencia) {
        if (CONFIGURACION == null || consolaForzada()) {
            LOG.info("[CORREO en modo consola] Para " + destino + " -> codigo de verificacion " + codigo
                    + " (vence en " + minutosVigencia + " minutos)");
            return;
        }
        try {
            MimeMessage mensaje = new MimeMessage(crearSesion());
            mensaje.setFrom(new InternetAddress(remitente(), "TutorPUCP", "UTF-8"));
            mensaje.setRecipient(Message.RecipientType.TO, new InternetAddress(destino));
            mensaje.setSubject("Tu código de verificación de TutorPUCP", "UTF-8");
            mensaje.setText(cuerpo(nombres, codigo, minutosVigencia), "UTF-8");
            Transport.send(mensaje);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new BL_exeption("No se pudo enviar el correo de verificacion: " + e.getMessage());
        }
    }

    // El texto del correo (texto plano, que es lo que menos filtra el spam).
    private static String cuerpo(String nombres, String codigo, int minutosVigencia) {
        return "Hola " + nombres + ",\n\n"
                + "Tu código de verificación de TutorPUCP es:\n\n"
                + "    " + codigo + "\n\n"
                + "Vence en " + minutosVigencia + " minutos. "
                + "Si no creaste una cuenta en TutorPUCP, ignora este mensaje.\n";
    }

    // La conexion con el servidor SMTP: direccion, puerto, cifrado y usuario/clave (segun correo.properties).
    private static Session crearSesion() {
        boolean autenticacion = !"false".equalsIgnoreCase(CONFIGURACION.getProperty("autenticacion", "true").trim());
        boolean starttls = !"false".equalsIgnoreCase(CONFIGURACION.getProperty("starttls", "true").trim());

        Properties smtp = new Properties();
        smtp.put("mail.smtp.host", CONFIGURACION.getProperty("host").trim());
        smtp.put("mail.smtp.port", CONFIGURACION.getProperty("puerto", "587").trim());
        smtp.put("mail.smtp.auth", String.valueOf(autenticacion));
        smtp.put("mail.smtp.starttls.enable", String.valueOf(starttls));
        // con STARTTLS "required" nunca se manda la clave sin cifrar si el servidor no ofrece el cifrado
        smtp.put("mail.smtp.starttls.required", String.valueOf(starttls));
        // si el servidor no responde, la peticion del REST no se queda colgada
        smtp.put("mail.smtp.connectiontimeout", "8000");
        smtp.put("mail.smtp.timeout", "10000");
        smtp.put("mail.smtp.writetimeout", "10000");
        if (starttls) {
            // se comprueba que el certificado sea del servidor al que se conecta
            smtp.put("mail.smtp.ssl.checkserveridentity", "true");
            // Dentro de GlassFish el JVM usa SU lista de certificados (cacerts.p12 del dominio), que no trae la CA
            // de Gmail y falla con "PKIX path building failed": por eso se usa la lista de CA del propio JDK.
            SSLSocketFactory fabricaTls = FabricaTls.CON_CA_DEL_JDK;
            if (fabricaTls != null) {
                smtp.put("mail.smtp.ssl.socketFactory", fabricaTls);
            }
        }

        if (!autenticacion) {
            return Session.getInstance(smtp);
        }
        String usuario = CONFIGURACION.getProperty("usuario", "").trim();
        // Gmail muestra la contrasena de aplicacion en grupos de 4 letras separados por espacios
        String clave = CONFIGURACION.getProperty("clave", "").replace(" ", "").trim();
        if (usuario.isEmpty() || clave.isEmpty()) {
            throw new BL_exeption("Faltan el usuario y la clave de correo en correo.properties");
        }
        return Session.getInstance(smtp, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuario, clave);
            }
        });
    }

    // Se mira en cada envio (y no al cargar la clase) para que Main pueda activarlo con System.setProperty
    // aunque su GestorUsuariosImpl ya se haya creado.
    private static boolean consolaForzada() {
        return "consola".equalsIgnoreCase(System.getProperty("tutorpucp.correo", "").trim());
    }

    // Direccion que aparece como remitente: "remitente" si se puso, y si no, la misma cuenta con la que se inicia sesion.
    private static String remitente() {
        String remitente = CONFIGURACION.getProperty("remitente", "").trim();
        return remitente.isEmpty() ? CONFIGURACION.getProperty("usuario", "").trim() : remitente;
    }

    // Fabrica de conexiones cifradas (TLS) que confia en las CA del JDK (lib/security/cacerts) y no usa el
    // almacen de claves de GlassFish. Vale null si no se pudo armar: entonces se usa la configuracion por defecto.
    private static final class FabricaTls {
        static final SSLSocketFactory CON_CA_DEL_JDK = crear();

        private static SSLSocketFactory crear() {
            Path cacerts = Path.of(System.getProperty("java.home"), "lib", "security", "cacerts");
            try (InputStream entrada = Files.newInputStream(cacerts)) {
                KeyStore almacen = KeyStore.getInstance(KeyStore.getDefaultType());
                almacen.load(entrada, "changeit".toCharArray());
                TrustManagerFactory confianza = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                confianza.init(almacen);
                SSLContext contexto = SSLContext.getInstance("TLS");
                contexto.init(null, confianza.getTrustManagers(), null);
                return contexto.getSocketFactory();
            } catch (IOException | GeneralSecurityException e) {
                LOG.log(Level.WARNING, "No se pudieron leer las CA del JDK (" + cacerts + "): se usa la configuracion del servidor", e);
                return null;
            }
        }
    }

    // Lee correo.properties desde el classpath (igual que DBManager con db.properties).
    private static Properties cargarConfiguracion() {
        try (InputStream entrada = EnviadorCorreo.class.getClassLoader().getResourceAsStream(ARCHIVO_CONFIGURACION)) {
            if (entrada == null) {
                return null;
            }
            String texto = new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
            if (texto.startsWith("﻿")) {
                texto = texto.substring(1);
            }
            Properties propiedades = new Properties();
            propiedades.load(new StringReader(texto));
            // un archivo vacio o sin servidor tampoco permite enviar: se queda en modo consola
            return propiedades.getProperty("host", "").isBlank() ? null : propiedades;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "No se pudo leer " + ARCHIVO_CONFIGURACION + ": se trabaja en modo consola", e);
            return null;
        }
    }
}
