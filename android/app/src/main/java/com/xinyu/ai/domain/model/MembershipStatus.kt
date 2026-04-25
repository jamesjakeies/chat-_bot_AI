package com.xinyu.ai.domain.model

data class MembershipStatus(
    val membershipLevel: MembershipTier,
    val plan: MembershipTier,
    val dailyMessages: Int,
    val baseRoleLimit: Int?,
    val customRoleLimit: Int,
    val memoryLimit: Int,
    val weeklyMoodSummary: Boolean,
    val proactiveGreeting: Boolean,
    val longContext: Boolean,
    val messagesToday: Int,
    val customRoles: Int,
    val memories: Int,
)
