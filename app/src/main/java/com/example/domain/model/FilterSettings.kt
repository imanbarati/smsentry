package com.example.domain.model

data class ProtectionStats(
    val totalScanned: Int = 0,
    val junkBlocked: Int = 0,
    val promotionsSorted: Int = 0,
    val transactionsSorted: Int = 0,
    val inboxAllowed: Int = 0
)

data class FilterSettings(
    val isProtectionEnabled: Boolean = true,
    val sensitivityLevel: SensitivityLevel = SensitivityLevel.BALANCED,
    val keepTransactionsInInbox: Boolean = false,
    val enabledLanguages: Set<SupportedLanguage> = setOf(
        SupportedLanguage.ENGLISH,
        SupportedLanguage.PERSIAN
    ),
    val zeroRetentionMode: Boolean = false,
    val autoDismissJunkNotifications: Boolean = true,
    val stats: ProtectionStats = ProtectionStats()
)
