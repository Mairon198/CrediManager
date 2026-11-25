package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Formato para mostrar datos en las vistas y reportes, y conversión de los
 * textos que llegan de los formularios HTML.
 */
public final class Formato {

    public static final String MONEDA = "L";
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a", Locale.forLanguageTag("es-HN"));

    private Formato() {
    }

    /** Redondea a 2 decimales (mitad hacia arriba), como ROUND(x, 2) en SQL Server. */
    public static BigDecimal redondear(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO.setScale(2) : valor.setScale(2, RoundingMode.HALF_UP);
    }

    /** {@code L 12,345.60} */
    public static String moneda(BigDecimal valor) {
        return MONEDA + " " + numero(valor);
    }

    /** {@code 12,345.60} */
    public static String numero(BigDecimal valor) {
        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return df.format(redondear(valor));
    }

    /** {@code 18.00 %} */
    public static String porcentaje(BigDecimal valor) {
        return numero(valor) + " %";
    }

    /** {@code 24/11/2026} */
    public static String fecha(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FECHA);
    }

    /** {@code 24/11/2026 08:15 p. m.} */
    public static String fechaHora(LocalDateTime fecha) {
        return fecha == null ? "" : fecha.format(FECHA_HORA);
    }

    // ----- Conversión de los valores de un formulario -----

    /** Valor de un {@code <input type="date">} (2026-11-24). Devuelve null si está vacío o no es válido. */
    public static LocalDate aFecha(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** "12,500.50" -> 12500.50. Devuelve null si está vacío o no es un número. */
    public static BigDecimal aDecimal(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(texto.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Devuelve el entero, o 0 si el texto está vacío o no es un número. */
    public static int aEntero(String texto) {
        if (texto == null || texto.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
