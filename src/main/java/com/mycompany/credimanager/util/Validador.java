package com.mycompany.credimanager.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Validaciones de datos que llegan de los formularios.
 * <p>
 * Se usan en las clases de servicio antes de llamar a un DAO, aunque la vista
 * también valide con JavaScript (la validación del servidor es la que manda).
 *
 * @author Integrante 1
 */
public final class Validador {

    /** DNI hondureño: 13 dígitos, con o sin guiones (0801-1999-12345). */
    private static final Pattern DNI = Pattern.compile("^\\d{4}-?\\d{4}-?\\d{5}$");
    /** Teléfono de 8 dígitos, con o sin guion (9999-9999). */
    private static final Pattern TELEFONO = Pattern.compile("^\\d{4}-?\\d{4}$");
    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    /** Usuario de 4 a 30 caracteres: letras, números, punto o guion bajo. */
    private static final Pattern USUARIO = Pattern.compile("^[A-Za-z0-9._]{4,30}$");

    private Validador() {
    }

    /** @return true si el texto es null o solo tiene espacios */
    public static boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    public static boolean esDniValido(String dni) {
        return !esVacio(dni) && DNI.matcher(dni.trim()).matches();
    }

    /** Quita guiones y espacios del DNI para guardarlo siempre igual (13 dígitos). */
    public static String normalizarDni(String dni) {
        return dni == null ? null : dni.replaceAll("[-\\s]", "");
    }

    public static boolean esTelefonoValido(String telefono) {
        return !esVacio(telefono) && TELEFONO.matcher(telefono.trim()).matches();
    }

    public static boolean esCorreoValido(String correo) {
        return !esVacio(correo) && CORREO.matcher(correo.trim()).matches();
    }

    public static boolean esUsuarioValido(String usuario) {
        return !esVacio(usuario) && USUARIO.matcher(usuario.trim()).matches();
    }

    /** Contraseña de al menos 6 caracteres. */
    public static boolean esContrasenaValida(String contrasena) {
        return contrasena != null && contrasena.length() >= 6;
    }

    /** @return true si el valor existe y es mayor que cero */
    public static boolean esPositivo(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Convierte el texto de un formulario a BigDecimal.
     *
     * @return el número, o null si el texto está vacío o no es un número
     */
    public static BigDecimal aDecimal(String texto) {
        if (esVacio(texto)) {
            return null;
        }
        try {
            return new BigDecimal(texto.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Convierte el texto de un formulario a entero.
     *
     * @return el número, o -1 si el texto está vacío o no es un entero
     */
    public static int aEntero(String texto) {
        if (esVacio(texto)) {
            return -1;
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
