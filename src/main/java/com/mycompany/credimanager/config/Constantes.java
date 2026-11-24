package com.mycompany.credimanager.config;

/**
 * Valores fijos que usa todo el sistema: roles, estados, métodos de pago,
 * nombres de atributos de sesión y formatos.
 * <p>
 * Los textos de roles y estados deben coincidir exactamente con lo que se
 * guarda en la base de datos.
 *
 * @author Integrante 1
 */
public final class Constantes {

    private Constantes() {
    }

    // ===== Roles (tabla rol, columna nombre) =====
    public static final String ROL_ADMINISTRADOR = "Administrador";
    public static final String ROL_ASESOR = "Asesor";
    public static final String ROL_CAJERO = "Cajero";

    // ===== Estados de un préstamo (tabla prestamo, columna estado) =====
    public static final String PRESTAMO_SOLICITADO = "SOLICITADO";
    public static final String PRESTAMO_APROBADO = "APROBADO";
    public static final String PRESTAMO_RECHAZADO = "RECHAZADO";
    public static final String PRESTAMO_ACTIVO = "ACTIVO";
    public static final String PRESTAMO_EN_MORA = "EN_MORA";
    public static final String PRESTAMO_CANCELADO = "CANCELADO";

    // ===== Estados de una cuota (tabla cuota, columna estado) =====
    public static final String CUOTA_PENDIENTE = "PENDIENTE";
    public static final String CUOTA_PAGADA = "PAGADA";
    public static final String CUOTA_VENCIDA = "VENCIDA";

    // ===== Métodos de pago (tabla pago, columna metodo_pago) =====
    public static final String PAGO_EFECTIVO = "EFECTIVO";
    public static final String PAGO_TRANSFERENCIA = "TRANSFERENCIA";
    public static final String PAGO_TARJETA = "TARJETA";

    // ===== Tipos de garantía (tabla garantia, columna tipo) =====
    public static final String GARANTIA_HIPOTECARIA = "HIPOTECARIA";
    public static final String GARANTIA_PRENDARIA = "PRENDARIA";
    public static final String GARANTIA_OTRA = "OTRA";

    // ===== Atributos de sesión =====
    /** Objeto Usuario que inició sesión. */
    public static final String SESION_USUARIO = "usuarioSesion";
    /** Mensaje de éxito para mostrar una sola vez en la siguiente vista. */
    public static final String SESION_MENSAJE = "mensaje";
    /** Mensaje de error para mostrar una sola vez en la siguiente vista. */
    public static final String SESION_ERROR = "error";

    // ===== Formatos =====
    public static final String FORMATO_FECHA = "dd/MM/yyyy";
    public static final String SIMBOLO_MONEDA = "L";
    public static final int DECIMALES_MONEDA = 2;

    // ===== Paginación =====
    public static final int REGISTROS_POR_PAGINA = 10;
}
