package com.mycompany.credimanager.dao;

import com.mycompany.credimanager.config.Conexion;
import com.mycompany.credimanager.model.Pago;
import com.mycompany.credimanager.util.FechaUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla {@code pago}.
 * <p>
 * Para cobrar una cuota usar {@link #registrarPago}, que guarda el pago y
 * actualiza la cuota (y el préstamo, si hace falta) en una sola transacción.
 *
 * @author Integrante 1
 */
public class PagoDAO implements ICrud<Pago> {

    private static final String SELECT_BASE
            = "SELECT pg.id_pago, pg.id_cuota, pg.id_usuario, pg.fecha_pago, pg.monto_pagado, pg.mora, pg.metodo_pago, "
            + "cu.id_prestamo, cu.numero AS numero_cuota, "
            + "c.nombres + ' ' + c.apellidos AS nombre_cliente, u.nombre AS nombre_usuario "
            + "FROM pago pg "
            + "INNER JOIN cuota cu ON cu.id_cuota = pg.id_cuota "
            + "INNER JOIN prestamo p ON p.id_prestamo = cu.id_prestamo "
            + "INNER JOIN cliente c ON c.id_cliente = p.id_cliente "
            + "INNER JOIN usuario u ON u.id_usuario = pg.id_usuario ";

    private static final String INSERT
            = "INSERT INTO pago (id_cuota, id_usuario, fecha_pago, monto_pagado, mora, metodo_pago) VALUES (?, ?, ?, ?, ?, ?)";

    @Override
    public boolean insertar(Pago p) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            asignar(ps, p);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                p.setIdPago(JdbcUtil.idGenerado(ps));
            }
            return ok;
        } catch (SQLException e) {
            throw JdbcUtil.error("registrar el pago", e);
        }
    }

    /**
     * Registra el cobro de una cuota en una sola transacción:
     * inserta el pago, cambia el estado de la cuota y, si se indica, el del préstamo.
     *
     * @param pago                pago a guardar (queda con su ID)
     * @param estadoCuota         nuevo estado de la cuota (normalmente Constantes.CUOTA_PAGADA)
     * @param idPrestamo          préstamo de la cuota
     * @param nuevoEstadoPrestamo nuevo estado del préstamo (ej. CANCELADO si era la última cuota,
     *                            ACTIVO si salió de mora), o null para no cambiarlo
     * @return true si todo se guardó
     */
    public boolean registrarPago(Pago pago, String estadoCuota, int idPrestamo, String nuevoEstadoPrestamo) {
        Connection cn = null;
        try {
            cn = Conexion.getConexion();
            cn.setAutoCommit(false);
            try (PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                asignar(ps, pago);
                ps.executeUpdate();
                pago.setIdPago(JdbcUtil.idGenerado(ps));
            }
            try (PreparedStatement ps = cn.prepareStatement("UPDATE cuota SET estado = ? WHERE id_cuota = ?")) {
                ps.setString(1, estadoCuota);
                ps.setInt(2, pago.getIdCuota());
                ps.executeUpdate();
            }
            if (nuevoEstadoPrestamo != null) {
                try (PreparedStatement ps = cn.prepareStatement("UPDATE prestamo SET estado = ? WHERE id_prestamo = ?")) {
                    ps.setString(1, nuevoEstadoPrestamo);
                    ps.setInt(2, idPrestamo);
                    ps.executeUpdate();
                }
            }
            cn.commit();
            return true;
        } catch (SQLException e) {
            JdbcUtil.rollback(cn);
            throw JdbcUtil.error("registrar el pago", e);
        } finally {
            if (cn != null) {
                try {
                    cn.close();
                } catch (SQLException ignorada) {
                    // La conexión ya no se usa.
                }
            }
        }
    }

    @Override
    public boolean actualizar(Pago p) {
        String sql = "UPDATE pago SET id_cuota = ?, id_usuario = ?, fecha_pago = ?, monto_pagado = ?, mora = ?, metodo_pago = ? "
                + "WHERE id_pago = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, p);
            ps.setInt(7, p.getIdPago());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.error("actualizar el pago", e);
        }
    }

    @Override
    public boolean eliminar(int id) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM pago WHERE id_pago = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.error("eliminar el pago", e);
        }
    }

    @Override
    public Pago buscarPorId(int id) {
        List<Pago> lista = consultar(SELECT_BASE + "WHERE pg.id_pago = ?", id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    /** Todos los pagos, del más reciente al más antiguo. */
    @Override
    public List<Pago> listar() {
        return consultar(SELECT_BASE + "ORDER BY pg.fecha_pago DESC, pg.id_pago DESC");
    }

    /** Pagos de una cuota. */
    public List<Pago> listarPorCuota(int idCuota) {
        return consultar(SELECT_BASE + "WHERE pg.id_cuota = ? ORDER BY pg.fecha_pago", idCuota);
    }

    /** Historial de pagos de un préstamo. */
    public List<Pago> listarPorPrestamo(int idPrestamo) {
        return consultar(SELECT_BASE + "WHERE cu.id_prestamo = ? ORDER BY cu.numero, pg.fecha_pago", idPrestamo);
    }

    /** Pagos entre dos fechas, ambas incluidas (para reportes). */
    public List<Pago> listarPorFechas(LocalDate desde, LocalDate hasta) {
        String sql = SELECT_BASE + "WHERE pg.fecha_pago BETWEEN ? AND ? ORDER BY pg.fecha_pago, pg.id_pago";
        return consultar(sql, FechaUtil.aSql(desde), FechaUtil.aSql(hasta));
    }

    /**
     * Total cobrado (cuotas + mora) entre dos fechas, ambas incluidas.
     *
     * @return total, o 0 si no hubo pagos
     */
    public BigDecimal totalCobrado(LocalDate desde, LocalDate hasta) {
        String sql = "SELECT COALESCE(SUM(monto_pagado + mora), 0) FROM pago WHERE fecha_pago BETWEEN ? AND ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, FechaUtil.aSql(desde));
            ps.setDate(2, FechaUtil.aSql(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            throw JdbcUtil.error("calcular el total cobrado", e);
        }
    }

    private List<Pago> consultar(String sql, Object... parametros) {
        List<Pago> lista = new ArrayList<>();
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
            throw JdbcUtil.error("consultar los pagos", e);
        }
        return lista;
    }

    private void asignar(PreparedStatement ps, Pago p) throws SQLException {
        ps.setInt(1, p.getIdCuota());
        ps.setInt(2, p.getIdUsuario());
        ps.setDate(3, FechaUtil.aSql(p.getFechaPago() == null ? FechaUtil.hoy() : p.getFechaPago()));
        JdbcUtil.setDecimal(ps, 4, p.getMontoPagado());
        JdbcUtil.setDecimal(ps, 5, p.getMora() == null ? BigDecimal.ZERO : p.getMora());
        ps.setString(6, p.getMetodoPago());
    }

    private Pago mapear(ResultSet rs) throws SQLException {
        Pago p = new Pago(
                rs.getInt("id_pago"),
                rs.getInt("id_cuota"),
                rs.getInt("id_usuario"),
                FechaUtil.desdeSql(rs.getDate("fecha_pago")),
                rs.getBigDecimal("monto_pagado"),
                rs.getBigDecimal("mora"),
                rs.getString("metodo_pago"));
        p.setIdPrestamo(rs.getInt("id_prestamo"));
        p.setNumeroCuota(rs.getInt("numero_cuota"));
        p.setNombreCliente(rs.getString("nombre_cliente"));
        p.setNombreUsuario(rs.getString("nombre_usuario"));
        return p;
    }
}
