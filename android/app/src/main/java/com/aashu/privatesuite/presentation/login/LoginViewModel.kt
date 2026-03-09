package com.aashu.privatesuite.presentation.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aashu.privatesuite.domain.repository.AuthRepository
import com.aashu.privatesuite.data.remote.PrivateSuiteApi
import com.aashu.privatesuite.data.remote.dto.LoginRequestDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val api: PrivateSuiteApi,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = mutableStateOf<LoginState>(LoginState.Idle)
    val state: State<LoginState> = _state

    private val _pin = mutableStateOf("")
    val pin: State<String> = _pin

    fun onPinChange(newPin: String) {
        _pin.value = newPin
    }

    fun login() {
        if (_pin.value.isBlank()) return

        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                val response = api.login(LoginRequestDto(pin = _pin.value.trim()))
                authRepository.saveToken(response.token)
                _state.value = LoginState.Success
            } catch (e: Exception) {
                _state.value = LoginState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
