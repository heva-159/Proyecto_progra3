package pe.edu.pucp.tutorpucp.tutorpucprest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import pe.edu.pucp.tutorPUCP.dbmanager.CacheLectura;

// En cada GET abre una memoria de corta vida (CacheLectura): si el listado recorre varias filas que apuntan al mismo
// usuario, curso o sesion, se cargan de la base una sola vez. Los GET no escriben, asi que no hay datos viejos que temer.
// En POST, PUT y DELETE no hay memoria: todo funciona como antes.
@Provider
public class MemoriaDeLecturaFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext peticion) {
        if ("GET".equals(peticion.getMethod())) {
            CacheLectura.abrir();
        } else {
            CacheLectura.cerrar();   // por si el hilo quedo con una memoria de una peticion anterior
        }
    }

    @Override
    public void filter(ContainerRequestContext peticion, ContainerResponseContext respuesta) {
        CacheLectura.cerrar();
    }
}