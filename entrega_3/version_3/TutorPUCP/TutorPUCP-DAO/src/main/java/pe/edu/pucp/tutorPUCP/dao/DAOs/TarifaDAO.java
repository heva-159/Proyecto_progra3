package pe.edu.pucp.tutorPUCP.dao.DAOs;

import pe.edu.pucp.tutorPUCP.dao.BaseDAO.BaseDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Tarifa;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;

import java.util.List;

public interface TarifaDAO extends BaseDAO<Tarifa, Integer> {
    Tarifa load(Integer id);
    Tarifa save(Tarifa tarifa);
    Tarifa update(Tarifa tarifa);
    void remove(Tarifa tarifa);
    List<Tarifa> listAll();

    Tarifa save(Tarifa tarifa, TransactionContext transactionContext);
    Tarifa update(Tarifa tarifa, TransactionContext transactionContext);
    void remove(Tarifa tarifa, TransactionContext transactionContext);

    List<Tarifa> listarPorTutor(Tutor tutor);
}
