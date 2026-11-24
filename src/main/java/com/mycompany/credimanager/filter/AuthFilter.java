package com.mycompany.credimanager.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

/**
 * Protege las rutas del sistema: si no hay usuario en sesión
 * (Constantes.SESION_USUARIO) redirige a /login, y si el rol no tiene permiso
 * para la ruta, muestra la página de acceso denegado.
 * <p>
 * Esqueleto creado por Integrante 1. Por ahora deja pasar todas las peticiones.
 * Al implementarlo, agregar {@code @WebFilter("/*")} y excluir /login y /assets.
 *
 * @author Integrante 2
 */
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // TODO Integrante 2: validar sesión y rol
        chain.doFilter(request, response);
    }
}
