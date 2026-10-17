package com.salah.callblocker.ui.kit

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
import com.salah.callblocker.ui.theme.Brand
import com.salah.callblocker.ui.theme.CType
import com.salah.callblocker.ui.theme.Ceramic
import com.salah.callblocker.ui.theme.LocalCeramic

val Gutter = 20.dp
private val CardShape = RoundedCornerShape(22.dp)

/** Semantic tint for tiles and badges. */
enum class Tone { Neutral, Red, Ok, Info, Ink }

private fun Ceramic.toneBg(t: Tone) = when (t) {
    Tone.Neutral -> surface2; Tone.Red -> redBg; Tone.Ok -> okBg; Tone.Info -> infoBg; Tone.Ink -> ink
}
private fun Ceramic.toneFg(t: Tone) = when (t) {
    Tone.Neutral -> text; Tone.Red -> redInk; Tone.Ok -> okInk; Tone.Info -> info; Tone.Ink -> onInk
}

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
            .clip(CardShape)
            .background(c.surface)
            .border(1.dp, c.hair, CardShape)
            .then(if (onClick != null) Modifier.pressable(onClick) else Modifier)
            .padding(padding),
        content = content,
    )
}

/** Grouped list card (rows separated by hairlines drawn by [CRowDivider]). */
@Composable
fun CListCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val c = LocalCeramic.current
    Column(modifier.fillMaxWidth().clip(CardShape).background(c.surface).border(1.dp, c.hair, CardShape), content = content)
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
    return this
        .scale(if (pressed) 0.97f else 1f)
        .clickable(interactionSource = src, indication = null, onClick = onClick)
}

/* ---------------- atoms ---------------- */

@Composable
fun Tile(icon: ImageVector, tone: Tone = Tone.Neutral, size: Dp = 44.dp, iconSize: Dp = 22.dp) {
    val c = LocalCeramic.current
    Box(
        Modifier.size(size).clip(RoundedCornerShape(size * 0.32f)).background(c.toneBg(tone)),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = c.toneFg(tone), modifier = Modifier.size(iconSize)) }
}

@Composable
fun TextTile(text: String, size: Dp = 44.dp) {
    val c = LocalCeramic.current
    Box(Modifier.size(size).clip(RoundedCornerShape(size * 0.32f)).background(c.surface2), contentAlignment = Alignment.Center) {
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
    Row(
        modifier
            .heightIn(min = if (small) 40.dp else 54.dp)
            .clip(CircleShape)
            .background(bg)
            .pressable(onClick)
            .padding(horizontal = if (small) 16.dp else 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, null, tint = fg, modifier = Modifier.size(18.dp))
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
    val x by animateDpAsState(if (checked) 20.dp else 0.dp, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "thumb")
    Box(
        modifier.size(50.dp, 30.dp).clip(CircleShape).background(track).clickable { onChange(!checked) }.padding(3.dp),
    ) {
        Box(Modifier.offset(x = x).size(24.dp).shadow(2.dp, CircleShape).clip(CircleShape).background(Color.White))
    }
}

@Composable
fun <T> ChipRow(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit, modifier: Modifier = Modifier) {
    val c = LocalCeramic.current
    Row(
        modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = Gutter),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (v, l) ->
            val on = v == selected
            Text(
                l,
                style = CType.label.copy(fontSize = CType.meta.fontSize),
                color = if (on) c.onInk else c.text2,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (on) c.ink else c.surface2)
                    .clickable { onSelect(v) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
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
    Row(
        modifier.fillMaxWidth().height(52.dp).clip(CircleShape).background(c.surface2).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Rounded.Search, null, tint = c.text2, modifier = Modifier.size(20.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(placeholder, style = CType.body, color = c.text2, maxLines = 1)
            BasicTextField(value, onChange, singleLine = true, textStyle = CType.body.copy(color = c.text), cursorBrush = SolidColor(c.red), modifier = Modifier.fillMaxWidth())
        }
        if (value.isNotEmpty()) Icon(Icons.Rounded.Close, "Clear", tint = c.text2, modifier = Modifier.size(20.dp).clickable { onChange("") })
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
