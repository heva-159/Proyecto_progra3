package pe.edu.pucp.tutorPUCP.dao.DAOs;

import pe.edu.pucp.tutorPUCP.dao.BaseDAO.BaseDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.Sesion;
import pe.edu.pucp.tutorPUCP.modelo.tutoria.SesionResumen;

import java.util.List;

public interface SesionDAO extends BaseDAO<Sesion, Integer> {
    Sesion load(Integer id);
    Sesion save(Sesion sesion);
    Sesion update(Sesion sesion);
    void remove(Sesion sesion);
    List<Sesion> listAll();

    // Las sesiones en las que participa el usuario (como tutor o como estudiante), de la mas reciente a la mas
    // antigua. Son dos consultas en total (las sesiones y sus temas) en vez de armar cada Sesion completa una por una.
    List<SesionResumen> listarResumenPorUsuario(int idUsuario);

    Sesion save(Sesion sesion, TransactionContext transactionContext);
    Sesion update(Sesion sesion, TransactionContext transactionContext);
    void remove(Sesion sesion, TransactionContext transactionContext);
}
