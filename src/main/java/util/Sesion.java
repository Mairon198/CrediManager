package util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import modelo.Usuario;
import modelo.enums.Rol;

/**
 * Usuario que inició sesión.
 * <p>
 * En una aplicación web cada navegador tiene su propia sesión (HttpSession),
 * así que el usuario se guarda ahí y no en una variable estática.
 */
public final class Sesion {

    public static final String USUARIO = "usuarioSesion";
    public static final String INICIO = "inicioSesion";
    /** Mensaje de éxito para mostrar una sola vez en la siguiente vista. */
    public static final String MENSAJE = "mensaje";
    /** Mensaje de error para mostrar una sola vez en la siguiente vista. */
    public static final String ERROR = "error";

    private Sesion() {
    }

    /** Guarda el usuario en una sesión nueva (descarta la anterior para evitar robo de sesión). */
    public static void iniciar(HttpServletRequest request, Usuario u) {
        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        HttpSession sesion = request.getSession(true);
        sesion.setAttribute(USUARIO, u);
        sesion.setAttribute(INICIO, LocalDateTime.now());
    }

    public static void cerrar(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
    }

    /** @return el usuario que inició sesión, o null si no hay sesión */
    public static Usuario actual(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        return sesion == null ? null : (Usuario) sesion.getAttribute(USUARIO);
    }

    public static boolean activa(HttpServletRequest request) {
        return actual(request) != null;
    }

    /** @return true si el usuario de la sesión tiene alguno de los roles indicados */
    public static boolean tieneRol(HttpServletRequest request, Rol... roles) {
        Usuario u = actual(request);
        if (u == null || u.getRol() == null) {
            return false;
        }
        for (Rol r : roles) {
            if (u.getRol() == r) {
                return true;
            }
        }
        return false;
    }

    /** Nombre para registrar en operaciones y bitácoras. */
    public static String nombreUsuario(HttpServletRequest request) {
        Usuario u = actual(request);
        return u == null ? null : u.getNombreUsuario();
    }
}
