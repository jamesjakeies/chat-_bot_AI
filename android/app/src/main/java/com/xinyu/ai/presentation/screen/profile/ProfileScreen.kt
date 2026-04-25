package com.xinyu.ai.presentation.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinyu.ai.presentation.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onMembershipClick: () -> Unit,
    onMoodCheckInClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val user by viewModel.user.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "我的",
                style = MaterialTheme.typography.headlineMedium,
            )
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(user?.nickname ?: "访客", style = MaterialTheme.typography.titleLarge)
                    Text("邮箱：${user?.email ?: "未获取"}")
                    Text("当前会员：${user?.membershipTier?.displayName ?: "免费版"}")
                    Text("年龄校验：${if (user?.ageVerified == true) "已完成" else "未完成"}")
                }
            }
        }
        item {
            Button(
                onClick = onMembershipClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("会员中心")
            }
        }
        item {
            Button(
                onClick = onMoodCheckInClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("情绪打卡")
            }
        }
        item {
            Button(
                onClick = onSettingsClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("设置")
            }
        }
    }
}
