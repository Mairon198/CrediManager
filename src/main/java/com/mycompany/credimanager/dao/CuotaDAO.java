package com.mycompany.credimanager.dao;

import com.mycompany.credimanager.config.Conexion;
import com.mycompany.credimanager.config.Constantes;
import com.mycompany.credimanager.model.Cuota;
import com.mycompany.credimanager.util.FechaUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla {@code cuota} (plan de pagos de cada préstamo).
 * <p>
 * Las cuotas de un préstamo nuevo se insertan normalmente con
 * {@link PrestamoDAO#aprobarConPlan}, que guarda el préstamo y sus cuotas en
 * una sola transacción.
 *
 * @author Integrante 1
 */
public class CuotaDAO implements ICrud<Cuota> {

    private static final String SELECT_BASE
            = "SELECT id_cuota, id_prestamo, numero, fecha_vencimiento, capital, interes, monto_total, saldo, estado FROM cuota ";

    private static final String INSERT
            = "INSERT INTO cuota (id_prestamo, numero, fecha_vencimiento, capital, interes, monto_total, saldo, estado) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    @Override
    public boolean insertar(Cuota c) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            asignar(ps, c);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                c.setIdCuota(JdbcUtil.idGenerado(ps));
            }
            return ok;
        } catch (SQLException e) {
            throw JdbcUtil.error("registrar la cuota", e);
        }
    }

    /**
     * Inserta todas las cuotas de un plan en una sola transacción.
     *
     * @return número de cuotas insertadas
     */
    public int insertarLote(List<Cuota> cuotas) {
        Connection cn = null;
        try {
            cn = Conexion.getConexion();
            cn.setAutoCommit(false);
            int total = insertarLote(cn, cuotas);
            cn.commit();
            return total;
        } catch (SQLException e) {
            JdbcUtil.rollback(cn);
            throw JdbcUtil.error("registrar el plan de pagos", e);
        } finally {
            cerrar(cn);
        }
    }

    /** Inserta las cuotas usando una conexión ya abierta (para transacciones de otros DAO). */
    int insertarLote(Connection cn, List<Cuota> cuotas) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(INSERT)) {
            for (Cuota c : cuotas) {
                asignar(ps, c);
                ps.addBatch();
            }
            int total = 0;
            for (int filas : ps.executeBatch()) {
                total += Math.max(filas, 0);
            }
            return total == 0 ? cuotas.size() : total;
        }
    }

    @Override
    public boolean actualizar(Cuota c) {
        String sql = "UPDATE cuota SET id_prestamo = ?, numero = ?, fecha_vencimiento = ?, capital = ?, interes = ?, "
                + "monto_total = ?, saldo = ?, estado = ? WHERE id_cuota = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, c);
            ps.setInt(9, c.getIdCuota());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.error("actualizar la cuota", e);
        }
    }

    @Override
    public boolean eliminar(int id) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM cuota WHERE id_cuota = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.error("eliminar la cuota", e);
        }
    }

    @Override
    public Cuota buscarPorId(int id) {
        List<Cuota> lista = consultar(SELECT_BASE + "WHERE id_cuota = ?", id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public List<Cuota> listar() {
        return consultar(SELECT_BASE + "ORDER BY id_prestamo, numero");
    }

    /** Plan de pagos completo de un préstamo, ordenado por número de cuota. */
    public List<Cuota> listarPorPrestamo(int idPrestamo) {
        return consultar(SELECT_BASE + "WHERE id_prestamo = ? ORDER BY numero", idPrestamo);
    }

    /**
     * Cuotas sin pagar cuya fecha de vencimiento ya pasó.
     *
     * @param fecha fecha de referencia (normalmente hoy)
     */
    public List<Cuota> listarVencidas(LocalDate fecha) {
        String sql = SELECT_BASE + "WHERE estado <> ? AND fecha_vencimiento < ? ORDER BY fecha_vencimiento";
        return consultar(sql, Constantes.CUOTA_PAGADA, FechaUtil.aSql(fecha));
    }

    /**
     * Primera cuota sin pagar de un préstamo (la que toca cobrar).
     *
     * @return la cuota, o null si ya están todas pagadas
     */
    public Cuota siguientePendiente(int idPrestamo) {
        String sql = "SELECT TOP 1 id_cuota, id_prestamo, numero, fecha_vencimiento, capital, interes, monto_total, saldo, estado "
                + "FROM cuota WHERE id_prestamo = ? AND estado <> ? ORDER BY numero";
        List<Cuota> lista = consultar(sql, idPrestamo, Constantes.CUOTA_PAGADA);
        return lista.isEmpty() ? null : lista.get(0);
    }

    /** Cuántas cuotas de un préstamo faltan por pagar (0 = préstamo cancelado). */
    public int contarNoPagadas(int idPrestamo) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM cuota WHERE id_prestamo = ? AND estado <> ?")) {
            ps.setInt(1, idPrestamo);
            ps.setString(2, Constantes.CUOTA_PAGADA);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw JdbcUtil.error("contar las cuotas pendientes", e);
        }
    }

    /** Cambia el estado de una cuota (ver Constantes.CUOTA_*). */
    public boolean actualizarEstado(int idCuota, String estado) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("UPDATE cuota SET estado = ? WHERE id_cuota = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, idCuota);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.error("actualizar el estado de la cuota", e);
        }
    }

    /**
     * Marca como VENCIDA toda cuota PENDIENTE cuyo vencimiento ya pasó.
     * Se puede llamar al iniciar sesión o al abrir el dashboard.
     *
     * @return número de cuotas marcadas
     */
    public int marcarVencidas(LocalDate fecha) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE cuota SET estado = ? WHERE estado = ? AND fecha_vencimiento < ?")) {
            ps.setString(1, Constantes.CUOTA_VENCIDA);
            ps.setString(2, Constantes.CUOTA_PENDIENTE);
            ps.setDate(3, FechaUtil.aSql(fecha));
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.error("marcar las cuotas vencidas", e);
        }
    }

    private List<Cuota> consultar(String sql, Object... parametros) {
        List<Cuota> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw JdbcUtil.error("consultar las cuotas", e);
        }
        return lista;
    }

    private void asignar(PreparedStatement ps, Cuota c) throws SQLException {
        ps.setInt(1, c.getIdPrestamo());
        ps.setInt(2, c.getNumero());
        ps.setDate(3, FechaUtil.aSql(c.getFechaVencimiento()));
        JdbcUtil.setDecimal(ps, 4, c.getCapital());
        JdbcUtil.setDecimal(ps, 5, c.getInteres());
        JdbcUtil.setDecimal(ps, 6, c.getMontoTotal());
        JdbcUtil.setDecimal(ps, 7, c.getSaldo());
        ps.setString(8, c.getEstado() == null ? Constantes.CUOTA_PENDIENTE : c.getEstado());
    }

    private Cuota mapear(ResultSet rs) throws SQLException {
        return new Cuota(
                rs.getInt("id_cuota"),
                rs.getInt("id_prestamo"),
                rs.getInt("numero"),
                FechaUtil.desdeSql(rs.getDate("fecha_vencimiento")),
                rs.getBigDecimal("capital"),
                rs.getBigDecimal("interes"),
                rs.getBigDecimal("monto_total"),
                rs.getBigDecimal("saldo"),
                rs.getString("estado"));
    }

    private static void cerrar(Connection cn) {
        if (cn != null) {
            try {
                cn.close();
            } catch (SQLException ignorada) {
                // La conexión ya no se usa.
            }
        }
    }
}
