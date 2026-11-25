package dao.impl;

import dao.CuotaDAO;
import excepciones.PersistenciaException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import modelo.Cuota;
import modelo.enums.EstadoCuota;

public class CuotaDAOImpl extends CrediDAOBase implements CuotaDAO {

    private static final String COLUMNAS = "cuota_id, prestamo_id, numero_cuota, fecha_vencimiento, capital, interes, "
            + "monto_cuota, saldo_restante, estado, fecha_pago";

    private Cuota mapear(ResultSet rs) throws SQLException {
        Cuota c = new Cuota();
        c.setId(rs.getInt("cuota_id"));
        c.setPrestamoId(rs.getInt("prestamo_id"));
        c.setNumeroCuota(rs.getInt("numero_cuota"));
        c.setFechaVencimiento(fecha(rs, "fecha_vencimiento"));
        c.setCapital(rs.getBigDecimal("capital"));
        c.setInteres(rs.getBigDecimal("interes"));
        c.setMontoCuota(rs.getBigDecimal("monto_cuota"));
        c.setSaldoRestante(rs.getBigDecimal("saldo_restante"));
        c.setEstado(EstadoCuota.valueOf(rs.getString("estado")));
        c.setFechaPago(fecha(rs, "fecha_pago"));
        return c;
    }

    /** Fila de vw_CuotasVencidas: la cuota con datos del cliente, días de atraso y mora. */
    private Cuota mapearVencida(ResultSet rs) throws SQLException {
        Cuota c = new Cuota();
        c.setId(rs.getInt("cuota_id"));
        c.setPrestamoId(rs.getInt("prestamo_id"));
        c.setNumeroPrestamo(rs.getString("numero_prestamo"));
        c.setIdentidadCliente(rs.getString("identificacion"));
        c.setCliente(rs.getString("cliente"));
        c.setTelefonoCliente(rs.getString("telefono"));
        c.setNumeroCuota(rs.getInt("numero_cuota"));
        c.setFechaVencimiento(fecha(rs, "fecha_vencimiento"));
        c.setDiasAtraso(rs.getInt("dias_atraso"));
        c.setMontoCuota(rs.getBigDecimal("monto_cuota"));
        c.setMoraAlDia(rs.getBigDecimal("mora_al_dia"));
        c.setTotalAPagar(rs.getBigDecimal("total_a_pagar"));
        c.setEstado(EstadoCuota.VENCIDA);
        return c;
    }

    @Override
    public List<Cuota> listarPorPrestamo(int prestamoId) throws PersistenciaException {
        return consultarLista("SELECT " + COLUMNAS + " FROM dbo.Cuotas WHERE prestamo_id = ? ORDER BY numero_cuota",
                this::mapear, prestamoId);
    }

    @Override
    public Optional<Cuota> buscarPorId(int cuotaId) throws PersistenciaException {
        return consultarUno("SELECT " + COLUMNAS + " FROM dbo.Cuotas WHERE cuota_id = ?", this::mapear, cuotaId);
    }

    @Override
    public Optional<Cuota> siguientePendiente(int prestamoId) throws PersistenciaException {
        return consultarUno("SELECT TOP (1) " + COLUMNAS + " FROM dbo.Cuotas WHERE prestamo_id = ? AND estado <> 'PAGADA' "
                + "ORDER BY numero_cuota", this::mapear, prestamoId);
    }

    @Override
    public List<Cuota> listarVencidas() throws PersistenciaException {
        return consultarLista("SELECT * FROM dbo.vw_CuotasVencidas ORDER BY dias_atraso DESC, numero_prestamo",
                this::mapearVencida);
    }
}
