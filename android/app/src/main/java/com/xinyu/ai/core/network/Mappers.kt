package com.xinyu.ai.core.network

import com.xinyu.ai.core.database.LocalChatMessageEntity
import com.xinyu.ai.core.database.LocalMemoryEntity
import com.xinyu.ai.domain.model.ChatMessage
import com.xinyu.ai.domain.model.ChatRiskLevel
import com.xinyu.ai.domain.model.ChatSession
import com.xinyu.ai.domain.model.MembershipStatus
import com.xinyu.ai.domain.model.MembershipTier
import com.xinyu.ai.domain.model.MemoryItem
import com.xinyu.ai.domain.model.MessageSenderType
import com.xinyu.ai.domain.model.MoodLog
import com.xinyu.ai.domain.model.MoodWeeklySummary
import com.xinyu.ai.domain.model.RelationshipType
import com.xinyu.ai.domain.model.RoleCategory
import com.xinyu.ai.domain.model.RoleProfile
import com.xinyu.ai.domain.model.SafetyLevel
import com.xinyu.ai.domain.model.UserProfile
import java.time.Instant

fun ProfileResponse.toDomain(): UserProfile {
    return UserProfile(
        id = id,
        nickname = nickname.orEmpty(),
        email = email,
        isMinor = isMinor,
        ageVerified = ageVerified,
        membershipTier = membershipLevel.toMembershipTier(),
    )
}

fun RoleDto.toDomain(): RoleProfile {
    val tags = personality
        .split(Regex("[,，、\\s]+"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .take(4)

    val relationType = relationshipType.toRelationshipType()

    return RoleProfile(
        id = id,
        name = name,
        avatarUrl = avatarUrl.orEmpty(),
        category = category.toRoleCategory(),
        relationshipType = relationType,
        personalityTags = if (tags.isEmpty()) listOf("AI role") else tags,
        shortDescription = speechStyle.ifBlank { "A companion AI role that listens carefully." },
        greeting = buildGreeting(name, relationType),
        sampleReply = buildSampleReply(name, relationType),
        isAdultOnly = isAdultOnly,
        isOfficial = isOfficial,
        safetyLevel = safetyLevel.toSafetyLevel(),
    )
}

fun ChatSessionDto.toDomain(): ChatSession {
    return ChatSession(
        id = id,
        userId = userId,
        roleId = roleId,
        title = title,
        createdAt = createdAt.toEpochMillis(),
        updatedAt = updatedAt.toEpochMillis(),
        riskLevel = riskLevel.toChatRiskLevel(),
    )
}

fun ChatMessageDto.toDomain(
    roleId: String,
    rememberCandidate: Boolean = false,
): ChatMessage {
    return ChatMessage(
        id = id,
        sessionId = sessionId,
        roleId = roleId,
        senderType = senderType.toSenderType(),
        content = content,
        createdAt = createdAt.toEpochMillis(),
        safetyLabel = safetyLabel,
        isRememberCandidate = rememberCandidate,
    )
}

fun ChatMessage.toEntity(): LocalChatMessageEntity {
    return LocalChatMessageEntity(
        id = id,
        sessionId = sessionId,
        roleId = roleId,
        senderType = senderType.name,
        content = content,
        createdAt = createdAt,
        safetyLabel = safetyLabel,
        isRememberCandidate = isRememberCandidate,
    )
}

fun LocalChatMessageEntity.toDomain(): ChatMessage {
    return ChatMessage(
        id = id,
        sessionId = sessionId,
        roleId = roleId,
        senderType = senderType.toSenderType(),
        content = content,
        createdAt = createdAt,
        safetyLabel = safetyLabel,
        isRememberCandidate = isRememberCandidate,
    )
}

fun MemoryDto.toDomain(): MemoryItem {
    return MemoryItem(
        id = id,
        roleId = roleId,
        roleName = role?.name ?: "Unknown role",
        content = content,
        memoryType = memoryType,
        sensitivityLevel = sensitivityLevel,
        userConsented = userConsented,
        isSensitive = sensitivityLevel != "NORMAL",
        createdAt = createdAt.toEpochMillis(),
    )
}

fun MemoryDto.toEntity(): LocalMemoryEntity {
    return toDomain().toEntity()
}

fun MemoryItem.toEntity(): LocalMemoryEntity {
    return LocalMemoryEntity(
        id = id,
        roleId = roleId,
        roleName = roleName,
        content = content,
        memoryType = memoryType,
        sensitivityLevel = sensitivityLevel,
        userConsented = userConsented,
        createdAt = createdAt,
    )
}

fun LocalMemoryEntity.toDomain(): MemoryItem {
    return MemoryItem(
        id = id,
        roleId = roleId,
        roleName = roleName,
        content = content,
        memoryType = memoryType,
        sensitivityLevel = sensitivityLevel,
        userConsented = userConsented,
        isSensitive = sensitivityLevel != "NORMAL",
        createdAt = createdAt,
    )
}

fun MoodLogDto.toDomain(): MoodLog {
    return MoodLog(
        id = id,
        moodScore = moodScore,
        moodLabel = moodLabel,
        pressureSources = pressureSources,
        note = note,
        createdAt = createdAt.toEpochMillis(),
    )
}

fun MoodWeeklySummaryDto.toDomain(): MoodWeeklySummary {
    return MoodWeeklySummary(
        checkInCount = checkInCount,
        mainMood = mainMood,
        frequentPressureSource = frequentPressureSource,
        gentleSuggestion = gentleSuggestion,
        recommendedRoles = recommendedRoles,
        disclaimer = disclaimer,
    )
}

fun SubscriptionStatusDto.toDomain(): MembershipStatus {
    return MembershipStatus(
        membershipLevel = membershipLevel.toMembershipTier(),
        plan = plan.toMembershipTier(),
        dailyMessages = limits.dailyMessages,
        baseRoleLimit = limits.baseRoleLimit,
        customRoleLimit = limits.customRoleLimit,
        memoryLimit = limits.memoryLimit,
        weeklyMoodSummary = limits.weeklyMoodSummary,
        proactiveGreeting = limits.proactiveGreeting,
        longContext = limits.longContext,
        messagesToday = usage.messagesToday,
        customRoles = usage.customRoles,
        memories = usage.memories,
    )
}

private fun buildGreeting(name: String, relationshipType: RelationshipType): String {
    return when (relationshipType) {
        RelationshipType.LISTENER -> "I am $name. You can take your time; I will listen first."
        RelationshipType.SUPPORT_PARTNER -> "I am $name. Let us steady the feeling first."
        RelationshipType.ROMANTIC_PARTNER -> "I am $name. I will stay transparent as AI and listen carefully."
        RelationshipType.STUDY_BUDDY -> "I am $name. Let us start with the smallest next step."
        RelationshipType.BEDTIME_COMPANION -> "I am $name. Tonight we can slow down."
        RelationshipType.MENTOR -> "I am $name. Let us look at the core issue first."
        RelationshipType.CUSTOM -> "I am $name. Good to meet you."
    }
}

private fun buildSampleReply(name: String, relationshipType: RelationshipType): String {
    return when (relationshipType) {
        RelationshipType.LISTENER -> "I will not rush into advice. What part of today feels heaviest?"
        RelationshipType.SUPPORT_PARTNER -> "Let us pause before solving it and name the feeling first."
        RelationshipType.ROMANTIC_PARTNER -> "You can speak slowly. I will listen within clear boundaries."
        RelationshipType.STUDY_BUDDY -> "Start with the smallest step; movement comes before motivation."
        RelationshipType.BEDTIME_COMPANION -> "Do not chase the problem tonight. Let your breathing slow first."
        RelationshipType.MENTOR -> "List facts, blockers, and the next step. We will review directly."
        RelationshipType.CUSTOM -> "$name is here. We can begin with the sentence you most want to say."
    }
}

private fun String.toEpochMillis(): Long {
    return runCatching { Instant.parse(this).toEpochMilli() }
        .getOrDefault(System.currentTimeMillis())
}

private fun String?.toMembershipTier(): MembershipTier {
    return when (this?.uppercase()) {
        "MONTHLY", "PLUS" -> MembershipTier.PLUS
        "PREMIUM" -> MembershipTier.PREMIUM
        else -> MembershipTier.FREE
    }
}

private fun String?.toRoleCategory(): RoleCategory {
    return when (this?.uppercase()) {
        "EMOTIONAL_SUPPORT" -> RoleCategory.EMOTIONAL_SUPPORT
        "ROMANTIC_COMPANION" -> RoleCategory.ROMANCE
        "SLEEP_COMPANION" -> RoleCategory.SLEEP
        "STUDY_BUDDY" -> RoleCategory.STUDY
        "CAREER_MENTOR" -> RoleCategory.CAREER
        else -> RoleCategory.CUSTOM
    }
}

private fun String?.toRelationshipType(): RelationshipType {
    return when (this?.uppercase()) {
        "LISTENER" -> RelationshipType.LISTENER
        "SUPPORT_PARTNER" -> RelationshipType.SUPPORT_PARTNER
        "ROMANTIC_PARTNER", "VIRTUAL_BOYFRIEND", "VIRTUAL_GIRLFRIEND" -> RelationshipType.ROMANTIC_PARTNER
        "STUDY_BUDDY" -> RelationshipType.STUDY_BUDDY
        "BEDTIME_COMPANION" -> RelationshipType.BEDTIME_COMPANION
        "CAREER_MENTOR" -> RelationshipType.MENTOR
        else -> RelationshipType.CUSTOM
    }
}

private fun String?.toSafetyLevel(): SafetyLevel {
    return when (this?.uppercase()) {
        "LOW" -> SafetyLevel.LOW
        "MEDIUM" -> SafetyLevel.MEDIUM
        "HIGH" -> SafetyLevel.HIGH
        else -> SafetyLevel.STRICT
    }
}

private fun String?.toChatRiskLevel(): ChatRiskLevel {
    return when (this?.uppercase()) {
        "ATTENTION" -> ChatRiskLevel.ATTENTION
        "CRISIS" -> ChatRiskLevel.CRISIS
        else -> ChatRiskLevel.NORMAL
    }
}

private fun String?.toSenderType(): MessageSenderType {
    return when (this?.uppercase()) {
        "USER" -> MessageSenderType.USER
        "SYSTEM" -> MessageSenderType.SYSTEM
        else -> MessageSenderType.AI
    }
}
