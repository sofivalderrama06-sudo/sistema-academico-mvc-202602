package com.uniajc.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidad para el cifrado y verificación de contraseñas mediante BCrypt.
 * Aplica una combinacion aleatoria automática para prevenir ataques.
 */
public class PasswordUtil {

    // Costo de procesamiento (12 rondas es el estándar recomendado en producción)
    private static final int LOG_ROUNDS = 12;

    /**
     * Genera un hash BCrypt a partir de una contraseña en texto plano.
     * 
     * @param passwordPlano Contraseña en texto plano a encriptar.
     * @return Hash BCrypt resultante (longitud exacta de 60 caracteres).
     * @throws IllegalArgumentException si la contraseña es nula o vacía.
     */
    public static String hashear(String passwordPlano) {
        if (passwordPlano == null || passwordPlano.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        return BCrypt.hashpw(passwordPlano, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Valida si una contraseña en texto plano coincide con el hash BCrypt guardado.
     * 
     * @param passwordPlano Contraseña ingresada por el usuario en texto plano.
     * @param hashAlmacenado Hash BCrypt previamente almacenado en la base de datos.
     * @return true si coinciden; false si no coinciden o si los argumentos son inválidos.
     */
    public static boolean verificar(String passwordPlano, String hashAlmacenado) {
        if (passwordPlano == null || hashAlmacenado == null || hashAlmacenado.trim().isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(passwordPlano, hashAlmacenado);
        } catch (IllegalArgumentException e) {
            // Se captura si el formato del hash no corresponde a BCrypt
            return false;
        }
    }
}
