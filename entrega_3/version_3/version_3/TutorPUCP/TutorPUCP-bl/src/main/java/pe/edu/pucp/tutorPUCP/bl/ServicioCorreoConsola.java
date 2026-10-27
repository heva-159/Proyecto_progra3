package pe.edu.pucp.tutorPUCP.bl;

import pe.edu.pucp.tutorPUCP.modelo.interfaces.IServicioCorreo;

public class ServicioCorreoConsola implements IServicioCorreo {
    @Override
    public void enviar(String destinatario, String asunto, String contenido) {
        System.out.println("----- Correo -----");
        System.out.println("Para: " + destinatario);
        System.out.println("Asunto: " + asunto);
        System.out.println(contenido);
        System.out.println("------------------");
    }
}
