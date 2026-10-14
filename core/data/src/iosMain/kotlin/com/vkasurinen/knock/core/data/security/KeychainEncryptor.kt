package com.vkasurinen.knock.core.data.security

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFErrorRefVar
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFNumberCreate
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFNumberIntType
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.NSLock
import platform.Security.SecItemCopyMatching
import platform.Security.SecKeyCopyPublicKey
import platform.Security.SecKeyCreateDecryptedData
import platform.Security.SecKeyCreateEncryptedData
import platform.Security.SecKeyCreateRandomKey
import platform.Security.SecKeyRef
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrApplicationTag
import platform.Security.kSecAttrIsPermanent
import platform.Security.kSecAttrKeySizeInBits
import platform.Security.kSecAttrKeyType
import platform.Security.kSecAttrKeyTypeECSECPrimeRandom
import platform.Security.kSecClass
import platform.Security.kSecClassKey
import platform.Security.kSecKeyAlgorithmECIESEncryptionCofactorVariableIVX963SHA256AESGCM
import platform.Security.kSecPrivateKeyAttrs
import platform.Security.kSecReturnRef

/**
 * ECIES (P-256 + AES-GCM) with a private key that is stored permanently in the iOS Keychain.
 * The key is only readable on this device after the first unlock and is not part of backups.
 */
@OptIn(ExperimentalForeignApi::class)
class KeychainEncryptor: Encryptor {

    private val algorithm = kSecKeyAlgorithmECIESEncryptionCofactorVariableIVX963SHA256AESGCM
    private val lock = NSLock()

    override fun encrypt(plain: ByteArray): ByteArray {
        val privateKey = getOrCreatePrivateKey()
        val publicKey = SecKeyCopyPublicKey(privateKey)
        CFRelease(privateKey)
        checkNotNull(publicKey) { "Unable to get public key from Keychain key" }

        return try {
            transform(plain) { data, error ->
                SecKeyCreateEncryptedData(publicKey, algorithm, data, error)
            }
        } finally {
            CFRelease(publicKey)
        }
    }

    override fun decrypt(encrypted: ByteArray): ByteArray {
        val privateKey = getOrCreatePrivateKey()
        return try {
            transform(encrypted) { data, error ->
                SecKeyCreateDecryptedData(privateKey, algorithm, data, error)
            }
        } finally {
            CFRelease(privateKey)
        }
    }

    private fun transform(
        input: ByteArray,
        operation: (CFDataRef?, CPointer<CFErrorRefVar>) -> CFDataRef?
    ): ByteArray = memScoped {
        val error = alloc<CFErrorRefVar>()
        val inputData = input.toCFData()
        val output = try {
            operation(inputData, error.ptr)
        } finally {
            inputData?.let(::CFRelease)
        }
        if(output == null) {
            error.value?.let(::CFRelease)
            throw IllegalStateException("Keychain crypto operation failed")
        }
        try {
            output.toByteArray()
        } finally {
            CFRelease(output)
        }
    }

    private fun getOrCreatePrivateKey(): SecKeyRef {
        lock.lock()
        try {
            return findPrivateKey() ?: createPrivateKey()
        } finally {
            lock.unlock()
        }
    }

    private fun findPrivateKey(): SecKeyRef? = memScoped {
        val tag = KEY_TAG.encodeToByteArray().toCFData()
        val query = cfDictionary(
            kSecClass to kSecClassKey,
            kSecAttrApplicationTag to tag,
            kSecAttrKeyType to kSecAttrKeyTypeECSECPrimeRandom,
            kSecReturnRef to kCFBooleanTrue
        )
        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        CFRelease(query)
        tag?.let(::CFRelease)

        if(status == errSecSuccess) result.value?.reinterpret() else null
    }

    private fun createPrivateKey(): SecKeyRef = memScoped {
        val tag = KEY_TAG.encodeToByteArray().toCFData()
        val keySize = alloc<IntVar>().apply { value = 256 }
        val keySizeNumber = CFNumberCreate(null, kCFNumberIntType, keySize.ptr)

        val privateKeyAttributes = cfDictionary(
            kSecAttrIsPermanent to kCFBooleanTrue,
            kSecAttrApplicationTag to tag,
            kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
        )
        val attributes = cfDictionary(
            kSecAttrKeyType to kSecAttrKeyTypeECSECPrimeRandom,
            kSecAttrKeySizeInBits to keySizeNumber,
            kSecPrivateKeyAttrs to privateKeyAttributes
        )

        val error = alloc<CFErrorRefVar>()
        val key = SecKeyCreateRandomKey(attributes, error.ptr)

        CFRelease(attributes)
        CFRelease(privateKeyAttributes)
        keySizeNumber?.let(::CFRelease)
        tag?.let(::CFRelease)

        if(key == null) {
            error.value?.let(::CFRelease)
            throw IllegalStateException("Unable to create Keychain key")
        }
        key
    }

    private fun cfDictionary(vararg entries: Pair<CFTypeRef?, CFTypeRef?>): CFMutableDictionaryRef {
        val dictionary = CFDictionaryCreateMutable(
            null,
            entries.size.convert(),
            kCFTypeDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr
        )
        checkNotNull(dictionary)
        entries.forEach { (key, value) ->
            CFDictionaryAddValue(dictionary, key, value)
        }
        return dictionary
    }

    private fun ByteArray.toCFData(): CFDataRef? {
        if(isEmpty()) {
            return CFDataCreate(null, null, 0)
        }
        return usePinned { pinned ->
            CFDataCreate(null, pinned.addressOf(0).reinterpret(), size.convert())
        }
    }

    private fun CFDataRef.toByteArray(): ByteArray {
        val length = CFDataGetLength(this).toInt()
        if(length == 0) {
            return ByteArray(0)
        }
        return checkNotNull(CFDataGetBytePtr(this)).readBytes(length)
    }

    private companion object {
        const val KEY_TAG = "com.vkasurinen.knock.prefs_key"
    }
}
