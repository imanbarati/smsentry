package com.example.data.classifier

import com.example.domain.model.MessageCategory
import com.example.domain.model.SensitivityLevel
import com.example.domain.model.SupportedLanguage
import kotlin.math.min

/**
 * Local sample/preview AI classifier used when Firebase AI Logic is not enabled.
 * Provides clearly labeled sample classification output across English, Persian,
 * Arabic, and Spanish while indicating that live Gemini AI requires Firebase.
 */
class PlaceholderAiClassifierRepository : AiClassifierRepository {

    companion object {
        const val FIREBASE_PREVIEW_NOTICE =
            "Sample AI Preview — Live Gemini AI classification requires Firebase AI Logic."
    }

    override suspend fun classifyMessage(
        sender: String,
        body: String,
        language: SupportedLanguage,
        sensitivity: SensitivityLevel
    ): AiClassificationOutput {
        val text = "$sender $body"
        val lower = text.lowercase()

        var junkScore = 0.0f
        var promoScore = 0.0f
        var transactionScore = 0.0f
        val signals = mutableListOf<String>()

        // 1. Suspicious links & phishing cues
        val hasUrl = Regex("(https?://|www\\.|bit\\.ly|tinyurl|\\.xyz|\\.top|\\.click|\\.info|t\\.ly)")
            .containsMatchIn(lower)
        val hasSuspiciousTld = Regex("(\\.xyz|\\.top|\\.click|\\.ru|\\.tk|verify-|track-|login-|claim-|secure-)").containsMatchIn(lower)
        if (hasSuspiciousTld) {
            junkScore += 0.48f
            signals += "Suspicious or shortened phishing-style URL detected"
        } else if (hasUrl) {
            junkScore += 0.16f
            promoScore += 0.14f
            signals += "Contains external link from an unknown sender"
        }

        // 2. Scam & phishing cues (English + Persian + Arabic + Spanish)
        val scamTerms = listOf(
            "urgent", "account suspended", "verify your account", "held at customs",
            "unpaid toll", "pay $", "claim your prize", "won $", "winner", "gift card",
            "crypto", "bitcoin", "wallet locked", "irs", "tax refund", "final notice",
            "click immediately", "act now", "limited time to claim", "ssn", "password expired",
            // Persian scams
            "برنده جایزه", "جایزه نقدی", "قرعه کشی", "کلیک کنید", "حساب شما مسدود",
            "ابلاغیه قضایی", "ثنا", "فوری", "لینک زیر", "دریافت جایزه", "میلیونی",
            // Arabic / Spanish scams
            "فزت بجائزة", "تم تعليق حسابك", "cuenta suspendida", "has ganado", "premio urgente"
        )
        val scamHits = scamTerms.filter { lower.contains(it) || text.contains(it) }
        if (scamHits.isNotEmpty()) {
            junkScore += min(0.75f, 0.30f + scamHits.size * 0.18f)
            signals += "Phishing/urgency phrasing detected (${scamHits.take(2).joinToString(", ")})"
        }

        // 3. Transaction / OTP / Bank / Delivery cues (English + Persian + Arabic + Spanish)
        val hasOtpCode = Regex("\\b\\d{4,8}\\b").containsMatchIn(body) ||
            Regex("[۰-۹]{4,8}").containsMatchIn(body)
        val transactionTerms = listOf(
            "verification code", "security code", "one-time", "otp", "2fa", "do not share",
            "bank", "receipt", "charged", "debited", "credited", "balance", "statement",
            "order #", "shipped", "out for delivery", "delivered", "tracking", "appointment",
            "flight", "boarding", "reservation", "invoice",
            // Persian transactions
            "رمز پویا", "کد تایید", "کد ورود", "بانک", "برداشت", "واریز", "مانده",
            "تراکنش", "حساب شما", "ریال", "تومان", "مرسوله", "تحویل", "کد رهگیری", "نوبت",
            // Arabic / Spanish transactions
            "رمز التحقق", "كود التفعيل", "تم خصم", "código de verificación", "tu pedido", "banco"
        )
        val txHits = transactionTerms.filter { lower.contains(it) || text.contains(it) }
        if (txHits.isNotEmpty()) {
            transactionScore += min(0.86f, 0.36f + txHits.size * 0.16f)
            if (hasOtpCode) {
                transactionScore += 0.18f
                signals += "Numeric verification/transaction code pattern detected"
            }
            signals += "Transactional signal matched (${txHits.take(2).joinToString(", ")})"
        }

        // 4. Promotional / marketing cues (English + Persian + Arabic + Spanish)
        val promoTerms = listOf(
            "off", "% off", "sale", "discount", "coupon", "promo", "deal", "flash sale",
            "limited offer", "shop now", "buy one", "free shipping", "subscribe", "unsubscribe",
            "reply stop", "text stop", "opt out", "use code", "save ", "weekend only",
            "black friday", "cyber monday", "exclusive offer", "new arrivals",
            // Persian promos
            "تخفیف", "فروش ویژه", "کد تخفیف", "خرید کنید", "لغو ۱۱", "لغو11",
            "پیشنهاد ویژه", "حراج", "رایگان", "جشنواره", "بهاره", "ارسال رایگان",
            // Arabic / Spanish promos
            "خصم", "عرض خاص", "تسوق الآن", "descuento", "oferta", "cupón", "envío gratis"
        )
        val promoHits = promoTerms.filter { lower.contains(it) || text.contains(it) }
        if (promoHits.isNotEmpty()) {
            promoScore += min(0.88f, 0.32f + promoHits.size * 0.17f)
            signals += "Marketing/promotional keywords detected (${promoHits.take(2).joinToString(", ")})"
        }

        // 5. Shortcode / automated commercial sender heuristic
        val isShortcodeOrAlphanumeric = sender.trim().let { s ->
            (s.length in 4..6 && s.all { it.isDigit() }) ||
                (s.any { it.isLetter() } && s.any { it == '-' || it.isDigit() })
        }
        if (isShortcodeOrAlphanumeric && (promoScore > 0f || junkScore > 0f)) {
            promoScore += 0.08f
            junkScore += 0.06f
            signals += "Automated shortcode or commercial sender ID ($sender)"
        }

        // Resolve category respecting sensitivity thresholds
        val finalCategory: MessageCategory
        val confidence: Float

        when {
            junkScore >= sensitivity.junkThreshold && junkScore >= transactionScore -> {
                finalCategory = MessageCategory.JUNK
                confidence = min(0.97f, 0.62f + junkScore * 0.36f)
            }
            transactionScore >= 0.45f && transactionScore >= promoScore && junkScore < 0.65f -> {
                finalCategory = MessageCategory.TRANSACTIONS
                confidence = min(0.96f, 0.64f + transactionScore * 0.32f)
            }
            promoScore >= sensitivity.promoThreshold -> {
                finalCategory = MessageCategory.PROMOTIONS
                confidence = min(0.95f, 0.60f + promoScore * 0.34f)
            }
            sensitivity == SensitivityLevel.MAXIMUM && hasUrl && transactionScore < 0.3f -> {
                finalCategory = MessageCategory.JUNK
                confidence = 0.72f
                signals += "Maximum sensitivity blocked unknown sender containing an external URL"
            }
            else -> {
                finalCategory = MessageCategory.INBOX
                confidence = 0.84f
                if (signals.isEmpty()) {
                    signals += "Conversational tone with no spam, phishing, or commercial triggers"
                } else {
                    signals += "Below ${sensitivity.displayName} sensitivity threshold — kept safely in Inbox"
                }
            }
        }

        val labeledReasons = listOf(
            "[Sample AI Preview] Evaluated for ${language.displayName} (${sensitivity.displayName} sensitivity)"
        ) + signals

        return AiClassificationOutput(
            category = finalCategory,
            confidence = confidence,
            reasons = labeledReasons,
            isPlaceholderPreview = true,
            previewNotice = FIREBASE_PREVIEW_NOTICE
        )
    }
}
