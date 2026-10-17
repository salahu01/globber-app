package com.salah.callblocker.ui.kit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.salah.callblocker.ui.theme.CType
import com.salah.callblocker.ui.theme.LocalCeramic
import kotlinx.coroutines.delay

data class AlertMsg(
    val title: String,
    val sub: String = "",
    val icon: ImageVector,
    val tone: Tone = Tone.Ok,
    val action: String? = null,
    val id: Long = System.nanoTime(),
    val onAction: (() -> Unit)? = null,
)

/** Top alert pill (the prototype's camera-cutout alert). One at a time; newest wins. */
@Stable
class Alerts {
    var current by mutableStateOf<AlertMsg?>(null)
        private set

    fun show(msg: AlertMsg) { current = msg }
    fun dismiss() { current = null }
}

val LocalAlerts = staticCompositionLocalOf { Alerts() }

@Composable
fun AlertHost(alerts: Alerts) {
    val msg = alerts.current
    LaunchedEffect(msg?.id) {
        if (msg != null) { delay(if (msg.action != null) 4000 else 2600); alerts.dismiss() }
    }
    Box(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 6.dp), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            visible = msg != null,
            enter = slideInVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) { -it } + scaleIn(initialScale = 0.6f) + fadeIn(),
            exit = slideOutVertically { -it / 2 } + scaleOut(targetScale = 0.8f) + fadeOut(),
        ) {
            val m = msg ?: return@AnimatedVisibility
            val c = LocalCeramic.current
            val tint = when (m.tone) { Tone.Red -> c.red; Tone.Ok -> c.ok; Tone.Info -> Color(0xFF55606E); Tone.Ink, Tone.Neutral -> Color(0xFF3A3A3D) }
            Row(
                Modifier.fillMaxWidth().shadow(18.dp, RoundedCornerShape(33.dp)).clip(RoundedCornerShape(33.dp)).background(Color(0xFF0A0A0A))
                    .clickable { alerts.dismiss() }.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(tint), contentAlignment = Alignment.Center) {
                    Icon(m.icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(m.title, style = CType.num.copy(fontWeight = CType.h.fontWeight), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (m.sub.isNotEmpty()) Text(m.sub, style = CType.label.copy(fontWeight = CType.meta.fontWeight), color = Color(0xFFA9A49C), maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                if (m.action != null) Text(
                    m.action, style = CType.label, color = Color.White,
                    modifier = Modifier.clip(CircleShape).background(tint).clickable { alerts.dismiss(); m.onAction?.invoke() }.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}
