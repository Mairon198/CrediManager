package com.mycompany.credimanager.dao;

import com.mycompany.credimanager.config.Conexion;
import com.mycompany.credimanager.config.Constantes;
import com.mycompany.credimanager.model.Cuota;
import com.mycompany.credimanager.model.Prestamo;
import com.mycompany.credimanager.util.FechaUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Acceso a la tabla {@code prestamo}.
 * <p>
 * Las consultas traen también el nombre del cliente, del tipo de préstamo y del
 * usuario que lo registró, para mostrarlos en las listas sin hacer más consultas.
 *
 * @author Integrante 1
 */
public class PrestamoDAO implements ICrud<Prestamo> {

    private static final String SELECT_BASE
            = "SELECT p.id_prestamo, p.id_cliente, p.id_tipo, p.id_usuario, p.monto, p.plazo_meses, p.tasa, p.cuota, "
            + "p.fecha_solicitud, p.fecha_aprobacion, p.estado, "
            + "c.nombres + ' ' + c.apellidos AS nombre_cliente, c.dni AS dni_cliente, "
            + "t.nombre AS nombre_tipo, u.nombre AS nombre_usuario "
            + "FROM prestamo p "
            + "INNER JOIN cliente c ON c.id_cliente = p.id_cliente "
            + "INNER JOIN tipo_prestamo t ON t.id_tipo = p.id_tipo "
            + "INNER JOIN usuario u ON u.id_usuario = p.id_usuario ";

    private final CuotaDAO cuotaDAO = new CuotaDAO();

    /**
     * Registra una solicitud. Si no trae estado ni fecha de solicitud,
     * se guardan SOLICITADO y la fecha de hoy.
     */
    @Override
    public boolean insertar(Prestamo p) {
        if (p.getEstado() == null) {
            p.setEstado(Constantes.PRESTAMO_SOLICITADO);
        }
        if (p.getFechaSolicitud() == null) {
            p.setFechaSolicitud(FechaUtil.hoy());
        }
        String sql = "INSERT INTO prestamo (id_cliente, id_tipo, id_usuario, monto, plazo_meses, tasa, cuota, "
                + "fecha_solicitud, fecha_aprobacion, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignar(ps, p);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                p.setIdPrestamo(JdbcUtil.idGenerado(ps));
            }
            return ok;
        } catch (SQLException e) {
            throw JdbcUtil.error("registrar el préstamo", e);
        }
    }

    @Override
    public boolean actualizar(Prestamo p) {
        String sql = "UPDATE prestamo SET id_cliente = ?, id_tipo = ?, id_usuario = ?, monto = ?, plazo_meses = ?, tasa = ?, "
                + "cuota = ?, fecha_solicitud = ?, fecha_aprobacion = ?, estado = ? WHERE id_prestamo = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, p);
            ps.setInt(11, p.getIdPrestamo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.error("actualizar el préstamo", e);
        }
    }

    /** Elimina un préstamo. Solo es posible si todavía no tiene cuotas (no fue aprobado). */
    @Override
    public boolean eliminar(int id) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM prestamo WHERE id_prestamo = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.error("eliminar el préstamo", e);
        }
    }

    @Override
    public Prestamo buscarPorId(int id) {
        List<Prestamo> lista = consultar(SELECT_BASE + "WHERE p.id_prestamo = ?", id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    /** Todos los préstamos, del más reciente al más antiguo. */
    @Override
    public List<Prestamo> listar() {
        return consultar(SELECT_BASE + "ORDER BY p.id_prestamo DESC");
    }

    /** Préstamos de un cliente. */
    public List<Prestamo> listarPorCliente(int idCliente) {
        return consultar(SELECT_BASE + "WHERE p.id_cliente = ? ORDER BY p.id_prestamo DESC", idCliente);
    }

    /**
     * Préstamos en un estado.
     *
     * @param estado ver Constantes.PRESTAMO_*; null o vacío = todos
     */
    public List<Prestamo> listarPorEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return listar();
        }
        return consultar(SELECT_BASE + "WHERE p.estado = ? ORDER BY p.id_prestamo DESC", estado);
    }

    /** Cambia solo el estado (rechazar, pasar a EN_MORA, CANCELADO, etc.). */
    public boolean actualizarEstado(int idPrestamo, String estado) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("UPDATE prestamo SET estado = ? WHERE id_prestamo = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, idPrestamo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.error("actualizar el estado del préstamo", e);
        }
    }

    /**
     * Aprueba un préstamo y guarda su plan de pagos en una sola transacción:
     * si algo falla, no queda ni el préstamo aprobado ni cuotas a medias.
     * <p>
     * Antes de llamar, el servicio debe llenar en {@code prestamo}: tasa, cuota,
     * fechaAprobacion y estado (normalmente ACTIVO); y generar las cuotas.
     *
     * @param prestamo préstamo con los datos de aprobación
     * @param cuotas   plan de pagos generado por AmortizacionService
     * @return true si todo se guardó
     */
    public boolean aprobarConPlan(Prestamo prestamo, List<Cuota> cuotas) {
        String sql = "UPDATE prestamo SET tasa = ?, cuota = ?, fecha_aprobacion = ?, estado = ? WHERE id_prestamo = ?";
        Connection cn = null;
        try {
            cn = Conexion.getConexion();
            cn.setAutoCommit(false);
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                JdbcUtil.setDecimal(ps, 1, prestamo.getTasa());
                JdbcUtil.setDecimal(ps, 2, prestamo.getCuota());
                ps.setDate(3, FechaUtil.aSql(prestamo.getFechaAprobacion()));
                ps.setString(4, prestamo.getEstado());
                ps.setInt(5, prestamo.getIdPrestamo());
                if (ps.executeUpdate() == 0) {
                    throw new SQLException("No existe el préstamo " + prestamo.getIdPrestamo());
                }
            }
            for (Cuota c : cuotas) {
                c.setIdPrestamo(prestamo.getIdPrestamo());
            }
            cuotaDAO.insertarLote(cn, cuotas);
            cn.commit();
            return true;
        } catch (SQLException e) {
            JdbcUtil.rollback(cn);
            throw JdbcUtil.error("aprobar el préstamo", e);
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

    /**
     * Cantidad de préstamos por estado (para el dashboard).
     *
     * @return mapa estado → cantidad, por ejemplo {ACTIVO=12, EN_MORA=3}
     */
    public Map<String, Integer> contarPorEstado() {
        Map<String, Integer> conteo = new LinkedHashMap<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("SELECT estado, COUNT(*) AS total FROM prestamo GROUP BY estado ORDER BY estado");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                conteo.put(rs.getString("estado"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            throw JdbcUtil.error("contar los préstamos", e);
        }
        return conteo;
    }

    /**
     * Suma de los montos prestados en un estado (por ejemplo, cartera ACTIVA).
     *
     * @return total, o 0 si no hay préstamos en ese estado
     */
    public BigDecimal sumarMontoPorEstado(String estado) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("SELECT COALESCE(SUM(monto), 0) FROM prestamo WHERE estado = ?")) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            throw JdbcUtil.error("sumar los montos de préstamos", e);
        }
    }

    private List<Prestamo> consultar(String sql, Object... parametros) {
        List<Prestamo> lista = new ArrayList<>();
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
            throw JdbcUtil.error("consultar los préstamos", e);
        }
        return lista;
    }

    private void asignar(PreparedStatement ps, Prestamo p) throws SQLException {
        ps.setInt(1, p.getIdCliente());
        ps.setInt(2, p.getIdTipo());
        ps.setInt(3, p.getIdUsuario());
        JdbcUtil.setDecimal(ps, 4, p.getMonto());
        ps.setInt(5, p.getPlazoMeses());
        JdbcUtil.setDecimal(ps, 6, p.getTasa());
        JdbcUtil.setDecimal(ps, 7, p.getCuota());
        ps.setDate(8, FechaUtil.aSql(p.getFechaSolicitud()));
        ps.setDate(9, FechaUtil.aSql(p.getFechaAprobacion()));
        ps.setString(10, p.getEstado());
    }

    private Prestamo mapear(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo(
                rs.getInt("id_prestamo"),
                rs.getInt("id_cliente"),
                rs.getInt("id_tipo"),
                rs.getInt("id_usuario"),
                rs.getBigDecimal("monto"),
                rs.getInt("plazo_meses"),
                rs.getBigDecimal("tasa"),
                rs.getBigDecimal("cuota"),
                FechaUtil.desdeSql(rs.getDate("fecha_solicitud")),
                FechaUtil.desdeSql(rs.getDate("fecha_aprobacion")),
                rs.getString("estado"));
        p.setNombreCliente(rs.getString("nombre_cliente"));
        p.setDniCliente(rs.getString("dni_cliente"));
        p.setNombreTipo(rs.getString("nombre_tipo"));
        p.setNombreUsuario(rs.getString("nombre_usuario"));
        return p;
    }
}
