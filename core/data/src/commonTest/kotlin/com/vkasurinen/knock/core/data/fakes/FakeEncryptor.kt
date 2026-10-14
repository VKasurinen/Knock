package com.vkasurinen.knock.core.data.fakes

import com.vkasurinen.knock.core.data.security.Encryptor

/**
 * XORs every byte, so the output is unreadable but reversible.
 */
class FakeEncryptor: Encryptor {

    var failOnDecrypt = false
    var decryptCount = 0
        private set

    override fun encrypt(plain: ByteArray): ByteArray {
        return plain.xor()
    }

    override fun decrypt(encrypted: ByteArray): ByteArray {
        decryptCount++
        if(failOnDecrypt) {
            throw IllegalStateException("Key not available")
        }
        return encrypted.xor()
    }

    private fun ByteArray.xor(): ByteArray {
        return ByteArray(size) { index -> (this[index].toInt() xor 0x5A).toByte() }
    }
}
