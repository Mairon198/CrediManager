package dao.impl;

import dao.TipoPrestamoDAO;
import excepciones.PersistenciaException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import modelo.TipoPrestamo;

public class TipoPrestamoDAOImpl extends CrediDAOBase implements TipoPrestamoDAO {

    private static final String COLUMNAS = "tipo_prestamo_id, nombre, descripcion, tasa_interes_anual, monto_minimo, "
            + "monto_maximo, plazo_minimo_meses, plazo_maximo_meses, porcentaje_mora, requiere_garantia, activo, fecha_registro";

    private TipoPrestamo mapear(ResultSet rs) throws SQLException {
        TipoPrestamo t = new TipoPrestamo();
        t.setId(rs.getInt("tipo_prestamo_id"));
        t.setNombre(rs.getString("nombre"));
        t.setDescripcion(rs.getString("descripcion"));
        t.setTasaInteresAnual(rs.getBigDecimal("tasa_interes_anual"));
        t.setMontoMinimo(rs.getBigDecimal("monto_minimo"));
        t.setMontoMaximo(rs.getBigDecimal("monto_maximo"));
        t.setPlazoMinimoMeses(rs.getInt("plazo_minimo_meses"));
        t.setPlazoMaximoMeses(rs.getInt("plazo_maximo_meses"));
        t.setPorcentajeMora(rs.getBigDecimal("porcentaje_mora"));
        t.setRequiereGarantia(rs.getBoolean("requiere_garantia"));
        t.setActivo(rs.getBoolean("activo"));
        t.setFechaCreacion(fechaHora(rs, "fecha_registro"));
        return t;
    }

    @Override
    public Integer insertar(TipoPrestamo t) throws PersistenciaException {
        String sql = "INSERT INTO dbo.TiposPrestamo (nombre, descripcion, tasa_interes_anual, monto_minimo, monto_maximo, "
                   + "plazo_minimo_meses, plazo_maximo_meses, porcentaje_mora, requiere_garantia, activo) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int id = ejecutarInsertConClave(sql, t.getNombre().trim(), vacioANull(t.getDescripcion()), t.getTasaInteresAnual(),
                t.getMontoMinimo(), t.getMontoMaximo(), t.getPlazoMinimoMeses(), t.getPlazoMaximoMeses(),
                t.getPorcentajeMora(), t.isRequiereGarantia(), t.isActivo());
        t.setId(id);
        return id;
    }

    /** Cambiar la tasa no afecta los préstamos ya registrados (guardan su propia tasa). */
    @Override
    public boolean actualizar(TipoPrestamo t) throws PersistenciaException {
        String sql = "UPDATE dbo.TiposPrestamo SET nombre = ?, descripcion = ?, tasa_interes_anual = ?, monto_minimo = ?, "
                   + "monto_maximo = ?, plazo_minimo_meses = ?, plazo_maximo_meses = ?, porcentaje_mora = ?, "
                   + "requiere_garantia = ?, activo = ? WHERE tipo_prestamo_id = ?";
        return ejecutarUpdate(sql, t.getNombre().trim(), vacioANull(t.getDescripcion()), t.getTasaInteresAnual(),
                t.getMontoMinimo(), t.getMontoMaximo(), t.getPlazoMinimoMeses(), t.getPlazoMaximoMeses(),
                t.getPorcentajeMora(), t.isRequiereGarantia(), t.isActivo(), t.getId()) > 0;
    }

    @Override
    public Optional<TipoPrestamo> buscarPorId(Integer id) throws PersistenciaException {
        return consultarUno("SELECT " + COLUMNAS + " FROM dbo.TiposPrestamo WHERE tipo_prestamo_id = ?", this::mapear, id);
    }

    @Override
    public List<TipoPrestamo> listar() throws PersistenciaException {
        return consultarLista("SELECT " + COLUMNAS + " FROM dbo.TiposPrestamo ORDER BY nombre", this::mapear);
    }

    @Override
    public List<TipoPrestamo> listarActivos() throws PersistenciaException {
        return consultarLista("SELECT " + COLUMNAS + " FROM dbo.TiposPrestamo WHERE activo = 1 ORDER BY nombre", this::mapear);
    }

    @Override
    public boolean desactivar(Integer id) throws PersistenciaException {
        return ejecutarUpdate("UPDATE dbo.TiposPrestamo SET activo = 0 WHERE tipo_prestamo_id = ?", id) > 0;
    }

    @Override
    public boolean existeNombre(String nombre, Integer excluirId) throws PersistenciaException {
        int excluir = excluirId == null ? 0 : excluirId;
        return consultarUno("SELECT COUNT(*) FROM dbo.TiposPrestamo WHERE nombre = ? AND tipo_prestamo_id <> ?",
                rs -> rs.getInt(1), nombre.trim(), excluir).map(c -> c > 0).orElse(false);
    }
}
