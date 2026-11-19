package com.example.data

import android.content.Context
import com.example.data.classifier.AiClassifierRepository
import com.example.data.classifier.PlaceholderAiClassifierRepository
import com.example.data.classifier.TextSentryFilterEngine
import com.example.data.local.TextSentryDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.domain.model.FilterRule
import com.example.domain.model.ScreenedMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TextSentryContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: TextSentryDatabase = TextSentryDatabase.getInstance(appContext)
    val customRuleDao = database.customRuleDao()
    val filteredMessageDao = database.filteredMessageDao()
    val preferencesRepository = UserPreferencesRepository(appContext)
    val aiClassifier: AiClassifierRepository = PlaceholderAiClassifierRepository()
    val filterEngine = TextSentryFilterEngine(
        customRuleDao = customRuleDao,
        filteredMessageDao = filteredMessageDao,
        preferencesRepository = preferencesRepository,
        aiClassifier = aiClassifier
    )

    val rulesFlow: Flow<List<FilterRule>> =
        customRuleDao.observeAllRules().map { list -> list.map { it.toDomain() } }

    val recentMessagesFlow: Flow<List<ScreenedMessage>> =
        filteredMessageDao.observeRecentMessages().map { list -> list.map { it.toDomain() } }

    companion object {
        @Volatile
        private var INSTANCE: TextSentryContainer? = null

        fun getInstance(context: Context): TextSentryContainer {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TextSentryContainer(context).also { INSTANCE = it }
            }
        }
    }
}
