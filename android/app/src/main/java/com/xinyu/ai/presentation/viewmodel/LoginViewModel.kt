package com.xinyu.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.core.common.UiState
import com.xinyu.ai.core.common.asMessage
import com.xinyu.ai.domain.usecase.LoginUseCase
import com.xinyu.ai.domain.usecase.ObserveCurrentUserUseCase
import com.xinyu.ai.domain.usecase.RefreshCurrentUserUseCase
import com.xinyu.ai.presentation.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val refreshCurrentUserUseCase: RefreshCurrentUserUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _navigationTarget = MutableSharedFlow<String>()
    val navigationTarget: SharedFlow<String> = _navigationTarget.asSharedFlow()

    fun login(email: String, password: String) {
        if (_uiState.value == UiState.Loading) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = loginUseCase(email.trim(), password)) {
                is AppResult.Success -> {
                    refreshCurrentUserUseCase()
                    val user = observeCurrentUserUseCase().first()
                    _uiState.value = UiState.Success(Unit)
                    _navigationTarget.emit(
                        if (user?.ageVerified == true) {
                            AppDestination.RoleList.route
                        } else {
                            AppDestination.AgeGate.route
                        },
                    )
                }

                is AppResult.Error -> {
                    _uiState.value = UiState.Error(result.error.asMessage())
                }
            }
        }
    }

    fun clearError() {
        if (_uiState.value is UiState.Error) {
            _uiState.value = UiState.Idle
        }
    }
}
