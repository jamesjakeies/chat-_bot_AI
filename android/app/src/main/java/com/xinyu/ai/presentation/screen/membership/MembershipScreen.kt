package com.xinyu.ai.presentation.screen.membership

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.xinyu.ai.domain.model.MembershipStatus
import com.xinyu.ai.domain.model.MembershipTier
import com.xinyu.ai.presentation.components.AiDisclosureBanner
import com.xinyu.ai.presentation.components.TagChip
import com.xinyu.ai.presentation.viewmodel.MembershipViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipScreen(
    onBack: () -> Unit,
    viewModel: MembershipViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.actionState) {
        when (val state = uiState.actionState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("会员状态已更新")
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
        topBar = {
            TopAppBar(
                title = { Text("会员中心") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                AiDisclosureBanner(
                    text = "当前为 mock 付费流程，不接入真实支付。角色不能用情感操控话术诱导付费。",
                )
            }
            item {
                CurrentMembershipCard(
                    membership = uiState.membership,
                    onRefresh = viewModel::refresh,
                )
            }
            items(MembershipTier.entries) { tier ->
                PlanCard(
                    tier = tier,
                    currentTier = uiState.membership?.membershipLevel,
                    isLoading = uiState.actionState is UiState.Loading,
                    onUpgrade = { viewModel.mockUpgrade(tier) },
                )
            }
        }
    }
}

@Composable
private fun CurrentMembershipCard(
    membership: MembershipStatus?,
    onRefresh: () -> Unit,
) {
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
                Text("当前状态", style = MaterialTheme.typography.titleLarge)
                TagChip(label = membership?.membershipLevel?.displayName ?: "加载中")
            }
            if (membership == null) {
                Text("正在从后端读取额度信息...")
            } else {
                QuotaLine("今日消息", membership.messagesToday, membership.dailyMessages)
                QuotaLine("自定义角色", membership.customRoles, membership.customRoleLimit)
                QuotaLine("长期记忆", membership.memories, membership.memoryLimit)
                Text(
                    text = "基础角色：${membership.baseRoleLimit?.let { "免费可用 $it 个" } ?: "全部解锁"}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = buildString {
                        append("情绪周报：")
                        append(if (membership.weeklyMoodSummary) "已解锁" else "未解锁")
                        append(" · 主动问候：")
                        append(if (membership.proactiveGreeting) "已解锁" else "未解锁")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            OutlinedButton(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("刷新额度")
            }
        }
    }
}

@Composable
private fun QuotaLine(
    label: String,
    used: Int,
    limit: Int,
) {
    Text(
        text = "$label：$used / $limit",
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun PlanCard(
    tier: MembershipTier,
    currentTier: MembershipTier?,
    isLoading: Boolean,
    onUpgrade: () -> Unit,
) {
    val features = when (tier) {
        MembershipTier.FREE -> listOf(
            "每天 30 条消息",
            "可使用 3 个基础角色",
            "可创建 1 个自定义角色",
            "可保存 3 条长期记忆",
        )

        MembershipTier.PLUS -> listOf(
            "每天 300 条消息",
            "解锁全部基础角色",
            "可创建 10 个自定义角色",
            "可保存 100 条长期记忆",
            "解锁情绪周报和角色主动问候",
        )

        MembershipTier.PREMIUM -> listOf(
            "每天 1000 条消息",
            "更多自定义角色和长期记忆",
            "高级角色包预留",
            "高级语音包预留",
            "更长上下文",
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(tier.displayName, style = MaterialTheme.typography.titleLarge)
                if (tier == currentTier) {
                    TagChip(label = "当前")
                }
            }
            features.forEach { feature ->
                Text("· $feature", style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = onUpgrade,
                enabled = !isLoading && tier != currentTier,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    when {
                        tier == currentTier -> "当前方案"
                        tier == MembershipTier.FREE -> "切换到免费版"
                        else -> "Mock 升级到 ${tier.displayName}"
                    },
                )
            }
        }
    }
}
