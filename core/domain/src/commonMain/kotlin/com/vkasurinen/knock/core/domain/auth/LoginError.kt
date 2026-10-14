package com.vkasurinen.knock.core.domain.auth

import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.Error

sealed interface LoginError: Error {
    /**
     * The credentials were correct but the email isn't verified yet.
     * [verificationEmailSent] is true if the server just sent a new verification email,
     * false if an active link already exists or the resend was rate limited.
     */
    data class EmailNotVerified(val verificationEmailSent: Boolean): LoginError
    data class Remote(val error: DataError.Remote): LoginError
}
