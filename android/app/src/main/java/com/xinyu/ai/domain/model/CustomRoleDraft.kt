package com.xinyu.ai.domain.model

data class CustomRoleDraft(
    val name: String,
    val avatarUrl: String = "",
    val category: RoleCategory = RoleCategory.CUSTOM,
    val relationshipType: RelationshipType = RelationshipType.CUSTOM,
    val personality: String,
    val speechStyle: String,
    val systemPrompt: String,
    val safetyLevel: SafetyLevel = SafetyLevel.STRICT,
    val isAdultOnly: Boolean = false,
)
