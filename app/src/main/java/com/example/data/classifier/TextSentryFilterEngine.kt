package com.example.data.classifier

import com.example.data.local.CustomRuleDao
import com.example.data.local.FilteredMessageDao
import com.example.data.local.FilteredMessageEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.domain.model.ClassificationResult
import com.example.domain.model.DecisionSource
import com.example.domain.model.FilterRule
import com.example.domain.model.FilterSettings
import com.example.domain.model.MessageCategory
import com.example.domain.model.RuleAction
import com.example.domain.model.SupportedLanguage

class TextSentryFilterEngine(
    private val customRuleDao: CustomRuleDao,
    private val filteredMessageDao: FilteredMessageDao,
    private val preferencesRepository: UserPreferencesRepository,
    private val aiClassifier: AiClassifierRepository
) {
    /**
     * Evaluates a message through the full TextSentry pipeline:
     * 1. Protection enabled check
     * 2. Custom Rules ("Your Rules Come First" — Allow always overrides Block on conflict)
     * 3. Language filter toggle check
     * 4. AI Smart Filter (via AiClassifierRepository)
     * 5. Keep Transactions in Main Inbox preference
     */
    suspend fun evaluateMessage(
        sender: String,
        body: String,
        originLabel: String,
        recordToLog: Boolean
    ): ClassificationResult {
        val cleanSender = sender.trim().ifEmpty { "Unknown Sender" }
        val cleanBody = body.trim()
        val settings: FilterSettings = preferencesRepository.getSettingsSnapshot()
        val detectedLanguage = SupportedLanguage.detect(cleanBody.ifEmpty { cleanSender })

        if (!settings.isProtectionEnabled) {
            val result = ClassificationResult(
                sender = cleanSender,
                body = cleanBody,
                finalCategory = MessageCategory.INBOX,
                detectedLanguage = detectedLanguage,
                decisionSource = DecisionSource.PROTECTION_PAUSED,
                confidence = 1.0f,
                reasons = listOf("TextSentry protection is currently paused. Message left in Inbox.")
            )
            return result
        }

        // Step 1: Evaluate Custom Rules ("Your Rules Come First")
        val enabledRules: List<FilterRule> = customRuleDao.getEnabledRulesSnapshot().map { it.toDomain() }
        val matchedAllow = enabledRules.firstOrNull {
            it.action == RuleAction.ALLOW && it.matches(cleanSender, cleanBody)
        }
        val matchedBlock = enabledRules.firstOrNull {
            it.action == RuleAction.BLOCK && it.matches(cleanSender, cleanBody)
        }

        val result: ClassificationResult = when {
            matchedAllow != null -> {
                customRuleDao.incrementHitCount(matchedAllow.id)
                val hadConflict = matchedBlock != null
                val reasons = buildList {
                    add("Matched your Allow rule \"${matchedAllow.pattern}\" (${matchedAllow.target.shortLabel}).")
                    if (hadConflict && matchedBlock != null) {
                        add("Conflict resolved: Block rule \"${matchedBlock.pattern}\" also matched, but Allow rules always take priority.")
                    }
                    add("Bypassed Smart Filter because custom rules run first.")
                }
                ClassificationResult(
                    sender = cleanSender,
                    body = cleanBody,
                    finalCategory = MessageCategory.INBOX,
                    detectedLanguage = detectedLanguage,
                    decisionSource = DecisionSource.CUSTOM_ALLOW_RULE,
                    matchedAllowRule = matchedAllow,
                    matchedBlockRule = matchedBlock,
                    hadRuleConflict = hadConflict,
                    confidence = 1.0f,
                    reasons = reasons
                )
            }
            matchedBlock != null -> {
                customRuleDao.incrementHitCount(matchedBlock.id)
                ClassificationResult(
                    sender = cleanSender,
                    body = cleanBody,
                    finalCategory = matchedBlock.blockCategory,
                    detectedLanguage = detectedLanguage,
                    decisionSource = DecisionSource.CUSTOM_BLOCK_RULE,
                    matchedBlockRule = matchedBlock,
                    confidence = 1.0f,
                    reasons = listOf(
                        "Matched your Block rule \"${matchedBlock.pattern}\" (${matchedBlock.target.shortLabel}).",
                        "Sorted directly into ${matchedBlock.blockCategory.displayName} without invoking the Smart Filter."
                    )
                )
            }
            // Step 2: Check language toggle
            detectedLanguage in SupportedLanguage.toggleableLanguages &&
                detectedLanguage !in settings.enabledLanguages -> {
                ClassificationResult(
                    sender = cleanSender,
                    body = cleanBody,
                    finalCategory = MessageCategory.INBOX,
                    detectedLanguage = detectedLanguage,
                    decisionSource = DecisionSource.LANGUAGE_DISABLED,
                    confidence = 1.0f,
                    reasons = listOf(
                        "Detected language: ${detectedLanguage.displayName} (${detectedLanguage.nativeName}).",
                        "Smart Filter is turned off for ${detectedLanguage.displayName} in Settings, so message stays in Inbox."
                    )
                )
            }
            // Step 3: Run AI Smart Filter
            else -> {
                val aiOutput = aiClassifier.classifyMessage(
                    sender = cleanSender,
                    body = cleanBody,
                    language = detectedLanguage,
                    sensitivity = settings.sensitivityLevel
                )
                val keepTxOverride = aiOutput.category == MessageCategory.TRANSACTIONS &&
                    settings.keepTransactionsInInbox
                val finalCategory = if (keepTxOverride) MessageCategory.INBOX else aiOutput.category
                val decisionSource = if (keepTxOverride) {
                    DecisionSource.TRANSACTION_INBOX_OVERRIDE
                } else {
                    DecisionSource.AI_SMART_FILTER_PREVIEW
                }
                val reasons = buildList {
                    addAll(aiOutput.reasons)
                    if (keepTxOverride) {
                        add("Kept in Main Inbox because \"Keep Transactions in Main Inbox\" is enabled.")
                    }
                }
                ClassificationResult(
                    sender = cleanSender,
                    body = cleanBody,
                    finalCategory = finalCategory,
                    rawAiCategory = aiOutput.category,
                    detectedLanguage = detectedLanguage,
                    decisionSource = decisionSource,
                    confidence = aiOutput.confidence,
                    reasons = reasons,
                    isAiPreviewMode = aiOutput.isPlaceholderPreview,
                    aiPreviewNotice = aiOutput.previewNotice
                )
            }
        }

        if (recordToLog) {
            preferencesRepository.incrementStat(result.finalCategory)
            if (!settings.zeroRetentionMode) {
                filteredMessageDao.insertMessage(
                    FilteredMessageEntity(
                        sender = result.sender,
                        body = result.body,
                        category = result.finalCategory.name,
                        decisionSource = result.decisionSource.name,
                        detectedLanguage = result.detectedLanguage.name,
                        confidence = result.confidence,
                        summaryReason = result.reasons.firstOrNull().orEmpty(),
                        originLabel = originLabel,
                        timestamp = result.timestamp
                    )
                )
            }
        }

        return result
    }
}
