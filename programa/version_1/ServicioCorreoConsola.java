public class ServicioCorreoConsola implements ServicioCorreo {
    @Override
    public void enviar(String destinatario, String asunto, String contenido) {
        System.out.println("----- Correo -----");
        System.out.println("Para: " + destinatario);
        System.out.println("Asunto: " + asunto);
        System.out.println(contenido);
        System.out.println("------------------");
    }
}
