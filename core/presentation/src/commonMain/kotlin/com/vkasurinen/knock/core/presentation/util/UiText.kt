package com.vkasurinen.knock.core.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Immutable
sealed interface UiText {
    data class DynamicString(val value: String): UiText

    class Resource(
        val id: StringResource,
        val args: Array<Any> = arrayOf()
    ): UiText {
        // Compares the arguments by content. Otherwise two equal texts are never equal, so UI
        // state that contains them looks changed every time it's rebuilt and is recomposed again.
        override fun equals(other: Any?): Boolean {
            if(this === other) return true
            if(other !is Resource) return false
            return id == other.id && args.contentEquals(other.args)
        }

        override fun hashCode(): Int {
            return 31 * id.hashCode() + args.contentHashCode()
        }
    }

    @Composable
    fun asString(): String {
        return when(this) {
            is DynamicString -> value
            is Resource -> stringResource(
                resource = id,
                *args
            )
        }
    }

    suspend fun asStringAsync(): String {
        return when(this) {
            is DynamicString -> value
            is Resource -> getString(
                resource = id,
                *args
            )
        }
    }
}