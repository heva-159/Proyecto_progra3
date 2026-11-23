package pe.edu.pucp.tutorPUCP.dao.DAOs;

import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.academico.Tema;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;

import java.util.List;

// Tabla asociativa sesion_tema (relacion N:M entre Sesion y Tema). No tiene clase de dominio
// propia, por eso no extiende BaseDAO: la capa de negocio la usa junto con SesionDAO.
public interface SesionTemaDAO {
    void save(Sesion sesion, Tema tema, TransactionContext transactionContext);
    void eliminarPorSesion(Sesion sesion, TransactionContext transactionContext);
    List<Tema> listarPorSesion(Sesion sesion);
}
