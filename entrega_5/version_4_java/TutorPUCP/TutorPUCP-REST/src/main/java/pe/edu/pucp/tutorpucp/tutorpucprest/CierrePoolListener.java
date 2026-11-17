package pe.edu.pucp.tutorpucp.tutorpucprest;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import pe.edu.pucp.tutorPUCP.dbmanager.DBManager;

// Al detener o redesplegar la aplicacion cierra el pool de conexiones, para no dejar conexiones ni hilos colgados.
@WebListener
public class CierrePoolListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        DBManager.cerrar();
    }
}