package com.vkasurinen.knock.core.data.auth

import com.vkasurinen.knock.core.data.fakes.FakeEncryptor
import com.vkasurinen.knock.core.data.fakes.FakeLogger
import com.vkasurinen.knock.core.data.fakes.FakePreferencesDataStore
import com.vkasurinen.knock.core.data.security.EncryptedPreferences
import com.vkasurinen.knock.core.domain.auth.AuthInfo
import com.vkasurinen.knock.core.domain.auth.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DataStoreSessionStorageTest {

    private val sessionStorage = DataStoreSessionStorage(
        EncryptedPreferences(FakePreferencesDataStore(), FakeEncryptor(), FakeLogger)
    )

    private val authInfo = AuthInfo(
        accessToken = "access",
        refreshToken = "refresh",
        user = User(
            id = "user-id",
            email = "test@knock.chat",
            username = "tester",
            hasVerifiedEmail = true,
            profilePictureUrl = null
        )
    )

    @Test
    fun set_thenObserve_returnsSameAuthInfo() = runTest {
        sessionStorage.set(authInfo)

        assertEquals(authInfo, sessionStorage.observeAuthInfo().first())
    }

    @Test
    fun set_withNull_clearsSession() = runTest {
        sessionStorage.set(authInfo)

        sessionStorage.set(null)

        assertNull(sessionStorage.observeAuthInfo().first())
    }
}
