package com.xinyu.ai.presentation.screen.rolelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinyu.ai.domain.model.RelationshipType
import com.xinyu.ai.domain.model.RoleCategory
import com.xinyu.ai.domain.model.RoleProfile
import com.xinyu.ai.presentation.components.AiDisclosureBanner
import com.xinyu.ai.presentation.components.RoleAvatar
import com.xinyu.ai.presentation.components.TagChip
import com.xinyu.ai.presentation.viewmodel.RoleListViewModel

@Composable
fun RoleListScreen(
    onOpenRole: (String) -> Unit,
    onStartChat: (String) -> Unit,
    onCreateRole: () -> Unit,
    viewModel: RoleListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedCategory by rememberSaveable { mutableStateOf<RoleCategory?>(null) }
    var showMinorDialog by rememberSaveable { mutableStateOf(false) }

    val roles = uiState.roles.filter { role ->
        selectedCategory == null || role.category == selectedCategory
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateRole,
                text = { Text("创建角色") },
                icon = { androidx.compose.material3.Icon(Icons.Outlined.Add, contentDescription = null) },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "今天想和谁聊聊？",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            item {
                AiDisclosureBanner()
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("全部") },
                        )
                    }
                    items(RoleCategory.entries) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category.label) },
                        )
                    }
                }
            }
            if (roles.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("正在加载角色列表...")
                            Text(
                                text = "如果这里持续为空，请确认后端服务已经启动，并且当前账号可以访问 `/roles`。",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
            items(roles, key = { it.id }) { role ->
                RoleCard(
                    role = role,
                    onClick = {
                        if (uiState.user?.isMinor == true && role.isRestrictedForMinor()) {
                            showMinorDialog = true
                        } else {
                            onOpenRole(role.id)
                        }
                    },
                    onStartChat = {
                        if (uiState.user?.isMinor == true && role.isRestrictedForMinor()) {
                            showMinorDialog = true
                        } else {
                            onStartChat(role.id)
                        }
                    },
                    isFavorite = role.id in uiState.favoriteRoleIds,
                )
            }
        }
    }

    if (showMinorDialog) {
        AlertDialog(
            onDismissRequest = { showMinorDialog = false },
            title = { Text("当前角色受限") },
            text = {
                Text("未成年人账号不能进入恋爱陪伴角色。请改用情绪支持、树洞倾听、学习陪跑或睡前陪伴角色。")
            },
            confirmButton = {
                TextButton(onClick = { showMinorDialog = false }) {
                    Text("知道了")
                }
            },
        )
    }
}

@Composable
private fun RoleCard(
    role: RoleProfile,
    onClick: () -> Unit,
    onStartChat: () -> Unit,
    isFavorite: Boolean,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                RoleAvatar(name = role.name)
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = role.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        if (isFavorite) {
                            AssistChip(
                                onClick = {},
                                label = { Text("常用") },
                            )
                        }
                        if (role.isAdultOnly) {
                            AssistChip(
                                onClick = {},
                                label = { Text("18+") },
                            )
                        }
                        if (!role.isOfficial) {
                            AssistChip(
                                onClick = {},
                                label = { Text("自定义") },
                            )
                        }
                    }
                    Text(
                        text = role.shortDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(role.personalityTags) { tag ->
                    TagChip(label = tag)
                }
            }
            Button(onClick = onStartChat) {
                Text("开始聊天")
            }
        }
    }
}

private fun RoleProfile.isRestrictedForMinor(): Boolean {
    return isAdultOnly || relationshipType == RelationshipType.ROMANTIC_PARTNER
}
