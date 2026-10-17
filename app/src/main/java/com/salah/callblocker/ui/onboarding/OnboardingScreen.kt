package com.salah.callblocker.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.salah.callblocker.R
import com.salah.callblocker.ui.kit.BtnStyle
import com.salah.callblocker.ui.kit.CButton
import com.salah.callblocker.ui.kit.Gutter
import com.salah.callblocker.ui.theme.Brand
import com.salah.callblocker.ui.theme.CType

private data class Page(val kicker: String, val title: String, val body: String, val cta: String, val art: Int)

private val PAGES = listOf(
    Page("Before", "Spam calls, all day",
        "Telemarketers rotate through hundreds of numbers. Blocking them one by one never ends.",
        "Show me the fix", R.drawable.onboard_before),
    Page("Setup", "One pattern stops them all",
        "Add a rule like +91 140*. Set Globber as your call screening app, and it rejects every match before it rings.",
        "Set as call screening app", R.drawable.onboard_setup),
    Page("After", "Your phone, quiet again",
        "Spam is stopped silently. Real people still get through. Everything stays on your phone.",
        "Start blocking", R.drawable.onboard_after),
)

/**
 * Three-scene story (before → setup → after). Always light/ceramic, like the splash.
 * [roleHeld]/[onRequestRole] drive page 2; [onDone] finishes onboarding.
 */
@Composable
fun OnboardingScreen(roleHeld: Boolean, onRequestRole: () -> Unit, onDone: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    // Role granted from the system dialog: move on to the "after" scene.
    LaunchedEffect(roleHeld) { if (roleHeld && page == 1) page = 2 }
    val ink = Brand.ink
    val mute = Color(0xFF6E6862)

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFFF7F5F2), Brand.bone), radius = 1400f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = Gutter, vertical = 12.dp),
    ) {
        Row(Modifier.fillMaxWidth().height(36.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                PAGES.indices.forEach { i ->
                    Box(Modifier.height(8.dp).width(if (i == page) 24.dp else 8.dp).clip(CircleShape).background(if (i == page) ink else Color(0xFFDDD7CF)))
                }
            }
            if (page < 2) Text("Skip", style = CType.label, color = mute, modifier = Modifier.clickable { page = 2 }.padding(8.dp))
        }

        AnimatedContent(
            targetState = page,
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally { it / 5 * dir } + fadeIn(tween(400))) togetherWith
                    (slideOutHorizontally { -it / 5 * dir } + fadeOut(tween(250)))
            },
            label = "onboard",
            modifier = Modifier.weight(1f),
        ) { p ->
            val pg = PAGES[p]
            val bob by rememberInfiniteTransition(label = "bob").animateFloat(0f, 1f, infiniteRepeatable(tween(3600), RepeatMode.Reverse), label = "y")
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Spacer(Modifier.height(8.dp))
                Image(
                    painterResource(pg.art), null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().aspectRatio(360f / 330f).graphicsLayer { translationY = -bob * 6.dp.toPx() },
                )
                Text("${p + 1} · ${pg.kicker}".uppercase(), style = CType.label.copy(letterSpacing = CType.label.fontSize * 0.1f), color = Brand.red)
                Text(pg.title, style = CType.title, color = ink)
                Text(pg.body, style = CType.body, color = mute)
            }
        }

        val cta = if (page == 1 && roleHeld) "Continue" else PAGES[page].cta
        CButton(
            cta,
            onClick = {
                when {
                    page == 0 -> page = 1
                    page == 1 && !roleHeld -> onRequestRole()
                    page == 1 -> page = 2
                    else -> onDone()
                }
            },
            trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward,
            modifier = Modifier.fillMaxWidth(),
        )
        if (page == 1 && !roleHeld) {
            Spacer(Modifier.height(12.dp))
            CButton("Not now", onClick = { page = 2 }, style = BtnStyle.Ghost, modifier = Modifier.fillMaxWidth())
        }
    }
}
