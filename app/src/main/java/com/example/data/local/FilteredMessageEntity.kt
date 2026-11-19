package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.DecisionSource
import com.example.domain.model.MessageCategory
import com.example.domain.model.ScreenedMessage
import com.example.domain.model.SupportedLanguage

@Entity(tableName = "filtered_messages")
data class FilteredMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sender: String,
    val body: String,
    val category: String,
    val decisionSource: String,
    val detectedLanguage: String,
    val confidence: Float,
    val summaryReason: String,
    val originLabel: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): ScreenedMessage = ScreenedMessage(
        id = id,
        sender = sender,
        body = body,
        category = MessageCategory.fromName(category),
        decisionSource = DecisionSource.fromName(decisionSource),
        detectedLanguage = SupportedLanguage.entries.firstOrNull { it.name == detectedLanguage }
            ?: SupportedLanguage.ENGLISH,
        confidence = confidence,
        summaryReason = summaryReason,
        originLabel = originLabel,
        timestamp = timestamp
    )
}
