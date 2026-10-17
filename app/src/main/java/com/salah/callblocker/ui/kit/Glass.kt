package com.salah.callblocker.ui.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.salah.callblocker.ui.theme.CType
import com.salah.callblocker.ui.theme.LocalCeramic
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/** Shared blur source: screen lists draw into it, the dock and pinned bars frost it. */
val LocalHaze = androidx.compose.runtime.staticCompositionLocalOf { HazeState() }

/** Frosted-glass style tinted with the page colour. */
@Composable
fun glassStyle(strength: Float = 0.62f): HazeStyle {
    val c = LocalCeramic.current
    return HazeStyle(
        backgroundColor = c.bg,
        tint = HazeTint(c.bg.copy(alpha = if (c.isDark) strength + 0.1f else strength)),
        blurRadius = 24.dp,
        noiseFactor = 0.04f,
    )
}

/** Blurs whatever [state]'s source draws behind this element. */
@Composable
fun Modifier.glass(state: HazeState, strength: Float = 0.62f): Modifier = hazeEffect(state, glassStyle(strength))

/** 0 while the large title is on screen, rising to 1 once it has scrolled under the bar. */
@Composable
fun LazyListState.collapse(distance: Dp = 64.dp): State<Float> {
    val px = with(LocalDensity.current) { distance.toPx() }
    return remember(this, px) {
        derivedStateOf { if (firstVisibleItemIndex > 0) 1f else (firstVisibleItemScrollOffset / px).coerceIn(0f, 1f) }
    }
}

/**
 * Pinned compact bar for a sliver-style header: invisible over the large
 * title, then fades in as frosted glass with a left-aligned title once scrolled.
 */
@Composable
fun SliverBar(title: String, progress: Float, haze: HazeState, top: Dp, modifier: Modifier = Modifier, leading: (@Composable () -> Unit)? = null) {
    val c = LocalCeramic.current
    Box(
        modifier
            .fillMaxWidth()
            .height(top + 52.dp)
            .hazeEffect(haze, glassStyle(0.7f)) { alpha = progress },
    ) {
        Row(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = Gutter, bottom = 12.dp)
                .graphicsLayer { alpha = progress; translationY = (1f - progress) * 10.dp.toPx() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            leading?.invoke()
            Text(
                title,
                style = CType.h.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                color = c.text,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(1.dp).background(c.line.copy(alpha = progress)))
    }
}

/** Large-title modifier: fades and lifts away as [progress] rises. */
fun Modifier.largeTitle(progress: () -> Float): Modifier = graphicsLayer {
    val p = progress()
    alpha = 1f - p
    translationY = -p * 12.dp.toPx()
    scaleX = 1f - p * 0.04f; scaleY = scaleX
    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
}

/** 0→1 as a plain scroll column moves its title out of view. */
@Composable
fun androidx.compose.foundation.ScrollState.collapse(distance: Dp = 64.dp): State<Float> {
    val px = with(LocalDensity.current) { distance.toPx() }
    return remember(this, px) { derivedStateOf { (value / px).coerceIn(0f, 1f) } }
}

/** One-shot entrance: fade + rise, staggered by [index]. */
@Composable
fun Modifier.reveal(index: Int): Modifier {
    val a = remember { androidx.compose.animation.core.Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(40L * index)
        a.animateTo(1f, androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 220f))
    }
    return graphicsLayer { alpha = a.value.coerceIn(0f, 1f); translationY = (1f - a.value) * 18.dp.toPx() }
}
