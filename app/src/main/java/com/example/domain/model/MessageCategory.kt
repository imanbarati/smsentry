package com.example.domain.model

enum class MessageCategory(
    val displayName: String,
    val shortLabel: String,
    val description: String
) {
    INBOX(
        displayName = "Inbox",
        shortLabel = "Inbox",
        description = "Personal messages & explicitly allowed senders"
    ),
    TRANSACTIONS(
        displayName = "Transactions",
        shortLabel = "Transactions",
        description = "Bank codes, OTPs, receipts, and delivery alerts"
    ),
    PROMOTIONS(
        displayName = "Promotions",
        shortLabel = "Promotions",
        description = "Sales, coupons, loyalty campaigns, and marketing"
    ),
    JUNK(
        displayName = "Junk",
        shortLabel = "Junk",
        description = "Scams, phishing links, impersonation, and spam"
    );

    companion object {
        fun fromName(name: String): MessageCategory =
            entries.firstOrNull { it.name == name } ?: INBOX
    }
}
