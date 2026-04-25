package com.xinyu.ai.core.network

data class ApiEnvelope<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
)

data class LoginRequest(
    val email: String,
    val password: String,
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val nickname: String,
    val phone: String? = null,
)

data class RefreshTokenRequest(
    val refreshToken: String,
)

data class AuthUserDto(
    val id: String,
    val email: String? = null,
    val nickname: String? = null,
    val isMinor: Boolean,
    val membershipLevel: String? = null,
)

data class AuthTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: AuthUserDto,
)

data class ProfileResponse(
    val id: String,
    val email: String? = null,
    val phone: String? = null,
    val nickname: String? = null,
    val birthYear: Int? = null,
    val ageVerified: Boolean,
    val isMinor: Boolean,
    val guardianConsent: Boolean,
    val membershipLevel: String? = null,
)

data class AgeVerificationRequest(
    val birthYear: Int,
    val ageVerified: Boolean,
    val guardianConsent: Boolean,
)

data class RoleDto(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val category: String,
    val relationshipType: String,
    val personality: String,
    val speechStyle: String,
    val systemPrompt: String,
    val safetyLevel: String,
    val isAdultOnly: Boolean,
    val isOfficial: Boolean,
)

data class CreateCustomRoleRequest(
    val name: String,
    val avatarUrl: String,
    val category: String,
    val relationshipType: String,
    val personality: String,
    val speechStyle: String,
    val systemPrompt: String,
    val safetyLevel: String,
    val isAdultOnly: Boolean,
)

data class CreateChatSessionRequest(
    val roleId: String,
    val title: String? = null,
)

data class ChatSessionDto(
    val id: String,
    val userId: String,
    val roleId: String,
    val title: String,
    val riskLevel: String,
    val createdAt: String,
    val updatedAt: String,
    val role: RoleDto? = null,
)

data class ChatMessageDto(
    val id: String,
    val sessionId: String,
    val senderType: String,
    val content: String,
    val safetyLabel: String? = null,
    val tokenCount: Int? = null,
    val createdAt: String,
)

data class SendMessageRequest(
    val content: String,
    val sceneMode: String? = null,
)

data class SafetyDto(
    val eventType: String,
    val riskLevel: String,
    val action: String,
    val shouldBlock: Boolean,
    val shouldSwitchToCrisisMode: Boolean,
)

data class SendMessageResponse(
    val session: ChatSessionDto,
    val messages: List<ChatMessageDto>,
    val safety: SafetyDto,
)

data class MemoryRoleDto(
    val id: String,
    val name: String,
)

data class MemoryDto(
    val id: String,
    val userId: String,
    val roleId: String,
    val memoryType: String,
    val content: String,
    val sensitivityLevel: String,
    val userConsented: Boolean,
    val sourceMessageId: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val role: MemoryRoleDto? = null,
)

data class CreateMemoryRequest(
    val roleId: String,
    val content: String,
    val memoryType: String? = null,
    val sensitivityLevel: String? = null,
    val userConsented: Boolean? = null,
    val sourceMessageId: String? = null,
)

data class ConfirmSensitiveMemoryRequest(
    val memoryId: String,
    val accepted: Boolean,
)

data class RoleMemorySettingRequest(
    val memoryEnabled: Boolean,
)

data class CreateMoodRequest(
    val moodLabel: String,
    val pressureSources: List<String>,
    val note: String? = null,
)

data class MoodLogDto(
    val id: String,
    val moodScore: Int,
    val moodLabel: String,
    val pressureSources: List<String> = emptyList(),
    val note: String? = null,
    val createdAt: String,
)

data class MoodWeeklySummaryDto(
    val checkInCount: Int,
    val mainMood: String,
    val frequentPressureSource: String,
    val gentleSuggestion: String,
    val recommendedRoles: List<String>,
    val disclaimer: String,
)

data class UsageLimitsDto(
    val dailyMessages: Int,
    val baseRoleLimit: Int? = null,
    val customRoleLimit: Int,
    val memoryLimit: Int,
    val weeklyMoodSummary: Boolean,
    val proactiveGreeting: Boolean,
    val longContext: Boolean,
)

data class UsageSnapshotDto(
    val messagesToday: Int,
    val customRoles: Int,
    val memories: Int,
)

data class SubscriptionStatusDto(
    val membershipLevel: String,
    val plan: String,
    val status: String,
    val limits: UsageLimitsDto,
    val usage: UsageSnapshotDto,
)

data class MockUpgradeRequest(
    val plan: String,
)

data class CreateReportRequest(
    val messageId: String,
    val reason: String,
)

data class ReportDto(
    val id: String,
    val userId: String,
    val messageId: String,
    val reason: String,
    val status: String,
    val createdAt: String,
)
