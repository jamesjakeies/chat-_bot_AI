package com.xinyu.ai.presentation.screen.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinyu.ai.core.common.UiState
import com.xinyu.ai.domain.model.ChatMessage
import com.xinyu.ai.domain.model.MessageSenderType
import com.xinyu.ai.presentation.components.AiDisclosureBanner
import com.xinyu.ai.presentation.components.RoleAvatar
import com.xinyu.ai.presentation.components.TagChip
import com.xinyu.ai.presentation.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }
    var selectedMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var memoryCandidate by remember { mutableStateOf<ChatMessage?>(null) }
    var sensitiveMemoryCandidate by remember { mutableStateOf<ChatMessage?>(null) }
    var quotaDialogMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var lastQuotaDialogMessageId by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.messages.size, uiState.isAiTyping) {
        if (uiState.messages.isNotEmpty() || uiState.isAiTyping) {
            val extraItem = if (uiState.isAiTyping) 1 else 0
            val targetIndex = (uiState.messages.lastIndex + extraItem).coerceAtLeast(0)
            lazyListState.animateScrollToItem(targetIndex)
        }
    }

    LaunchedEffect(uiState.memoryState) {
        when (val state = uiState.memoryState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("已提交记忆，敏感记忆会在记忆页等待确认")
                viewModel.clearMemoryState()
            }

            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearMemoryState()
            }

            else -> Unit
        }
    }

    LaunchedEffect(uiState.messages) {
        val latestQuotaMessage = uiState.messages.lastOrNull { it.isQuotaLimitMessage() }
        if (
            latestQuotaMessage != null &&
            latestQuotaMessage.id != lastQuotaDialogMessageId
        ) {
            quotaDialogMessage = latestQuotaMessage
            lastQuotaDialogMessageId = latestQuotaMessage.id
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        uiState.role?.let { RoleAvatar(name = it.name, size = 40.dp) }
                        Column {
                            Text(uiState.role?.name ?: "聊天")
                            Text(
                                text = "AI 角色，不是真人",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            AiDisclosureBanner(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.quickActions.forEach { action ->
                    AssistChip(
                        onClick = {
                            viewModel.sendMessage(
                                content = action.message,
                                sceneMode = action.sceneMode,
                            )
                        },
                        label = { Text(action.label) },
                    )
                }
            }
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    ChatMessageBubble(
                        message = message,
                        onLongPress = { selectedMessage = message },
                    )
                }
                if (uiState.isAiTyping) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Card {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.padding(2.dp),
                                        strokeWidth = 2.dp,
                                    )
                                    Text("AI 正在输入...")
                                }
                            }
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("输入你想说的话...") },
                    maxLines = 4,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val message = input.trim()
                                if (message.isNotEmpty()) {
                                    viewModel.sendMessage(message)
                                    input = ""
                                }
                            },
                            enabled = input.isNotBlank() && !uiState.isAiTyping,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Send,
                                contentDescription = "发送",
                            )
                        }
                    },
                )
                Text(
                    text = "快捷按钮会携带 scene_mode，后端会据此调整角色回复。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
            }
        }
    }

    selectedMessage?.let { message ->
        MessageActionDialog(
            message = message,
            onDismiss = { selectedMessage = null },
            onCopy = {
                clipboardManager.setText(AnnotatedString(message.content))
                selectedMessage = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("已复制消息内容")
                }
            },
            onDelete = {
                viewModel.deleteMessage(message.id)
                selectedMessage = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("已删除这条消息")
                }
            },
            onReport = {
                viewModel.reportMessage(message.id)
                selectedMessage = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("已记录举报，后续会接入审核后台")
                }
            },
            onRemember = {
                selectedMessage = null
                memoryCandidate = message
            },
        )
    }

    memoryCandidate?.let { message ->
        RememberChoiceDialog(
            onDismiss = { memoryCandidate = null },
            onNormalMemory = {
                viewModel.rememberMessage(message, sensitive = false)
                memoryCandidate = null
            },
            onSensitiveMemory = {
                sensitiveMemoryCandidate = message
                memoryCandidate = null
            },
        )
    }

    sensitiveMemoryCandidate?.let { message ->
        SensitiveMemoryConfirmDialog(
            onDismiss = { sensitiveMemoryCandidate = null },
            onConfirm = {
                viewModel.rememberMessage(message, sensitive = true)
                sensitiveMemoryCandidate = null
            },
        )
    }

    quotaDialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { quotaDialogMessage = null },
            title = { Text("额度提醒") },
            text = {
                Text("${message.content}\n\n你可以到“我的 -> 会员中心”使用 mock upgrade。")
            },
            confirmButton = {
                TextButton(onClick = { quotaDialogMessage = null }) {
                    Text("知道了")
                }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    onLongPress: () -> Unit,
) {
    val isUser = message.senderType == MessageSenderType.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongPress,
                ),
        ) {
            Column(
                modifier = Modifier
                    .background(
                        if (isUser) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = message.content)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (isUser) "You" else "AI",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    )
                    if (message.isRememberCandidate) {
                        TagChip(label = "已提交记忆")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MessageActionDialog(
    message: ChatMessage,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit,
    onRemember: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("消息操作") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("选择你想对这条消息执行的操作。")
                if (message.isRememberCandidate) {
                    TagChip(label = "已提交为记忆")
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(onClick = onCopy) { Text("复制") }
                    TextButton(onClick = onDelete) { Text("删除") }
                    TextButton(onClick = onReport) { Text("举报") }
                    if (
                        message.senderType == MessageSenderType.USER &&
                        !message.isRememberCandidate
                    ) {
                        TextButton(onClick = onRemember) {
                            Text("让 TA 记住")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
    )
}

@Composable
private fun RememberChoiceDialog(
    onDismiss: () -> Unit,
    onNormalMemory: () -> Unit,
    onSensitiveMemory: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("让 TA 记住这件事？") },
        text = {
            Text(
                "普通记忆适合称呼、聊天偏好、近期目标等。涉及心理健康、医疗、家庭冲突、真实身份、学校公司住址等信息，请选择敏感记忆并单独确认。",
            )
        },
        confirmButton = {
            TextButton(onClick = onNormalMemory) {
                Text("普通记忆")
            }
        },
        dismissButton = {
            TextButton(onClick = onSensitiveMemory) {
                Text("包含敏感信息")
            }
        },
    )
}

@Composable
private fun SensitiveMemoryConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认保存敏感记忆") },
        text = {
            Text(
                "这可能包含心理健康、医疗、家庭、亲密关系、财务、真实身份或地点信息。确认后它会先进入记忆页待确认，你仍然可以拒绝或删除。",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认提交")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

private fun ChatMessage.isQuotaLimitMessage(): Boolean {
    if (senderType != MessageSenderType.SYSTEM) return false

    return content.contains("额度") ||
        content.contains("会员") ||
        content.contains("上限") ||
        content.contains("解锁")
}
