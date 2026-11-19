package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.FilterRule
import com.example.domain.model.MessageCategory
import com.example.domain.model.RuleAction
import com.example.domain.model.RuleTarget

@Entity(tableName = "custom_rules")
data class CustomRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val pattern: String,
    val action: String,
    val target: String,
    val blockCategory: String,
    val isEnabled: Boolean = true,
    val hitCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): FilterRule = FilterRule(
        id = id,
        pattern = pattern,
        action = RuleAction.fromName(action),
        target = RuleTarget.fromName(target),
        blockCategory = MessageCategory.fromName(blockCategory),
        isEnabled = isEnabled,
        hitCount = hitCount,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(rule: FilterRule): CustomRuleEntity = CustomRuleEntity(
            id = rule.id,
            pattern = rule.pattern.trim(),
            action = rule.action.name,
            target = rule.target.name,
            blockCategory = rule.blockCategory.name,
            isEnabled = rule.isEnabled,
            hitCount = rule.hitCount,
            createdAt = rule.createdAt
        )
    }
}
