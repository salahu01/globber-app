package com.salah.callblocker.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Ceramic design system: bone surfaces, ink text and controls, one signal red
 * reserved for spam / blocked states. Mirrors docs/prototype (app.css "CERAMIC SYSTEM").
 */
@Immutable
data class Ceramic(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val text: Color,
    val text2: Color,
    val line: Color,
    val hair: Color,
    /** Primary control fill (buttons, active chip, tab indicator). */
    val ink: Color,
    /** Content on [ink]. */
    val onInk: Color,
    val red: Color,
    val redBg: Color,
    val redInk: Color,
    val ok: Color,
    val okBg: Color,
    val okInk: Color,
    val info: Color,
    val infoBg: Color,
    val isDark: Boolean,
)

val CeramicLight = Ceramic(
    bg = Color(0xFFF8F7F4),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFF3F1EC),
    text = Color(0xFF15140F),
    text2 = Color(0xFF5E5A51),
    line = Color(0xFFE7E4DC),
    hair = Color(0xFFE7E4DC),
    ink = Color(0xFF15140F),
    onInk = Color(0xFFF7F5F2),
    red = Color(0xFFE8392B),
    redBg = Color(0x14E8392B),
    redInk = Color(0xFFC42A1E),
    ok = Color(0xFF1E9E5A),
    okBg = Color(0x171E9E5A),
    okInk = Color(0xFF147444),
    info = Color(0xFF55606E),
    infoBg = Color(0x1455606E),
    isDark = false,
)

val CeramicDark = Ceramic(
    bg = Color(0xFF0B0B0A),
    surface = Color(0xFF141412),
    surface2 = Color(0xFF1C1B18),
    text = Color(0xFFF3F1EB),
    text2 = Color(0xFFA29E94),
    line = Color(0xFF24231F),
    hair = Color(0xFF24231F),
    ink = Color(0xFFF3F1EB),
    onInk = Color(0xFF141414),
    red = Color(0xFFFF4A3B),
    redBg = Color(0x1CFF4A3B),
    redInk = Color(0xFFFF7B6E),
    ok = Color(0xFF3BC07A),
    okBg = Color(0x1C3BC07A),
    okInk = Color(0xFF6AD49C),
    info = Color(0xFF9AA4B1),
    infoBg = Color(0x1A9AA4B1),
    isDark = true,
)

/** Fixed brand colors used by the splash and glob-star mark regardless of theme. */
object Brand {
    val bone = Color(0xFFEEEBE6)
    val boneTop = Color(0xFFFDFCFA)
    val boneBottom = Color(0xFFE2DED8)
    val ink = Color(0xFF141414)
    val red = Color(0xFFE8392B)
    val redLight = Color(0xFFFF7A63)
    val redDeep = Color(0xFF6E0F09)
    val mute = Color(0xFF9A948D)
}

val LocalCeramic = staticCompositionLocalOf { CeramicLight }
