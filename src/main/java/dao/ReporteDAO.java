package dao;

import excepciones.PersistenciaException;
import java.util.List;
import modelo.ResumenCartera;

/** Consultas de resumen para el tablero y los reportes. */
public interface ReporteDAO {

    /** Cantidad de préstamos, monto prestado y saldo pendiente por estado (vw_CarteraPorEstado). */
    List<ResumenCartera> carteraPorEstado() throws PersistenciaException;

    int contarClientesActivos() throws PersistenciaException;
}
