package com.salah.callblocker.ui.dashboard

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
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Gutter, end = Gutter, top = contentPadding.calculateTopPadding() + 8.dp, bottom = contentPadding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Kpi(Icons.Rounded.Block, Tone.Red, "Today", s.blockedToday.toString(), "calls blocked", Modifier.weight(1f), red = true)
                    Kpi(Icons.Rounded.CalendarMonth, Tone.Neutral, "7 days", s.blockedWeek.toString(), "blocked this week", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Kpi(globStarIcon(2.4f), Tone.Info, null, "${s.activeRules}", "rules active", Modifier.weight(1f), suffix = " / ${s.totalRules}", onClick = onOpenRules)
                    val tr = s.topRule
                    Kpi(Icons.Rounded.StarOutline, Tone.Red, null, tr?.pattern ?: "—", if (tr != null) "top rule · ${s.topRuleHits} hits" else "no hits yet",
                        Modifier.weight(1f), mono = true, onClick = if (tr != null) ({ onOpenRule(tr) }) else null)
                }
            }
        }
        item { WeekCard(s.week, s.blockedWeek) }
        item { SectionLabel("Recent activity", right = "See all", onRight = onOpenLog) }
        item {
            if (s.recent.isEmpty()) {
                CCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.Shield, null, tint = c.text2, modifier = Modifier.size(32.dp))
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
    CCard(modifier, onClick = onClick, padding = PaddingValues(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Tile(icon, tone, size = 36.dp, iconSize = 18.dp)
            Box(Modifier.weight(1f))
            if (tag != null) Text(tag, style = CType.label, color = c.text2)
            if (onClick != null) Icon(Icons.Rounded.ChevronRight, null, tint = c.text2, modifier = Modifier.size(18.dp))
        }
        Box(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = if (mono) CType.mono else CType.numL, color = if (red) c.red else c.text, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 140.dp))
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
    CCard(Modifier.fillMaxWidth()) {
        SectionLabel("This week", right = "$total blocked")
        Row(Modifier.fillMaxWidth().height(120.dp).padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            week.forEach { d ->
                Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    val frac = (0.06f + 0.94f * d.count / max) * grow.value
                    Box(Modifier.fillMaxWidth().fillMaxHeight(frac * 0.82f).clip(RoundedCornerShape(8.dp)).background(if (d.today) c.red else c.surface2))
                    Box(Modifier.height(6.dp))
                    Text(d.label, style = CType.label.copy(fontSize = CType.label.fontSize * 0.92f), color = c.text2)
                }
            }
        }
    }
}
