package com.salah.callblocker.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.salah.callblocker.ui.kit.globStar
import com.salah.callblocker.ui.theme.Brand
import com.salah.callblocker.ui.theme.CType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** One number drifting right→left into the lens. Matches get "140" highlighted and break apart. */
private data class Lane(val prefix: String, val hit: String, val rest: String, val match: Boolean, val y: Int, val delayMs: Int)

private val LANES = listOf(
    Lane("+91 ", "140", " 2210 455", true, -48, 0),
    Lane("+91 98470 ", "", "55120", false, 0, 350),
    Lane("+91 ", "140", " 6612 003", true, 48, 700),
    Lane("+44 20 7946 ", "", "0018", false, -24, 1050),
    Lane("+91 ", "140", " 9921 470", true, 24, 1400),
)

/**
 * "The filter": spam numbers stream into the ceramic glob-star tile; numbers
 * matching +91 140* light up red and dissolve, others pass through. Then the
 * pattern types out and the wordmark settles. Tap to skip.
 */
@Composable
fun SplashScreen(dark: Boolean = false, onFinished: () -> Unit) {
    // Light = bone ceramic; dark = the app's night palette with an inverted (light) star.
    val bg = if (dark) Color(0xFF0B0B0A) else Brand.bone
    val glow = if (dark) Color(0xFF1C1B18) else Color(0xFFF7F5F2)
    val ink = if (dark) Color(0xFFF3F1EB) else Brand.ink
    val mute = if (dark) Color(0xFF6E6A62) else Brand.mute
    val sub = if (dark) Color(0xFFA29E94) else Color(0xFF6B6660)
    val tileTop = if (dark) Color(0xFF2A2925) else Brand.boneTop
    val tileBottom = if (dark) Color(0xFF151412) else Brand.boneBottom
    val pill = if (dark) Color(0xFF1C1B18) else Color.White
    val tile = remember { Animatable(0f) }
    val spin = remember { Animatable(-180f) }
    val type = remember { Animatable(0f) }
    val brand = remember { Animatable(0f) }
    val lanes = remember { LANES.map { Animatable(0f) } }

    LaunchedEffect(Unit) {
        launch { tile.animateTo(1f, tween(800, easing = FastOutSlowInEasing)) }
        launch { delay(300); spin.animateTo(0f, tween(2400, easing = FastOutSlowInEasing)) }
        lanes.forEachIndexed { i, a -> launch { delay(LANES[i].delayMs.toLong()); a.animateTo(1f, tween(2600, easing = LinearEasing)) } }
        launch { delay(600); type.animateTo(1f, tween(700, easing = LinearEasing)) }
        launch { delay(3000); brand.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }
        delay(4400)
        onFinished()
    }

    Box(
        Modifier
            .fillMaxSize()
            // Starts as flat bone (same as the system splash) and warms into the glow.
            .background(bg)
            .background(Brush.radialGradient(listOf(glow, bg), radius = 1400f), alpha = tile.value)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onFinished),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val cy = maxHeight * 0.42f
            val travel = maxWidth.value + 80f

            // lanes (behind the tile)
            LANES.forEachIndexed { i, lane ->
                val p = lanes[i].value
                val hitOn = p > 0.22f
                val x = if (lane.match) {
                    // stop inside the tile and dissolve
                    val stop = 0.5f
                    val q = (p / stop).coerceAtMost(1f)
                    maxWidth.value + 10f - q * (maxWidth.value / 2f + 70f)
                } else {
                    maxWidth.value + 10f - p * travel * 1.9f
                }
                val gone = if (lane.match) ((p - 0.42f) / 0.14f).coerceIn(0f, 1f) else 0f
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(textDecoration = if (lane.match && hitOn) TextDecoration.LineThrough else null)) {
                            append(lane.prefix)
                            if (lane.hit.isNotEmpty()) withStyle(
                                SpanStyle(
                                    color = if (hitOn) Color.White else mute,
                                    background = if (hitOn) Brand.red else Color.Transparent,
                                ),
                            ) { append(lane.hit) }
                            append(lane.rest)
                        }
                    },
                    style = CType.monoL,
                    color = when {
                        !lane.match -> ink
                        hitOn -> Brand.red
                        else -> mute
                    },
                    softWrap = false,
                    modifier = Modifier
                        .offset(x = x.dp, y = cy + lane.y.dp - 11.dp)
                        .alpha(if (p == 0f) 0f else 1f - gone)
                        .blur((gone * 8f).dp)
                        .scale(1f - gone * 0.15f),
                )
            }

            // tile + pattern pill
            Column(
                Modifier.align(Alignment.TopCenter).offset(y = cy - 84.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Canvas(
                    Modifier
                        .size(168.dp)
                        .graphicsLayer { val s = 0.4f + 0.6f * tile.value; scaleX = s; scaleY = s }
                        .shadow(28.dp * tile.value, androidx.compose.foundation.shape.RoundedCornerShape(42.dp), clip = false),
                ) {
                    drawRoundRect(
                        Brush.verticalGradient(listOf(tileTop, tileBottom)),
                        cornerRadius = CornerRadius(42.dp.toPx()),
                    )
                    val w = size.width
                    globStar(armHalf = w * 0.216f, stroke = w * 0.1f, eye = w * 0.0625f, ink = ink, rotation = spin.value)
                }
                Box(Modifier.size(26.dp))
                val shown = "+91 140*".take((type.value * 8).toInt())
                Row(
                    Modifier
                        .alpha(if (type.value > 0f) 1f else 0f)
                        .shadow(6.dp, CircleShape, clip = false)
                        .clip(CircleShape)
                        .background(pill)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(shown.padEnd(8, ' '), style = CType.mono, color = ink)
                }
            }
        }

        // brand
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 36.dp)
                .alpha(brand.value)
                .offset(y = ((1f - brand.value) * 12f).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Globber", style = CType.display, color = ink)
            Text("Spam caught by its pattern. Real calls ring.", style = CType.meta, color = sub)
            Box(Modifier.size(28.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF1E9E5A)))
                Text("Screening on this phone · no internet", style = CType.label, color = sub)
            }
        }
    }
}
