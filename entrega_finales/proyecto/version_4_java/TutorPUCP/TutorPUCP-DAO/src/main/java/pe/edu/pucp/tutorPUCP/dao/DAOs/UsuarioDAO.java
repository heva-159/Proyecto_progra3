package pe.edu.pucp.tutorPUCP.dao.DAOs;

import pe.edu.pucp.tutorPUCP.dao.BaseDAO.BaseDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Usuario;

import java.util.List;

public interface UsuarioDAO extends BaseDAO<Usuario, Integer> {
    Usuario load(Integer id);
    Usuario save(Usuario usuario);
    Usuario update(Usuario usuario);
    void remove(Usuario usuario);
    List<Usuario> listAll();

    Usuario save(Usuario usuario, TransactionContext transactionContext);
    Usuario update(Usuario usuario, TransactionContext transactionContext);
    void remove(Usuario usuario, TransactionContext transactionContext);

    Usuario buscarPorCorreo(String correoInstitucional);
    Usuario buscarPorCodigoPucp(String codigoPucp);
}
