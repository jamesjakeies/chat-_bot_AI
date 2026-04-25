package com.xinyu.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinyu.ai.domain.model.UserProfile
import com.xinyu.ai.domain.usecase.ObserveCurrentUserUseCase
import com.xinyu.ai.domain.usecase.RefreshCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val refreshCurrentUserUseCase: RefreshCurrentUserUseCase,
) : ViewModel() {
    val user: StateFlow<UserProfile?> = observeCurrentUserUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    init {
        viewModelScope.launch {
            refreshCurrentUserUseCase()
        }
    }
}
