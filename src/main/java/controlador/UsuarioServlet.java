package controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

/**
 * Gestión de usuarios (solo ADMIN).
 * <p>
 * Ruta: {@code /usuarios}. Esqueleto creado por Integrante 1. Responsable: Integrante 2.
 */
@WebServlet(name = "UsuarioServlet", urlPatterns = {"/usuarios"})
public class UsuarioServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // TODO Integrante 2: implementar doGet y doPost
}
