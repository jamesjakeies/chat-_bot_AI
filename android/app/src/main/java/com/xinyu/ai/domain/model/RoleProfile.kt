package com.xinyu.ai.domain.model

data class RoleProfile(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val category: RoleCategory,
    val relationshipType: RelationshipType,
    val personalityTags: List<String>,
    val shortDescription: String,
    val greeting: String,
    val sampleReply: String,
    val isAdultOnly: Boolean,
    val isOfficial: Boolean,
    val safetyLevel: SafetyLevel,
)
