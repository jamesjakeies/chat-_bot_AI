package com.xinyu.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinyu.ai.domain.model.RoleProfile
import com.xinyu.ai.domain.model.UserProfile
import com.xinyu.ai.domain.usecase.ObserveCurrentUserUseCase
import com.xinyu.ai.domain.usecase.ObserveFavoriteRoleIdsUseCase
import com.xinyu.ai.domain.usecase.ObserveRolesUseCase
import com.xinyu.ai.domain.usecase.RefreshCurrentUserUseCase
import com.xinyu.ai.domain.usecase.RefreshRolesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RoleListUiState(
    val user: UserProfile? = null,
    val roles: List<RoleProfile> = emptyList(),
    val favoriteRoleIds: Set<String> = emptySet(),
)

@HiltViewModel
class RoleListViewModel @Inject constructor(
    observeRolesUseCase: ObserveRolesUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    observeFavoriteRoleIdsUseCase: ObserveFavoriteRoleIdsUseCase,
    private val refreshRolesUseCase: RefreshRolesUseCase,
    private val refreshCurrentUserUseCase: RefreshCurrentUserUseCase,
) : ViewModel() {
    val uiState: StateFlow<RoleListUiState> = combine(
        observeCurrentUserUseCase(),
        observeRolesUseCase(),
        observeFavoriteRoleIdsUseCase(),
    ) { user, roles, favoriteRoleIds ->
        RoleListUiState(
            user = user,
            roles = roles,
            favoriteRoleIds = favoriteRoleIds,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RoleListUiState(),
    )

    init {
        viewModelScope.launch {
            refreshCurrentUserUseCase()
            refreshRolesUseCase()
        }
    }
}
