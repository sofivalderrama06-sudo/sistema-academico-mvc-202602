package com.uniajc.soporte;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.uniajc.db.DatabaseConexion;

/**
 * DOBLES DE PRUEBA: BdFalsa (Simulador de Conexión JDBC)
 * ¿Por qué existe esta clase?
 * 1. Simulación de Base de Datos: Intercepta las llamadas de JDBC (Connection,
 *    PreparedStatement y ResultSet) usando Proxies dinámicos de Java.
 * 2. Prueba de Controladores: Como el Controlador instancia su propio DAO y este
 *    obtiene la conexión de DatabaseConexion, BdFalsa inyecta una conexión falsa
 *    directamente en el campo estático de DatabaseConexion.
 * 3. Inspección de SQLs: Registra en 'sqls' las consultas ejecutadas y en
 *    'parametros' los valores asignados a cada signo de interrogación (?).
 * 4. Control de Fallos: Permite simular caídas de base de datos activando la
 *    bandera 'fallarAlPreparar = true' para probar el manejo de excepciones.
 */
public class BdFalsa {
    public final List<String> sqls = new ArrayList<>();
    public int actualizacionesEjecutadas;
    public boolean fallarAlPreparar;

    private final List<Map<String, Object>> filas = new ArrayList<>();
    private List<Object> parametros = new ArrayList<>();

    public BdFalsa instalar() {
        Connection conexion = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, metodo, argumentos) -> {
                    if ("prepareStatement".equals(metodo.getName()) || "prepareCall".equals(metodo.getName())) {
                        if (fallarAlPreparar) {
                            throw new SQLException("conexion de prueba");
                        }
                        return crearPreparedStatement((String) argumentos[0]);
                    }
                    if ("close".equals(metodo.getName())) {
                        return null;
                    }
                    if ("isClosed".equals(metodo.getName())) {
                        return false;
                    }
                    if ("setAutoCommit".equals(metodo.getName())) {
                        return null;
                    }
                    if ("commit".equals(metodo.getName()) || "rollback".equals(metodo.getName())) {
                        return null;
                    }
                    if ("getAutoCommit".equals(metodo.getName())) {
                        return true;
                    }
                    return valorPorDefecto(metodo.getReturnType());
                });
        try {
            Field campo = DatabaseConexion.class.getDeclaredField("connection");
            campo.setAccessible(true);
            campo.set(null, conexion);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException("No se pudo instalar la base falsa.", ex);
        }
        return this;
    }

    public static void desinstalar() {
        try {
            Field campo = DatabaseConexion.class.getDeclaredField("connection");
            campo.setAccessible(true);
            campo.set(null, null);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException("No se pudo desinstalar la base falsa.", ex);
        }
    }

    public BdFalsa fila(Object... pares) {
        if (pares.length % 2 != 0) {
            throw new IllegalArgumentException("Debe pasar clave y valor en pares.");
        }
        Map<String, Object> fila = new LinkedHashMap<>();
        for (int i = 0; i < pares.length; i += 2) {
            fila.put(String.valueOf(pares[i]).toLowerCase(), pares[i + 1]);
        }
        filas.add(fila);
        return this;
    }

    public String ultimoSql() {
        if (sqls.isEmpty()) {
            return null;
        }
        return sqls.get(sqls.size() - 1);
    }

    public Map<Integer, Object> ultimosParametros() {
        Map<Integer, Object> mapa = new LinkedHashMap<>();
        for (int i = 0; i < parametros.size(); i++) {
            mapa.put(i + 1, parametros.get(i));
        }
        return mapa;
    }

    private PreparedStatement crearPreparedStatement(String sql) {
        sqls.add(sql);
        List<Object> parametrosLocales = new ArrayList<>();
        return (CallableStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class<?>[]{CallableStatement.class, PreparedStatement.class},
                (proxy, metodo, argumentos) -> {
                    String nombre = metodo.getName();
                    if (nombre.startsWith("set") && argumentos != null && argumentos.length >= 2) {
                        int indice = (Integer) argumentos[0] - 1;
                        while (parametrosLocales.size() <= indice) {
                            parametrosLocales.add(null);
                        }
                        parametrosLocales.set(indice, argumentos[1]);
                        return null;
                    }
                    if ("executeQuery".equals(nombre)) {
                        parametros = new ArrayList<>(parametrosLocales);
                        return crearResultSet();
                    }
                    if ("executeUpdate".equals(nombre) || "execute".equals(nombre)) {
                        parametros = new ArrayList<>(parametrosLocales);
                        actualizacionesEjecutadas++;
                        return "execute".equals(nombre) ? true : 1;
                    }
                    if ("close".equals(nombre)) {
                        return null;
                    }
                    return valorPorDefecto(metodo.getReturnType());
                });
    }

    private ResultSet crearResultSet() {
        final int[] indice = {-1};
        final List<Map<String, Object>> datos = new ArrayList<>(filas);
        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                (proxy, metodo, argumentos) -> {
                    String nombre = metodo.getName();
                    if ("next".equals(nombre)) {
                        indice[0]++;
                        return indice[0] < datos.size();
                    }
                    if ("close".equals(nombre)) {
                        return null;
                    }
                    if (indice[0] < 0 || indice[0] >= datos.size()) {
                        throw new SQLException("El cursor no esta en una fila valida.");
                    }
                    Map<String, Object> filaActual = datos.get(indice[0]);
                    String columna = String.valueOf(argumentos[0]).toLowerCase();
                    Object valor = filaActual.get(columna);

                    if ("getString".equals(nombre)) {
                        return valor == null ? null : String.valueOf(valor);
                    }
                    if ("getInt".equals(nombre)) {
                        if (valor == null) {
                            return 0;
                        }
                        if (valor instanceof Number) {
                            return ((Number) valor).intValue();
                        }
                        return Integer.parseInt(String.valueOf(valor));
                    }
                    if ("getBoolean".equals(nombre)) {
                        if (valor == null) {
                            return false;
                        }
                        if (valor instanceof Boolean) {
                            return (Boolean) valor;
                        }
                        return Boolean.parseBoolean(String.valueOf(valor));
                    }
                    if ("getTimestamp".equals(nombre)) {
                        return valor == null ? null : (Timestamp) valor;
                    }
                    if ("getDouble".equals(nombre)) {
                        if (valor == null) {
                            return 0.0;
                        }
                        if (valor instanceof Number) {
                            return ((Number) valor).doubleValue();
                        }
                        return Double.parseDouble(String.valueOf(valor));
                    }
                    if ("getObject".equals(nombre)) {
                        return valor;
                    }
                    return valorPorDefecto(metodo.getReturnType());
                });
    }

    private Object valorPorDefecto(Class<?> tipo) {
        if (!tipo.isPrimitive()) {
            return null;
        }
        if (tipo == boolean.class) {
            return false;
        }
        if (tipo == int.class) {
            return 0;
        }
        if (tipo == long.class) {
            return 0L;
        }
        if (tipo == double.class) {
            return 0.0;
        }
        if (tipo == float.class) {
            return 0.0f;
        }
        return 0;
    }
}
