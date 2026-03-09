package com.aashu.privatesuite.data.remote

import com.aashu.privatesuite.data.local.AuthManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val authManager: AuthManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = authManager.getToken()
        
        if (token == null) {
            android.util.Log.w("AuthInterceptor", "Token is NULL. Proceeding without auth header.")
            return chain.proceed(originalRequest)
        } else {
            // android.util.Log.d("AuthInterceptor", "Attaching token: ${token.take(10)}...")
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        val response = chain.proceed(authenticatedRequest)
        
        if (response.code == 401) {
            android.util.Log.e("AuthInterceptor", "Server returned 401 Unauthorized for ${originalRequest.url}")
        }
        
        return response
    }
}
