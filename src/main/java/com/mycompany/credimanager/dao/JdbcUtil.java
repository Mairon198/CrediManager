package com.mycompany.credimanager.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Ayudas internas para los DAO (solo se usan dentro del paquete dao).
 *
 * @author Integrante 1
 */
final class JdbcUtil {

    /** Código de error de SQL Server cuando una llave foránea impide borrar o insertar. */
    private static final int ERROR_LLAVE_FORANEA = 547;
    /** Códigos de error de SQL Server por valor duplicado en un índice único. */
    private static final int ERROR_DUPLICADO = 2627;
    private static final int ERROR_DUPLICADO_INDICE = 2601;

    private JdbcUtil() {
    }

    /** Lee el ID generado por un INSERT hecho con RETURN_GENERATED_KEYS. */
    static int idGenerado(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Asigna un BigDecimal que puede ser null. */
    static void setDecimal(PreparedStatement ps, int indice, BigDecimal valor) throws SQLException {
        if (valor == null) {
            ps.setNull(indice, Types.DECIMAL);
        } else {
            ps.setBigDecimal(indice, valor);
        }
    }

    /** Asigna un texto que puede ser null o vacío (se guarda NULL). */
    static void setTexto(PreparedStatement ps, int indice, String valor) throws SQLException {
        if (valor == null || valor.isBlank()) {
            ps.setNull(indice, Types.NVARCHAR);
        } else {
            ps.setString(indice, valor.trim());
        }
    }

    /** Deshace una transacción sin lanzar otra excepción. */
    static void rollback(Connection cn) {
        if (cn != null) {
            try {
                cn.rollback();
            } catch (SQLException ignorada) {
                // No hay nada más que hacer: se reporta el error original.
            }
        }
    }

    /**
     * Convierte una SQLException en DAOException con un mensaje entendible.
     *
     * @param accion qué se intentaba hacer, ej. "eliminar el cliente"
     */
    static DAOException error(String accion, SQLException e) {
        int codigo = e.getErrorCode();
        if (codigo == ERROR_LLAVE_FORANEA) {
            return new DAOException("No se puede " + accion + " porque tiene registros relacionados.", e);
        }
        if (codigo == ERROR_DUPLICADO || codigo == ERROR_DUPLICADO_INDICE) {
            return new DAOException("No se puede " + accion + ": ya existe un registro con ese dato.", e);
        }
        return new DAOException("Error al " + accion + ": " + e.getMessage(), e);
    }
}
