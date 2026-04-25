package com.xinyu.ai.presentation.screen.createrole

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinyu.ai.core.common.UiState
import com.xinyu.ai.domain.model.CustomRoleDraft
import com.xinyu.ai.domain.model.RelationshipType
import com.xinyu.ai.domain.model.RoleCategory
import com.xinyu.ai.domain.model.SafetyLevel
import com.xinyu.ai.presentation.components.AiDisclosureBanner
import com.xinyu.ai.presentation.viewmodel.CreateRoleViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateRoleScreen(
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    viewModel: CreateRoleViewModel = hiltViewModel(),
) {
    val createState by viewModel.createState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var name by rememberSaveable { mutableStateOf("") }
    var avatarUrl by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(RoleCategory.CUSTOM) }
    var relationshipType by rememberSaveable { mutableStateOf(RelationshipType.CUSTOM) }
    var personality by rememberSaveable { mutableStateOf("温柔、稳定、有边界") }
    var speechStyle by rememberSaveable { mutableStateOf("短句为主，先倾听，再轻轻回应") }
    var systemPrompt by rememberSaveable {
        mutableStateOf("保持 AI 身份透明，不诱导依赖，不提供医疗诊断或违法建议。")
    }
    var safetyLevel by rememberSaveable { mutableStateOf(SafetyLevel.STRICT) }
    var isAdultOnly by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(createState) {
        when (val state = createState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("角色已创建")
                viewModel.clearState()
                onCreated(state.data.id)
            }

            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearState()
            }

            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("创建角色") },
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
                    text = "自定义角色也必须明确自己是 AI，不能伪装真人、诱导依赖或越过安全边界。",
                )
            }
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("角色名称") },
                    singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    value = avatarUrl,
                    onValueChange = { avatarUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("头像 URL（可选）") },
                    singleLine = true,
                )
            }
            item {
                ChipSection(
                    title = "角色分类",
                    options = RoleCategory.entries,
                    selected = category,
                    label = { it.label },
                    onSelected = {
                        category = it
                        relationshipType = it.defaultRelationshipType()
                    },
                )
            }
            item {
                ChipSection(
                    title = "关系类型",
                    options = RelationshipType.entries,
                    selected = relationshipType,
                    label = { it.label },
                    onSelected = { relationshipType = it },
                )
            }
            item {
                OutlinedTextField(
                    value = personality,
                    onValueChange = { personality = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("性格设定") },
                    minLines = 2,
                )
            }
            item {
                OutlinedTextField(
                    value = speechStyle,
                    onValueChange = { speechStyle = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("说话风格") },
                    minLines = 3,
                )
            }
            item {
                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("角色边界与补充设定") },
                    minLines = 4,
                )
            }
            item {
                ChipSection(
                    title = "安全等级",
                    options = SafetyLevel.entries,
                    selected = safetyLevel,
                    label = { it.label },
                    onSelected = { safetyLevel = it },
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("成人限定", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "恋爱陪伴会自动按成人限定处理。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                        )
                    }
                    Switch(
                        checked = isAdultOnly || relationshipType == RelationshipType.ROMANTIC_PARTNER,
                        onCheckedChange = { isAdultOnly = it },
                        enabled = relationshipType != RelationshipType.ROMANTIC_PARTNER,
                    )
                }
            }
            item {
                Button(
                    onClick = {
                        viewModel.createRole(
                            CustomRoleDraft(
                                name = name.trim(),
                                avatarUrl = avatarUrl.trim(),
                                category = category,
                                relationshipType = relationshipType,
                                personality = personality.trim(),
                                speechStyle = speechStyle.trim(),
                                systemPrompt = systemPrompt.trim(),
                                safetyLevel = safetyLevel,
                                isAdultOnly = isAdultOnly,
                            ),
                        )
                    },
                    enabled = createState !is UiState.Loading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (createState is UiState.Loading) "创建中..." else "创建角色")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ChipSection(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = selected == option,
                    onClick = { onSelected(option) },
                    label = { Text(label(option)) },
                )
            }
        }
    }
}

private fun RoleCategory.defaultRelationshipType(): RelationshipType {
    return when (this) {
        RoleCategory.EMOTIONAL_SUPPORT -> RelationshipType.SUPPORT_PARTNER
        RoleCategory.ROMANCE -> RelationshipType.ROMANTIC_PARTNER
        RoleCategory.SLEEP -> RelationshipType.BEDTIME_COMPANION
        RoleCategory.STUDY -> RelationshipType.STUDY_BUDDY
        RoleCategory.CAREER -> RelationshipType.MENTOR
        RoleCategory.CUSTOM -> RelationshipType.CUSTOM
    }
}
