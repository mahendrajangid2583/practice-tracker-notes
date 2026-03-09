package com.aashu.privatesuite.data.remote.dto

data class LoginRequestDto(
    val pin: String
)

data class LoginResponseDto(
    val message: String,
    val token: String
)
