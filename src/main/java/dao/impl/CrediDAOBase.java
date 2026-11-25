package dao.impl;

import excepciones.PersistenciaException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Base de los DAO que llaman procedimientos almacenados: agrega a BaseDAO el
 * manejo de los errores de negocio que lanza SQL Server (THROW 5xxxx) y la
 * lectura de fechas que pueden venir NULL.
 */
public abstract class CrediDAOBase extends BaseDAO {

    /** Convierte una SQLException en PersistenciaException conservando el mensaje de negocio (THROW 5xxxx). */
    protected PersistenciaException error(String contexto, SQLException e) {
        if (e.getErrorCode() >= 50000) {
            return new PersistenciaException(e.getMessage(), e);   // regla de negocio validada por SQL Server
        }
        return new PersistenciaException(contexto + ": " + e.getMessage(), e);
    }

    /** Prepara la llamada a un procedimiento: "{call dbo.nombre(?,?,...)}". */
    protected CallableStatement llamada(Connection cn, String procedimiento, int parametros) throws SQLException {
        StringBuilder sb = new StringBuilder("{call dbo.").append(procedimiento).append('(');
        for (int i = 0; i < parametros; i++) {
            sb.append(i == 0 ? "?" : ",?");
        }
        return cn.prepareCall(sb.append(")}").toString());
    }

    /** Lee una columna DATE que puede ser NULL. */
    protected static LocalDate fecha(ResultSet rs, String columna) throws SQLException {
        Date d = rs.getDate(columna);
        return d == null ? null : d.toLocalDate();
    }

    /** Lee una columna DATETIME2 que puede ser NULL. */
    protected static LocalDateTime fechaHora(ResultSet rs, String columna) throws SQLException {
        Timestamp t = rs.getTimestamp(columna);
        return t == null ? null : t.toLocalDateTime();
    }

    /** Convierte texto vacío en null (para columnas opcionales). */
    protected static String vacioANull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
