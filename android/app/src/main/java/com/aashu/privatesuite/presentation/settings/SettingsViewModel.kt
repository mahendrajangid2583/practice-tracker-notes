package com.aashu.privatesuite.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aashu.privatesuite.domain.repository.AuthRepository
import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val repository: OfflineFirstRepository
) : ViewModel() {

    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut = _isLoggingOut.asStateFlow()

    private val _logoutComplete = MutableStateFlow(false)
    val logoutComplete = _logoutComplete.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            _isLoggingOut.value = true
            try {
                // Clear local data
                repository.clearAllData()
                // Clear token
                authRepository.clearToken()
                // Signal completion
                _logoutComplete.value = true
            } catch (e: Exception) {
                // Handle error if needed, but for logout we generally force it
                e.printStackTrace()
                authRepository.clearToken() // Ensure token is cleared at minimum
                _logoutComplete.value = true
            } finally {
                _isLoggingOut.value = false
            }
        }
    }
    
    fun resetLogoutState() {
        _logoutComplete.value = false
    }
}
