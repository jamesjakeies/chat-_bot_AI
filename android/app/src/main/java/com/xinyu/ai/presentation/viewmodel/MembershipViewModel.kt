package com.xinyu.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.core.common.UiState
import com.xinyu.ai.core.common.asMessage
import com.xinyu.ai.domain.model.MembershipStatus
import com.xinyu.ai.domain.model.MembershipTier
import com.xinyu.ai.domain.usecase.MockUpgradeMembershipUseCase
import com.xinyu.ai.domain.usecase.ObserveMembershipUseCase
import com.xinyu.ai.domain.usecase.RefreshMembershipUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MembershipUiState(
    val membership: MembershipStatus? = null,
    val actionState: UiState<Unit> = UiState.Idle,
)

@HiltViewModel
class MembershipViewModel @Inject constructor(
    observeMembershipUseCase: ObserveMembershipUseCase,
    private val refreshMembershipUseCase: RefreshMembershipUseCase,
    private val mockUpgradeMembershipUseCase: MockUpgradeMembershipUseCase,
) : ViewModel() {
    private val actionState = MutableStateFlow<UiState<Unit>>(UiState.Idle)

    val uiState: StateFlow<MembershipUiState> = combine(
        observeMembershipUseCase(),
        actionState,
    ) { membership, state ->
        MembershipUiState(membership = membership, actionState = state)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MembershipUiState(),
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            refreshMembershipUseCase()
        }
    }

    fun mockUpgrade(tier: MembershipTier) {
        viewModelScope.launch {
            actionState.value = UiState.Loading
            when (val result = mockUpgradeMembershipUseCase(tier)) {
                is AppResult.Success -> actionState.value = UiState.Success(Unit)
                is AppResult.Error -> actionState.value = UiState.Error(result.error.asMessage())
            }
        }
    }

    fun clearActionState() {
        actionState.value = UiState.Idle
    }
}
