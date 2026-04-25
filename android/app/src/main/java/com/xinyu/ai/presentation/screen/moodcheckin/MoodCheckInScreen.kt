package com.xinyu.ai.presentation.screen.moodcheckin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinyu.ai.core.common.UiState
import com.xinyu.ai.domain.model.MoodLog
import com.xinyu.ai.domain.model.MoodWeeklySummary
import com.xinyu.ai.presentation.components.AiDisclosureBanner
import com.xinyu.ai.presentation.components.TagChip
import com.xinyu.ai.presentation.viewmodel.MoodCheckInViewModel

private val moods = listOf("开心", "疲惫", "焦虑", "低落", "孤独", "生气", "麻木", "睡不着")
private val sources = listOf("工作", "学习", "感情", "家庭", "人际关系", "身体状态", "财务", "其他")

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MoodCheckInScreen(
    onBack: () -> Unit,
    viewModel: MoodCheckInViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedMood by rememberSaveable { mutableStateOf("焦虑") }
    var selectedSources by rememberSaveable { mutableStateOf(listOf("工作")) }
    var note by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState.submitState) {
        when (val state = uiState.submitState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("今日心情已保存")
                note = ""
                viewModel.clearSubmitState()
            }

            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearSubmitState()
            }

            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("情绪打卡") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                AiDisclosureBanner(
                    text = "情绪周报只做温和整理和陪伴建议，不提供医疗诊断。",
                )
            }
            item {
                Text("今天的心情", style = MaterialTheme.typography.titleLarge)
            }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    moods.forEach { mood ->
                        FilterChip(
                            selected = selectedMood == mood,
                            onClick = { selectedMood = mood },
                            label = { Text(mood) },
                        )
                    }
                }
            }
            item {
                Text("压力来源", style = MaterialTheme.typography.titleLarge)
            }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    sources.forEach { source ->
                        val selected = source in selectedSources
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedSources = if (selected) {
                                    selectedSources - source
                                } else {
                                    selectedSources + source
                                }
                            },
                            label = { Text(source) },
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("补充一句备注") },
                    minLines = 3,
                )
            }
            item {
                Button(
                    onClick = {
                        viewModel.submit(
                            moodLabel = selectedMood,
                            pressureSources = selectedSources,
                            note = note,
                        )
                    },
                    enabled = uiState.submitState !is UiState.Loading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (uiState.submitState is UiState.Loading) "保存中..." else "保存今日心情")
                }
            }
            item {
                RecommendationCard(selectedMood, selectedSources)
            }
            uiState.weeklySummary?.let { summary ->
                item {
                    WeeklySummaryCard(summary)
                }
            }
            if (uiState.recentMoods.isNotEmpty()) {
                item {
                    Text("最近打卡", style = MaterialTheme.typography.titleLarge)
                }
                items(uiState.recentMoods, key = { it.id }) { mood ->
                    MoodLogCard(mood)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecommendationCard(
    mood: String,
    pressureSources: List<String>,
) {
    val roles = recommendRoles(mood, pressureSources)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("现在适合找谁聊聊", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                roles.forEach { role ->
                    TagChip(label = role)
                }
            }
            Text(
                text = "推荐基于你选择的心情和压力来源生成，只用于陪伴场景建议。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
        }
    }
}

@Composable
private fun WeeklySummaryCard(summary: MoodWeeklySummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("本周情绪摘要", style = MaterialTheme.typography.titleMedium)
            Text("打卡次数：${summary.checkInCount}")
            Text("主要情绪：${summary.mainMood}")
            Text("高频压力来源：${summary.frequentPressureSource}")
            Text("温和建议：${summary.gentleSuggestion}")
            Text("推荐角色：${summary.recommendedRoles.joinToString(" / ")}")
            Text(
                text = summary.disclaimer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun MoodLogCard(mood: MoodLog) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("${mood.moodLabel} · ${mood.moodScore}/10", style = MaterialTheme.typography.titleSmall)
            Text("压力来源：${mood.pressureSources.joinToString(" / ").ifBlank { "未选择" }}")
            mood.note?.takeIf { it.isNotBlank() }?.let {
                Text("备注：$it")
            }
        }
    }
}

private fun recommendRoles(
    mood: String,
    pressureSources: List<String>,
): List<String> {
    if ("工作" in pressureSources) return listOf("职场导师", "成熟姐姐")

    return when (mood) {
        "焦虑" -> listOf("情绪支持伙伴")
        "低落" -> listOf("温柔男友", "成熟姐姐")
        "孤独" -> listOf("树洞倾听者", "治愈女友")
        "睡不着" -> listOf("睡前陪伴")
        else -> listOf("树洞倾听者", "情绪支持伙伴")
    }
}
