package dao.impl;

import dao.ReporteDAO;
import excepciones.PersistenciaException;
import java.util.List;
import modelo.ResumenCartera;
import modelo.enums.EstadoPrestamo;

public class ReporteDAOImpl extends CrediDAOBase implements ReporteDAO {

    @Override
    public List<ResumenCartera> carteraPorEstado() throws PersistenciaException {
        return consultarLista("SELECT estado, cantidad, monto_prestado, saldo_pendiente FROM dbo.vw_CarteraPorEstado "
                + "ORDER BY secuencia", rs -> {
                    ResumenCartera r = new ResumenCartera();
                    r.setEstado(EstadoPrestamo.valueOf(rs.getString("estado")));
                    r.setCantidad(rs.getInt("cantidad"));
                    r.setMontoPrestado(rs.getBigDecimal("monto_prestado"));
                    r.setSaldoPendiente(rs.getBigDecimal("saldo_pendiente"));
                    return r;
                });
    }

    @Override
    public int contarClientesActivos() throws PersistenciaException {
        return consultarUno("SELECT COUNT(*) FROM dbo.Clientes WHERE activo = 1", rs -> rs.getInt(1)).orElse(0);
    }
}
