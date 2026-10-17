package com.salah.callblocker.ui.onboarding

import android.animation.ValueAnimator
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.salah.callblocker.R
import kotlin.math.floor

/*
 * The three onboarding scenes, animated like docs/prototype (ONBOARDING v2/v3 CSS).
 * Each scene is a static base plus the moving parts as separate layers, all placed in the
 * prototype's 360×330 SVG coordinates. Layers come from the prototype SVG at 3×.
 */

private const val VW = 360f
private const val VH = 330f

private val Ease = CubicBezierEasing(.16f, 1f, .3f, 1f)
private val Spring = CubicBezierEasing(.34f, 1.56f, .64f, 1f)
private val EaseIn = CubicBezierEasing(.42f, 0f, 1f, 1f)
private val EaseInOut = CubicBezierEasing(.42f, 0f, .58f, 1f)

/** 0→1 progress of a one-shot animation that starts at [delay] and lasts [dur] seconds. */
private fun once(t: Float, delay: Float, dur: Float, e: Easing = Ease) =
    e.transform(((t - delay) / dur).coerceIn(0f, 1f))

/** 0→1 phase of a looping animation with [period] seconds. */
private fun loop(t: Float, period: Float, delay: Float = 0f) =
    if (t < delay) 0f else ((t - delay) % period) / period

/** Piecewise keyframes: [kf] is (offset, value) pairs sorted by offset; eased per segment. */
private fun keys(p: Float, vararg kf: Pair<Float, Float>, e: Easing = EaseInOut): Float {
    if (p <= kf.first().first) return kf.first().second
    for (i in 1 until kf.size) {
        val (o1, v1) = kf[i]
        if (p <= o1) {
            val (o0, v0) = kf[i - 1]
            return v0 + (v1 - v0) * e.transform((p - o0) / (o1 - o0))
        }
    }
    return kf.last().second
}

private fun lerp(a: Float, b: Float, f: Float) = a + (b - a) * f

/** Seconds since the scene appeared. Frozen at a settled moment when system animations are off. */
@Composable
private fun sceneTime(): () -> Float {
    val t = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        if (!ValueAnimator.areAnimatorsEnabled()) { t.floatValue = 4f; return@LaunchedEffect }
        val start = withFrameMillis { it }
        while (true) withFrameMillis { t.floatValue = (it - start) / 1000f }
    }
    return { t.floatValue }
}

/** One layer at its prototype bounds (x0,y0)-(x1,y1); [pivot] is in scene coordinates. */
@Composable
private fun BoxWithConstraintsScope.Layer(
    @DrawableRes res: Int, x0: Float, y0: Float, x1: Float, y1: Float,
    pivot: Pair<Float, Float>? = null,
    reveal: (() -> Float)? = null,
    anim: (GraphicsLayerScope.(unit: Float) -> Unit)? = null,
) {
    val u: Dp = maxWidth / VW
    Image(
        painterResource(res), null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .offset(u * x0, u * y0)
            .size(u * (x1 - x0), u * (y1 - y0))
            .then(
                if (anim == null) Modifier else Modifier.graphicsLayer {
                    if (pivot != null) transformOrigin = TransformOrigin((pivot.first - x0) / (x1 - x0), (pivot.second - y0) / (y1 - y0))
                    anim(u.toPx())
                },
            )
            .then(if (reveal == null) Modifier else Modifier.drawWithContent { clipRect(right = size.width * reveal()) { this@drawWithContent.drawContent() } }),
    )
}

@Composable
private fun Scene(content: @Composable BoxWithConstraintsScope.() -> Unit) =
    BoxWithConstraints(Modifier.aspectRatio(VW / VH), content = content)

@Composable
fun OnboardingScene(page: Int, modifier: Modifier = Modifier) {
    Box(modifier) {
        when (page) {
            0 -> SceneBefore()
            1 -> SceneSetup()
            else -> SceneAfter()
        }
    }
}

/* 1 BEFORE · busy office desk: buzzing phone, sweat drip, spam bubbles pop in. */
@Composable
private fun SceneBefore() {
    val t = sceneTime()
    val buzzPivot = 233.5f to 134.7f
    val buzz: GraphicsLayerScope.(Float) -> Unit = {
        rotationZ = keys(loop(t(), 2.4f), 0f to 0f, .6f to 0f, .64f to -4f, .68f to 4f, .72f to -3f, .76f to 2f, .8f to 0f, 1f to 0f)
    }
    Scene {
        Layer(R.drawable.ob_before_base, 16f, 20f, 342f, 314f)
        Layer(R.drawable.ob_before_drip, 189f, 94f, 203f, 113f) { u ->
            val p = loop(t(), 3f)
            translationY = 26f * u * EaseIn.transform(p)
            alpha = keys(p, 0f to 0f, .2f to 1f, 1f to 0f, e = EaseIn)
        }
        Layer(R.drawable.ob_before_buzz, 214.3f, 106f, 260f, 163.3f, pivot = buzzPivot, anim = buzz)
        Layer(R.drawable.ob_before_wave, 205.3f, 119f, 261.7f, 157f, pivot = buzzPivot) { u ->
            buzz(u)
            alpha = keys(loop(t(), 2.4f), 0f to 0f, .58f to 0f, .66f to 1f, .8f to 1f, 1f to 0f)
        }
        Bubble(R.drawable.ob_before_bub1, 7.3f, 125.3f, 126.7f, 158.7f, 54f to 142f, .3f, t)
        Bubble(R.drawable.ob_before_bub2, 233.3f, 167.3f, 345.7f, 200.7f, 280f to 184f, 1.1f, t)
        Bubble(R.drawable.ob_before_bub3, 147.3f, 37.3f, 259.7f, 70.7f, 194f to 54f, 1.9f, t)
    }
}

@Composable
private fun BoxWithConstraintsScope.Bubble(@DrawableRes res: Int, x0: Float, y0: Float, x1: Float, y1: Float, pivot: Pair<Float, Float>, delay: Float, t: () -> Float) =
    Layer(res, x0, y0, x1, y1, pivot = pivot) {
        val p = once(t(), delay, .7f)
        alpha = p
        scaleX = lerp(.4f, 1f, p); scaleY = scaleX
    }

/* 2 SETUP · home sofa: rule card rises, pattern types in, toggle flips on. */
@Composable
private fun SceneSetup() {
    val t = sceneTime()
    val card: GraphicsLayerScope.(Float) -> Unit = { u ->
        val p = once(t(), .3f, .9f)
        alpha = p
        translationY = (1 - p) * 12f * u
    }
    Scene {
        Layer(R.drawable.ob_setup_base, 16f, 32f, 342f, 318f)
        Layer(R.drawable.ob_setup_lampglow, 282f, 92f, 354f, 112f) {
            alpha = keys(loop(t(), 5f), 0f to 1f, .5f to .45f, 1f to 1f)
        }
        Layer(R.drawable.ob_setup_ok_bar, 174.3f, 208.7f, 194.3f, 219f) { alpha = once(t(), 2.8f, .5f, FastOutSlowInEasing) }
        Layer(R.drawable.ob_setup_card_in, 211.3f, 101.3f, 352.7f, 202.7f, anim = card)
        Layer(R.drawable.ob_setup_typed, 238.3f, 141.3f, 301.3f, 155f, reveal = { floor(once(t(), 1f, 1.1f, { it }) * 8f) / 8f }, anim = card)
        Layer(R.drawable.ob_setup_tgl_off, 300f, 170f, 338f, 192f, anim = card)
        Layer(R.drawable.ob_setup_tgl_on, 300f, 170f, 338f, 192f) { u ->
            card(u)
            alpha *= once(t(), 2.4f, .4f, FastOutSlowInEasing)
        }
        Layer(R.drawable.ob_setup_tgl_k, 318.3f, 172.3f, 335.7f, 189.7f) { u ->
            card(u)
            translationX = lerp(-16f, 0f, once(t(), 2.4f, .5f)) * u
        }
    }
}

/* 3 AFTER · balcony at sunset: sun sinks, birds drift, tea steams, quiet card rises. */
@Composable
private fun SceneAfter() {
    val t = sceneTime()
    Scene {
        Layer(R.drawable.ob_after_base, 8f, 8f, 352f, 306f)
        Layer(R.drawable.ob_after_sun, 214f, 114f, 286f, 186f) { u ->
            val c = t() % 18f
            val p = EaseInOut.transform(if (c < 9f) c / 9f else 2f - c / 9f)
            translationY = 10f * p * u
            alpha = lerp(1f, .85f, p)
        }
        Layer(R.drawable.ob_after_birds, 117f, 59.3f, 149f, 73f) { u ->
            val p = loop(t(), 12f)
            translationX = 200f * p * u
            translationY = -20f * p * u
        }
        Layer(R.drawable.ob_after_steam, 187.7f, 176.7f, 204.3f, 197.3f) { u ->
            val p = loop(t(), 3.6f)
            alpha = keys(p, 0f to 0f, .4f to .9f, 1f to 0f)
            translationY = keys(p, 0f to 6f, 1f to -10f) * u
        }
        Layer(R.drawable.ob_after_calm, 259.3f, 187.3f, 345.7f, 222.7f) { u ->
            val p = once(t(), .6f, .9f)
            alpha = p
            translationY = (1 - p) * 12f * u
        }
    }
}
