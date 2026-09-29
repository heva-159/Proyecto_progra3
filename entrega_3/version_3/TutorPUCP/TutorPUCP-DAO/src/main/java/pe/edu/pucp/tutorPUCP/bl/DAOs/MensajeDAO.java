package pe.edu.pucp.tutorPUCP.bl.DAOs;

import pe.edu.pucp.tutorPUCP.bl.BaseDAO.BaseDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Mensaje;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.util.List;

public interface MensajeDAO extends BaseDAO<Mensaje, Integer> {
    Mensaje load(Integer id);
    Mensaje save(Mensaje mensaje);
    Mensaje update(Mensaje mensaje);
    void remove(Mensaje mensaje);
    List<Mensaje> listAll();

    Mensaje save(Mensaje mensaje, TransactionContext transactionContext);
    Mensaje update(Mensaje mensaje, TransactionContext transactionContext);
    void remove(Mensaje mensaje, TransactionContext transactionContext);

    List<Mensaje> listarEntreUsuarios(Usuario usuario1, Usuario usuario2);
}
