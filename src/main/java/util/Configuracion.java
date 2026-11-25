package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Lee los datos de conexión de config.properties.
 * <p>
 * En una aplicación web el programa corre dentro de Tomcat, así que el archivo
 * se busca primero en el classpath ({@code src/main/resources/config.properties})
 * y, si no está ahí, en la carpeta desde donde se ejecuta.
 */
public final class Configuracion {

    private static final String ARCHIVO = "config.properties";
    private static final Properties PROPS = new Properties();
    private static boolean cargado = false;

    static {
        try (InputStream in = Configuracion.class.getClassLoader().getResourceAsStream(ARCHIVO)) {
            if (in != null) {
                try (Reader lector = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    PROPS.load(lector);   // UTF-8: la contraseña puede tener ñ o tildes
                }
                cargado = true;
            }
        } catch (IOException e) {
            cargado = false;
        }
        if (!cargado) {
            try (Reader lector = new InputStreamReader(new FileInputStream(ARCHIVO), StandardCharsets.UTF_8)) {
                PROPS.load(lector);
                cargado = true;
            } catch (IOException e) {
                cargado = false;
            }
        }
    }

    private Configuracion() {
    }

    public static boolean estaCargado() {
        return cargado;
    }

    public static String get(String clave) {
        String valor = PROPS.getProperty(clave);
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
