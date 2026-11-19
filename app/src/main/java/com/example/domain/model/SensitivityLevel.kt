package com.example.domain.model

enum class SensitivityLevel(
    val displayName: String,
    val shortName: String,
    val summary: String,
    val junkThreshold: Float,
    val promoThreshold: Float
) {
    LOW(
        displayName = "Low",
        shortName = "Low",
        summary = "Only blocks obvious scams and high-confidence spam. Minimizes false positives.",
        junkThreshold = 0.78f,
        promoThreshold = 0.75f
    ),
    BALANCED(
        displayName = "Balanced",
        shortName = "Balanced",
        summary = "Recommended balance between catching phishing/promos and keeping real texts safe.",
        junkThreshold = 0.58f,
        promoThreshold = 0.56f
    ),
    HIGH(
        displayName = "High",
        shortName = "High",
        summary = "Aggressively filters unsolicited promotions, bulk senders, and suspicious links.",
        junkThreshold = 0.42f,
        promoThreshold = 0.42f
    ),
    MAXIMUM(
        displayName = "Maximum",
        shortName = "Max",
        summary = "Strictest protection. Unknown commercial or automated texts are filtered proactively.",
        junkThreshold = 0.28f,
        promoThreshold = 0.30f
    );

    companion object {
        fun fromName(name: String): SensitivityLevel =
            entries.firstOrNull { it.name == name } ?: BALANCED
    }
}
