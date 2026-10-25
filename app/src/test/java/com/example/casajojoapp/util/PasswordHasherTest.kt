package com.example.casajojoapp.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Hash de contraseñas: nunca texto plano, salt único por hash y
 * verificación exacta (la contraseña es la única clave válida).
 */
class PasswordHasherTest {

    @Test
    fun verificaLaContrasenaCorrectaYRechazaLaIncorrecta() {
        val hash = PasswordHasher.hash("casa-jojo-2026")

        assertTrue(PasswordHasher.verificar("casa-jojo-2026", hash.saltHex, hash.hashHex))
        assertFalse(PasswordHasher.verificar("casa-jojo-2027", hash.saltHex, hash.hashHex))
        assertFalse(PasswordHasher.verificar("", hash.saltHex, hash.hashHex))
    }

    @Test
    fun laMismaContrasenaGeneraHashesDistintosPorElSalt() {
        val a = PasswordHasher.hash("misma-clave")
        val b = PasswordHasher.hash("misma-clave")

        assertNotEquals(a.saltHex, b.saltHex)
        assertNotEquals(a.hashHex, b.hashHex)
        // Y un hash no verifica contra el salt del otro.
        assertFalse(PasswordHasher.verificar("misma-clave", a.saltHex, b.hashHex))
    }

    @Test
    fun losHashesNoContienenLaContrasenaEnClaro() {
        val secreto = "clave-super-secreta-123"
        val hash = PasswordHasher.hash(secreto)

        assertFalse(hash.hashHex.contains(secreto))
        assertFalse(hash.saltHex.contains(secreto))
        // Formato hex y largo esperado (32 bytes = 64 caracteres).
        assertTrue(hash.hashHex.matches(Regex("[0-9a-f]{64}")))
        assertTrue(hash.saltHex.matches(Regex("[0-9a-f]{32}")))
    }
}
