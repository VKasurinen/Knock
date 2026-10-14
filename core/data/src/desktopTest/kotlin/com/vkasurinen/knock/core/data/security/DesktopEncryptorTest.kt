package com.vkasurinen.knock.core.data.security

import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class DesktopEncryptorTest {

    private lateinit var directory: File
    private lateinit var keyFile: File

    @BeforeTest
    fun setUp() {
        directory = Files.createTempDirectory("knock-encryptor").toFile()
        keyFile = File(directory, "prefs.key")
    }

    @AfterTest
    fun tearDown() {
        directory.deleteRecursively()
    }

    @Test
    fun encrypt_thenDecrypt_returnsOriginal() {
        val encryptor = DesktopEncryptor(keyFile)
        val plain = "secret token".encodeToByteArray()

        val encrypted = encryptor.encrypt(plain)

        assertFalse(encrypted.contentEquals(plain))
        assertContentEquals(plain, encryptor.decrypt(encrypted))
    }

    @Test
    fun newInstance_reusesKeyFromFile() {
        val plain = "secret token".encodeToByteArray()
        val encrypted = DesktopEncryptor(keyFile).encrypt(plain)

        val decrypted = DesktopEncryptor(keyFile).decrypt(encrypted)

        assertContentEquals(plain, decrypted)
    }

    @Test
    fun decrypt_withDifferentKey_fails() {
        val encrypted = DesktopEncryptor(keyFile).encrypt("secret token".encodeToByteArray())
        val otherEncryptor = DesktopEncryptor(File(directory, "other.key"))

        assertFailsWith<Exception> {
            otherEncryptor.decrypt(encrypted)
        }
    }

    @Test
    fun keyFile_isOnlyAccessibleByOwner() {
        DesktopEncryptor(keyFile).encrypt(byteArrayOf(1))

        val path = keyFile.toPath()
        if(Files.getFileStore(path).supportsFileAttributeView("posix")) {
            assertEquals(
                setOf(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE),
                Files.getPosixFilePermissions(path)
            )
        }
    }
}
