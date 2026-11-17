package pe.edu.pucp.tutorPUCP.dbmanager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

// Entrega las conexiones a la base de datos.
// Abrir una conexion nueva a la base (esta en AWS) cuesta cerca de un segundo, asi que se mantiene un pool:
// las conexiones se reutilizan en vez de abrirse y cerrarse en cada consulta. Quien pide una conexion la sigue
// cerrando con close() (try-with-resources): eso la devuelve al pool, no la destruye.
public class DBManager {
    private static DBManager instance;
    private Properties properties;
    private final HikariDataSource pool;
    private final String DB_CREDENTIALS_FILE = "db.properties";

    // la conexion que el hilo actual tiene en uso en este momento (si tiene alguna)
    private final ThreadLocal<Prestamo> prestamoDelHilo = new ThreadLocal<>();

    private DBManager() {
        properties = new Properties();
        try{
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(DB_CREDENTIALS_FILE);
            properties.load(inputStream);
        }catch(IOException ex){
            System.out.println("Error when loading properties file: " + ex.getMessage());
        }
        String host = properties.getProperty("host");
        String port = properties.getProperty("port");
        String database = properties.getProperty("database");
        // cacheCallableStmts: sin esto el driver consulta los datos de cada procedimiento almacenado en CADA llamada
        // (un viaje de red de mas); con el pool las conexiones viven, asi que ese dato se guarda y se pide una sola vez
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?sslMode=DISABLED&allowPublicKeyRetrieval=true&cacheCallableStmts=true&callableStmtCacheSize=200";

        HikariConfig config = new HikariConfig();
        config.setPoolName("TutorPUCP");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");   // tambien evita el "No suitable driver" dentro de GlassFish
        config.setJdbcUrl(url);
        config.setUsername(properties.getProperty("user"));
        config.setPassword(properties.getProperty("password"));
        config.setMaximumPoolSize(10);             // el servidor atiende 5 peticiones a la vez; la base la comparte todo el equipo
        config.setMinimumIdle(1);                  // en reposo se queda con una sola conexion abierta
        config.setConnectionTimeout(15_000);       // si no hay conexion en 15 s, falla en vez de colgarse
        config.setIdleTimeout(5 * 60_000);         // las conexiones de sobra se cierran a los 5 minutos sin uso
        config.setKeepaliveTime(2 * 60_000);       // cada 2 minutos comprueba que la conexion siga viva
        config.setMaxLifetime(15 * 60_000);        // cada 15 minutos renueva la conexion
        config.setInitializationFailTimeout(-1);   // si al arrancar no hay red, no falla: reintenta al pedir conexion
        this.pool = new HikariDataSource(config);
    }

    public static synchronized DBManager getInstance(){
        if(instance == null)
            instance = new DBManager();
        return instance;
    }

    // Entrega una conexion para consultar.
    // Si este mismo hilo ya tiene una en uso (una consulta llama a otra mientras sigue abierta, como al armar un
    // reclamo con su sesion, su tutor y su estudiante), se le da esa misma en vez de pedir otra al pool: cada
    // peticion usa UNA sola conexion. Sin esto, varias peticiones a la vez se quedaban todas con una conexion
    // esperando otra y el pool se agotaba (errores a los 15 s). close() solo devuelve la conexion al pool cuando
    // se cierra la ultima de las que se pidieron.
    public Connection getConnection() {
        try {
            Prestamo prestamo = prestamoDelHilo.get();
            if (prestamo != null && prestamo.real.isClosed()) {
                prestamoDelHilo.remove();
                prestamo = null;
            }
            if (prestamo == null) {
                prestamo = new Prestamo(pool.getConnection());
                prestamoDelHilo.set(prestamo);
            }
            return prestamo.nuevaReferencia();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Una conexion propia del pool que nunca se comparte con otras consultas del mismo hilo.
    // La usan las transacciones (TransactionContext), porque cambian el autocommit y hacen commit/rollback.
    public Connection getConnectionExclusiva() {
        try {
            return pool.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Cierra el pool (se llama al detener la aplicacion). Si despues se pide otra conexion, se crea un pool nuevo.
    public static synchronized void cerrar() {
        if (instance != null) {
            instance.pool.close();
            instance = null;
        }
    }

    // La conexion real del pool que usa un hilo y cuantas veces se pidio sin cerrarse todavia.
    private final class Prestamo {
        private final Connection real;
        private int abiertas;

        private Prestamo(Connection real) {
            this.real = real;
        }

        private Connection nuevaReferencia() {
            abiertas++;
            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    new Referencia(this));
        }

        // se cerro una de las referencias: si era la ultima, la conexion vuelve al pool
        private void soltar() throws SQLException {
            abiertas--;
            if (abiertas == 0) {
                if (prestamoDelHilo.get() == this) {
                    prestamoDelHilo.remove();
                }
                real.close();
            }
        }
    }

    // Lo que recibe quien pide la conexion: se comporta como la conexion real, salvo close(), que solo cierra
    // esta referencia (la conexion real se devuelve al pool cuando se cierran todas).
    private static final class Referencia implements InvocationHandler {
        private final Prestamo prestamo;
        private boolean cerrada;

        private Referencia(Prestamo prestamo) {
            this.prestamo = prestamo;
        }

        @Override
        public Object invoke(Object proxy, Method metodo, Object[] args) throws Throwable {
            String nombre = metodo.getName();
            if (metodo.getDeclaringClass() == Object.class) {
                switch (nombre) {
                    case "equals":
                        return proxy == args[0];
                    case "hashCode":
                        return System.identityHashCode(proxy);
                    default:
                        return "Conexion compartida de " + prestamo.real;
                }
            }
            if (metodo.getParameterCount() == 0 && nombre.equals("close")) {
                if (!cerrada) {
                    cerrada = true;
                    prestamo.soltar();
                }
                return null;
            }
            if (metodo.getParameterCount() == 0 && nombre.equals("isClosed")) {
                return cerrada || prestamo.real.isClosed();
            }
            if (cerrada) {
                throw new SQLException("La conexion ya esta cerrada");
            }
            try {
                return metodo.invoke(prestamo.real, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }
}
