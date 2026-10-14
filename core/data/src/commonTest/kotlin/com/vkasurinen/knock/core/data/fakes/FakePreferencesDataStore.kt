package com.vkasurinen.knock.core.data.fakes

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePreferencesDataStore(
    initial: Preferences = emptyPreferences()
): DataStore<Preferences> {

    private val state = MutableStateFlow(initial)

    val current: Preferences
        get() = state.value

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}
