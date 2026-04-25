package com.xinyu.ai.presentation.screen.roledetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinyu.ai.domain.model.RelationshipType
import com.xinyu.ai.domain.model.RoleProfile
import com.xinyu.ai.presentation.components.AiDisclosureBanner
import com.xinyu.ai.presentation.components.RoleAvatar
import com.xinyu.ai.presentation.components.TagChip
import com.xinyu.ai.presentation.viewmodel.RoleDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDetailScreen(
    onBack: () -> Unit,
    onStartChat: (String) -> Unit,
    viewModel: RoleDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showMinorDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Role details") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        uiState.role?.let { profile ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    AiDisclosureBanner()
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        RoleAvatar(name = profile.name)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = profile.name, style = MaterialTheme.typography.headlineMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = profile.relationshipType.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                if (profile.isAdultOnly) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("18+") },
                                    )
                                }
                                if (!profile.isOfficial) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("Custom") },
                                    )
                                }
                            }
                            Text(
                                text = profile.shortDescription,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = "Greeting: ${profile.greeting}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(profile.personalityTags) { tag ->
                            TagChip(label = tag)
                        }
                    }
                }
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("Sample conversation", style = MaterialTheme.typography.titleLarge)
                            Text("User: I feel tired today.")
                            Text("${profile.name}: ${profile.sampleReply}")
                        }
                    }
                }
                item {
                    Button(
                        onClick = {
                            if (uiState.user?.isMinor == true && profile.isRestrictedForMinor()) {
                                showMinorDialog = true
                            } else {
                                onStartChat(profile.id)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Start chat")
                    }
                }
                item {
                    OutlinedButton(
                        onClick = viewModel::toggleFavorite,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (uiState.isFavorite) "Saved as favorite" else "Set as favorite")
                    }
                }
            }
        } ?: Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Loading role details...")
            Text("If no content appears here, confirm that the current role ID is available from the backend.")
        }
    }

    if (showMinorDialog) {
        AlertDialog(
            onDismissRequest = { showMinorDialog = false },
            title = { Text("This role is restricted") },
            text = { Text("Minor accounts cannot use intimate relationship roles such as virtual boyfriend or girlfriend roles.") },
            confirmButton = {
                TextButton(onClick = { showMinorDialog = false }) {
                    Text("Got it")
                }
            },
        )
    }
}

private fun RoleProfile.isRestrictedForMinor(): Boolean {
    return isAdultOnly || relationshipType == RelationshipType.ROMANTIC_PARTNER
}
