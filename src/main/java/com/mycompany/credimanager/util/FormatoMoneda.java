package com.mycompany.credimanager.util;

import com.mycompany.credimanager.config.Constantes;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Redondeo y formato de cantidades de dinero (lempiras).
 * <p>
 * Todo el dinero se maneja con {@link BigDecimal}, nunca con double, para no
 * perder centavos en los cálculos.
 *
 * @author Integrante 1
 */
public final class FormatoMoneda {

    private FormatoMoneda() {
    }

    /** Redondea a 2 decimales (mitad hacia arriba). Devuelve 0.00 si el valor es null. */
    public static BigDecimal redondear(BigDecimal valor) {
        if (valor == null) {
            return BigDecimal.ZERO.setScale(Constantes.DECIMALES_MONEDA);
        }
        return valor.setScale(Constantes.DECIMALES_MONEDA, RoundingMode.HALF_UP);
    }

    /**
     * Da formato para mostrar: {@code L 12,345.60}.
     *
     * @return texto con símbolo de moneda, separador de miles y 2 decimales
     */
    public static String formatear(BigDecimal valor) {
        return Constantes.SIMBOLO_MONEDA + " " + formatearSinSimbolo(valor);
    }

    /** Da formato sin símbolo: {@code 12,345.60}. */
    public static String formatearSinSimbolo(BigDecimal valor) {
        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return df.format(redondear(valor));
    }

    /** Da formato a un porcentaje: {@code 18.00%}. */
    public static String porcentaje(BigDecimal valor) {
        return formatearSinSimbolo(valor) + "%";
    }
}
