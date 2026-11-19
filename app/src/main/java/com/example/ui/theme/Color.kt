package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Brand Core - Obsidian & Shield Emerald
val Obsidian950 = Color(0xFF071118)
val Obsidian900 = Color(0xFF0B1F2A)
val Obsidian800 = Color(0xFF132A37)
val Obsidian700 = Color(0xFF1E3A4A)

val Emerald500 = Color(0xFF0F9D7A)
val Emerald400 = Color(0xFF10B981)
val Emerald300 = Color(0xFF34D399)
val Emerald100 = Color(0xFFD1FAE5)
val Emerald900 = Color(0xFF064E3B)

// Crisp Porcelain Light Surfaces
val PorcelainBg = Color(0xFFF4F7F9)
val PorcelainSurface = Color(0xFFFFFFFF)
val PorcelainSurfaceVariant = Color(0xFFE8EEF2)
val SlateBorderLight = Color(0xFFD6E0E7)
val SlateTextPrimaryLight = Color(0xFF0C1821)
val SlateTextSecondaryLight = Color(0xFF4B606E)

// Deep Obsidian Dark Surfaces
val DarkBg = Color(0xFF0A1218)
val DarkSurface = Color(0xFF111C24)
val DarkSurfaceVariant = Color(0xFF192833)
val SlateBorderDark = Color(0xFF253846)
val SlateTextPrimaryDark = Color(0xFFF0F5F8)
val SlateTextSecondaryDark = Color(0xFF9AB0BF)

// Semantic Category Accents
val InboxGreenLight = Color(0xFF0D8A6B)
val InboxGreenContainerLight = Color(0xFFD9F7EE)
val InboxGreenDark = Color(0xFF34D399)
val InboxGreenContainerDark = Color(0xFF0B3A2E)

val TransactionAmberLight = Color(0xFFB45309)
val TransactionAmberContainerLight = Color(0xFFFEF3C7)
val TransactionAmberDark = Color(0xFFFBBF24)
val TransactionAmberContainerDark = Color(0xFF3D2A09)

val PromotionIndigoLight = Color(0xFF4338CA)
val PromotionIndigoContainerLight = Color(0xFFE0E7FF)
val PromotionIndigoDark = Color(0xFF818CF8)
val PromotionIndigoContainerDark = Color(0xFF1E2252)

val JunkCrimsonLight = Color(0xFFDC2626)
val JunkCrimsonContainerLight = Color(0xFFFEE2E2)
val JunkCrimsonDark = Color(0xFFF87171)
val JunkCrimsonContainerDark = Color(0xFF451217)

val InfoCyanLight = Color(0xFF0369A1)
val InfoCyanContainerLight = Color(0xFFE0F2FE)
val InfoCyanDark = Color(0xFF38BDF8)
val InfoCyanContainerDark = Color(0xFF0C3046)

@Immutable
data class TextSentrySemanticColors(
    val inbox: Color,
    val inboxContainer: Color,
    val transaction: Color,
    val transactionContainer: Color,
    val promotion: Color,
    val promotionContainer: Color,
    val junk: Color,
    val junkContainer: Color,
    val info: Color,
    val infoContainer: Color,
    val cardBorder: Color,
    val heroBackground: Color,
    val heroAccent: Color,
    val heroOnBackground: Color,
    val heroSubtext: Color
)

val LightTextSentryColors = TextSentrySemanticColors(
    inbox = InboxGreenLight,
    inboxContainer = InboxGreenContainerLight,
    transaction = TransactionAmberLight,
    transactionContainer = TransactionAmberContainerLight,
    promotion = PromotionIndigoLight,
    promotionContainer = PromotionIndigoContainerLight,
    junk = JunkCrimsonLight,
    junkContainer = JunkCrimsonContainerLight,
    info = InfoCyanLight,
    infoContainer = InfoCyanContainerLight,
    cardBorder = SlateBorderLight,
    heroBackground = Obsidian900,
    heroAccent = Emerald400,
    heroOnBackground = Color(0xFFF8FAFC),
    heroSubtext = Color(0xFFB7CCD7)
)

val DarkTextSentryColors = TextSentrySemanticColors(
    inbox = InboxGreenDark,
    inboxContainer = InboxGreenContainerDark,
    transaction = TransactionAmberDark,
    transactionContainer = TransactionAmberContainerDark,
    promotion = PromotionIndigoDark,
    promotionContainer = PromotionIndigoContainerDark,
    junk = JunkCrimsonDark,
    junkContainer = JunkCrimsonContainerDark,
    info = InfoCyanDark,
    infoContainer = InfoCyanContainerDark,
    cardBorder = SlateBorderDark,
    heroBackground = Obsidian950,
    heroAccent = Emerald300,
    heroOnBackground = Color(0xFFF8FAFC),
    heroSubtext = Color(0xFF9EB6C4)
)

val LocalTextSentryColors = staticCompositionLocalOf { LightTextSentryColors }
