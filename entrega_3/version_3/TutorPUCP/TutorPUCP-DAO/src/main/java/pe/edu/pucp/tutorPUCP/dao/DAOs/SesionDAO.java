package pe.edu.pucp.tutorPUCP.dao.DAOs;

import pe.edu.pucp.tutorPUCP.dao.BaseDAO.BaseDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;

import java.util.List;

public interface SesionDAO extends BaseDAO<Sesion, Integer> {
    Sesion load(Integer id);
    Sesion save(Sesion sesion);
    Sesion update(Sesion sesion);
    void remove(Sesion sesion);
    List<Sesion> listAll();

    Sesion save(Sesion sesion, TransactionContext transactionContext);
    Sesion update(Sesion sesion, TransactionContext transactionContext);
    void remove(Sesion sesion, TransactionContext transactionContext);
}
