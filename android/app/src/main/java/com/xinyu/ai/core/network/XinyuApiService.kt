package com.xinyu.ai.core.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface XinyuApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiEnvelope<AuthTokenResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiEnvelope<AuthTokenResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): ApiEnvelope<AuthTokenResponse>

    @GET("users/me")
    suspend fun getProfile(): ApiEnvelope<ProfileResponse>

    @PATCH("users/me/age-verification")
    suspend fun verifyAge(@Body request: AgeVerificationRequest): ApiEnvelope<ProfileResponse>

    @GET("roles")
    suspend fun getRoles(
        @Query("category") category: String? = null,
    ): ApiEnvelope<List<RoleDto>>

    @GET("roles/{id}")
    suspend fun getRole(@Path("id") roleId: String): ApiEnvelope<RoleDto>

    @POST("roles/custom")
    suspend fun createCustomRole(
        @Body request: CreateCustomRoleRequest,
    ): ApiEnvelope<RoleDto>

    @GET("chat/sessions")
    suspend fun getChatSessions(): ApiEnvelope<List<ChatSessionDto>>

    @POST("chat/sessions")
    suspend fun createChatSession(
        @Body request: CreateChatSessionRequest,
    ): ApiEnvelope<ChatSessionDto>

    @GET("chat/sessions/{id}/messages")
    suspend fun getChatMessages(
        @Path("id") sessionId: String,
    ): ApiEnvelope<List<ChatMessageDto>>

    @POST("chat/sessions/{id}/messages")
    suspend fun sendMessage(
        @Path("id") sessionId: String,
        @Body request: SendMessageRequest,
    ): ApiEnvelope<SendMessageResponse>

    @DELETE("chat/sessions/{id}")
    suspend fun deleteChatSession(
        @Path("id") sessionId: String,
    ): ApiEnvelope<Map<String, Boolean>>

    @GET("memories")
    suspend fun getMemories(
        @Query("roleId") roleId: String? = null,
        @Query("includePending") includePending: Boolean = true,
    ): ApiEnvelope<List<MemoryDto>>

    @POST("memories")
    suspend fun createMemory(
        @Body request: CreateMemoryRequest,
    ): ApiEnvelope<MemoryDto>

    @POST("memories/confirm-sensitive")
    suspend fun confirmSensitiveMemory(
        @Body request: ConfirmSensitiveMemoryRequest,
    ): ApiEnvelope<MemoryDto>

    @DELETE("memories/{id}")
    suspend fun deleteMemory(
        @Path("id") memoryId: String,
    ): ApiEnvelope<Map<String, Boolean>>

    @PATCH("memories/role-settings/{roleId}")
    suspend fun updateRoleMemorySetting(
        @Path("roleId") roleId: String,
        @Body request: RoleMemorySettingRequest,
    ): ApiEnvelope<Map<String, Boolean>>

    @POST("moods")
    suspend fun createMood(
        @Body request: CreateMoodRequest,
    ): ApiEnvelope<MoodLogDto>

    @GET("moods/recent")
    suspend fun getRecentMoods(): ApiEnvelope<List<MoodLogDto>>

    @GET("moods/weekly-summary")
    suspend fun getWeeklyMoodSummary(): ApiEnvelope<MoodWeeklySummaryDto>

    @GET("subscriptions/me")
    suspend fun getSubscriptionMe(): ApiEnvelope<SubscriptionStatusDto>

    @POST("subscriptions/mock-upgrade")
    suspend fun mockUpgrade(
        @Body request: MockUpgradeRequest,
    ): ApiEnvelope<SubscriptionStatusDto>

    @POST("reports")
    suspend fun createReport(
        @Body request: CreateReportRequest,
    ): ApiEnvelope<ReportDto>
}
