package util;

import excepciones.ValidacionException;
import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class Validador {

    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    /** Identidad hondureña: 13 dígitos, con o sin guiones (0801-1990-12345). */
    private static final Pattern IDENTIDAD = Pattern.compile("^\\d{4}-?\\d{4}-?\\d{5}$");
    /** Teléfono de 8 dígitos, con o sin guion (9999-9999). */
    private static final Pattern TELEFONO = Pattern.compile("^\\d{4}-?\\d{4}$");
    /** Nombre de usuario: 4 a 30 caracteres, letras minúsculas, números, punto o guion bajo. */
    private static final Pattern USUARIO = Pattern.compile("^[a-z0-9._]{4,30}$");

    private Validador() {
    }

    public static void requerido(String valor, String campo) throws ValidacionException {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException("El campo '" + campo + "' es obligatorio.");
        }
    }

    public static void correo(String valor) throws ValidacionException {
        if (valor != null && !valor.isBlank() && !CORREO.matcher(valor.trim()).matches()) {
            throw new ValidacionException("El correo no tiene un formato válido.");
        }
    }

    public static void identidad(String valor) throws ValidacionException {
        requerido(valor, "Identidad");
        if (!IDENTIDAD.matcher(valor.trim()).matches()) {
            throw new ValidacionException("La identidad debe tener 13 dígitos (formato 0801-1990-12345).");
        }
    }

    /** Deja la identidad como la guarda la BD: "0801199012345" -> "0801-1990-12345". */
    public static String normalizarIdentidad(String valor) {
        if (valor == null) {
            return null;
        }
        String d = valor.replaceAll("\\D", "");
        return d.length() == 13 ? d.substring(0, 4) + "-" + d.substring(4, 8) + "-" + d.substring(8) : valor.trim();
    }

    public static void telefono(String valor) throws ValidacionException {
        if (valor != null && !valor.isBlank() && !TELEFONO.matcher(valor.trim()).matches()) {
            throw new ValidacionException("El teléfono debe tener 8 dígitos (formato 9999-9999).");
        }
    }

    /** Deja el teléfono como "9999-9999". */
    public static String normalizarTelefono(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String d = valor.replaceAll("\\D", "");
        return d.length() == 8 ? d.substring(0, 4) + "-" + d.substring(4) : valor.trim();
    }

    public static void nombreUsuario(String valor) throws ValidacionException {
        requerido(valor, "Usuario");
        if (!USUARIO.matcher(valor.trim()).matches()) {
            throw new ValidacionException("El usuario debe tener de 4 a 30 caracteres: minúsculas, números, punto o guion bajo.");
        }
    }

    public static void contrasena(char[] valor) throws ValidacionException {
        if (valor == null || valor.length < 8) {
            throw new ValidacionException("La contraseña debe tener al menos 8 caracteres.");
        }
    }

    public static void noNegativo(int valor, String campo) throws ValidacionException {
        if (valor < 0) {
            throw new ValidacionException("El campo '" + campo + "' no puede ser negativo.");
        }
    }

    public static void positivo(int valor, String campo) throws ValidacionException {
        if (valor <= 0) {
            throw new ValidacionException("El campo '" + campo + "' debe ser mayor que cero.");
        }
    }

    public static void positivo(BigDecimal valor, String campo) throws ValidacionException {
        if (valor == null || valor.signum() <= 0) {
            throw new ValidacionException("El campo '" + campo + "' debe ser mayor que cero.");
        }
    }

    public static void noNegativo(BigDecimal valor, String campo) throws ValidacionException {
        if (valor == null || valor.signum() < 0) {
            throw new ValidacionException("El campo '" + campo + "' no puede ser negativo.");
        }
    }

    public static void rango(BigDecimal valor, BigDecimal minimo, BigDecimal maximo, String campo) throws ValidacionException {
        if (valor == null || valor.compareTo(minimo) < 0 || valor.compareTo(maximo) > 0) {
            throw new ValidacionException("El campo '" + campo + "' debe estar entre "
                    + Formato.moneda(minimo) + " y " + Formato.moneda(maximo) + ".");
        }
    }

    public static void rango(int valor, int minimo, int maximo, String campo) throws ValidacionException {
        if (valor < minimo || valor > maximo) {
            throw new ValidacionException("El campo '" + campo + "' debe estar entre " + minimo + " y " + maximo + ".");
        }
    }
}
