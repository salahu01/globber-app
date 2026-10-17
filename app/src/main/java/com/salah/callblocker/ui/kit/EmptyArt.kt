package com.salah.callblocker.ui.kit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.salah.callblocker.ui.theme.Ceramic
import com.salah.callblocker.ui.theme.LocalCeramic

/** Which empty-state illustration to draw. */
enum class EmptyArt { NoCalls, Quiet, Rules, Search, Allowed }

/**
 * Empty-state vector art, drawn in theme colours on a 160×116dp canvas:
 * a soft halo, a hero object in card colours, and one signal accent.
 */
@Composable
fun EmptyIllustration(art: EmptyArt, modifier: Modifier = Modifier) {
    val c = LocalCeramic.current
    Canvas(modifier.size(160.dp, 116.dp)) {
        val u = size.width / 160f
        halo(c, u)
        when (art) {
            EmptyArt.NoCalls -> phoneWithShield(c, u, check = true)
            EmptyArt.Quiet -> phoneWithShield(c, u, check = false)
            EmptyArt.Rules -> ruleCards(c, u)
            EmptyArt.Search -> searchCards(c, u)
            EmptyArt.Allowed -> phoneWithBubble(c, u)
        }
    }
}

private fun DrawScope.halo(c: Ceramic, u: Float) {
    val ctr = Offset(80 * u, 62 * u)
    drawCircle(Brush.radialGradient(listOf(c.red.copy(alpha = if (c.isDark) 0.16f else 0.10f), Color.Transparent), ctr, 70 * u), 70 * u, ctr)
    drawCircle(c.surface2, 44 * u, ctr)
    // sparkles
    for ((x, y, r) in listOf(Triple(26f, 30f, 2.4f), Triple(136f, 22f, 3f), Triple(140f, 92f, 2f), Triple(18f, 88f, 1.6f))) {
        drawCircle(c.text2.copy(alpha = 0.35f), r * u, Offset(x * u, y * u))
    }
    plus(Offset(122 * u, 46 * u), 5 * u, c.red.copy(alpha = 0.7f), 1.6f * u)
    plus(Offset(34 * u, 58 * u), 4 * u, c.text2.copy(alpha = 0.5f), 1.4f * u)
}

private fun DrawScope.plus(at: Offset, r: Float, col: Color, w: Float) {
    drawLine(col, Offset(at.x - r, at.y), Offset(at.x + r, at.y), w, StrokeCap.Round)
    drawLine(col, Offset(at.x, at.y - r), Offset(at.x, at.y + r), w, StrokeCap.Round)
}

/** Card-coloured rounded rect with hairline and a soft drop. */
private fun DrawScope.card(c: Ceramic, tl: Offset, sz: Size, r: Float) {
    drawRoundRect(Color.Black.copy(alpha = if (c.isDark) 0.35f else 0.07f), tl + Offset(0f, r * 0.35f), sz, CornerRadius(r))
    drawRoundRect(c.surface, tl, sz, CornerRadius(r))
    drawRoundRect(c.line, tl, sz, CornerRadius(r), style = Stroke(1.2f))
}

private fun DrawScope.bar(col: Color, x: Float, y: Float, w: Float, h: Float) =
    drawRoundRect(col, Offset(x, y), Size(w, h), CornerRadius(h / 2))

private fun redGlass(c: Ceramic, center: Offset, r: Float) = Brush.radialGradient(
    0f to lerp(c.red, Color.White, 0.35f), 0.55f to c.red, 1f to lerp(c.red, Color.Black, 0.35f),
    center = center - Offset(r * 0.3f, r * 0.4f), radius = r * 1.6f,
)

private fun DrawScope.phoneWithShield(c: Ceramic, u: Float, check: Boolean) {
    // phone
    val pTl = Offset(58 * u, 22 * u); val pSz = Size(44 * u, 78 * u)
    card(c, pTl, pSz, 10 * u)
    bar(c.line, 72 * u, 28 * u, 16 * u, 3.5f * u)
    for (i in 0..2) bar(c.surface2, 64 * u, (44 + i * 11) * u, (32 - i * 8) * u, 6 * u)
    // shield
    val s = Offset(98 * u, 70 * u); val w = 24 * u; val h = 28 * u
    val shield = Path().apply {
        moveTo(s.x, s.y - h / 2)
        cubicTo(s.x + w * 0.35f, s.y - h * 0.38f, s.x + w / 2, s.y - h * 0.42f, s.x + w / 2, s.y - h * 0.42f)
        lineTo(s.x + w / 2, s.y)
        cubicTo(s.x + w / 2, s.y + h * 0.28f, s.x + w * 0.2f, s.y + h * 0.45f, s.x, s.y + h / 2)
        cubicTo(s.x - w * 0.2f, s.y + h * 0.45f, s.x - w / 2, s.y + h * 0.28f, s.x - w / 2, s.y)
        lineTo(s.x - w / 2, s.y - h * 0.42f)
        cubicTo(s.x - w / 2, s.y - h * 0.42f, s.x - w * 0.35f, s.y - h * 0.38f, s.x, s.y - h / 2)
        close()
    }
    drawPath(shield, Color.Black.copy(alpha = 0.12f), alpha = 1f)
    drawPath(shield, redGlass(c, s, w * 0.6f))
    val ink = Stroke(2.6f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
    if (check) {
        drawPath(Path().apply { moveTo(s.x - 6 * u, s.y); lineTo(s.x - 1.5f * u, s.y + 4.5f * u); lineTo(s.x + 6.5f * u, s.y - 4 * u) }, Color.White, style = ink)
    } else {
        // "z z" — all quiet
        val zp = Path().apply { moveTo(s.x - 5 * u, s.y - 4 * u); lineTo(s.x + 5 * u, s.y - 4 * u); lineTo(s.x - 5 * u, s.y + 5 * u); lineTo(s.x + 5 * u, s.y + 5 * u) }
        drawPath(zp, Color.White, style = ink)
        val z2 = Path().apply { moveTo(108 * u, 30 * u); lineTo(114 * u, 30 * u); lineTo(108 * u, 36 * u); lineTo(114 * u, 36 * u) }
        drawPath(z2, c.text2.copy(alpha = 0.6f), style = Stroke(1.8f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

private fun DrawScope.ruleCards(c: Ceramic, u: Float) {
    // back card
    card(c, Offset(40 * u, 26 * u), Size(84 * u, 30 * u), 9 * u)
    bar(c.surface2, 50 * u, 37 * u, 40 * u, 7 * u)
    // front rule card, slightly lifted
    val tl = Offset(32 * u, 50 * u); val sz = Size(96 * u, 40 * u)
    card(c, tl, sz, 11 * u)
    // star tile
    val t = Offset(52 * u, 70 * u)
    drawCircle(c.red.copy(alpha = 0.12f), 11 * u, t)
    for (deg in listOf(90.0, 30.0, -30.0)) {
        val r = Math.toRadians(deg); val dx = (6 * u * Math.cos(r)).toFloat(); val dy = (6 * u * Math.sin(r)).toFloat()
        drawLine(c.red, Offset(t.x - dx, t.y - dy), Offset(t.x + dx, t.y + dy), 2.4f * u, StrokeCap.Round)
    }
    bar(c.text.copy(alpha = 0.75f), 68 * u, 63 * u, 30 * u, 5 * u)
    bar(c.text2.copy(alpha = 0.4f), 68 * u, 73 * u, 20 * u, 4 * u)
    // toggle on
    drawRoundRect(c.ok, Offset(104 * u, 64 * u), Size(16 * u, 10 * u), CornerRadius(5 * u))
    drawCircle(Color.White, 3.6f * u, Offset(115 * u, 69 * u))
}

private fun DrawScope.searchCards(c: Ceramic, u: Float) {
    card(c, Offset(36 * u, 28 * u), Size(80 * u, 62 * u), 11 * u)
    for (i in 0..2) {
        drawCircle(c.surface2, 5 * u, Offset(50 * u, (44 + i * 16) * u))
        bar(c.surface2, 60 * u, (41 + i * 16) * u, (44 - i * 10) * u, 6 * u)
    }
    // magnifier
    val m = Offset(108 * u, 72 * u)
    drawLine(c.text, m + Offset(9 * u, 9 * u), m + Offset(18 * u, 18 * u), 5 * u, StrokeCap.Round)
    drawCircle(c.surface, 13 * u, m)
    drawCircle(c.red.copy(alpha = 0.10f), 13 * u, m)
    drawCircle(c.text, 13 * u, m, style = Stroke(3.2f * u))
    drawLine(c.red, m + Offset(-4 * u, 0f), m + Offset(4 * u, 0f), 2.4f * u, StrokeCap.Round)
}

private fun DrawScope.phoneWithBubble(c: Ceramic, u: Float) {
    card(c, Offset(54 * u, 22 * u), Size(44 * u, 78 * u), 10 * u)
    bar(c.line, 68 * u, 28 * u, 16 * u, 3.5f * u)
    drawCircle(c.surface2, 9 * u, Offset(76 * u, 52 * u))
    bar(c.surface2, 64 * u, 66 * u, 24 * u, 6 * u)
    // green check bubble
    val b = Offset(102 * u, 72 * u)
    drawCircle(Color.Black.copy(alpha = 0.12f), 13 * u, b + Offset(0f, 2 * u))
    drawCircle(Brush.radialGradient(listOf(lerp(c.ok, Color.White, 0.3f), c.ok), b - Offset(4 * u, 5 * u), 20 * u), 13 * u, b)
    drawPath(Path().apply { moveTo(b.x - 5.5f * u, b.y); lineTo(b.x - 1.5f * u, b.y + 4 * u); lineTo(b.x + 6 * u, b.y - 4 * u) }, Color.White,
        style = Stroke(2.6f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
}
