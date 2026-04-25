package com.xinyu.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinyu.ai.core.common.UiState
import com.xinyu.ai.domain.model.UserProfile
import com.xinyu.ai.domain.usecase.ObserveCurrentUserUseCase
import com.xinyu.ai.domain.usecase.RefreshCurrentUserUseCase
import com.xinyu.ai.domain.usecase.VerifyAgeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AgeGateUiState(
    val user: UserProfile? = null,
    val submitState: UiState<Unit> = UiState.Idle,
)

@HiltViewModel
class AgeGateViewModel @Inject constructor(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val refreshCurrentUserUseCase: RefreshCurrentUserUseCase,
    private val verifyAgeUseCase: VerifyAgeUseCase,
) : ViewModel() {
    private val _submitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    private val _navigationTarget = MutableSharedFlow<Unit>()

    val navigationTarget: SharedFlow<Unit> = _navigationTarget.asSharedFlow()
    val uiState: StateFlow<AgeGateUiState> = combine(
        observeCurrentUserUseCase(),
        _submitState,
    ) { user, submitState ->
        AgeGateUiState(
            user = user,
            submitState = submitState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AgeGateUiState(),
    )

    init {
        viewModelScope.launch {
            refreshCurrentUserUseCase()
        }
    }

    fun submit(
        birthYear: Int,
        guardianConsent: Boolean,
    ) {
        if (_submitState.value == UiState.Loading) return

        viewModelScope.launch {
            _submitState.value = UiState.Loading
            runCatching {
                verifyAgeUseCase(
                    birthYear = birthYear,
                    ageVerified = true,
                    guardianConsent = guardianConsent,
                )
            }.onSuccess {
                _submitState.value = UiState.Success(Unit)
                _navigationTarget.emit(Unit)
            }.onFailure { error ->
                _submitState.value = UiState.Error(
                    error.message ?: "年龄信息提交失败，请稍后再试。",
                )
            }
        }
    }

    fun clearError() {
        if (_submitState.value is UiState.Error) {
            _submitState.value = UiState.Idle
        }
    }
}
