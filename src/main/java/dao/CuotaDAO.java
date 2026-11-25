package dao;

import excepciones.PersistenciaException;
import java.util.List;
import java.util.Optional;
import modelo.Cuota;

/** Contrato de persistencia del plan de pagos. Las cuotas las crea sp_DesembolsarPrestamo. */
public interface CuotaDAO {

    List<Cuota> listarPorPrestamo(int prestamoId) throws PersistenciaException;

    Optional<Cuota> buscarPorId(int cuotaId) throws PersistenciaException;

    /** La cuota que toca cobrar (la más antigua sin pagar). */
    Optional<Cuota> siguientePendiente(int prestamoId) throws PersistenciaException;

    /** Cuotas atrasadas con días de atraso y mora al día (vista vw_CuotasVencidas). */
    List<Cuota> listarVencidas() throws PersistenciaException;
}
