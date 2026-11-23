package pe.edu.pucp.tutorpucp.tutorpucprest.Implementacion;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorMensajes;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorUsuarios;
import pe.edu.pucp.tutorPUCP.bl.Exeptions.BL_exeption;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorMensajesImpl;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorUsuariosImpl;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Mensaje;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.ConversacionDTO;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.EnviarMensajeRequest;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.MensajeDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// RF010: mensajería interna entre estudiante y tutor.
@Path("/mensaje")
public class GestorMensajesRecurso {
    private final GestorMensajes gestorMensajesBL = new GestorMensajesImpl();
    private final GestorUsuarios gestorUsuariosBL = new GestorUsuariosImpl();

    @POST
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Response enviar(EnviarMensajeRequest datos) {
        if (datos == null) {
            throw new BadRequestException("Faltan los datos del mensaje");
        }
        Usuario emisor = gestorUsuariosBL.obtenerUsuarioPorId(datos.getIdEmisor());
        Usuario receptor = gestorUsuariosBL.obtenerUsuarioPorId(datos.getIdReceptor());
        if (emisor == null || receptor == null) {
            return noExiste("El emisor o el receptor no existe");
        }
        try {
            Mensaje mensaje = gestorMensajesBL.enviarMensaje(emisor, receptor, datos.getContenido());
            // se vuelve a leer para devolver la fecha que puso la BD
            Mensaje guardado = gestorMensajesBL.obtenerMensajePorId(mensaje.getIdMensaje());
            return Response.status(Response.Status.CREATED)
                    .entity(new MensajeDTO(guardado != null ? guardado : mensaje)).build();
        } catch (BL_exeption e) {
            return error(e);
        }
    }

    // Conversación entre dos usuarios, en orden de envío.
    @GET
    @Path("/conversacion")
    @Produces(MediaType.APPLICATION_JSON)
    public Response conversacion(@QueryParam("usuario1") Integer idUsuario1, @QueryParam("usuario2") Integer idUsuario2) {
        if (idUsuario1 == null || idUsuario2 == null) {
            throw new BadRequestException("Indique usuario1 y usuario2");
        }
        Usuario usuario1 = gestorUsuariosBL.obtenerUsuarioPorId(idUsuario1);
        Usuario usuario2 = gestorUsuariosBL.obtenerUsuarioPorId(idUsuario2);
        if (usuario1 == null || usuario2 == null) {
            return noExiste("Alguno de los usuarios no existe");
        }
        List<MensajeDTO> mensajes = new ArrayList<>();
        for (Mensaje mensaje : gestorMensajesBL.obtenerConversacion(usuario1, usuario2)) {
            mensajes.add(new MensajeDTO(mensaje));
        }
        return Response.ok(mensajes).build();
    }

    // Bandeja: una fila por cada persona con la que el usuario conversó, la más reciente primero.
    @GET
    @Path("/usuario/{id}/bandeja")
    @Produces(MediaType.APPLICATION_JSON)
    public Response bandeja(@PathParam("id") Integer id) {
        Usuario usuario = gestorUsuariosBL.obtenerUsuarioPorId(id);
        if (usuario == null) {
            return noExiste("No existe el usuario " + id);
        }
        List<Mensaje> mensajes = new ArrayList<>(gestorMensajesBL.listarMensajesDeUsuario(usuario));
        mensajes.sort(Comparator.comparing(Mensaje::getFechaEnvio).thenComparing(Mensaje::getIdMensaje));

        Map<Integer, ConversacionDTO> porContacto = new LinkedHashMap<>();
        for (Mensaje mensaje : mensajes) {
            boolean esMio = mensaje.getEmisor().getIdUsuario() == id;
            Usuario contacto = esMio ? mensaje.getReceptor() : mensaje.getEmisor();
            ConversacionDTO fila = porContacto.computeIfAbsent(contacto.getIdUsuario(), k -> {
                ConversacionDTO nueva = new ConversacionDTO();
                nueva.setIdContacto(contacto.getIdUsuario());
                nueva.setNombresContacto(contacto.getNombres());
                nueva.setApellidosContacto(contacto.getApellidos());
                return nueva;
            });
            // como van en orden de envío, el último que pasa es el más reciente
            fila.setUltimoMensaje(mensaje.getContenido());
            fila.setFechaUltimo(mensaje.getFechaEnvio().toString());
            fila.setUltimoEsMio(esMio);
            if (!esMio && !mensaje.isLeido()) {
                fila.setNoLeidos(fila.getNoLeidos() + 1);
            }
        }
        List<ConversacionDTO> bandeja = new ArrayList<>(porContacto.values());
        bandeja.sort(Comparator.comparing(ConversacionDTO::getFechaUltimo).reversed());
        return Response.ok(bandeja).build();
    }

    // Marca un mensaje como leído; solo lo puede hacer su receptor.
    @PUT
    @Path("/{id}/leido")
    @Produces(MediaType.APPLICATION_JSON)
    public Response marcarLeido(@PathParam("id") Integer id, @QueryParam("lector") Integer idLector) {
        if (idLector == null) {
            throw new BadRequestException("Indique el lector");
        }
        Mensaje mensaje = gestorMensajesBL.obtenerMensajePorId(id);
        Usuario lector = gestorUsuariosBL.obtenerUsuarioPorId(idLector);
        if (mensaje == null || lector == null) {
            return noExiste("No existe el mensaje o el lector");
        }
        try {
            gestorMensajesBL.marcarComoLeido(mensaje, lector);
            return Response.noContent().build();
        } catch (BL_exeption e) {
            return error(e);
        }
    }

    // Al abrir una conversación: marca como leídos todos los mensajes que "otro" le envió a "lector".
    @PUT
    @Path("/conversacion/leida")
    @Produces(MediaType.APPLICATION_JSON)
    public Response marcarConversacionLeida(@QueryParam("lector") Integer idLector, @QueryParam("otro") Integer idOtro) {
        if (idLector == null || idOtro == null) {
            throw new BadRequestException("Indique lector y otro");
        }
        Usuario lector = gestorUsuariosBL.obtenerUsuarioPorId(idLector);
        Usuario otro = gestorUsuariosBL.obtenerUsuarioPorId(idOtro);
        if (lector == null || otro == null) {
            return noExiste("Alguno de los usuarios no existe");
        }
        int marcados = gestorMensajesBL.marcarConversacionComoLeida(lector, otro);
        return Response.ok(Map.of("marcados", marcados)).build();
    }

    private static Response noExiste(String mensaje) {
        return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", mensaje)).build();
    }

    private static Response error(BL_exeption e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
    }
}
