package pe.edu.pucp.tutorPUCP.bl.BaseDAO;

import java.util.List;

public interface BaseDAO <T,ID>{
    T load(ID id);
    T save(T t);
    T update(T t);
    void remove(T t);
    List<T> listAll();
}
