package com.aashu.privatesuite.domain.repository

interface AuthRepository {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
}
