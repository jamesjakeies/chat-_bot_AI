package com.xinyu.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.core.common.UiState
import com.xinyu.ai.core.common.asMessage
import com.xinyu.ai.domain.model.CustomRoleDraft
import com.xinyu.ai.domain.model.RoleProfile
import com.xinyu.ai.domain.usecase.CreateCustomRoleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CreateRoleViewModel @Inject constructor(
    private val createCustomRoleUseCase: CreateCustomRoleUseCase,
) : ViewModel() {
    private val _createState = MutableStateFlow<UiState<RoleProfile>>(UiState.Idle)
    val createState: StateFlow<UiState<RoleProfile>> = _createState

    fun createRole(draft: CustomRoleDraft) {
        if (_createState.value is UiState.Loading) return

        if (draft.name.isBlank() || draft.personality.isBlank() || draft.speechStyle.isBlank()) {
            _createState.value = UiState.Error("请至少填写角色名称、性格设定和说话风格。")
            return
        }

        viewModelScope.launch {
            _createState.value = UiState.Loading
            when (val result = createCustomRoleUseCase(draft)) {
                is AppResult.Success -> _createState.value = UiState.Success(result.data)
                is AppResult.Error -> _createState.value = UiState.Error(result.error.asMessage())
            }
        }
    }

    fun clearState() {
        _createState.value = UiState.Idle
    }
}
