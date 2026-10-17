package com.salah.callblocker.ui.kit

import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import com.salah.callblocker.ui.theme.Brand
import com.salah.callblocker.ui.theme.CType
import com.salah.callblocker.ui.theme.Ceramic
import com.salah.callblocker.ui.theme.LocalCeramic

val Gutter = 20.dp
private val CardShape = RoundedCornerShape(20.dp)

/** Semantic tint for tiles and badges. */
enum class Tone { Neutral, Red, Ok, Info, Ink }

private fun Ceramic.toneBg(t: Tone) = when (t) {
    Tone.Neutral -> surface2; Tone.Red -> redBg; Tone.Ok -> okBg; Tone.Info -> infoBg; Tone.Ink -> ink
}
private fun Ceramic.toneFg(t: Tone) = when (t) {
    Tone.Neutral -> text; Tone.Red -> redInk; Tone.Ok -> okInk; Tone.Info -> info; Tone.Ink -> onInk
}

/** Accent hue a tone tints its tile with. */
private fun Ceramic.toneHue(t: Tone) = when (t) {
    Tone.Neutral -> text; Tone.Red -> red; Tone.Ok -> ok; Tone.Info -> info; Tone.Ink -> ink
}

/**
 * Premium card surface: solid card fill, 1dp hairline, soft two-layer drop shadow,
 * and (dark mode) a 1px top highlight like light catching the edge.
 */
@Composable
fun Modifier.cardSurface(shape: Shape = CardShape): Modifier {
    val c = LocalCeramic.current
    return this
        .shadow(if (c.isDark) 10.dp else 6.dp, shape, ambientColor = Color.Black.copy(alpha = if (c.isDark) 0.5f else 0.06f), spotColor = Color.Black.copy(alpha = if (c.isDark) 0.6f else 0.10f))
        .clip(shape)
        .background(c.surface)
        .drawBehind {
            if (c.isDark) drawLine(Color.White.copy(alpha = 0.05f), Offset(size.height * 0.1f, 0.5f), Offset(size.width - size.height * 0.1f, 0.5f), 1.dp.toPx())
        }
        .border(1.dp, c.line, shape)
}

/** Icon tile: tone hue washed top-to-bottom into the card, with a faint tone-tinted inner ring. */
@Composable
fun Modifier.toneTile(tone: Tone, shape: Shape): Modifier {
    val c = LocalCeramic.current
    if (tone == Tone.Ink) return this.clip(shape).background(c.ink)
    val hue = c.toneHue(tone)
    val k = if (tone == Tone.Neutral) 0.5f else 1f
    return this
        .shadow(1.dp, shape)
        .clip(shape)
        .background(Brush.verticalGradient(listOf(lerp(c.surface, hue, 0.14f * k), lerp(c.surface, hue, 0.05f * k))))
        .border(1.dp, hue.copy(alpha = 0.18f * k), shape)
}

/** The glob star "*" (wildcard rules): three rounded strokes on a 24dp grid; [weight] sets stroke width. */
fun globStarIcon(weight: Float): ImageVector = ImageVector.Builder("GlobStar$weight", 24.dp, 24.dp, 24f, 24f).apply {
    val stroke = androidx.compose.ui.graphics.SolidColor(Color.Black)
    for (deg in listOf(90.0, 30.0, -30.0)) {
        val r = Math.toRadians(deg)
        val dx = (7.5 * Math.cos(r)).toFloat()
        val dy = (7.5 * Math.sin(r)).toFloat()
        addPath(
            androidx.compose.ui.graphics.vector.PathData { moveTo(12f - dx, 12f - dy); lineTo(12f + dx, 12f + dy) },
            stroke = stroke, strokeLineWidth = weight, strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
    }
}.build()

/* ---------------- brand mark ---------------- */

/** Draws the glob star with the red glass eye, centred in the current DrawScope. */
fun DrawScope.globStar(armHalf: Float, stroke: Float, eye: Float, ink: Color = Brand.ink, rotation: Float = 0f) {
    val c = center
    rotate(rotation, c) {
        for (deg in listOf(0f, 60f, 120f)) rotate(deg, c) {
            drawLine(ink, Offset(c.x, c.y - armHalf), Offset(c.x, c.y + armHalf), stroke, StrokeCap.Round)
        }
    }
    if (eye > 0f) drawCircle(
        Brush.radialGradient(
            0f to Brand.redLight, 0.45f to Brand.red, 1f to Brand.redDeep,
            center = Offset(c.x, c.y - eye * 0.37f), radius = eye * 1.28f,
        ),
        radius = eye, center = c,
    )
}

/** The ceramic app icon: bone tile, ink glob star, red eye. */
@Composable
fun GlobIcon(size: Dp, modifier: Modifier = Modifier, corner: Dp = size * 0.26f, shadow: Boolean = true) {
    Canvas(
        modifier
            .size(size)
            .then(if (shadow) Modifier.shadow(size * 0.12f, RoundedCornerShape(corner), clip = false) else Modifier),
    ) {
        val w = this.size.width
        drawRoundRect(
            Brush.verticalGradient(listOf(Brand.boneTop, Brand.boneBottom)),
            cornerRadius = CornerRadius(corner.toPx()),
        )
        globStar(armHalf = w * 0.148f, stroke = w * 0.067f, eye = w * 0.043f)
    }
}

/* ---------------- surfaces ---------------- */

@Composable
fun CCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = LocalCeramic.current
    Column(
        modifier
            .cardSurface()
            .then(if (onClick != null) Modifier.pressable(onClick) else Modifier)
            .padding(padding),
        content = content,
    )
}

/** Grouped list card (rows separated by hairlines drawn by [CRowDivider]). */
@Composable
fun CListCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val c = LocalCeramic.current
    Column(modifier.fillMaxWidth().cardSurface(), content = content)
}

@Composable
fun CRowDivider(inset: Dp = 72.dp) {
    val c = LocalCeramic.current
    Box(Modifier.padding(start = inset).fillMaxWidth().height(1.dp).background(c.line))
}

/** Scale-on-press clickable used for every tappable surface. */
@Composable
fun Modifier.pressable(onClick: () -> Unit): Modifier {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val k by animateFloatAsState(if (pressed) 0.96f else 1f, spring(dampingRatio = 0.55f, stiffness = 700f), label = "press")
    val haptic = LocalHapticFeedback.current
    return this
        .graphicsLayer { scaleX = k; scaleY = k }
        .clickable(interactionSource = src, indication = null) { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onClick() }
}

/* ---------------- atoms ---------------- */

@Composable
fun Tile(icon: ImageVector, tone: Tone = Tone.Neutral, size: Dp = 44.dp, iconSize: Dp = 22.dp) {
    val c = LocalCeramic.current
    Box(
        Modifier.toneTile(tone, RoundedCornerShape(size * 0.3f)).size(size),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = c.toneFg(tone), modifier = Modifier.size(iconSize)) }
}

@Composable
fun TextTile(text: String, size: Dp = 44.dp) {
    val c = LocalCeramic.current
    Box(Modifier.toneTile(Tone.Neutral, RoundedCornerShape(size * 0.3f)).size(size), contentAlignment = Alignment.Center) {
        Text(text, style = CType.monoL.copy(fontSize = CType.monoL.fontSize * 1.3f), color = c.text)
    }
}

@Composable
fun Badge(text: String, tone: Tone = Tone.Neutral, icon: ImageVector? = null) {
    val c = LocalCeramic.current
    Row(
        Modifier.clip(CircleShape).background(c.toneBg(tone)).padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) Icon(icon, null, tint = c.toneFg(tone), modifier = Modifier.size(14.dp))
        Text(text, style = CType.label, color = c.toneFg(tone), maxLines = 1)
    }
}

enum class BtnStyle { Ink, Ghost, Danger }

/**
 * Glossy pill fill: top-lit vertical gradient, a light rim that fades
 * downward, and (for solid styles) a soft drop in the button's own hue.
 */
@Composable
fun Modifier.premiumFill(bg: Color, shape: androidx.compose.ui.graphics.Shape, glow: Boolean = true, solid: Boolean = true): Modifier {
    val c = LocalCeramic.current
    val top = lerp(bg, Color.White, if (solid) (if (c.isDark) 0.10f else 0.16f) else 0.5f)
    val bottom = lerp(bg, Color.Black, if (solid) 0.18f else 0.02f)
    val rim = Brush.verticalGradient(
        listOf(Color.White.copy(alpha = if (solid) 0.28f else 0.9f), Color.White.copy(alpha = 0f), (if (solid) Color.Black else c.line).copy(alpha = if (solid) 0.25f else 1f)),
    )
    return this
        .then(if (glow) Modifier.shadow(14.dp, shape, ambientColor = bg, spotColor = bg) else Modifier)
        .clip(shape)
        .background(Brush.verticalGradient(listOf(top, bottom)))
        .border(1.dp, rim, shape)
}

@Composable
fun CButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    style: BtnStyle = BtnStyle.Ink,
    small: Boolean = false,
    trailingIcon: ImageVector? = null,
) {
    val c = LocalCeramic.current
    val (bg, fg) = when (style) {
        BtnStyle.Ink -> c.ink to c.onInk
        BtnStyle.Ghost -> c.surface2 to c.text
        BtnStyle.Danger -> c.red to Color.White
    }
    val solid = style != BtnStyle.Ghost
    Row(
        modifier
            .heightIn(min = if (small) 40.dp else 54.dp)
            .premiumFill(bg, CircleShape, glow = solid && !small, solid = solid)
            .pressable(onClick)
            .padding(start = if (icon != null && !small) 8.dp else if (small) 16.dp else 22.dp, end = if (small) 16.dp else 22.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            if (small) Icon(icon, null, tint = fg, modifier = Modifier.size(18.dp))
            else Box(
                Modifier.size(38.dp).clip(CircleShape).background(fg.copy(alpha = if (solid) 0.14f else 0.07f)),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp)) }
        }
        Text(text, style = if (small) CType.num.copy(fontWeight = CType.btn.fontWeight) else CType.btn, color = fg, maxLines = 1)
        if (trailingIcon != null) Icon(trailingIcon, null, tint = fg, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun SectionLabel(left: String, modifier: Modifier = Modifier, right: String? = null, onRight: (() -> Unit)? = null) {
    val c = LocalCeramic.current
    Row(modifier.fillMaxWidth().padding(horizontal = 2.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(left.uppercase(), style = CType.label.copy(letterSpacing = CType.label.fontSize * 0.08f), color = c.text2)
        if (right != null) Text(
            right, style = CType.label,
            color = if (onRight != null) c.text else c.text2,
            modifier = if (onRight != null) Modifier.clickable(onClick = onRight) else Modifier,
        )
    }
}

@Composable
fun CToggle(checked: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val c = LocalCeramic.current
    val track by animateColorAsState(if (checked) c.ok else c.line, label = "track")
    val haptic = LocalHapticFeedback.current
    val x by animateDpAsState(if (checked) 20.dp else 0.dp, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "thumb")
    Box(
        modifier.size(50.dp, 30.dp).clip(CircleShape).background(track).clickable { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onChange(!checked) }.padding(3.dp),
    ) {
        Box(Modifier.offset(x = x).size(24.dp).shadow(2.dp, CircleShape).clip(CircleShape).background(Color.White))
    }
}

@Composable
fun <T> ChipRow(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    lead: (@Composable (T) -> Unit)? = null,
) {
    val c = LocalCeramic.current
    Row(
        modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = Gutter, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (v, l) ->
            val on = v == selected
            val edge by animateColorAsState(if (on) c.text.copy(alpha = 0.55f) else c.line, tween(180), label = "chipEdge")
            val ink by animateColorAsState(if (on) c.text else c.text2, tween(180), label = "chipInk")
            val lift by animateDpAsState(if (on) 4.dp else 0.dp, tween(180), label = "chipLift")
            // Toolwand .chip: pill, hairline border on card; active = strong border + small lift.
            Row(
                Modifier
                    .pressable { onSelect(v) }
                    .shadow(lift, CircleShape, ambientColor = Color.Black.copy(alpha = 0.12f), spotColor = Color.Black.copy(alpha = 0.18f))
                    .clip(CircleShape)
                    .background(c.surface)
                    .border(1.dp, edge, CircleShape)
                    .padding(start = if (lead != null) 12.dp else 14.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                lead?.invoke(v)
                Text(
                    l,
                    style = CType.body.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium),
                    color = ink,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Bare chip lead icon — tone-coloured, no tile behind it. */
@Composable
fun ChipIcon(icon: ImageVector, tone: Tone = Tone.Neutral) {
    val c = LocalCeramic.current
    Icon(icon, null, tint = if (tone == Tone.Neutral || tone == Tone.Ink) c.text2 else c.toneFg(tone), modifier = Modifier.size(18.dp))
}

/** Bare chip lead glyph (pattern-type symbol). */
@Composable
fun ChipGlyph(text: String) {
    val c = LocalCeramic.current
    Text(text, style = CType.mono.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), color = c.text2, maxLines = 1)
}

@Composable
fun <T> Segmented(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit, modifier: Modifier = Modifier) {
    val c = LocalCeramic.current
    BoxWithConstraints(modifier.fillMaxWidth().clip(CircleShape).background(c.surface2).padding(4.dp)) {
        val w = maxWidth / options.size
        val idx = options.indexOfFirst { it.first == selected }.coerceAtLeast(0)
        val x by animateDpAsState(w * idx, spring(dampingRatio = 0.75f), label = "seg")
        Box(Modifier.offset(x = x).width(w).height(36.dp).clip(CircleShape).background(c.ink))
        Row {
            options.forEach { (v, l) ->
                Box(Modifier.width(w).height(36.dp).clip(CircleShape).clickable { onSelect(v) }, contentAlignment = Alignment.Center) {
                    Text(l, style = CType.label.copy(fontSize = CType.meta.fontSize), color = if (v == selected) c.onInk else c.text2)
                }
            }
        }
    }
}

@Composable
fun SearchField(value: String, onChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    val c = LocalCeramic.current
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier.fillMaxWidth().height(54.dp)
            .fieldSurface(focused)
            .padding(start = 7.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FieldIcon(Icons.Rounded.Search, focused)
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(placeholder, style = CType.body, color = c.text2, maxLines = 1)
            BasicTextField(value, onChange, singleLine = true, textStyle = CType.body.copy(color = c.text), cursorBrush = SolidColor(c.red), modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused })
        }
        if (value.isNotEmpty()) Box(
            Modifier.size(28.dp).clip(CircleShape).background(c.surface2).clickable { onChange("") },
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.Close, "Clear", tint = c.text2, modifier = Modifier.size(16.dp)) }
    }
}

/** Premium input surface: soft top-lit fill, hairline rim, red glow while focused. */
@Composable
fun Modifier.fieldSurface(focused: Boolean, error: Boolean = false, shape: Shape = CircleShape): Modifier {
    val c = LocalCeramic.current
    val glow by animateDpAsState(if (focused || error) 14.dp else 6.dp, tween(200), label = "fieldGlow")
    val hot = focused || error
    return this
        .shadow(glow, shape, ambientColor = if (hot) c.red else Color.Black, spotColor = if (hot) c.red.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.25f))
        .clip(shape)
        .background(Brush.verticalGradient(listOf(c.surface, lerp(c.surface, c.surface2, 0.6f))))
        .border(
            if (hot) 1.5.dp else 1.dp,
            if (hot) Brush.horizontalGradient(listOf(c.red.copy(alpha = if (error) 1f else 0.7f), c.red.copy(alpha = if (error) 0.6f else 0.25f)))
            else Brush.verticalGradient(listOf(if (c.isDark) Color.White.copy(alpha = 0.10f) else Color.White, c.line)),
            shape,
        )
}

/** Round icon well at the start of an input. */
@Composable
fun FieldIcon(icon: ImageVector, focused: Boolean) {
    val c = LocalCeramic.current
    val bg by animateColorAsState(if (focused) c.red.copy(alpha = 0.12f) else c.surface2, tween(200), label = "wellBg")
    val fg by animateColorAsState(if (focused) c.red else c.text, tween(200), label = "wellFg")
    Box(Modifier.size(40.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp))
    }
}

/** Big screen title with subtitle and optional trailing action. */
@Composable
fun ScreenHeader(title: String, sub: String? = null, trailing: (@Composable () -> Unit)? = null) {
    val c = LocalCeramic.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = CType.title, color = c.text)
            if (sub != null) Text(sub, style = CType.meta, color = c.text2)
        }
        trailing?.invoke()
    }
}

@Composable
fun RoundIconButton(icon: ImageVector, desc: String, onClick: () -> Unit) {
    val c = LocalCeramic.current
    Box(
        Modifier.size(44.dp).clip(CircleShape).background(c.surface).border(1.dp, c.hair, CircleShape).pressable(onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, desc, tint = c.text, modifier = Modifier.size(20.dp)) }
}

/** Standard list row: tile · title + sub · trailing. */
@Composable
fun CRow(
    lead: @Composable () -> Unit,
    title: String,
    sub: String? = null,
    titleStyle: TextStyle = CType.h,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val c = LocalCeramic.current
    Row(
        Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .heightIn(min = 68.dp).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        lead()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = titleStyle, color = c.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (sub != null) Text(sub, style = CType.meta, color = c.text2, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        trailing?.invoke(this)
    }
}

@Composable
fun VSpace(h: Dp) = Spacer(Modifier.height(h))

/**
 * Toolwand-style page backdrop: paper base, a 48dp hairline grid that fades out from the
 * top, and a soft signal-red glow bleeding in from above the status bar.
 */
@Composable
fun GridGlowBackdrop(modifier: Modifier = Modifier) {
    val c = LocalCeramic.current
    val glow = c.red.copy(alpha = if (c.isDark) 0.20f else 0.14f)
    Canvas(modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        drawRect(c.bg)
        val step = 48.dp.toPx()
        val line = if (c.isDark) Color.White.copy(alpha = 0.10f) else c.text.copy(alpha = 0.10f)
        val fadeH = h * 0.55f
        val cx = w * 0.5f
        fun a(x: Float, y: Float): Float {
            val nx = (x - cx) / (w * 0.75f); val ny = y / fadeH
            val d = kotlin.math.sqrt(nx * nx + ny * ny)
            return ((1f - d) / 0.6f).coerceIn(0f, 1f) * 1f
        }
        var y = 0f
        while (y < fadeH) {
            drawLine(Brush.horizontalGradient(listOf(line.copy(alpha = 0f), line.copy(alpha = line.alpha * a(cx, y)), line.copy(alpha = 0f))), Offset(0f, y), Offset(w, y), 1f)
            y += step
        }
        var x = (cx % step)
        while (x < w) {
            drawLine(Brush.verticalGradient(listOf(line.copy(alpha = line.alpha * a(x, 0f)), line.copy(alpha = 0f)), endY = fadeH), Offset(x, 0f), Offset(x, fadeH), 1f)
            x += step
        }
        drawOval(
            Brush.radialGradient(listOf(glow, Color.Transparent), center = Offset(cx, -h * 0.06f), radius = w * 0.95f),
            topLeft = Offset(cx - w * 1.1f, -h * 0.35f), size = androidx.compose.ui.geometry.Size(w * 2.2f, h * 0.7f),
        )
    }
}
