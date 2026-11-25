package dao.impl;

import dao.GarantiaDAO;
import excepciones.PersistenciaException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import modelo.Garantia;
import modelo.enums.TipoGarantia;

public class GarantiaDAOImpl extends CrediDAOBase implements GarantiaDAO {

    private static final String COLUMNAS = "garantia_id, prestamo_id, tipo, descripcion, valor_estimado, activo, fecha_registro";

    private Garantia mapear(ResultSet rs) throws SQLException {
        Garantia g = new Garantia();
        g.setId(rs.getInt("garantia_id"));
        g.setPrestamoId(rs.getInt("prestamo_id"));
        g.setTipo(TipoGarantia.valueOf(rs.getString("tipo")));
        g.setDescripcion(rs.getString("descripcion"));
        g.setValorEstimado(rs.getBigDecimal("valor_estimado"));
        g.setActivo(rs.getBoolean("activo"));
        g.setFechaCreacion(fechaHora(rs, "fecha_registro"));
        return g;
    }

    @Override
    public Integer insertar(Garantia g) throws PersistenciaException {
        String sql = "INSERT INTO dbo.Garantias (prestamo_id, tipo, descripcion, valor_estimado, activo) VALUES (?, ?, ?, ?, ?)";
        int id = ejecutarInsertConClave(sql, g.getPrestamoId(), g.getTipo(), g.getDescripcion().trim(), g.getValorEstimado(),
                g.isActivo());
        g.setId(id);
        return id;
    }

    @Override
    public boolean actualizar(Garantia g) throws PersistenciaException {
        String sql = "UPDATE dbo.Garantias SET tipo = ?, descripcion = ?, valor_estimado = ?, activo = ? WHERE garantia_id = ?";
        return ejecutarUpdate(sql, g.getTipo(), g.getDescripcion().trim(), g.getValorEstimado(), g.isActivo(), g.getId()) > 0;
    }

    @Override
    public Optional<Garantia> buscarPorId(Integer id) throws PersistenciaException {
        return consultarUno("SELECT " + COLUMNAS + " FROM dbo.Garantias WHERE garantia_id = ?", this::mapear, id);
    }

    @Override
    public List<Garantia> listar() throws PersistenciaException {
        return consultarLista("SELECT " + COLUMNAS + " FROM dbo.Garantias ORDER BY garantia_id DESC", this::mapear);
    }

    @Override
    public List<Garantia> listarPorPrestamo(int prestamoId) throws PersistenciaException {
        return consultarLista("SELECT " + COLUMNAS + " FROM dbo.Garantias WHERE prestamo_id = ? AND activo = 1 "
                + "ORDER BY garantia_id", this::mapear, prestamoId);
    }

    @Override
    public boolean desactivar(Integer id) throws PersistenciaException {
        return ejecutarUpdate("UPDATE dbo.Garantias SET activo = 0 WHERE garantia_id = ?", id) > 0;
    }
}
