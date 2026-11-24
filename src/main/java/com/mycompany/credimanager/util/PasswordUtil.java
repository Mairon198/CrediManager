package com.mycompany.credimanager.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Genera y verifica hashes de contraseñas con PBKDF2 (incluido en Java, sin librerías extra).
 * <p>
 * El hash se guarda en la columna {@code usuario.contrasena_hash} con el formato
 * {@code iteraciones:sal:hash} (sal y hash en Base64). Nunca se guarda la contraseña en texto plano.
 *
 * <pre>
 * String hash = PasswordUtil.hashear("admin123");        // al crear o cambiar contraseña
 * boolean ok  = PasswordUtil.verificar("admin123", hash); // al iniciar sesión
 * </pre>
 *
 * @author Integrante 1
 */
public final class PasswordUtil {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 65_536;
    private static final int LARGO_SAL = 16;
    private static final int LARGO_HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /**
     * Genera el hash de una contraseña.
     *
     * @param contrasena contraseña en texto plano
     * @return texto con el formato iteraciones:sal:hash, listo para guardar
     */
    public static String hashear(String contrasena) {
        byte[] sal = new byte[LARGO_SAL];
        RANDOM.nextBytes(sal);
        byte[] hash = pbkdf2(contrasena.toCharArray(), sal, ITERACIONES);
        Base64.Encoder b64 = Base64.getEncoder();
        return ITERACIONES + ":" + b64.encodeToString(sal) + ":" + b64.encodeToString(hash);
    }

    /**
     * Comprueba si una contraseña coincide con un hash guardado.
     *
     * @param contrasena     contraseña escrita por el usuario
     * @param hashGuardado   valor de la columna contrasena_hash
     * @return true si coincide
     */
    public static boolean verificar(String contrasena, String hashGuardado) {
        if (contrasena == null || hashGuardado == null) {
            return false;
        }
        String[] partes = hashGuardado.split(":");
        if (partes.length != 3) {
            return false;
        }
        try {
            int iteraciones = Integer.parseInt(partes[0]);
            byte[] sal = Base64.getDecoder().decode(partes[1]);
            byte[] esperado = Base64.getDecoder().decode(partes[2]);
            byte[] calculado = pbkdf2(contrasena.toCharArray(), sal, iteraciones);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] contrasena, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(contrasena, sal, iteraciones, LARGO_HASH_BITS);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo generar el hash de la contraseña", e);
        }
    }

    /**
     * Permite generar hashes para insertar usuarios de prueba en la base de datos.
     * Ejecutar este archivo (Run File) y copiar el resultado en el script SQL.
     */
    public static void main(String[] args) {
        String[] contrasenas = args.length > 0 ? args : new String[]{"admin123", "asesor123", "cajero123"};
        for (String c : contrasenas) {
            System.out.println(c + " -> " + hashear(c));
        }
    }
}
