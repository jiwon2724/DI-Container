package jiwondev.di_container.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val USER_PREFERENCE = "user_preference"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCE)

class UserLocalDataSource(private val context: Context) {
    suspend fun getLoginState(): Boolean? = context.dataStore.data.map { it[LOGIN_STATE] }.first()

    suspend fun setLoginState(isLogin: Boolean) {
        context.dataStore.edit { it[LOGIN_STATE] = isLogin }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    companion object {
        private val LOGIN_STATE = booleanPreferencesKey("isLogin")
    }
}