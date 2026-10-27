package pe.edu.pucp.tutorPUCP.dao.DAOs;

import pe.edu.pucp.tutorPUCP.dao.BaseDAO.BaseDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Administrador;

import java.util.List;

public interface AdministradorDAO extends BaseDAO<Administrador, Integer> {
    Administrador load(Integer id);
    Administrador save(Administrador administrador);
    Administrador update(Administrador administrador);
    void remove(Administrador administrador);
    List<Administrador> listAll();

    Administrador save(Administrador administrador, TransactionContext transactionContext);
    Administrador update(Administrador administrador, TransactionContext transactionContext);
    void remove(Administrador administrador, TransactionContext transactionContext);

    Administrador buscarPorCorreo(String correo);
}
