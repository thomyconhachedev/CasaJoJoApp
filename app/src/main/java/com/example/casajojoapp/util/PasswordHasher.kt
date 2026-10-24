package com.example.casajojoapp.util

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// =========================================================
// Hash de contraseñas con PBKDF2-HMAC-SHA256 (disponible desde API 26;
// el minSdk es 28). En el móvil NO se guarda jamás la contraseña en claro:
// la entidad UsuarioEntity solo persiste salt + hash.
// NOTA: esto protege la base local. En el backend (Spring Boot) se usa
// BCrypt vía Spring Security — ver docs/backend_login_paso_a_paso.md.
// =========================================================
object PasswordHasher {

    private const val ITERACIONES = 120_000
    private const val LARGO_SALT_BYTES = 16
    private const val LARGO_HASH_BYTES = 32

    data class Hash(val saltHex: String, val hashHex: String)

    fun hash(password: String): Hash {
        val salt = ByteArray(LARGO_SALT_BYTES).also { SecureRandom().nextBytes(it) }
        return Hash(saltHex = salt.toHex(), hashHex = derivar(password, salt).toHex())
    }

    fun verificar(password: String, saltHex: String, hashHex: String): Boolean {
        val salt = saltHex.hexToBytes() ?: return false
        val esperado = hashHex.hexToBytes() ?: return false
        return derivar(password, salt).contentEquals(esperado)
    }

    private fun derivar(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERACIONES, LARGO_HASH_BYTES * 8)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun ByteArray.toHex(): String =
        joinToString(separator = "") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray? {
        if (length % 2 != 0) return null
        return try {
            ByteArray(length / 2) { i ->
                substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
        } catch (_: NumberFormatException) {
            null
        }
    }
}
