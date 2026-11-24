package com.uniajc.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para la utilidad criptográfica PasswordUtil.
 * Valida la generación y verificación de hashes BCrypt.
 */
public class PasswordUtilTest {

    @Test
    @DisplayName("hashear genera un hash BCrypt válido de 60 caracteres")
    void hashearGeneraHashValido() {
        String hash = PasswordUtil.hashear("claveSecreta123");

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$12$"), "El hash debe iniciar con el prefijo BCrypt $2a$12$");
        assertTrue(hash.length() == 60, "Un hash BCrypt estándar debe medir exactamente 60 caracteres");
    }

    @Test
    @DisplayName("Dos hashes de la misma contraseña son diferentes debido a la sal aleatoria")
    void hashesTienenSalesDiferentes() {
        String hash1 = PasswordUtil.hashear("miPassword");
        String hash2 = PasswordUtil.hashear("miPassword");

        assertFalse(hash1.equals(hash2), "BCrypt debe generar diferentes hashes con sales aleatorias");
    }

    @Test
    @DisplayName("verificar retorna true cuando la contraseña en texto plano coincide con el hash")
    void verificarCoincidenciaExitosa() {
        String clavePlana = "superPassword2026";
        String hash = PasswordUtil.hashear(clavePlana);

        assertTrue(PasswordUtil.verificar(clavePlana, hash));
    }

    @Test
    @DisplayName("verificar retorna false cuando la contraseña no coincide con el hash")
    void verificarClaveIncorrecta() {
        String hash = PasswordUtil.hashear("claveCorrecta");

        assertFalse(PasswordUtil.verificar("claveErronea", hash));
    }

    @Test
    @DisplayName("hashear con contraseña nula o vacía lanza IllegalArgumentException")
    void hashearInvalidoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashear(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashear(""));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashear("   "));
    }

    @Test
    @DisplayName("verificar con hash nulo, vacío o malformado retorna false sin lanzar excepción")
    void verificarConValoresInvalidosNoFalla() {
        assertFalse(PasswordUtil.verificar("123456", null));
        assertFalse(PasswordUtil.verificar("123456", ""));
        assertFalse(PasswordUtil.verificar("123456", "formato_no_bcrypt"));
    }
}
