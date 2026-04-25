package com.xinyu.ai.presentation.screen.memory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinyu.ai.core.common.UiState
import com.xinyu.ai.domain.model.MemoryItem
import com.xinyu.ai.presentation.components.AiDisclosureBanner
import com.xinyu.ai.presentation.components.TagChip
import com.xinyu.ai.presentation.viewmodel.MemoryViewModel

@Composable
fun MemoryScreen(
    viewModel: MemoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.actionState) {
        when (val state = uiState.actionState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("记忆已更新")
                viewModel.clearActionState()
            }

            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearActionState()
            }

            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "角色记忆",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            item {
                AiDisclosureBanner(
                    text = "记忆按角色隔离保存。敏感记忆必须单独确认，你也可以随时删除。",
                )
            }
            item {
                OutlinedButton(
                    onClick = viewModel::refresh,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("刷新记忆")
                }
            }
            if (uiState.memories.isEmpty()) {
                item {
                    EmptyMemoryCard()
                }
            } else {
                items(uiState.memories, key = { it.id }) { memory ->
                    MemoryCard(
                        memory = memory,
                        isActionLoading = uiState.actionState is UiState.Loading,
                        onAccept = { viewModel.confirmSensitive(memory.id, accepted = true) },
                        onReject = { viewModel.confirmSensitive(memory.id, accepted = false) },
                        onDelete = { viewModel.deleteMemory(memory.id) },
                        onDisableRoleMemory = {
                            viewModel.setRoleMemoryEnabled(memory.roleId, enabled = false)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMemoryCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("TA 还没有记住任何事", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "在聊天页长按你的消息，可以选择“让 TA 记住这件事”。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
private fun MemoryCard(
    memory: MemoryItem,
    isActionLoading: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onDelete: () -> Unit,
    onDisableRoleMemory: () -> Unit,
) {
    val isPendingSensitive = memory.isSensitive && !memory.userConsented

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(memory.roleName, style = MaterialTheme.typography.titleMedium)
                TagChip(
                    label = when {
                        isPendingSensitive -> "敏感待确认"
                        memory.isSensitive -> "敏感记忆"
                        else -> "普通记忆"
                    },
                )
            }
            Text(memory.content, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "类型：${memory.memoryType} · 安全级别：${memory.sensitivityLevel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
            if (isPendingSensitive) {
                Text(
                    text = "这条记忆包含敏感信息，确认前不会作为正式长期记忆使用。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onAccept,
                        enabled = !isActionLoading,
                    ) {
                        Text("确认保存")
                    }
                    TextButton(
                        onClick = onReject,
                        enabled = !isActionLoading,
                    ) {
                        Text("不保存")
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = onDelete,
                        enabled = !isActionLoading,
                    ) {
                        Text("删除这条记忆")
                    }
                    TextButton(
                        onClick = onDisableRoleMemory,
                        enabled = !isActionLoading,
                    ) {
                        Text("关闭该角色记忆")
                    }
                }
            }
        }
    }
}
