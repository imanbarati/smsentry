package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.FilterSettings
import com.example.domain.model.MessageCategory
import com.example.domain.model.ProtectionStats
import com.example.domain.model.SensitivityLevel
import com.example.domain.model.SupportedLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.textSentryDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "textsentry_preferences"
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val PROTECTION_ENABLED = booleanPreferencesKey("protection_enabled")
        val SENSITIVITY_LEVEL = stringPreferencesKey("sensitivity_level")
        val KEEP_TRANSACTIONS_IN_INBOX = booleanPreferencesKey("keep_transactions_in_inbox")
        val LANG_ENGLISH = booleanPreferencesKey("lang_english")
        val LANG_PERSIAN = booleanPreferencesKey("lang_persian")
        val LANG_ARABIC = booleanPreferencesKey("lang_arabic")
        val LANG_SPANISH = booleanPreferencesKey("lang_spanish")
        val ZERO_RETENTION_MODE = booleanPreferencesKey("zero_retention_mode")
        val AUTO_DISMISS_JUNK = booleanPreferencesKey("auto_dismiss_junk")

        val STAT_TOTAL = intPreferencesKey("stat_total")
        val STAT_JUNK = intPreferencesKey("stat_junk")
        val STAT_PROMOTIONS = intPreferencesKey("stat_promotions")
        val STAT_TRANSACTIONS = intPreferencesKey("stat_transactions")
        val STAT_INBOX = intPreferencesKey("stat_inbox")
    }

    val settingsFlow: Flow<FilterSettings> = context.textSentryDataStore.data.map { prefs ->
        val langs = buildSet {
            if (prefs[Keys.LANG_ENGLISH] ?: true) add(SupportedLanguage.ENGLISH)
            if (prefs[Keys.LANG_PERSIAN] ?: true) add(SupportedLanguage.PERSIAN)
            if (prefs[Keys.LANG_ARABIC] ?: false) add(SupportedLanguage.ARABIC)
            if (prefs[Keys.LANG_SPANISH] ?: false) add(SupportedLanguage.SPANISH)
        }
        FilterSettings(
            isProtectionEnabled = prefs[Keys.PROTECTION_ENABLED] ?: true,
            sensitivityLevel = SensitivityLevel.fromName(
                prefs[Keys.SENSITIVITY_LEVEL] ?: SensitivityLevel.BALANCED.name
            ),
            keepTransactionsInInbox = prefs[Keys.KEEP_TRANSACTIONS_IN_INBOX] ?: false,
            enabledLanguages = langs,
            zeroRetentionMode = prefs[Keys.ZERO_RETENTION_MODE] ?: false,
            autoDismissJunkNotifications = prefs[Keys.AUTO_DISMISS_JUNK] ?: true,
            stats = ProtectionStats(
                totalScanned = prefs[Keys.STAT_TOTAL] ?: 0,
                junkBlocked = prefs[Keys.STAT_JUNK] ?: 0,
                promotionsSorted = prefs[Keys.STAT_PROMOTIONS] ?: 0,
                transactionsSorted = prefs[Keys.STAT_TRANSACTIONS] ?: 0,
                inboxAllowed = prefs[Keys.STAT_INBOX] ?: 0
            )
        )
    }

    suspend fun getSettingsSnapshot(): FilterSettings = settingsFlow.first()

    suspend fun setProtectionEnabled(enabled: Boolean) {
        context.textSentryDataStore.edit { it[Keys.PROTECTION_ENABLED] = enabled }
    }

    suspend fun setSensitivityLevel(level: SensitivityLevel) {
        context.textSentryDataStore.edit { it[Keys.SENSITIVITY_LEVEL] = level.name }
    }

    suspend fun setKeepTransactionsInInbox(keep: Boolean) {
        context.textSentryDataStore.edit { it[Keys.KEEP_TRANSACTIONS_IN_INBOX] = keep }
    }

    suspend fun setLanguageEnabled(language: SupportedLanguage, enabled: Boolean) {
        context.textSentryDataStore.edit { prefs ->
            when (language) {
                SupportedLanguage.ENGLISH -> prefs[Keys.LANG_ENGLISH] = enabled
                SupportedLanguage.PERSIAN -> prefs[Keys.LANG_PERSIAN] = enabled
                SupportedLanguage.ARABIC -> prefs[Keys.LANG_ARABIC] = enabled
                SupportedLanguage.SPANISH -> prefs[Keys.LANG_SPANISH] = enabled
                SupportedLanguage.OTHER -> Unit
            }
        }
    }

    suspend fun setZeroRetentionMode(enabled: Boolean) {
        context.textSentryDataStore.edit { it[Keys.ZERO_RETENTION_MODE] = enabled }
    }

    suspend fun setAutoDismissJunkNotifications(enabled: Boolean) {
        context.textSentryDataStore.edit { it[Keys.AUTO_DISMISS_JUNK] = enabled }
    }

    suspend fun incrementStat(category: MessageCategory) {
        context.textSentryDataStore.edit { prefs ->
            prefs[Keys.STAT_TOTAL] = (prefs[Keys.STAT_TOTAL] ?: 0) + 1
            when (category) {
                MessageCategory.JUNK -> prefs[Keys.STAT_JUNK] = (prefs[Keys.STAT_JUNK] ?: 0) + 1
                MessageCategory.PROMOTIONS ->
                    prefs[Keys.STAT_PROMOTIONS] = (prefs[Keys.STAT_PROMOTIONS] ?: 0) + 1
                MessageCategory.TRANSACTIONS ->
                    prefs[Keys.STAT_TRANSACTIONS] = (prefs[Keys.STAT_TRANSACTIONS] ?: 0) + 1
                MessageCategory.INBOX ->
                    prefs[Keys.STAT_INBOX] = (prefs[Keys.STAT_INBOX] ?: 0) + 1
            }
        }
    }

    suspend fun resetStats() {
        context.textSentryDataStore.edit { prefs ->
            prefs[Keys.STAT_TOTAL] = 0
            prefs[Keys.STAT_JUNK] = 0
            prefs[Keys.STAT_PROMOTIONS] = 0
            prefs[Keys.STAT_TRANSACTIONS] = 0
            prefs[Keys.STAT_INBOX] = 0
        }
    }
}
