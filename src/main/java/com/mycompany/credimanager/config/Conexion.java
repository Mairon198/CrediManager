package com.mycompany.credimanager.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Entrega conexiones a la base de datos SQL Server.
 * <p>
 * Lee la URL, el usuario y la contraseña del archivo {@code db.properties}
 * (en {@code src/main/resources}). Cada DAO pide una conexión nueva y la cierra
 * con try-with-resources al terminar.
 *
 * @author Integrante 1
 */
public final class Conexion {

    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String ARCHIVO = "db.properties";

    private static String url;
    private static String usuario;
    private static String contrasena;

    static {
        cargarConfiguracion();
    }

    private Conexion() {
    }

    private static void cargarConfiguracion() {
        Properties props = new Properties();
        try (InputStream in = Conexion.class.getClassLoader().getResourceAsStream(ARCHIVO)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró " + ARCHIVO
                        + " en src/main/resources. Copia db.properties.example como db.properties y pon tus datos.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + ARCHIVO, e);
        }
        url = props.getProperty("db.url");
        usuario = props.getProperty("db.usuario");
        contrasena = props.getProperty("db.contrasena");

        try {
            // En Tomcat el driver no siempre se registra solo; por eso se carga a mano.
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No se encontró el driver de SQL Server (mssql-jdbc) en el proyecto", e);
        }
    }

    /**
     * Abre una conexión nueva. Quien la pide debe cerrarla (usar try-with-resources).
     *
     * @return conexión abierta a la base de datos
     * @throws SQLException si no se puede conectar
     */
    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(url, usuario, contrasena);
    }

    /**
     * Prueba rápida de conexión.
     *
     * @return true si se pudo conectar
     */
    public static boolean probar() {
        try (Connection cn = getConexion()) {
            return cn.isValid(3);
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            return false;
        }
    }
}
