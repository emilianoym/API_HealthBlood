package org.lasalle.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Abre conexiones a MySQL.
 *
 * Los valores por defecto se pueden cambiar aqui o, sin tocar el codigo,
 * con las variables de entorno HB_DB_URL, HB_DB_USER y HB_DB_PASSWORD.
 *
 * @author emili
 */
public class ConnectionMySql {

    private static final String URL = valor("HB_DB_URL",
            "jdbc:mysql://localhost:3306/healthblood"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    private static final String USER = valor("HB_DB_USER", "root");
    private static final String PASSWORD = valor("HB_DB_PASSWORD", "");

    private static String valor(String variable, String porDefecto) {
        String v = System.getenv(variable);
        return (v == null || v.isEmpty()) ? porDefecto : v;
    }

    public Connection open() throws SQLException {
        try {
            // Necesario en Tomcat para que el driver se registre desde el WEB-INF/lib
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontro el driver de MySQL en WEB-INF/lib", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
}
