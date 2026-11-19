package com.example.data.classifier

import com.example.domain.model.MessageCategory
import com.example.domain.model.SensitivityLevel
import com.example.domain.model.SupportedLanguage

data class AiClassificationOutput(
    val category: MessageCategory,
    val confidence: Float,
    val reasons: List<String>,
    val isPlaceholderPreview: Boolean,
    val previewNotice: String?
)

/**
 * Single AI classification contract for TextSentry.
 * Backed by [PlaceholderAiClassifierRepository] when Firebase AI Logic is not enabled,
 * allowing a drop-in Firebase.ai Gemini implementation whenever Firebase is enabled.
 */
interface AiClassifierRepository {
    suspend fun classifyMessage(
        sender: String,
        body: String,
        language: SupportedLanguage,
        sensitivity: SensitivityLevel
    ): AiClassificationOutput
}
