package com.xinyu.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinyu.ai.domain.usecase.ObserveCurrentUserUseCase
import com.xinyu.ai.domain.usecase.ObserveSettingsUseCase
import com.xinyu.ai.domain.usecase.RefreshCurrentUserUseCase
import com.xinyu.ai.presentation.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val refreshCurrentUserUseCase: RefreshCurrentUserUseCase,
) : ViewModel() {
    private val _destination = MutableStateFlow<String?>(null)
    val destination: StateFlow<String?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = observeSettingsUseCase().first()
            if (settings.authToken.isNullOrBlank()) {
                _destination.value = AppDestination.Login.route
                return@launch
            }

            refreshCurrentUserUseCase()
            val user = observeCurrentUserUseCase().first()
            _destination.value = when {
                user == null -> AppDestination.Login.route
                user.ageVerified -> AppDestination.RoleList.route
                else -> AppDestination.AgeGate.route
            }
        }
    }
}
