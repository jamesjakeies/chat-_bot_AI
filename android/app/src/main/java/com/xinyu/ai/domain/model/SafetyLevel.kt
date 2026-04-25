package com.xinyu.ai.domain.model

enum class SafetyLevel(val label: String) {
    LOW("宽松"),
    MEDIUM("标准"),
    HIGH("严格"),
    STRICT("高敏感"),
}
