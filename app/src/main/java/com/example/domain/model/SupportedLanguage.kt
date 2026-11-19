package com.example.domain.model

enum class SupportedLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val isCoreModelLanguage: Boolean
) {
    ENGLISH("EN", "English", "English", true),
    PERSIAN("FA", "Persian", "فارسی", true),
    ARABIC("AR", "Arabic", "العربية", false),
    SPANISH("ES", "Spanish", "Español", false),
    OTHER("OTHER", "Other", "Other", false);

    companion object {
        val toggleableLanguages: List<SupportedLanguage> =
            listOf(ENGLISH, PERSIAN, ARABIC, SPANISH)

        fun detect(text: String): SupportedLanguage {
            val trimmed = text.trim()
            if (trimmed.isEmpty()) return ENGLISH

            var arabicScriptCount = 0
            var persianSpecificCount = 0
            var latinCount = 0
            var spanishAccentCount = 0

            val persianSpecificChars = setOf('پ', 'چ', 'ژ', 'گ', 'ک', 'ی', '۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
            val spanishChars = setOf('á', 'é', 'í', 'ó', 'ú', 'ñ', 'ü', '¿', '¡')

            for (ch in trimmed) {
                when {
                    ch in persianSpecificChars -> {
                        arabicScriptCount++
                        persianSpecificCount++
                    }
                    ch in '\u0600'..'\u06FF' || ch in '\u0750'..'\u077F' -> {
                        arabicScriptCount++
                    }
                    ch.lowercaseChar() in spanishChars -> {
                        latinCount++
                        spanishAccentCount++
                    }
                    ch in 'A'..'Z' || ch in 'a'..'z' -> {
                        latinCount++
                    }
                }
            }

            if (arabicScriptCount >= 2) {
                val persianKeywords = listOf(
                    "تخفیف", "بانک", "رمز", "لغو", "کد", "خرید", "جایزه", "برداشت",
                    "واریز", "میلیون", "ریال", "تومان", "شما", "برای", "ارسال", "فوری", "کلیک"
                )
                val hasPersianWord = persianKeywords.any { trimmed.contains(it) }
                return if (persianSpecificCount > 0 || hasPersianWord) PERSIAN else ARABIC
            }

            if (latinCount >= 2) {
                val lower = trimmed.lowercase()
                val spanishKeywords = listOf(
                    "descuento", "oferta", "gratis", "premio", "banco", "código",
                    "envío", "paquete", "urgente", "haz clic", "compra", "cuenta"
                )
                val spanishHits = spanishKeywords.count { lower.contains(it) }
                if (spanishAccentCount >= 1 || spanishHits >= 2) {
                    return SPANISH
                }
                return ENGLISH
            }

            return OTHER
        }
    }
}
