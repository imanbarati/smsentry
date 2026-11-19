package com.example.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.data.TextSentryContainer
import com.example.domain.model.MessageCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Real Android NotificationListenerService that monitors incoming SMS/RCS notifications
 * from popular Android messaging apps without requesting restricted READ_SMS permissions.
 */
class TextSentryNotificationListener : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val recentMessageFingerprints = LinkedHashMap<String, Long>()

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val pkg = sbn.packageName ?: return
        if (pkg == packageName) return
        if (pkg !in MONITORED_SMS_PACKAGES) return

        val notification = sbn.notification ?: return
        if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return

        val extras = notification.extras ?: return
        val sender = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim().orEmpty()
        val standardText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty()
        val body = bigText.ifEmpty { standardText }

        if (sender.isEmpty() && body.isEmpty()) return
        if (isDuplicateRecent(pkg, sender, body)) return

        val container = TextSentryContainer.getInstance(applicationContext)
        serviceScope.launch {
            try {
                val settings = container.preferencesRepository.getSettingsSnapshot()
                if (!settings.isProtectionEnabled) return@launch

                val result = container.filterEngine.evaluateMessage(
                    sender = sender,
                    body = body,
                    originLabel = "Live SMS",
                    recordToLog = true
                )

                if (result.finalCategory == MessageCategory.JUNK &&
                    settings.autoDismissJunkNotifications
                ) {
                    try {
                        cancelNotification(sbn.key)
                    } catch (cancelError: SecurityException) {
                        Log.w(TAG, "Unable to dismiss notification", cancelError)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to evaluate incoming SMS notification", e)
            }
        }
    }

    private fun isDuplicateRecent(pkg: String, sender: String, body: String): Boolean {
        val now = System.currentTimeMillis()
        val key = "$pkg|$sender|$body"
        synchronized(recentMessageFingerprints) {
            val prev = recentMessageFingerprints[key]
            if (prev != null && (now - prev) < DUPLICATE_WINDOW_MS) {
                return true
            }
            recentMessageFingerprints[key] = now
            if (recentMessageFingerprints.size > 40) {
                val oldestKey = recentMessageFingerprints.entries.firstOrNull()?.key
                if (oldestKey != null) recentMessageFingerprints.remove(oldestKey)
            }
        }
        return false
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "TextSentryListener"
        private const val DUPLICATE_WINDOW_MS = 10_000L

        val MONITORED_SMS_PACKAGES: Set<String> = setOf(
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging",
            "com.android.messaging",
            "com.android.mms",
            "com.moez.QKSMS",
            "com.textra",
            "com.simplemobiletools.smsmessenger",
            "org.fossify.messages",
            "com.oneplus.mms",
            "com.miui.mms"
        )
    }
}
