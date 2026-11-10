package pe.edu.pucp.tutorpucp.tutorpucprest.Implementacion;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorReclamos;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorSesiones;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorUsuarios;
import pe.edu.pucp.tutorPUCP.bl.Exeptions.BL_exeption;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorReclamosImpl;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorSesionesImpl;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorUsuariosImpl;
import pe.edu.pucp.tutorPUCP.modelo.enums.Rol;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Reclamo;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;
import pe.edu.pucp.tutorPUCP.modelo.usuario.PersonaPUCP;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.ReclamoDTO;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.RegistrarReclamoRequest;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.ResolverReclamoRequest;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.SesionReclamableDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// RF015: registrar reclamos asociados a sesiones.
// RF016: el administrador consulta y resuelve los reclamos.
@Path("/reclamo")
public class GestorReclamosRecurso {
    private final GestorReclamos gestorReclamosBL = new GestorReclamosImpl();
    private final GestorSesiones gestorSesionesBL = new GestorSesionesImpl();
    private final GestorUsuarios gestorUsuariosBL = new GestorUsuariosImpl();

    @POST
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Response registrar(RegistrarReclamoRequest datos) {
        if (datos == null) {
            throw new BadRequestException("Faltan los datos del reclamo");
        }
        Sesion sesion = gestorSesionesBL.obtenerSesionPorId(datos.getIdSesion());
        if (sesion == null) {
            return noExiste("No existe la sesion " + datos.getIdSesion());
        }
        Usuario reclamante = gestorUsuariosBL.obtenerUsuarioPorId(datos.getIdUsuario());
        if (reclamante == null) {
            return noExiste("No existe el usuario " + datos.getIdUsuario());
        }
        Rol rol;
        try {
            rol = Rol.valueOf(datos.getRol() == null ? "" : datos.getRol().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return error(new BL_exeption("El rol debe ser TUTOR o ESTUDIANTE"));
        }
        if (participante(sesion, rol) == null) {
            return error(new BL_exeption("La sesion no tiene " + (rol == Rol.TUTOR ? "tutor" : "estudiante") + " registrado"));
        }
        try {
            Reclamo reclamo = gestorReclamosBL.registrarReclamo(sesion, reclamante, rol, datos.getMotivo());
            Reclamo guardado = gestorReclamosBL.obtenerReclamoPorId(reclamo.getIdReclamo());
            return Response.status(Response.Status.CREATED)
                    .entity(new ReclamoDTO(guardado != null ? guardado : reclamo)).build();
        } catch (BL_exeption e) {
            return error(e);
        }
    }

    // Todos los reclamos (pendientes y resueltos), el más reciente primero.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar() {
        return Response.ok(aDTO(gestorReclamosBL.listarReclamos())).build();
    }

    @GET
    @Path("/pendientes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarPendientes() {
        return Response.ok(aDTO(gestorReclamosBL.listarPendientes())).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtener(@PathParam("id") Integer id) {
        Reclamo reclamo = gestorReclamosBL.obtenerReclamoPorId(id);
        if (reclamo == null) {
            return noExiste("No existe el reclamo " + id);
        }
        return Response.ok(new ReclamoDTO(reclamo)).build();
    }

    // Reclamos que registró el usuario.
    @GET
    @Path("/usuario/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarPorUsuario(@PathParam("id") Integer id) {
        Usuario usuario = gestorUsuariosBL.obtenerUsuarioPorId(id);
        if (usuario == null) {
            return noExiste("No existe el usuario " + id);
        }
        return Response.ok(aDTO(gestorReclamosBL.listarPorUsuario(usuario))).build();
    }

    // Sesiones en las que participó el usuario (como tutor o estudiante), para elegir sobre cuál reclamar.
    // No hay SP de sesiones por usuario, así que se filtra la lista completa.
    @GET
    @Path("/usuario/{id}/sesiones")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sesionesDelUsuario(@PathParam("id") Integer id) {
        Usuario usuario = gestorUsuariosBL.obtenerUsuarioPorId(id);
        if (usuario == null) {
            return noExiste("No existe el usuario " + id);
        }
        Set<Integer> reclamadas = gestorReclamosBL.listarPorUsuario(usuario).stream()
                .filter(r -> r.getSesion() != null)
                .map(r -> r.getSesion().getIdSesion())
                .collect(Collectors.toSet());

        List<SesionReclamableDTO> sesiones = new ArrayList<>();
        for (Sesion sesion : gestorSesionesBL.listarSesiones()) {
            boolean esTutor = esUsuario(sesion.getTutor(), id);
            boolean esEstudiante = esUsuario(sesion.getEstudiante(), id);
            if (esTutor || esEstudiante) {
                sesiones.add(new SesionReclamableDTO(sesion, esTutor, reclamadas.contains(sesion.getIdSesion())));
            }
        }
        sesiones.sort(Comparator.comparing(SesionReclamableDTO::getFechaHoraInicio,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return Response.ok(sesiones).build();
    }

    @PUT
    @Path("/{id}/resolver")
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Response resolver(@PathParam("id") Integer id, ResolverReclamoRequest datos) {
        if (datos == null) {
            throw new BadRequestException("Faltan los datos de la resolucion");
        }
        Reclamo reclamo = gestorReclamosBL.obtenerReclamoPorId(id);
        if (reclamo == null) {
            return noExiste("No existe el reclamo " + id);
        }
        Administrador administrador = gestorUsuariosBL.obtenerAdministradorPorId(datos.getIdAdministrador());
        if (administrador == null) {
            return noExiste("No existe el administrador " + datos.getIdAdministrador());
        }
        try {
            gestorReclamosBL.resolverReclamo(reclamo, administrador, datos.isAprobar(), datos.getResolucion());
            Reclamo resuelto = gestorReclamosBL.obtenerReclamoPorId(id);
            return Response.ok(new ReclamoDTO(resuelto != null ? resuelto : reclamo)).build();
        } catch (BL_exeption e) {
            return error(e);
        }
    }

    private static PersonaPUCP participante(Sesion sesion, Rol rol) {
        if (rol == Rol.TUTOR) return sesion.getTutor();
        if (rol == Rol.ESTUDIANTE) return sesion.getEstudiante();
        return sesion.getTutor(); // AMBOS: el BL se encarga de rechazarlo
    }

    private static boolean esUsuario(PersonaPUCP persona, int idUsuario) {
        return persona != null && persona.getUsuario() != null && persona.getUsuario().getIdUsuario() == idUsuario;
    }

    private static List<ReclamoDTO> aDTO(List<Reclamo> reclamos) {
        List<ReclamoDTO> lista = new ArrayList<>();
        for (Reclamo reclamo : reclamos) {
            lista.add(new ReclamoDTO(reclamo));
        }
        lista.sort(Comparator.comparing(ReclamoDTO::getFechaReclamo, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ReclamoDTO::getIdReclamo, Comparator.reverseOrder()));
        return lista;
    }

    private static Response noExiste(String mensaje) {
        return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", mensaje)).build();
    }

    private static Response error(BL_exeption e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
    }
}
