package com.xinyu.ai.domain.model

data class UserProfile(
    val id: String,
    val nickname: String,
    val email: String? = null,
    val isMinor: Boolean,
    val ageVerified: Boolean = false,
    val membershipTier: MembershipTier,
)
