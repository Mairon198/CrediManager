package filtro;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

/**
 * Protege las rutas: si no hay sesión (util.Sesion.activa) redirige a /login y,
 * si el rol no tiene permiso para la ruta (util.Sesion.tieneRol), muestra la
 * página de acceso denegado.
 * <p>
 * Esqueleto creado por Integrante 1. Responsable: Integrante 2.
 * Por ahora deja pasar todo. Al implementarlo, agregar {@code @WebFilter("/*")}
 * y excluir /login y /assets.
 */
public class FiltroSesion implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // TODO Integrante 2: validar sesión y rol
        chain.doFilter(request, response);
    }
}
