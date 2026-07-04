package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "student_profile")

data class StudentProfile(
    val selectedClass: Int,
    val gender: String,
    val mobileNumber: String,
    val email: String,
    val isRegistered: Boolean
)

class StudentProfileStore(private val context: Context) {

    companion object {
        private val KEY_CLASS = intPreferencesKey("selected_class")
        private val KEY_GENDER = stringPreferencesKey("gender")
        private val KEY_MOBILE = stringPreferencesKey("mobile_number")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_REGISTERED = booleanPreferencesKey("is_registered")
    }

    val profileFlow: Flow<StudentProfile> = context.dataStore.data.map { preferences ->
        StudentProfile(
            selectedClass = preferences[KEY_CLASS] ?: 6,
            gender = preferences[KEY_GENDER] ?: "Not Specified",
            mobileNumber = preferences[KEY_MOBILE] ?: "",
            email = preferences[KEY_EMAIL] ?: "",
            isRegistered = preferences[KEY_REGISTERED] ?: false
        )
    }

    suspend fun saveProfile(profile: StudentProfile) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CLASS] = profile.selectedClass
            preferences[KEY_GENDER] = profile.gender
            preferences[KEY_MOBILE] = profile.mobileNumber
            preferences[KEY_EMAIL] = profile.email
            preferences[KEY_REGISTERED] = true
        }
    }

    suspend fun clearProfile() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
