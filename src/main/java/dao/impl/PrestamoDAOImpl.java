package dao.impl;

import dao.PrestamoDAO;
import excepciones.PersistenciaException;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import modelo.Cliente;
import modelo.Prestamo;
import modelo.TipoPrestamo;
import modelo.enums.EstadoPrestamo;

public class PrestamoDAOImpl extends CrediDAOBase implements PrestamoDAO {

    /** La vista trae el préstamo con cliente, tipo, usuario y el resumen de pagos. */
    private static final String SELECT = "SELECT * FROM dbo.vw_PrestamosResumen ";

    private Prestamo mapear(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo();
        p.setId(rs.getInt("prestamo_id"));
        p.setNumeroPrestamo(rs.getString("numero_prestamo"));

        Cliente c = new Cliente();
        c.setId(rs.getInt("cliente_id"));
        c.setIdentidad(rs.getString("identificacion"));
        c.setNombre(rs.getString("cliente_nombre"));
        c.setApellido(rs.getString("cliente_apellido"));
        c.setTelefono(rs.getString("telefono"));
        p.setCliente(c);

        p.setTipoPrestamo(new TipoPrestamo(rs.getInt("tipo_prestamo_id"), rs.getString("tipo_prestamo")));
        p.setUsuarioId(rs.getInt("usuario_id"));
        p.setRegistradoPor(rs.getString("registrado_por"));
        p.setMonto(rs.getBigDecimal("monto"));
        p.setPlazoMeses(rs.getInt("plazo_meses"));
        p.setTasaInteresAnual(rs.getBigDecimal("tasa_interes_anual"));
        p.setPorcentajeMora(rs.getBigDecimal("porcentaje_mora"));
        p.setCuotaMensual(rs.getBigDecimal("cuota_mensual"));
        p.setDestino(rs.getString("destino"));
        p.setEstado(EstadoPrestamo.valueOf(rs.getString("estado")));
        p.setFechaSolicitud(fecha(rs, "fecha_solicitud"));
        p.setFechaAprobacion(fecha(rs, "fecha_aprobacion"));
        p.setFechaDesembolso(fecha(rs, "fecha_desembolso"));
        p.setObservaciones(rs.getString("observaciones"));
        p.setActivo(rs.getBoolean("activo"));
        p.setFechaCreacion(fechaHora(rs, "fecha_registro"));

        p.setTotalAPagar(rs.getBigDecimal("total_a_pagar"));
        p.setTotalPagado(rs.getBigDecimal("total_pagado"));
        p.setMoraPagada(rs.getBigDecimal("mora_pagada"));
        p.setCapitalPendiente(rs.getBigDecimal("capital_pendiente"));
        p.setSaldoPendiente(rs.getBigDecimal("saldo_pendiente"));
        p.setCuotasPagadas(rs.getInt("cuotas_pagadas"));
        p.setCuotasPendientes(rs.getInt("cuotas_pendientes"));
        p.setCuotasVencidas(rs.getInt("cuotas_vencidas"));
        p.setProximoVencimiento(fecha(rs, "proximo_vencimiento"));
        return p;
    }

    @Override
    public int registrarSolicitud(Prestamo p) throws PersistenciaException {
        try (Connection cn = obtenerConexion(); CallableStatement cs = llamada(cn, "sp_RegistrarSolicitud", 8)) {
            cs.setInt(1, p.getCliente().getId());
            cs.setInt(2, p.getTipoPrestamo().getId());
            cs.setInt(3, p.getUsuarioId());
            cs.setBigDecimal(4, p.getMonto());
            cs.setInt(5, p.getPlazoMeses());
            String destino = vacioANull(p.getDestino());
            if (destino == null) cs.setNull(6, Types.VARCHAR); else cs.setString(6, destino);
            if (p.getFechaSolicitud() == null) cs.setNull(7, Types.DATE); else cs.setDate(7, Date.valueOf(p.getFechaSolicitud()));
            cs.registerOutParameter(8, Types.INTEGER);
            cs.execute();
            int id = cs.getInt(8);
            p.setId(id);
            return id;
        } catch (SQLException e) {
            throw error("Error al registrar la solicitud", e);
        }
    }

    @Override
    public boolean actualizarSolicitud(Prestamo p) throws PersistenciaException {
        // La tasa y la mora se vuelven a copiar del tipo por si cambió de tipo de préstamo
        String sql = "UPDATE p SET cliente_id = ?, tipo_prestamo_id = t.tipo_prestamo_id, monto = ?, plazo_meses = ?, "
                   + "destino = ?, tasa_interes_anual = t.tasa_interes_anual, porcentaje_mora = t.porcentaje_mora "
                   + "FROM dbo.Prestamos p INNER JOIN dbo.TiposPrestamo t ON t.tipo_prestamo_id = ? "
                   + "WHERE p.prestamo_id = ? AND p.estado = 'SOLICITADO' AND p.activo = 1";
        return ejecutarUpdate(sql, p.getCliente().getId(), p.getMonto(), p.getPlazoMeses(), vacioANull(p.getDestino()),
                p.getTipoPrestamo().getId(), p.getId()) > 0;
    }

    @Override
    public Optional<Prestamo> buscarPorId(int prestamoId) throws PersistenciaException {
        return consultarUno(SELECT + "WHERE prestamo_id = ?", this::mapear, prestamoId);
    }

    @Override
    public Optional<Prestamo> buscarPorNumero(String numeroPrestamo) throws PersistenciaException {
        return consultarUno(SELECT + "WHERE numero_prestamo = ?", this::mapear,
                numeroPrestamo == null ? null : numeroPrestamo.trim().toUpperCase());
    }

    @Override
    public List<Prestamo> listar() throws PersistenciaException {
        return consultarLista(SELECT + "ORDER BY prestamo_id DESC", this::mapear);
    }

    @Override
    public List<Prestamo> listarPorEstado(EstadoPrestamo estado) throws PersistenciaException {
        if (estado == null) {
            return listar();
        }
        return consultarLista(SELECT + "WHERE estado = ? ORDER BY prestamo_id DESC", this::mapear, estado);
    }

    @Override
    public List<Prestamo> listarPorCliente(int clienteId) throws PersistenciaException {
        return consultarLista(SELECT + "WHERE cliente_id = ? ORDER BY prestamo_id DESC", this::mapear, clienteId);
    }

    @Override
    public List<Prestamo> buscarPorCriterio(String filtro) throws PersistenciaException {
        String like = "%" + (filtro == null ? "" : filtro.trim()) + "%";
        return consultarLista(SELECT + "WHERE numero_prestamo LIKE ? OR identificacion LIKE ? OR cliente LIKE ? "
                + "ORDER BY prestamo_id DESC", this::mapear, like, like, like);
    }

    @Override
    public BigDecimal aprobar(int prestamoId, String observaciones) throws PersistenciaException {
        try (Connection cn = obtenerConexion(); CallableStatement cs = llamada(cn, "sp_AprobarPrestamo", 4)) {
            cs.setInt(1, prestamoId);
            cs.setNull(2, Types.DATE);
            String obs = vacioANull(observaciones);
            if (obs == null) cs.setNull(3, Types.VARCHAR); else cs.setString(3, obs);
            cs.registerOutParameter(4, Types.DECIMAL, 2);
            cs.execute();
            return cs.getBigDecimal(4);
        } catch (SQLException e) {
            throw error("Error al aprobar el préstamo", e);
        }
    }

    @Override
    public void desembolsar(int prestamoId, LocalDate fecha) throws PersistenciaException {
        try (Connection cn = obtenerConexion(); CallableStatement cs = llamada(cn, "sp_DesembolsarPrestamo", 2)) {
            cs.setInt(1, prestamoId);
            if (fecha == null) cs.setNull(2, Types.DATE); else cs.setDate(2, Date.valueOf(fecha));
            cs.execute();
        } catch (SQLException e) {
            throw error("Error al desembolsar el préstamo", e);
        }
    }

    @Override
    public void cambiarEstado(int prestamoId, EstadoPrestamo nuevo, String observaciones) throws PersistenciaException {
        try (Connection cn = obtenerConexion(); CallableStatement cs = llamada(cn, "sp_CambiarEstadoPrestamo", 3)) {
            cs.setInt(1, prestamoId);
            cs.setString(2, nuevo.name());
            String obs = vacioANull(observaciones);
            if (obs == null) cs.setNull(3, Types.VARCHAR); else cs.setString(3, obs);
            cs.execute();
        } catch (SQLException e) {
            throw error("Error al cambiar el estado del préstamo", e);
        }
    }

    @Override
    public boolean desactivar(int prestamoId) throws PersistenciaException {
        return ejecutarUpdate("UPDATE dbo.Prestamos SET activo = 0 WHERE prestamo_id = ?", prestamoId) > 0;
    }

    @Override
    public int[] actualizarMora() throws PersistenciaException {
        try (Connection cn = obtenerConexion(); CallableStatement cs = llamada(cn, "sp_ActualizarMora", 3)) {
            cs.setNull(1, Types.DATE);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();
            return new int[]{cs.getInt(2), cs.getInt(3)};
        } catch (SQLException e) {
            throw error("Error al actualizar la mora", e);
        }
    }

    @Override
    public BigDecimal calcularCuota(BigDecimal monto, BigDecimal tasaAnual, int plazoMeses) throws PersistenciaException {
        return consultarUno("SELECT dbo.fn_CalcularCuota(?, ?, ?)", rs -> rs.getBigDecimal(1), monto, tasaAnual, plazoMeses)
                .orElse(null);
    }
}
