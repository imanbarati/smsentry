package com.example.domain.model

enum class RuleAction(val label: String) {
    ALLOW("Always Allow"),
    BLOCK("Always Block");

    companion object {
        fun fromName(name: String): RuleAction =
            entries.firstOrNull { it.name == name } ?: BLOCK
    }
}

enum class RuleTarget(val label: String, val shortLabel: String) {
    BOTH("Sender or Message", "Sender & Body"),
    SENDER("Sender Only", "Sender"),
    MESSAGE("Message Text Only", "Message");

    companion object {
        fun fromName(name: String): RuleTarget =
            entries.firstOrNull { it.name == name } ?: BOTH
    }
}

data class FilterRule(
    val id: Long = 0L,
    val pattern: String,
    val action: RuleAction,
    val target: RuleTarget = RuleTarget.BOTH,
    val blockCategory: MessageCategory = MessageCategory.JUNK,
    val isEnabled: Boolean = true,
    val hitCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun matches(sender: String, body: String): Boolean {
        if (!isEnabled) return false
        val needle = pattern.trim()
        if (needle.isEmpty()) return false
        return when (target) {
            RuleTarget.SENDER -> sender.contains(needle, ignoreCase = true)
            RuleTarget.MESSAGE -> body.contains(needle, ignoreCase = true)
            RuleTarget.BOTH ->
                sender.contains(needle, ignoreCase = true) ||
                    body.contains(needle, ignoreCase = true)
        }
    }
}
