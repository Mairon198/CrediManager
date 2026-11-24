package com.mycompany.credimanager.util;

import com.mycompany.credimanager.config.Constantes;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Conversión y cálculo de fechas.
 * <p>
 * En Java se usa {@link LocalDate}; en la base de datos, columnas {@code DATE};
 * en los formularios HTML, {@code <input type="date">} (formato yyyy-MM-dd).
 *
 * @author Integrante 1
 */
public final class FechaUtil {

    private static final DateTimeFormatter FORMATO_VISTA = DateTimeFormatter.ofPattern(Constantes.FORMATO_FECHA);

    private FechaUtil() {
    }

    /** @return la fecha de hoy */
    public static LocalDate hoy() {
        return LocalDate.now();
    }

    /**
     * Da formato para mostrar en pantalla o en reportes: 23/11/2026.
     *
     * @return texto formateado o cadena vacía si la fecha es null
     */
    public static String formatear(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FORMATO_VISTA);
    }

    /**
     * Convierte el valor de un {@code <input type="date">} (2026-11-23) a LocalDate.
     *
     * @return la fecha, o null si el texto está vacío o no es válido
     */
    public static LocalDate desdeFormulario(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Convierte de LocalDate a java.sql.Date (para PreparedStatement). Acepta null. */
    public static Date aSql(LocalDate fecha) {
        return fecha == null ? null : Date.valueOf(fecha);
    }

    /** Convierte de java.sql.Date (de un ResultSet) a LocalDate. Acepta null. */
    public static LocalDate desdeSql(Date fecha) {
        return fecha == null ? null : fecha.toLocalDate();
    }

    /**
     * Días de atraso de un pago respecto al vencimiento.
     *
     * @return 0 si se pagó a tiempo; si no, los días de atraso
     */
    public static long diasDeAtraso(LocalDate vencimiento, LocalDate fechaPago) {
        if (vencimiento == null || fechaPago == null || !fechaPago.isAfter(vencimiento)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(vencimiento, fechaPago);
    }

    /** Suma meses a una fecha (para generar los vencimientos de las cuotas). */
    public static LocalDate sumarMeses(LocalDate fecha, int meses) {
        return fecha.plusMonths(meses);
    }
}
