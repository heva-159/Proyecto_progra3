package pe.edu.pucp.tutorPUCP.dao.DAOs;

import pe.edu.pucp.tutorPUCP.dao.BaseDAO.BaseDAO;
import pe.edu.pucp.tutorPUCP.dbmanager.TransactionContext;
import pe.edu.pucp.tutorPUCP.modelo.usuario.PerfilTutor;
import pe.edu.pucp.tutorPUCP.modelo.usuario.Tutor;

import java.util.List;

public interface TutorDAO extends BaseDAO<Tutor, Integer> {
    Tutor load(Integer id);
    Tutor save(Tutor tutor);
    Tutor update(Tutor tutor);
    void remove(Tutor tutor);
    List<Tutor> listAll();

    // El perfil publico de un tutor (datos, cursos aprobados con su tarifa y calificaciones recibidas), o null si no
    // existe ese tutor. Son cuatro consultas en total, sin armar el Tutor completo con sus postulaciones y sesiones.
    PerfilTutor obtenerPerfil(int idTutor);

    Tutor save(Tutor tutor, TransactionContext transactionContext);
    Tutor update(Tutor tutor, TransactionContext transactionContext);
    void remove(Tutor tutor, TransactionContext transactionContext);
}
