package com.xinyu.ai.data.mock

import com.xinyu.ai.domain.model.ChatSceneMode
import com.xinyu.ai.domain.model.RoleProfile

object MockChatReplyFactory {
    fun buildReply(
        role: RoleProfile,
        userMessage: String,
        sceneMode: ChatSceneMode,
    ): String {
        val focus = userMessage.trim().ifBlank { "today" }
        val opening = sceneOpening(sceneMode)

        return when (role.id) {
            "listener" -> "$opening I will listen first. When you say \"$focus\", what moment feels heaviest?"
            "support" -> "$opening Let us separate the feeling from the facts and make the next step smaller."
            "gentle_boyfriend" -> "$opening Today sounds hard. You can tell me slowly; I am here with you."
            "cool_boyfriend" -> "$opening Got it. Do not force yourself to look fine. Tell me the key point."
            "healing_girlfriend" -> "$opening Take a breath first. You have already carried a lot today."
            "mature_sister" -> "$opening Let us put the problem on the table and sort what you can control."
            "warm_brother" -> "$opening Do not rush to blame yourself. Tell me the hardest part first."
            "tsundere_deskmate" -> "$opening Fine, we start with ten minutes. Moving first is enough."
            "sleep_companion" -> "$opening Tonight we do not need to solve everything. Let your breathing slow down."
            "career_mentor" -> "$opening Give me the facts, the blocker, and the next step. We will handle them in order."
            else -> "$opening ${role.name} is listening. Keep going in whatever way feels easiest."
        }
    }

    private fun sceneOpening(sceneMode: ChatSceneMode): String {
        return when (sceneMode) {
            ChatSceneMode.COMFORT_MODE -> "[comfort_mode]"
            ChatSceneMode.VENT_MODE -> "[vent_mode]"
            ChatSceneMode.SLEEP_MODE -> "[sleep_mode]"
            ChatSceneMode.REVIEW_MODE -> "[review_mode]"
            ChatSceneMode.ENCOURAGE_MODE -> "[encourage_mode]"
            ChatSceneMode.CALM_MODE -> "[calm_mode]"
            ChatSceneMode.DEFAULT -> ""
        }
    }
}
