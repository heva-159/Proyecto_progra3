package pe.edu.pucp.tutorPUCP.dbmanager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

// Memoria de corta vida para UNA consulta de lectura (un GET del REST).
// Cuando un listado recorre filas que apuntan al mismo usuario, curso o sesion, cada DAO lo pedia a la base una vez
// por fila (una conexion y un viaje de red cada vez). Con la memoria abierta, la primera vez se carga y las
// siguientes se reutiliza el mismo objeto.
//
// Solo existe entre abrir() y cerrar() y solo en el hilo que la abrio. El filtro del REST la abre unicamente en los
// GET (que no escriben); fuera de eso no hay memoria y los DAO se comportan exactamente como antes.
public final class CacheLectura {
    private static final ThreadLocal<Map<String, Object>> ACTUAL = new ThreadLocal<>();

    private CacheLectura() {
    }

    // empieza una memoria nueva en este hilo (si habia otra, la descarta)
    public static void abrir() {
        ACTUAL.set(new HashMap<>());
    }

    // termina la memoria de este hilo
    public static void cerrar() {
        ACTUAL.remove();
    }

    // Sin memoria abierta solo ejecuta cargar. Con memoria abierta: si (tipo, id) ya se cargo lo devuelve
    // (incluso si no existia y salio null); si no, lo carga y lo guarda.
    @SuppressWarnings("unchecked")
    public static <T> T obtener(String tipo, int id, Supplier<T> cargar) {
        Map<String, Object> memoria = ACTUAL.get();
        if (memoria == null) {
            return cargar.get();
        }
        String clave = tipo + "#" + id;
        if (memoria.containsKey(clave)) {
            return (T) memoria.get(clave);
        }
        T valor = cargar.get();
        memoria.put(clave, valor);
        return valor;
    }
}