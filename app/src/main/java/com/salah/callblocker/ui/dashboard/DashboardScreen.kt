package com.salah.callblocker.ui.dashboard

import androidx.compose.foundation.lazy.rememberLazyListState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.salah.callblocker.ui.kit.SliverBar
import com.salah.callblocker.ui.kit.reveal
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.animation.togetherWith
import com.salah.callblocker.ui.kit.LocalHaze
import com.salah.callblocker.ui.kit.collapse
import com.salah.callblocker.ui.kit.largeTitle
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.salah.callblocker.ui.kit.EmptyArt
import com.salah.callblocker.ui.kit.EmptyIllustration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.salah.callblocker.ui.kit.globStarIcon
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salah.callblocker.data.BlockRule
import com.salah.callblocker.data.BlockedCall
import com.salah.callblocker.domain.RuleMatcher
import com.salah.callblocker.ui.kit.BlockedCallRow
import com.salah.callblocker.ui.kit.CButton
import com.salah.callblocker.ui.kit.CCard
import com.salah.callblocker.ui.kit.CListCard
import com.salah.callblocker.ui.kit.CRowDivider
import com.salah.callblocker.ui.kit.GlobIcon
import com.salah.callblocker.ui.kit.Gutter
import com.salah.callblocker.ui.kit.SectionLabel
import com.salah.callblocker.ui.kit.Tile
import com.salah.callblocker.ui.kit.Tone
import com.salah.callblocker.ui.kit.pressable
import com.salah.callblocker.ui.theme.CType
import com.salah.callblocker.ui.theme.LocalCeramic
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    roleHeld: Boolean,
    allowList: Set<String>,
    onRequestRole: () -> Unit,
    onOpenRules: () -> Unit,
    onOpenLog: () -> Unit,
    onOpenRule: (BlockRule) -> Unit,
    onOpenCall: (BlockedCall) -> Unit,
    contentPadding: PaddingValues,
    vm: DashboardViewModel = viewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val c = LocalCeramic.current
    val list = rememberLazyListState()
    val haze = LocalHaze.current
    val collapsed by list.collapse()
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize().hazeSource(haze),
            state = list,
            contentPadding = PaddingValues(start = Gutter, end = Gutter, top = contentPadding.calculateTopPadding() + 8.dp, bottom = contentPadding.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(Modifier.largeTitle { collapsed }, verticalAlignment = Alignment.CenterVertically) {
                    GlobIcon(40.dp, corner = 13.dp)
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text("Globber", style = CType.h, color = c.text)
                        Text(SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date()), style = CType.meta, color = c.text2)
                    }
                    Row(
                        Modifier.clip(CircleShape).background(if (roleHeld) c.okBg else c.redBg).pressable { if (!roleHeld) onRequestRole() }
                            .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(if (roleHeld) c.okInk else c.redInk))
                        Text(if (roleHeld) "Protected" else "Not screening", style = CType.label, color = if (roleHeld) c.okInk else c.redInk)
                    }
                }
            }
            if (!roleHeld) item {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(c.redBg).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Tile(Icons.Rounded.WarningAmber, Tone.Red)
                    Column(Modifier.weight(1f)) {
                        Text("Calls aren't being screened", style = CType.num, color = c.text)
                        Text("Set Globber as call screening app", style = CType.meta, color = c.text2)
                    }
                    CButton("Fix", onRequestRole, small = true)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Kpi(Icons.Rounded.Block, Tone.Red, "Today", s.blockedToday.toString(), "calls blocked", Modifier.weight(1f).reveal(1), red = s.blockedToday > 0)
                        Kpi(Icons.Rounded.CalendarMonth, Tone.Neutral, "7 days", s.blockedWeek.toString(), "blocked this week", Modifier.weight(1f).reveal(2))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Kpi(globStarIcon(2.4f), Tone.Info, null, "${s.activeRules}", "rules active", Modifier.weight(1f).reveal(3), suffix = " / ${s.totalRules}", onClick = onOpenRules)
                        val tr = s.topRule
                        Kpi(Icons.Rounded.StarOutline, Tone.Red, null, tr?.pattern ?: "None yet", if (tr != null) "top rule · ${s.topRuleHits} hits" else "no hits yet",
                            Modifier.weight(1f).reveal(4), mono = true, onClick = if (tr != null) ({ onOpenRule(tr) }) else null)
                    }
                }
            }
            item { WeekCard(s.week, s.blockedWeek) }
            item { SectionLabel("Recent activity", right = "See all", onRight = onOpenLog) }
            item {
                if (s.recent.isEmpty()) {
                    CCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            EmptyIllustration(EmptyArt.NoCalls)
                            Text("No blocked calls yet", style = CType.h, color = c.text)
                            Text("They'll appear here the moment a rule fires.", style = CType.meta, color = c.text2)
                        }
                    }
                } else CListCard {
                    s.recent.forEachIndexed { i, call ->
                        if (i > 0) CRowDivider()
                        BlockedCallRow(call, RuleMatcher.national(call.number) in allowList) { onOpenCall(call) }
                    }
                }
            }
        }
        SliverBar("Globber", collapsed, haze, contentPadding.calculateTopPadding()) { GlobIcon(28.dp, corner = 9.dp) }
    }
}

@Composable
private fun Kpi(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tone: Tone,
    tag: String?,
    value: String,
    caption: String,
    modifier: Modifier,
    suffix: String? = null,
    red: Boolean = false,
    mono: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val c = LocalCeramic.current
    CCard(modifier.height(138.dp), onClick = onClick, padding = PaddingValues(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Tile(icon, tone, size = 36.dp, iconSize = 18.dp)
            Box(Modifier.weight(1f))
            if (tag != null) Text(tag, style = CType.label, color = c.text2)
            if (onClick != null) Icon(Icons.Rounded.ChevronRight, null, tint = c.text2, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.height(36.dp), verticalAlignment = Alignment.Bottom) {
            val muted = value == "None yet"
            androidx.compose.animation.AnimatedContent(value, transitionSpec = {
                (androidx.compose.animation.slideInVertically { it / 2 } + androidx.compose.animation.fadeIn()) togetherWith
                    (androidx.compose.animation.slideOutVertically { -it / 2 } + androidx.compose.animation.fadeOut())
            }, label = "kpi") { v ->
            Text(v, style = if (muted) CType.h.copy(fontSize = 18.sp) else if (mono) CType.monoL else CType.numL, color = if (muted) c.text2 else if (red) c.red else c.text, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 140.dp))
            }
            if (suffix != null) Text(suffix, style = CType.meta, color = c.text2, modifier = Modifier.padding(bottom = 2.dp))
        }
        Box(Modifier.height(4.dp))
        Text(caption, style = CType.meta, color = c.text2, maxLines = 1)
    }
}

@Composable
private fun WeekCard(week: List<DayBar>, total: Int) {
    val c = LocalCeramic.current
    val grow = remember { Animatable(0f) }
    LaunchedEffect(Unit) { grow.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }
    val max = (week.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    var picked0 by remember { mutableStateOf(-1) } // -1 = follow today
    val sel = if (picked0 in week.indices) picked0 else week.indexOfFirst { it.today }.takeIf { it >= 0 } ?: (week.size - 1)
    val picked = week.getOrNull(sel)
    CCard(Modifier.fillMaxWidth().reveal(5), padding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text("THIS WEEK", style = CType.label.copy(letterSpacing = 1.4.sp), color = c.text2)
                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 6.dp)) {
                    Text("$total", style = CType.h.copy(fontSize = 34.sp, lineHeight = 36.sp, fontWeight = FontWeight.ExtraBold), color = c.text)
                    Text(if (total == 1) " call blocked" else " calls blocked", style = CType.meta, color = c.text2, modifier = Modifier.padding(bottom = 5.dp))
                }
            }
            if (picked != null) Column(
                Modifier.clip(RoundedCornerShape(14.dp)).background(if (picked.today) c.redBg else c.surface2).padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.End,
            ) {
                val ago = (week.indexOfFirst { it.today }.takeIf { it >= 0 } ?: (week.size - 1)) - sel
                Text(when (ago) { 0 -> "Today"; 1 -> "Yesterday"; else -> "$ago days ago" }, style = CType.label, color = if (picked.today) c.redInk else c.text2)
                Text("${picked.count}", style = CType.h.copy(fontWeight = FontWeight.ExtraBold), color = if (picked.today) c.redInk else c.text)
            }
        }
        Box(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().height(168.dp).padding(top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom,
        ) {
            week.forEachIndexed { i, d ->
                val on = i == sel
                val frac = if (d.count == 0) 0f else (0.08f + 0.92f * d.count / max) * grow.value
                val lift by animateFloatAsState(if (on) 1f else 0f, spring(dampingRatio = 0.6f, stiffness = 500f), label = "lift")
                Column(
                    Modifier.weight(1f).fillMaxHeight().pressable { picked0 = i },
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        "${d.count}", style = CType.label.copy(fontWeight = FontWeight.Bold),
                        color = (if (d.today) c.redInk else c.text).copy(alpha = if (d.count == 0 && !on) 0f else 0.35f + 0.65f * lift),
                        modifier = Modifier.padding(bottom = 6.dp).graphicsLayer { translationY = (1f - lift) * 4.dp.toPx() },
                    )
                    // track + fill
                    Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(12.dp)).background(c.surface2.copy(alpha = if (c.isDark) 0.45f else 0.6f)), contentAlignment = Alignment.BottomCenter) {
                        val fill = when {
                            d.today -> Brush.verticalGradient(listOf(lerp(c.red, Color.White, 0.25f), c.red, lerp(c.red, Color.Black, 0.25f)))
                            on -> Brush.verticalGradient(listOf(c.text.copy(alpha = 0.85f), c.text))
                            else -> Brush.verticalGradient(listOf(c.text.copy(alpha = 0.22f), c.text.copy(alpha = 0.32f)))
                        }
                        if (frac > 0f) Box(Modifier.fillMaxWidth().fillMaxHeight(frac).clip(RoundedCornerShape(12.dp)).background(fill))
                    }
                    Box(Modifier.height(8.dp))
                    Text(d.label, style = CType.label.copy(fontWeight = if (on) FontWeight.Bold else FontWeight.Medium), color = if (on) c.text else c.text2)
                }
            }
        }
        if (total == 0) Text(
            "A quiet week, nothing blocked yet",
            style = CType.label, color = c.text2,
            modifier = Modifier.align(Alignment.Center).padding(bottom = 8.dp)
                .shadow(6.dp, CircleShape).clip(CircleShape).background(c.surface)
                .border(1.dp, c.line, CircleShape).padding(horizontal = 14.dp, vertical = 8.dp),
        )
        }
    }
}
