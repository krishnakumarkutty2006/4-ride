package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "four_ride_session")

data class UserSession(
    val role: UserRole? = null,
    val userId: String = "",
    val phone: String = ""
) {
    val isLoggedIn: Boolean get() = role != null && userId.isNotBlank()
}

class SessionPreferences(private val context: Context) {

    private object Keys {
        val ROLE = stringPreferencesKey("logged_in_role")
        val USER_ID = stringPreferencesKey("logged_in_user_id")
        val PHONE = stringPreferencesKey("logged_in_phone")
    }

    val sessionFlow: Flow<UserSession> = context.dataStore.data.map { prefs ->
        val roleRaw = prefs[Keys.ROLE]
        val role = enumValues<UserRole>().firstOrNull { it.name == roleRaw }
        UserSession(
            role = role,
            userId = prefs[Keys.USER_ID].orEmpty(),
            phone = prefs[Keys.PHONE].orEmpty()
        )
    }

    suspend fun saveSession(role: UserRole, userId: String, phone: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ROLE] = role.name
            prefs[Keys.USER_ID] = userId
            prefs[Keys.PHONE] = phone
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.ROLE)
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.PHONE)
        }
    }
}
