package com.xinyu.ai.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinyu.ai.domain.model.UserProfile
import com.xinyu.ai.domain.usecase.ObserveCurrentUserUseCase
import com.xinyu.ai.domain.usecase.ObserveFavoriteRoleIdsUseCase
import com.xinyu.ai.domain.usecase.ObserveRoleUseCase
import com.xinyu.ai.domain.usecase.RefreshCurrentUserUseCase
import com.xinyu.ai.domain.usecase.RefreshRoleUseCase
import com.xinyu.ai.domain.usecase.ToggleFavoriteRoleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RoleDetailUiState(
    val role: com.xinyu.ai.domain.model.RoleProfile? = null,
    val user: UserProfile? = null,
    val isFavorite: Boolean = false,
)

@HiltViewModel
class RoleDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeRoleUseCase: ObserveRoleUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    observeFavoriteRoleIdsUseCase: ObserveFavoriteRoleIdsUseCase,
    private val toggleFavoriteRoleUseCase: ToggleFavoriteRoleUseCase,
    private val refreshRoleUseCase: RefreshRoleUseCase,
    private val refreshCurrentUserUseCase: RefreshCurrentUserUseCase,
) : ViewModel() {
    private val roleId: String = checkNotNull(savedStateHandle["roleId"])

    val uiState: StateFlow<RoleDetailUiState> = combine(
        observeRoleUseCase(roleId),
        observeCurrentUserUseCase(),
        observeFavoriteRoleIdsUseCase(),
    ) { role, user, favoriteRoleIds ->
        RoleDetailUiState(
            role = role,
            user = user,
            isFavorite = role?.id in favoriteRoleIds,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RoleDetailUiState(),
    )

    init {
        viewModelScope.launch {
            refreshCurrentUserUseCase()
            refreshRoleUseCase(roleId)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            toggleFavoriteRoleUseCase(roleId)
        }
    }
}
