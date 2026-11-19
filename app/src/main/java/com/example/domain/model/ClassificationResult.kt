package com.example.domain.model

enum class DecisionSource(val badgeLabel: String) {
    CUSTOM_ALLOW_RULE("Allow Rule"),
    CUSTOM_BLOCK_RULE("Block Rule"),
    PROTECTION_PAUSED("Filter Paused"),
    LANGUAGE_DISABLED("Language Bypassed"),
    TRANSACTION_INBOX_OVERRIDE("Transaction Kept"),
    AI_SMART_FILTER_PREVIEW("AI Smart Filter (Preview)");

    companion object {
        fun fromName(name: String): DecisionSource =
            entries.firstOrNull { it.name == name } ?: AI_SMART_FILTER_PREVIEW
    }
}

data class ClassificationResult(
    val sender: String,
    val body: String,
    val finalCategory: MessageCategory,
    val rawAiCategory: MessageCategory? = null,
    val detectedLanguage: SupportedLanguage,
    val decisionSource: DecisionSource,
    val matchedAllowRule: FilterRule? = null,
    val matchedBlockRule: FilterRule? = null,
    val hadRuleConflict: Boolean = false,
    val confidence: Float,
    val reasons: List<String>,
    val isAiPreviewMode: Boolean = false,
    val aiPreviewNotice: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class ScreenedMessage(
    val id: Long = 0L,
    val sender: String,
    val body: String,
    val category: MessageCategory,
    val decisionSource: DecisionSource,
    val detectedLanguage: SupportedLanguage,
    val confidence: Float,
    val summaryReason: String,
    val originLabel: String,
    val timestamp: Long
)
