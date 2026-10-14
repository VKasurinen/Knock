package com.vkasurinen.knock.core.data.security

/**
 * Encrypts and decrypts data with a key that is kept in the platform's secure key storage
 * (Android Keystore / iOS Keychain), so the key itself never touches app storage.
 * Desktop has no such storage, so there the key is kept in a file only the OS user can read.
 */
interface Encryptor {
    fun encrypt(plain: ByteArray): ByteArray
    fun decrypt(encrypted: ByteArray): ByteArray
}
