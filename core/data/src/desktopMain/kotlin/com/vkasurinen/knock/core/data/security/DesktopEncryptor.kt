package com.vkasurinen.knock.core.data.security

import com.vkasurinen.knock.core.data.util.appDataDirectory
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-256-GCM, same output format as KeystoreEncryptor: [12 byte IV][ciphertext + 16 byte auth tag].
 *
 * The JVM has no hardware-backed key storage like the Android Keystore, so the key is kept in a
 * file next to the preferences that only the current OS user can read. This keeps the preferences
 * unreadable when they are copied on their own, but not from someone with access to the user account.
 */
class DesktopEncryptor(
    private val keyFile: File = File(appDataDirectory, KEY_FILE_NAME)
): Encryptor {

    @Volatile
    private var secretKey: SecretKey? = null

    override fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        return cipher.iv + cipher.doFinal(plain)
    }

    override fun decrypt(encrypted: ByteArray): ByteArray {
        val iv = encrypted.copyOfRange(0, IV_SIZE)
        val cipherText = encrypted.copyOfRange(IV_SIZE, encrypted.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_SIZE_BITS, iv))
        return cipher.doFinal(cipherText)
    }

    @Synchronized
    private fun getOrCreateKey(): SecretKey {
        secretKey?.let { return it }

        val existingKey = keyFile
            .takeIf { it.exists() }
            ?.readBytes()
            ?.takeIf { it.size == KEY_SIZE_BYTES }
        if(existingKey != null) {
            return SecretKeySpec(existingKey, ALGORITHM).also { secretKey = it }
        }

        val keyBytes = ByteArray(KEY_SIZE_BYTES).also { SecureRandom().nextBytes(it) }
        writeKeyFile(keyBytes)
        return SecretKeySpec(keyBytes, ALGORITHM).also { secretKey = it }
    }

    private fun writeKeyFile(keyBytes: ByteArray) {
        keyFile.parentFile?.mkdirs()

        // Restrict the permissions before the key is written, then move it in place in one step
        val tempFile = File(keyFile.parentFile, "$KEY_FILE_NAME.tmp")
        tempFile.delete()
        tempFile.createNewFile()
        restrictToOwner(tempFile)
        tempFile.writeBytes(keyBytes)

        Files.move(
            tempFile.toPath(),
            keyFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE
        )
    }

    private fun restrictToOwner(file: File) {
        val path = file.toPath()
        if(Files.getFileStore(path).supportsFileAttributeView("posix")) {
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rw-------"))
        } else {
            // Windows: %APPDATA% is already private to the user, this drops access for everyone else
            file.setReadable(false, false)
            file.setWritable(false, false)
            file.setReadable(true, true)
            file.setWritable(true, true)
        }
    }

    private companion object {
        const val KEY_FILE_NAME = "prefs.key"
        const val ALGORITHM = "AES"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_SIZE_BYTES = 32
        const val IV_SIZE = 12
        const val TAG_SIZE_BITS = 128
    }
}
