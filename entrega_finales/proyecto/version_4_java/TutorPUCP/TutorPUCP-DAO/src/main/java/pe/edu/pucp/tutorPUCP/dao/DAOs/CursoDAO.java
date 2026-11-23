package pe.edu.pucp.tutorPUCP.dao.DAOs;

import pe.edu.pucp.tutorPUCP.dao.BaseDAO.BaseDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;

import java.util.List;

public interface CursoDAO extends BaseDAO<Curso, Integer> {
    Curso load(Integer id);
    Curso save(Curso curso);
    Curso update(Curso curso);
    void remove(Curso curso);
    List<Curso> listAll();

    Curso save(Curso curso, TransactionContext transactionContext);
    Curso update(Curso curso, TransactionContext transactionContext);
    void remove(Curso curso, TransactionContext transactionContext);

    Curso buscarPorCodigo(String codigo);
}
