package pe.edu.pucp.tutorPUCP.bl.DAOs;

import pe.edu.pucp.tutorPUCP.bl.BaseDAO.BaseDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.academico.Postulacion;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;

import java.util.List;

public interface PostulacionDAO extends BaseDAO<Postulacion, Integer> {
    Postulacion load(Integer id);
    Postulacion save(Postulacion postulacion);
    Postulacion update(Postulacion postulacion);
    void remove(Postulacion postulacion);
    List<Postulacion> listAll();

    Postulacion save(Postulacion postulacion, TransactionContext transactionContext);
    Postulacion update(Postulacion postulacion, TransactionContext transactionContext);
    void remove(Postulacion postulacion, TransactionContext transactionContext);

    List<Postulacion> listarPorTutor(Tutor tutor);
}
