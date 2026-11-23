package pe.edu.pucp.tutorPUCP.dao.DAOs;

import pe.edu.pucp.tutorPUCP.dao.BaseDAO.BaseDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Reclamo;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;

import java.util.List;

public interface ReclamoDAO extends BaseDAO<Reclamo, Integer> {
    Reclamo load(Integer id);
    Reclamo save(Reclamo reclamo);
    Reclamo update(Reclamo reclamo);
    void remove(Reclamo reclamo);
    List<Reclamo> listAll();

    Reclamo save(Reclamo reclamo, TransactionContext transactionContext);
    Reclamo update(Reclamo reclamo, TransactionContext transactionContext);
    void remove(Reclamo reclamo, TransactionContext transactionContext);

    List<Reclamo> listarPendientes();
    List<Reclamo> listarPorSesion(Sesion sesion);
}
