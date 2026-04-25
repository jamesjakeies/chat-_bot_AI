package com.xinyu.ai.presentation.screen.agegate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinyu.ai.core.common.UiState
import com.xinyu.ai.presentation.components.AiDisclosureBanner
import com.xinyu.ai.presentation.viewmodel.AgeGateViewModel
import java.time.Year

@Composable
fun AgeGateScreen(
    onComplete: () -> Unit,
    viewModel: AgeGateViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var birthYear by rememberSaveable { mutableStateOf("2000") }
    var guardianConsent by rememberSaveable { mutableStateOf(false) }
    val parsedBirthYear = birthYear.toIntOrNull()
    val isMinor = parsedBirthYear?.let { Year.now().value - it < 18 } == true

    LaunchedEffect(Unit) {
        viewModel.navigationTarget.collect {
            onComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Age and interaction notice",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        AiDisclosureBanner()
        Spacer(modifier = Modifier.height(16.dp))
        uiState.user?.let { user ->
            Text(
                text = "Current account: ${user.nickname}",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("1. Romantic companion roles are available only to adults.")
                Text("2. Emotional support roles do not replace medical care, treatment, or professional counseling.")
                Text("3. Crisis content such as self-harm, suicide, or violence will switch to global safety mode.")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = birthYear,
            onValueChange = {
                birthYear = it
                viewModel.clearError()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Birth year") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            supportingText = {
                Text(
                    if (isMinor) {
                        "This account is recognized as a minor account. Romantic companion and adult content will be disabled automatically."
                    } else {
                        "Adult accounts can access all base roles within the safety rules."
                    },
                )
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = guardianConsent,
                onCheckedChange = { guardianConsent = it },
            )
            Text("If you are under 18, you have guardian consent.")
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (uiState.submitState is UiState.Error) {
            Text(
                text = (uiState.submitState as UiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Button(
            onClick = {
                parsedBirthYear?.let {
                    viewModel.submit(
                        birthYear = it,
                        guardianConsent = guardianConsent,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = parsedBirthYear != null &&
                (!isMinor || guardianConsent) &&
                uiState.submitState != UiState.Loading,
        ) {
            if (uiState.submitState == UiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White,
                )
            } else {
                Text("Submit and continue")
            }
        }
    }
}
