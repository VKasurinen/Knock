package com.vkasurinen.knock.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val ColorScheme.extended: ExtendedColors
    @ReadOnlyComposable
    @Composable
    get() = LocalExtendedColors.current

/**
 * Semantic tokens from the Knock Figma file ("02 — Colors & Tokens").
 */
@Immutable
data class ExtendedColors(
    // Button states
    val primaryHover: Color,
    val secondaryOutline: Color,
    val secondaryFill: Color,
    val destructiveHover: Color,
    val destructiveSecondaryOutline: Color,
    val disabledOutline: Color,
    val disabledFill: Color,
    val tertiary: Color,
    val successOutline: Color,
    val success: Color,
    val onSuccess: Color,

    // Text variants
    val textPrimary: Color,
    val textOnPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textPlaceholder: Color,
    val textDisabled: Color,
    val textDestructive: Color,

    // Surface variants
    val surfaceContainer: Color,
    val surfaceTopBar: Color,
    val surfaceLower: Color,
    val surfaceHigher: Color,
    val surfaceOutline: Color,
    val surfaceOnBg: Color,
    val overlay: Color,

    // Chat
    val wallpaperTop: Color,
    val wallpaperMid: Color,
    val wallpaperBottom: Color,
    val wallpaperPattern: Color,
    val bubbleOutStart: Color,
    val bubbleOutEnd: Color,
    val onBubbleOut: Color,
    val bubbleIn: Color,
    val onBubbleIn: Color,
    val readReceipt: Color,

    // Accent & status
    val accentBrand: Color,
    val onAccentBrand: Color,
    val quoteName: Color,
    val online: Color,

    // Default avatar (initials) – stronger than surface-higher so it stands out on the top bar
    val avatarBackground: Color,
    val onAvatar: Color,

    // Sender colors used for names and avatar initials in group chats
    val senderColors: List<Color>,
)

val LightExtendedColors = ExtendedColors(
    primaryHover = KnockWood700,
    secondaryOutline = KnockWood300,
    secondaryFill = KnockWood50,
    destructiveHover = KnockBrick700,
    destructiveSecondaryOutline = KnockBrick300,
    disabledOutline = KnockWood150,
    disabledFill = KnockWood100,
    tertiary = KnockWood600,
    successOutline = KnockSage300,
    success = KnockSage600,
    onSuccess = KnockWood50,

    textPrimary = KnockWood850,
    textOnPrimary = KnockWood50,
    textSecondary = KnockMocha700,
    textTertiary = KnockMocha600,
    textPlaceholder = KnockMocha500,
    textDisabled = KnockMocha400,
    textDestructive = KnockBrick600,

    surfaceContainer = KnockWood50,
    surfaceTopBar = KnockWood150,
    surfaceLower = KnockWood150,
    surfaceHigher = KnockWood150,
    surfaceOutline = KnockWood850Alpha10,
    surfaceOnBg = KnockWood50,
    overlay = KnockWood1000Alpha70,

    wallpaperTop = KnockWood200,
    wallpaperMid = KnockWood300,
    wallpaperBottom = KnockWood250,
    wallpaperPattern = KnockWood500,
    bubbleOutStart = KnockWood550,
    bubbleOutEnd = KnockWood700,
    onBubbleOut = KnockWood50,
    bubbleIn = KnockWood50,
    onBubbleIn = KnockWood850,
    readReceipt = KnockMoss300,

    accentBrand = KnockBuff500,
    onAccentBrand = KnockWood850,
    quoteName = KnockCaramel800,
    online = KnockSage500,

    avatarBackground = KnockWood300,
    onAvatar = KnockWood850,

    senderColors = listOf(
        KnockSenderCaramel,
        KnockSenderOlive,
        KnockSenderBerry,
        KnockSenderSlate,
        KnockSenderPlum,
    ),
)

val DarkExtendedColors = ExtendedColors(
    primaryHover = KnockWood300,
    secondaryOutline = KnockEspresso600,
    secondaryFill = KnockEspresso850,
    destructiveHover = KnockBrick200,
    destructiveSecondaryOutline = KnockBrick600,
    disabledOutline = KnockEspresso750,
    disabledFill = KnockEspresso900,
    tertiary = KnockBuff500,
    successOutline = KnockSage600,
    success = KnockSage300,
    onSuccess = KnockEspresso1000,

    textPrimary = KnockWood100,
    textOnPrimary = KnockEspresso1000,
    textSecondary = KnockWood150,
    textTertiary = KnockWood300,
    textPlaceholder = KnockWood300,
    textDisabled = KnockMocha500,
    textDestructive = KnockBrick200,

    surfaceContainer = KnockEspresso750,
    surfaceTopBar = KnockEspresso750,
    surfaceLower = KnockEspresso850,
    surfaceHigher = KnockEspresso750,
    surfaceOutline = KnockWood100Alpha18,
    surfaceOnBg = KnockWood50,
    overlay = KnockWood1000Alpha80,

    wallpaperTop = KnockEspresso850,
    wallpaperMid = KnockEspresso850,
    wallpaperBottom = KnockEspresso850,
    wallpaperPattern = KnockWood400,
    bubbleOutStart = KnockBuff500,
    bubbleOutEnd = KnockCaramel500,
    onBubbleOut = KnockEspresso1000,
    bubbleIn = KnockEspresso700,
    onBubbleIn = KnockWood100,
    readReceipt = KnockMoss700,

    accentBrand = KnockBuff500,
    onAccentBrand = KnockEspresso1000,
    quoteName = KnockBuff500,
    online = KnockSage300,

    avatarBackground = KnockWood300,
    onAvatar = KnockEspresso900,

    senderColors = listOf(
        KnockSenderDarkCaramel,
        KnockSenderDarkOlive,
        KnockSenderDarkBerry,
        KnockSenderDarkSlate,
        KnockSenderDarkPlum,
    ),
)

val LightColorScheme = lightColorScheme(
    primary = KnockWood600,
    onPrimary = KnockWood50,
    primaryContainer = KnockWood150,
    onPrimaryContainer = KnockWood850,

    secondary = KnockMocha700,
    onSecondary = KnockWood50,
    secondaryContainer = KnockWood150,
    onSecondaryContainer = KnockWood850,

    tertiary = KnockWood600,
    onTertiary = KnockWood50,
    tertiaryContainer = KnockTuscan300,
    onTertiaryContainer = KnockWood850,

    error = KnockBrick600,
    onError = KnockWood50,
    errorContainer = KnockBrick200,
    onErrorContainer = KnockBrick700,

    background = KnockWood700,
    onBackground = KnockWood50,
    surface = KnockWood50,
    onSurface = KnockWood850,
    surfaceVariant = KnockWood150,
    onSurfaceVariant = KnockMocha700,
    surfaceContainer = KnockWood50,

    outline = KnockWood850Alpha10,
    outlineVariant = KnockWood300,
    scrim = KnockWood1000Alpha70,
)

val DarkColorScheme = darkColorScheme(
    primary = KnockWood400,
    onPrimary = KnockEspresso1000,
    primaryContainer = KnockEspresso750,
    onPrimaryContainer = KnockWood100,

    secondary = KnockWood150,
    onSecondary = KnockEspresso1000,
    secondaryContainer = KnockEspresso750,
    onSecondaryContainer = KnockWood100,

    tertiary = KnockBuff500,
    onTertiary = KnockEspresso1000,
    tertiaryContainer = KnockEspresso750,
    onTertiaryContainer = KnockWood100,

    error = KnockBrick300,
    onError = KnockEspresso1000,
    errorContainer = KnockBrick600,
    onErrorContainer = KnockBrick200,

    background = KnockEspressoBrand,
    onBackground = KnockWood50,
    surface = KnockEspresso850,
    onSurface = KnockWood100,
    surfaceVariant = KnockEspresso750,
    onSurfaceVariant = KnockWood150,
    surfaceContainer = KnockEspresso750,

    outline = KnockWood100Alpha18,
    outlineVariant = KnockEspresso600,
    scrim = KnockWood1000Alpha80,
)
