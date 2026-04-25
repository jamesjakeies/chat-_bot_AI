package com.xinyu.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.core.common.UiState
import com.xinyu.ai.core.common.asMessage
import com.xinyu.ai.domain.model.MemoryItem
import com.xinyu.ai.domain.usecase.ConfirmSensitiveMemoryUseCase
import com.xinyu.ai.domain.usecase.DeleteMemoryUseCase
import com.xinyu.ai.domain.usecase.ObserveMemoriesUseCase
import com.xinyu.ai.domain.usecase.RefreshMemoriesUseCase
import com.xinyu.ai.domain.usecase.UpdateRoleMemorySettingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MemoryUiState(
    val memories: List<MemoryItem> = emptyList(),
    val actionState: UiState<Unit> = UiState.Idle,
)

@HiltViewModel
class MemoryViewModel @Inject constructor(
    observeMemoriesUseCase: ObserveMemoriesUseCase,
    private val refreshMemoriesUseCase: RefreshMemoriesUseCase,
    private val deleteMemoryUseCase: DeleteMemoryUseCase,
    private val confirmSensitiveMemoryUseCase: ConfirmSensitiveMemoryUseCase,
    private val updateRoleMemorySettingUseCase: UpdateRoleMemorySettingUseCase,
) : ViewModel() {
    private val actionState = MutableStateFlow<UiState<Unit>>(UiState.Idle)

    val uiState: StateFlow<MemoryUiState> = combine(
        observeMemoriesUseCase(),
        actionState,
    ) { memories, state ->
        MemoryUiState(
            memories = memories,
            actionState = state,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MemoryUiState(),
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            refreshMemoriesUseCase(includePending = true)
        }
    }

    fun deleteMemory(memoryId: String) {
        viewModelScope.launch {
            actionState.value = UiState.Loading
            when (val result = deleteMemoryUseCase(memoryId)) {
                is AppResult.Success -> actionState.value = UiState.Success(Unit)
                is AppResult.Error -> actionState.value = UiState.Error(result.error.asMessage())
            }
        }
    }

    fun confirmSensitive(memoryId: String, accepted: Boolean) {
        viewModelScope.launch {
            actionState.value = UiState.Loading
            when (val result = confirmSensitiveMemoryUseCase(memoryId, accepted)) {
                is AppResult.Success -> actionState.value = UiState.Success(Unit)
                is AppResult.Error -> actionState.value = UiState.Error(result.error.asMessage())
            }
        }
    }

    fun setRoleMemoryEnabled(roleId: String, enabled: Boolean) {
        viewModelScope.launch {
            actionState.value = UiState.Loading
            when (val result = updateRoleMemorySettingUseCase(roleId, enabled)) {
                is AppResult.Success -> actionState.value = UiState.Success(Unit)
                is AppResult.Error -> actionState.value = UiState.Error(result.error.asMessage())
            }
        }
    }

    fun clearActionState() {
        actionState.value = UiState.Idle
    }
}
