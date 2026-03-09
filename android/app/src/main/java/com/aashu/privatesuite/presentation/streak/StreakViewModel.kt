package com.aashu.privatesuite.presentation.streak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aashu.privatesuite.domain.usecase.CalculateStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StreakViewModel @Inject constructor(
    calculateStreakUseCase: CalculateStreakUseCase
) : ViewModel() {

    val currentStreak: StateFlow<com.aashu.privatesuite.domain.usecase.StreakResult> = calculateStreakUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.aashu.privatesuite.domain.usecase.StreakResult(0, emptyList())
        )
}
