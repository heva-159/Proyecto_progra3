package pe.edu.pucp.tutorpucp.tutorpucprest.Implementacion;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorUsuarios;
import pe.edu.pucp.tutorPUCP.bl.Exeptions.BL_exeption;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorUsuariosImpl;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import jakarta.ws.rs.core.MediaType;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.RegistarUsuarioRequest;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.UsuarioDTO;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Path ("/usuario")
public class GestorUsuariosRecurso{
    private final GestorUsuarios gestorUsuariosBL = new GestorUsuariosImpl();

    @POST
    @Path("/registrar")
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Response registrar(RegistarUsuarioRequest datos) {
        if (datos == null) {
            throw new BadRequestException("Faltan los datos del usuario");
        }
        try {
            Usuario usuario = gestorUsuariosBL.registrar(
                    datos.getNombres(), datos.getApellidos(), datos.getCodigoPucp(),
                    datos.getFacultad(), datos.getEspecialidad(), datos.getCorreoInstitucional(),
                    datos.getPassword(), datos.getRol());
            return Response.status(Response.Status.CREATED).entity(new UsuarioDTO(usuario)).build();
        } catch (BL_exeption e) {
            // las reglas del BL (correo repetido, código inválido...) se responden como 400
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }
}
