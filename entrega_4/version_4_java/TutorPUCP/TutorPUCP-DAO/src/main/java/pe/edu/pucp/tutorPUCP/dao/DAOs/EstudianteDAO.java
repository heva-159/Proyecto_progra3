package pe.edu.pucp.tutorPUCP.dao.DAOs;

import pe.edu.pucp.tutorPUCP.dao.BaseDAO.BaseDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Estudiante;

import java.util.List;

public interface EstudianteDAO extends BaseDAO<Estudiante, Integer> {
    Estudiante load(Integer id);
    Estudiante save(Estudiante estudiante);
    Estudiante update(Estudiante estudiante);
    void remove(Estudiante estudiante);
    List<Estudiante> listAll();

    Estudiante save(Estudiante estudiante, TransactionContext transactionContext);
    Estudiante update(Estudiante estudiante, TransactionContext transactionContext);
    void remove(Estudiante estudiante, TransactionContext transactionContext);
}
