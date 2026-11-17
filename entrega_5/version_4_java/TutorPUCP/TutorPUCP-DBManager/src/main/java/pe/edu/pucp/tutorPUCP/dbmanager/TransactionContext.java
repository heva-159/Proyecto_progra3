package pe.edu.pucp.tutorPUCP.dbmanager;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionContext implements AutoCloseable{
    private final Connection connection;
    private boolean completado;

    public TransactionContext() throws SQLException{
        // conexion propia (no compartida con otras consultas del hilo): aqui se cambia el autocommit
        this.connection = DBManager.getInstance().getConnectionExclusiva();
        this.connection.setAutoCommit(false);
        this.completado = false;
    }

    public Connection getConnection(){
        return connection;
    }

    public void commit() throws SQLException{
        connection.commit();
        completado = true;
    }

    public void rollback() throws SQLException{
        connection.rollback();
        completado = true;
    }

    @Override
    public void close() throws SQLException{
        if (!completado){
            connection.rollback();
        }
        connection.close();
    }
}
