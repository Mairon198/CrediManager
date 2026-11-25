package dao.impl;

import dao.PagoDAO;
import excepciones.PersistenciaException;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import modelo.Pago;
import modelo.enums.MetodoPago;

public class PagoDAOImpl extends CrediDAOBase implements PagoDAO {

    private static final String SELECT = "SELECT * FROM dbo.vw_PagosDetalle ";

    private Pago mapear(ResultSet rs) throws SQLException {
        Pago p = new Pago();
        p.setId(rs.getInt("pago_id"));
        p.setNumeroRecibo(rs.getString("numero_recibo"));
        p.setFecha(fechaHora(rs, "fecha_pago"));
        p.setPrestamoId(rs.getInt("prestamo_id"));
        p.setNumeroPrestamo(rs.getString("numero_prestamo"));
        p.setIdentidadCliente(rs.getString("identificacion"));
        p.setCliente(rs.getString("cliente"));
        p.setCuotaId(rs.getInt("cuota_id"));
        p.setNumeroCuota(rs.getInt("numero_cuota"));
        p.setFechaVencimiento(fecha(rs, "fecha_vencimiento"));
        p.setDiasAtraso(rs.getInt("dias_atraso"));
        p.setMontoCuota(rs.getBigDecimal("monto_cuota"));
        p.setMora(rs.getBigDecimal("mora"));
        p.setTotal(rs.getBigDecimal("total"));
        p.setMetodo(MetodoPago.valueOf(rs.getString("metodo")));
        p.setReferencia(rs.getString("referencia"));
        p.setUsuarioId(rs.getInt("usuario_id"));
        p.setCajero(rs.getString("cajero"));
        return p;
    }

    @Override
    public Pago registrarPago(int prestamoId, int usuarioId, MetodoPago metodo, String referencia) throws PersistenciaException {
        int pagoId;
        try (Connection cn = obtenerConexion(); CallableStatement cs = llamada(cn, "sp_RegistrarPago", 7)) {
            cs.setInt(1, prestamoId);
            cs.setInt(2, usuarioId);
            cs.setString(3, metodo.name());
            String ref = vacioANull(referencia);
            if (ref == null) cs.setNull(4, Types.VARCHAR); else cs.setString(4, ref);
            cs.setNull(5, Types.DATE);                       // fecha de pago = hoy
            cs.registerOutParameter(6, Types.INTEGER);
            cs.registerOutParameter(7, Types.DECIMAL, 2);
            cs.execute();
            pagoId = cs.getInt(6);
        } catch (SQLException e) {
            throw error("Error al registrar el pago", e);
        }
        return buscarPorId(pagoId)
                .orElseThrow(() -> new PersistenciaException("El pago se registró pero no se pudo leer el recibo."));
    }

    @Override
    public BigDecimal calcularMora(int prestamoId, LocalDate fecha) throws PersistenciaException {
        LocalDate dia = fecha == null ? LocalDate.now() : fecha;
        String sql = "SELECT TOP (1) dbo.fn_CalcularMora(c.monto_cuota, p.porcentaje_mora, "
                   + "CASE WHEN ? > c.fecha_vencimiento THEN DATEDIFF(DAY, c.fecha_vencimiento, ?) ELSE 0 END) "
                   + "FROM dbo.Cuotas c INNER JOIN dbo.Prestamos p ON p.prestamo_id = c.prestamo_id "
                   + "WHERE c.prestamo_id = ? AND c.estado <> 'PAGADA' ORDER BY c.numero_cuota";
        return consultarUno(sql, rs -> rs.getBigDecimal(1), dia, dia, prestamoId).orElse(BigDecimal.ZERO);
    }

    @Override
    public Optional<Pago> buscarPorId(int pagoId) throws PersistenciaException {
        return consultarUno(SELECT + "WHERE pago_id = ?", this::mapear, pagoId);
    }

    @Override
    public Optional<Pago> buscarPorRecibo(String numeroRecibo) throws PersistenciaException {
        return consultarUno(SELECT + "WHERE numero_recibo = ?", this::mapear,
                numeroRecibo == null ? null : numeroRecibo.trim().toUpperCase());
    }

    @Override
    public List<Pago> listarPorPrestamo(int prestamoId) throws PersistenciaException {
        return consultarLista(SELECT + "WHERE prestamo_id = ? ORDER BY numero_cuota", this::mapear, prestamoId);
    }

    @Override
    public List<Pago> listarEntre(LocalDate desde, LocalDate hasta) throws PersistenciaException {
        // fecha_pago es fecha y hora: se toma hasta antes del día siguiente para incluir todo "hasta"
        return consultarLista(SELECT + "WHERE fecha_pago >= ? AND fecha_pago < ? ORDER BY fecha_pago, pago_id",
                this::mapear, desde, hasta.plusDays(1));
    }

    @Override
    public BigDecimal totalCobradoEntre(LocalDate desde, LocalDate hasta) throws PersistenciaException {
        return consultarUno("SELECT ISNULL(SUM(total), 0) FROM dbo.Pagos WHERE fecha_pago >= ? AND fecha_pago < ?",
                rs -> rs.getBigDecimal(1), desde, hasta.plusDays(1)).orElse(BigDecimal.ZERO);
    }
}
