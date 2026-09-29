package pe.edu.pucp.tutorPUCP.bl.DAOs;

import pe.edu.pucp.tutorPUCP.bl.BaseDAO.BaseDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.academico.Tema;
import pe.edu.pucp.tutorPUCP.modelo.academico.Curso;

import java.util.List;

public interface TemaDAO extends BaseDAO<Tema, Integer> {
    Tema load(Integer id);
    Tema save(Tema tema);
    Tema update(Tema tema);
    void remove(Tema tema);
    List<Tema> listAll();

    Tema save(Tema tema, TransactionContext transactionContext);
    Tema update(Tema tema, TransactionContext transactionContext);
    void remove(Tema tema, TransactionContext transactionContext);

    List<Tema> listarPorCurso(Curso curso);
}
