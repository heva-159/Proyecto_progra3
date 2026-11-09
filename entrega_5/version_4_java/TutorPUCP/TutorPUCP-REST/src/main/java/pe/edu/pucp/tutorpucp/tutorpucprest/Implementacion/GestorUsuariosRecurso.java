package pe.edu.pucp.tutorpucp.tutorpucprest.Implementacion;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.tutorPUCP.bl.Clases.GestorUsuarios;
import pe.edu.pucp.tutorPUCP.bl.Exeptions.BL_exeption;
import pe.edu.pucp.tutorPUCP.bl.Implementacion.GestorUsuariosImpl;
import pe.edu.pucp.tutorPUCP.modelo.enums.EstadoCuenta;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Estudiante;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import jakarta.ws.rs.core.MediaType;
import pe.edu.pucp.tutorpucp.tutorpucprest.DTO.*;

import java.util.ArrayList;
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

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") Integer id, RegistarUsuarioRequest datos) {
        if (datos == null) {
            throw new BadRequestException("Faltan los datos del usuario");
        }
        // se parte del usuario guardado: así password, estado y verificación no se pierden
        Usuario usuario = gestorUsuariosBL.obtenerUsuarioPorId(id);
        if (usuario == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No existe el usuario " + id)).build();
        }
        usuario.setNombres(datos.getNombres());
        usuario.setApellidos(datos.getApellidos());
        usuario.setCodigoPucp(datos.getCodigoPucp());
        usuario.setFacultad(datos.getFacultad());
        usuario.setEspecialidad(datos.getEspecialidad());
        usuario.setCorreoInstitucional(datos.getCorreoInstitucional());
        try {
            Usuario modificado = gestorUsuariosBL.modificarUsuario(usuario);
            return Response.ok(new UsuarioDTO(modificado)).build();
        } catch (BL_exeption e) {
            // por ejemplo: "Ya existe otro usuario con ese correo"
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminar(@PathParam("id") Integer id) {
        Usuario usuario = gestorUsuariosBL.obtenerUsuarioPorId(id);
        if (usuario == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No existe el usuario " + id)).build();
        }
        try{
            gestorUsuariosBL.eliminarUsuario(usuario);
            return Response.noContent().build();
        }catch(BL_exeption e){
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtener(@PathParam("id") Integer id) {
        Usuario usuario = gestorUsuariosBL.obtenerUsuarioPorId(id);
        if (usuario == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No existe el usuario " + id)).build();
        }
        return Response.ok(new UsuarioDTO(usuario)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar() {
        List<UsuarioDTO> usuarios = new ArrayList<>();
        for (Usuario usuario : gestorUsuariosBL.listarUsuarios()) {
            usuarios.add(new UsuarioDTO(usuario));
        }
        return Response.ok(usuarios).build();
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest datos) {
        if (datos.getCorreo() == null || datos.getPassword() == null) {
            throw new BadRequestException("Faltan los datos del usuario");
        }
        Usuario usuario = gestorUsuariosBL.iniciarSesion(datos.getCorreo(), datos.getPassword());
        if (usuario == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "No existe el usuario " + datos.getCorreo())).build();
        }
        return Response.ok(new UsuarioDTO(usuario)).build();
    }

    @GET
    @Path("/{id}/tutor")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerTutorPorId(@PathParam("id") Integer id) {
        Tutor tutor = gestorUsuariosBL.obtenerTutorPorId(id);
        if (tutor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "El usuario " + id + " no existe o no es tutor")).build();
        }
        return Response.ok(new TutorDTO(tutor)).build();
    }

    @GET
    @Path("/{id}/estudiante")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerEstudiantePorId(@PathParam("id") Integer id) {
        Estudiante estudiante= gestorUsuariosBL.obtenerEstudiantePorId(id);
        if (estudiante == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "El usuario " + id + " no existe o no es estudiante")).build();
        }
        return Response.ok(new EstudianteDTO(estudiante)).build(); //todo
    }

    @POST
    @Path("/{id}/tutor")
    @Produces(MediaType.APPLICATION_JSON)
    public Response resgistarTutor(@PathParam("id") Integer id) {
        Usuario usuario = gestorUsuariosBL.obtenerUsuarioPorId(id);
        if (usuario == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No existe el usuario " + id)).build();
        }
        try {
            Tutor tutor = gestorUsuariosBL.registrarTutor(usuario);
            return Response.status(Response.Status.CREATED).entity(new TutorDTO(tutor)).build();
        } catch (BL_exeption e) {
            // por ejemplo: "El usuario ya tiene perfil de tutor"
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/{id}/estudiante")
    @Produces(MediaType.APPLICATION_JSON)
    public Response resgistrarEstudiante(@PathParam("id") Integer id) {
        Usuario usuario = gestorUsuariosBL.obtenerUsuarioPorId(id);
        if (usuario == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No existe el usuario " + id)).build();
        }
        try {
            Estudiante estudiante = gestorUsuariosBL.registrarEstudiante(usuario);
            return Response.status(Response.Status.CREATED).entity(new EstudianteDTO(estudiante)).build();
        } catch (BL_exeption e) {
            // por ejemplo: "El usuario ya tiene perfil de estudiante"
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{id}/tutor")
    @Produces(MediaType.APPLICATION_JSON)
    public Response modificarTutor(@PathParam("id") Integer id) {
        Tutor tutor = gestorUsuariosBL.obtenerTutorPorId(id);
        if (tutor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "El usuario " + id + " no existe o no es tutor")).build();
        }
        try{
            gestorUsuariosBL.modificarTutor(tutor);
        }catch (BL_exeption e){
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
        return Response.ok(new TutorDTO(tutor)).build();
    }

    @PUT
    @Path("/{id}/estudiante")
    @Produces(MediaType.APPLICATION_JSON)
    public Response modificarEstudiante(@PathParam("id") Integer id) {
        Estudiante estudiante = gestorUsuariosBL.obtenerEstudiantePorId(id);
        if (estudiante == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "El usuario " + id + " no existe o no es estudiante")).build();
        }
        try{
            gestorUsuariosBL.modificarEstudiante(estudiante);
        }catch (BL_exeption e){
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
        return Response.ok(new EstudianteDTO(estudiante)).build();
    }

    @DELETE
    @Path("/{id}/tutor") @Produces(MediaType.APPLICATION_JSON)
    public Response eliminarTutorPorId(@PathParam("id") Integer id) {
        Tutor tutor = gestorUsuariosBL.obtenerTutorPorId(id);
        if (tutor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "El usuario " + id + " no existe o no es tutor")).build();
        }
        try{
            gestorUsuariosBL.eliminarTutor(tutor);
        }catch (BL_exeption e){
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
        return Response.ok(new TutorDTO(tutor)).build();
    }

    @DELETE
    @Path("/{id}/estudiante") @Produces(MediaType.APPLICATION_JSON)
    public Response eliminarEstudiantePorId(@PathParam("id") Integer id) {
        Estudiante estudiante = gestorUsuariosBL.obtenerEstudiantePorId(id);
        if (estudiante == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "El usuario " + id + " no existe o no es estudiante")).build();
        }
        try{
            gestorUsuariosBL.eliminarEstudiante(estudiante);
        }catch (BL_exeption e){
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
        return Response.ok(new EstudianteDTO(estudiante)).build();
    }

    @GET
    @Path("/tutores")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerTutores() {
        List<TutorDTO> tutores = new ArrayList<>();
        for (Tutor tutor : gestorUsuariosBL.listarTutores()) {
            if (tutor.getUsuario().getEstado() == EstadoCuenta.ACTIVA) {
                tutores.add(new TutorDTO(tutor));
            }
        }
        return Response.ok(tutores).build();

    }

    @GET
    @Path("/estudiantes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerEstudiantes() {
        List<EstudianteDTO> estudiantes = new ArrayList<>();
        for (Estudiante estudiante : gestorUsuariosBL.listarEstudiantes()) {
            if (estudiante.getUsuario().getEstado() == EstadoCuenta.ACTIVA) {
                estudiantes.add(new EstudianteDTO(estudiante));
            }
        }
        return Response.ok(estudiantes).build();
    }

    @POST
    @Path("/administradores")
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Response registrarAdministrador(RegistrarAdministradorRequest datos) {
        if (datos == null) {
            throw new BadRequestException("Faltan los datos del administrador");
        }
        try {
            Administrador administrador = gestorUsuariosBL.registrarAdministrador(
                    datos.getNombre(), datos.getCorreo(), datos.getPassword(), datos.getNivelPermiso());
            return Response.status(Response.Status.CREATED).entity(new AdministradorDTO(administrador)).build();
        } catch (BL_exeption e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @GET
    @Path("/administradores")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarAdministradores() {
        List<AdministradorDTO> administradores = new ArrayList<>();
        for (Administrador administrador : gestorUsuariosBL.listarAdministradores()) {
            administradores.add(new AdministradorDTO(administrador));
        }
        return Response.ok(administradores).build();
    }

    @GET
    @Path("/administradores/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerAdministrador(@PathParam("id") Integer id) {
        Administrador administrador = gestorUsuariosBL.obtenerAdministradorPorId(id);
        if (administrador == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No existe el administrador " + id)).build();
        }
        return Response.ok(new AdministradorDTO(administrador)).build();
    }

    @PUT
    @Path("/administradores/{id}")
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Response modificarAdministrador(@PathParam("id") Integer id, RegistrarAdministradorRequest datos) {
        if (datos == null) {
            throw new BadRequestException("Faltan los datos del administrador");
        }
        // se parte del guardado: modificar_administrador también escribe password_hash
        Administrador administrador = gestorUsuariosBL.obtenerAdministradorPorId(id);
        if (administrador == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No existe el administrador " + id)).build();
        }
        administrador.setNombre(datos.getNombre());
        administrador.setCorreo(datos.getCorreo());
        administrador.setNivelPermiso(datos.getNivelPermiso());
        try {
            Administrador modificado = gestorUsuariosBL.modificarAdministrador(administrador);
            return Response.ok(new AdministradorDTO(modificado)).build();
        } catch (BL_exeption e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/administradores/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminarAdministrador(@PathParam("id") Integer id) {
        Administrador administrador = gestorUsuariosBL.obtenerAdministradorPorId(id);
        if (administrador == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No existe el administrador " + id)).build();
        }
        try {
            gestorUsuariosBL.eliminarAdministrador(administrador);
            return Response.noContent().build();
        } catch (BL_exeption e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/administradores/login")
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Response loginAdministrador(LoginRequest datos) {
        if (datos == null) {
            throw new BadRequestException("Faltan el correo y la contraseña");
        }
        Administrador administrador = gestorUsuariosBL.iniciarSesionAdministrador(datos.getCorreo(), datos.getPassword());
        if (administrador == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Correo o contraseña incorrectos")).build();
        }
        return Response.ok(new AdministradorDTO(administrador)).build();
    }
}
