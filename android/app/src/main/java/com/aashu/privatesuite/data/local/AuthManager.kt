package com.aashu.privatesuite.data.local

import android.content.Context
import android.content.SharedPreferences
import com.aashu.privatesuite.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) : AuthRepository {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "auth_prefs",
        Context.MODE_PRIVATE
    )

    override fun saveToken(token: String) {
        sharedPreferences.edit().putString("jwt_token", token).apply()
    }

    override fun getToken(): String? {
        return sharedPreferences.getString("jwt_token", null)
    }

    override fun clearToken() {
        sharedPreferences.edit().remove("jwt_token").apply()
    }
}
