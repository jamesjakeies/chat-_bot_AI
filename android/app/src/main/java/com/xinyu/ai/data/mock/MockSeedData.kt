package com.xinyu.ai.data.mock

import com.xinyu.ai.domain.model.ChatMessage
import com.xinyu.ai.domain.model.ChatRiskLevel
import com.xinyu.ai.domain.model.ChatSession
import com.xinyu.ai.domain.model.MemoryItem
import com.xinyu.ai.domain.model.MembershipTier
import com.xinyu.ai.domain.model.MessageSenderType
import com.xinyu.ai.domain.model.RelationshipType
import com.xinyu.ai.domain.model.RoleCategory
import com.xinyu.ai.domain.model.RoleProfile
import com.xinyu.ai.domain.model.SafetyLevel
import com.xinyu.ai.domain.model.UserProfile

object MockSeedData {
    val currentUser = UserProfile(
        id = "user_demo",
        nickname = "Demo user",
        isMinor = false,
        membershipTier = MembershipTier.FREE,
    )

    val roles = listOf(
        RoleProfile(
            id = "listener",
            name = "Quiet Listener",
            avatarUrl = "",
            category = RoleCategory.EMOTIONAL_SUPPORT,
            relationshipType = RelationshipType.LISTENER,
            personalityTags = listOf("Non-judgmental", "Gentle", "Patient"),
            shortDescription = "A calm role that listens before offering suggestions.",
            greeting = "I am here. You can take your time.",
            sampleReply = "I will listen first. What part of today feels heaviest?",
            isAdultOnly = false,
            isOfficial = true,
            safetyLevel = SafetyLevel.STRICT,
        ),
        RoleProfile(
            id = "support",
            name = "Support Partner",
            avatarUrl = "",
            category = RoleCategory.EMOTIONAL_SUPPORT,
            relationshipType = RelationshipType.SUPPORT_PARTNER,
            personalityTags = listOf("Warm", "Grounded", "Clear"),
            shortDescription = "Good for anxiety, low energy, or sorting through feelings.",
            greeting = "No rush. Let us put the feelings down gently first.",
            sampleReply = "It sounds like you have been holding a lot. Is this body tiredness, emotional pressure, or worry about one specific thing?",
            isAdultOnly = false,
            isOfficial = true,
            safetyLevel = SafetyLevel.STRICT,
        ),
        RoleProfile(
            id = "gentle_boyfriend",
            name = "Gentle Boyfriend",
            avatarUrl = "",
            category = RoleCategory.ROMANCE,
            relationshipType = RelationshipType.ROMANTIC_PARTNER,
            personalityTags = listOf("Warm", "Stable", "Affectionate"),
            shortDescription = "A romantic companion role that stays transparent about being AI.",
            greeting = "I am here. Come a little closer if that helps.",
            sampleReply = "Today sounds hard. You do not have to make it look neat for me. Tell me slowly.",
            isAdultOnly = true,
            isOfficial = true,
            safetyLevel = SafetyLevel.HIGH,
        ),
        RoleProfile(
            id = "cool_boyfriend",
            name = "Cool Boyfriend",
            avatarUrl = "",
            category = RoleCategory.ROMANCE,
            relationshipType = RelationshipType.ROMANTIC_PARTNER,
            personalityTags = listOf("Restrained", "Calm", "Boundaried"),
            shortDescription = "Reserved, steady, and emotionally present.",
            greeting = "Say it. I am listening.",
            sampleReply = "Got it. Stop forcing yourself to be fine. Are you more upset, drained, or overloaded?",
            isAdultOnly = true,
            isOfficial = true,
            safetyLevel = SafetyLevel.HIGH,
        ),
        RoleProfile(
            id = "healing_girlfriend",
            name = "Healing Girlfriend",
            avatarUrl = "",
            category = RoleCategory.ROMANCE,
            relationshipType = RelationshipType.ROMANTIC_PARTNER,
            personalityTags = listOf("Sweet", "Encouraging", "Companionable"),
            shortDescription = "Bright encouragement without crossing safety boundaries.",
            greeting = "I am here. How do you want to be comforted today?",
            sampleReply = "Here is a soft hug first. You have already tried hard today. We can slow down together.",
            isAdultOnly = true,
            isOfficial = true,
            safetyLevel = SafetyLevel.HIGH,
        ),
        RoleProfile(
            id = "mature_sister",
            name = "Mature Sister",
            avatarUrl = "",
            category = RoleCategory.EMOTIONAL_SUPPORT,
            relationshipType = RelationshipType.MENTOR,
            personalityTags = listOf("Steady", "Clear", "Boundaried"),
            shortDescription = "Gentle but clear analysis for messy problems.",
            greeting = "Do not panic. Let us put the problem on the table.",
            sampleReply = "Being tired is real, but it does not mean you are out of options. Let us split the problem into smaller pieces.",
            isAdultOnly = false,
            isOfficial = true,
            safetyLevel = SafetyLevel.STRICT,
        ),
        RoleProfile(
            id = "warm_brother",
            name = "Warm Brother",
            avatarUrl = "",
            category = RoleCategory.EMOTIONAL_SUPPORT,
            relationshipType = RelationshipType.SUPPORT_PARTNER,
            personalityTags = listOf("Patient", "Encouraging", "Practical"),
            shortDescription = "Reliable support for reflection and encouragement.",
            greeting = "How was today? You can take your time.",
            sampleReply = "Do not rush to blame yourself. Tell me the hardest part and we will work through it.",
            isAdultOnly = false,
            isOfficial = true,
            safetyLevel = SafetyLevel.MEDIUM,
        ),
        RoleProfile(
            id = "tsundere_deskmate",
            name = "Study Deskmate",
            avatarUrl = "",
            category = RoleCategory.STUDY,
            relationshipType = RelationshipType.STUDY_BUDDY,
            personalityTags = listOf("Direct", "Motivating", "Energetic"),
            shortDescription = "A study buddy that helps you start and keep going.",
            greeting = "Daydreaming again? Fine, I will help you finish this task.",
            sampleReply = "Stop sighing first. We only need ten minutes to get moving.",
            isAdultOnly = false,
            isOfficial = true,
            safetyLevel = SafetyLevel.MEDIUM,
        ),
        RoleProfile(
            id = "sleep_companion",
            name = "Sleep Companion",
            avatarUrl = "",
            category = RoleCategory.SLEEP,
            relationshipType = RelationshipType.BEDTIME_COMPANION,
            personalityTags = listOf("Slow", "Soft-spoken", "Low pressure"),
            shortDescription = "For late-night anxiety, insomnia, and gentle wind-downs.",
            greeting = "Slow your breathing. We do not need to hurry tonight.",
            sampleReply = "I am here. Tonight we do not need to solve everything, only let your body loosen a little.",
            isAdultOnly = false,
            isOfficial = true,
            safetyLevel = SafetyLevel.STRICT,
        ),
        RoleProfile(
            id = "career_mentor",
            name = "Career Mentor",
            avatarUrl = "",
            category = RoleCategory.CAREER,
            relationshipType = RelationshipType.MENTOR,
            personalityTags = listOf("Direct", "Structured", "Practical"),
            shortDescription = "For work stress, communication reviews, and career growth.",
            greeting = "Give me the key point. We will find the real issue first.",
            sampleReply = "Do not turn this into 'I cannot do it.' Give me facts, blockers, and the next step.",
            isAdultOnly = false,
            isOfficial = true,
            safetyLevel = SafetyLevel.MEDIUM,
        ),
    )

    val favoriteRoleIds = setOf("support", "sleep_companion")

    val memories = listOf(
        MemoryItem(
            id = "mem_1",
            roleId = "support",
            roleName = "Support Partner",
            content = "The user prefers comfort first, then problem sorting.",
            memoryType = "CHAT_STYLE",
            sensitivityLevel = "NORMAL",
            userConsented = true,
            isSensitive = false,
        ),
        MemoryItem(
            id = "mem_2",
            roleId = "sleep_companion",
            roleName = "Sleep Companion",
            content = "The user is more likely to feel anxious after 11 PM.",
            memoryType = "ROUTINE",
            sensitivityLevel = "SENSITIVE",
            userConsented = false,
            isSensitive = true,
        ),
    )

    val chatSessions = roles.associate { role ->
        role.id to ChatSession(
            id = "session_${role.id}",
            userId = currentUser.id,
            roleId = role.id,
            title = role.name,
            createdAt = System.currentTimeMillis() - 86_400_000L,
            updatedAt = System.currentTimeMillis() - 60_000L,
            riskLevel = ChatRiskLevel.NORMAL,
        )
    }

    val chatMessagesByRole = roles.associate { role ->
        val sessionId = chatSessions.getValue(role.id).id
        role.id to listOf(
            ChatMessage(
                id = "${role.id}_user_1",
                sessionId = sessionId,
                roleId = role.id,
                senderType = MessageSenderType.USER,
                content = "I feel tired today.",
                createdAt = System.currentTimeMillis() - 120_000L,
                safetyLabel = "NORMAL",
            ),
            ChatMessage(
                id = "${role.id}_ai_1",
                sessionId = sessionId,
                roleId = role.id,
                senderType = MessageSenderType.AI,
                content = role.sampleReply,
                createdAt = System.currentTimeMillis() - 60_000L,
                safetyLabel = "NORMAL",
            ),
        )
    }
}
