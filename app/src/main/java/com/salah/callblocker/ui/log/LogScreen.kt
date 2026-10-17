package com.salah.callblocker.ui.log

import androidx.compose.material.icons.rounded.Apps
import com.salah.callblocker.ui.kit.Tile
import androidx.compose.foundation.lazy.rememberLazyListState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.salah.callblocker.ui.kit.SliverBar
import com.salah.callblocker.ui.kit.LocalHaze
import com.salah.callblocker.ui.kit.collapse
import com.salah.callblocker.ui.kit.largeTitle
import androidx.compose.runtime.remember
import com.salah.callblocker.ui.kit.EmptyArt
import com.salah.callblocker.ui.kit.EmptyIllustration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salah.callblocker.data.BlockedCall
import com.salah.callblocker.data.RuleAction
import com.salah.callblocker.domain.RuleMatcher
import com.salah.callblocker.ui.kit.AlertMsg
import com.salah.callblocker.ui.kit.Badge
import com.salah.callblocker.ui.kit.BtnStyle
import com.salah.callblocker.ui.kit.CButton
import com.salah.callblocker.ui.kit.CCard
import com.salah.callblocker.ui.kit.ChipRow
import com.salah.callblocker.ui.kit.Gutter
import com.salah.callblocker.ui.kit.LocalAlerts
import com.salah.callblocker.ui.kit.RoundIconButton
import com.salah.callblocker.ui.kit.ScreenHeader
import com.salah.callblocker.ui.kit.SearchField
import com.salah.callblocker.ui.kit.Tone
import com.salah.callblocker.ui.kit.ChipIcon
import com.salah.callblocker.ui.kit.ChipGlyph
import com.salah.callblocker.ui.kit.dayLabel
import com.salah.callblocker.ui.kit.displayNumber
import com.salah.callblocker.ui.kit.globText
import com.salah.callblocker.ui.kit.icon
import com.salah.callblocker.ui.kit.label
import com.salah.callblocker.ui.kit.startOfDay
import com.salah.callblocker.ui.kit.timeLabel
import com.salah.callblocker.ui.kit.tone
import com.salah.callblocker.ui.theme.CType
import com.salah.callblocker.ui.theme.LocalCeramic

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogScreen(
    allowList: Set<String>,
    onOpenCall: (BlockedCall) -> Unit,
    onConfirmClear: (count: Int, confirm: () -> Unit) -> Unit,
    contentPadding: PaddingValues,
    vm: LogViewModel = viewModel(),
) {
    val calls by vm.calls.collectAsStateWithLifecycle()
    val alerts = LocalAlerts.current
    val c = LocalCeramic.current
    var q by rememberSaveable { mutableStateOf("") }
    var f by rememberSaveable { mutableStateOf<String?>(null) }

    val count = { a: RuleAction -> calls.count { it.action == a } }
    val filters = listOf<Pair<String?, String>>(
        null to "All · ${calls.size}",
        RuleAction.REJECT.name to "Rejected · ${count(RuleAction.REJECT)}",
        RuleAction.SILENCE.name to "Silenced · ${count(RuleAction.SILENCE)}",
        RuleAction.VOICEMAIL.name to "Voicemail · ${count(RuleAction.VOICEMAIL)}",
    )
    val qq = q.filterNot { it.isWhitespace() }
    val visible = calls.filter { call ->
        (qq.isEmpty() || call.number.filterNot { it.isWhitespace() }.contains(qq, true) || call.matchedPattern.filterNot { it.isWhitespace() }.contains(qq, true)) &&
            (f == null || call.action.name == f)
    }
    val groups = visible.groupBy { dayLabel(it.timestamp) }

    val today = startOfDay(System.currentTimeMillis())
    val todayN = calls.count { it.timestamp >= today }
    val weekN = calls.count { it.timestamp >= today - 6 * 86_400_000L }
    val top = calls.groupingBy { it.matchedPattern }.eachCount().maxByOrNull { it.value }

    val list = rememberLazyListState()
    val haze = LocalHaze.current
    val collapsed by list.collapse()
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize().hazeSource(haze),
            state = list,
            contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + 8.dp, bottom = contentPadding.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Box(Modifier.padding(horizontal = Gutter).largeTitle { collapsed }) {
                    ScreenHeader("Blocked calls", "Tap a call to allow it or make a rule") {
                        if (calls.isNotEmpty()) RoundIconButton(Icons.Rounded.DeleteOutline, "Clear log") {
                            onConfirmClear(calls.size) {
                                val old = calls
                                vm.clear()
                                alerts.show(AlertMsg("Log cleared", "${old.size} entries removed", Icons.Rounded.DeleteOutline, Tone.Red, "Undo") { vm.restore(old) })
                            }
                        }
                    }
                }
            }
            if (calls.isNotEmpty()) item {
                CCard(Modifier.fillMaxWidth().padding(horizontal = Gutter)) {
                    Row(Modifier.height(IntrinsicSize.Min)) {
                        Stat("Today", "$todayN", red = todayN > 0, modifier = Modifier.weight(1f))
                        Box(Modifier.width(1.dp).fillMaxHeight().background(c.line))
                        Stat("7 days", "$weekN", modifier = Modifier.weight(1f).padding(start = 14.dp))
                        Box(Modifier.width(1.dp).fillMaxHeight().background(c.line))
                        Column(Modifier.weight(1.3f).padding(start = 14.dp)) {
                            Text(globText(top?.key ?: "—"), style = CType.mono, color = c.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("top pattern · ${top?.value ?: 0}", style = CType.label, color = c.text2)
                        }
                    }
                }
            }
            item { Box(Modifier.padding(horizontal = Gutter)) { SearchField(q, { q = it }, "Search number or pattern") } }
            item {
            ChipRow(filters, f, { f = it }) { k ->
                if (k == null) ChipIcon(Icons.Rounded.Apps, Tone.Neutral)
                else RuleAction.valueOf(k).let { a -> ChipIcon(a.icon, a.tone) }
            }
        }
            when {
                calls.isEmpty() -> item { EmptyCard(EmptyArt.Quiet, "All quiet", "Calls Globber stops will show up here.", Modifier.fillParentMaxHeight(0.62f)) }
                visible.isEmpty() -> item {
                    EmptyCard(EmptyArt.Search, "Nothing matches", "No blocked call fits that search.", Modifier.fillParentMaxHeight(0.62f)) {
                        CButton("Clear search", { q = ""; f = null }, style = BtnStyle.Ghost, small = true)
                    }
                }
                else -> groups.forEach { (day, list) ->
                    stickyHeader(key = "h$day") {
                        Row(
                            Modifier.fillMaxWidth().background(c.bg).padding(horizontal = Gutter + 2.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(day.uppercase(), style = CType.label.copy(letterSpacing = CType.label.fontSize * 0.08f), color = c.text, modifier = Modifier.weight(1f))
                            Badge("${list.size} blocked")
                        }
                    }
                    itemsIndexed(list, key = { _, call -> call.id }) { i, call ->
                        TimelineRow(call, RuleMatcher.national(call.number) in allowList, first = i == 0, last = i == list.lastIndex) { onOpenCall(call) }
                    }
                }
            }
        }
        SliverBar("Blocked calls", collapsed, haze, contentPadding.calculateTopPadding())
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier, red: Boolean = false) {
    val c = LocalCeramic.current
    Column(modifier) {
        Text(value, style = CType.numL, color = if (red) c.red else c.text)
        Text(label, style = CType.label, color = c.text2)
    }
}

/** Timeline entry: time · rail with action-coloured dot · call card. */
@Composable
private fun TimelineRow(call: BlockedCall, allowed: Boolean, first: Boolean, last: Boolean, onClick: () -> Unit) {
    val c = LocalCeramic.current
    val dot = when { allowed -> c.ok; call.action == RuleAction.REJECT -> c.red; call.action == RuleAction.SILENCE -> c.info; else -> c.text2 }
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(horizontal = Gutter), verticalAlignment = Alignment.CenterVertically) {
        Text(timeLabel(call.timestamp), style = CType.label, color = c.text2, textAlign = TextAlign.End, modifier = Modifier.width(42.dp))
        Box(Modifier.width(28.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
            Column(Modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.width(2.dp).weight(1f).background(if (first) Color.Transparent else c.line))
                Box(Modifier.width(2.dp).weight(1f).background(if (last) Color.Transparent else c.line))
            }
            Box(Modifier.size(14.dp).clip(CircleShape).background(c.bg))
            Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
        }
        CCard(Modifier.weight(1f), onClick = onClick, padding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(displayNumber(call.number), style = CType.mono, color = c.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(globText(call.matchedPattern), style = CType.meta.copy(fontFamily = CType.mono.fontFamily), color = c.text2, maxLines = 1)
                }
                if (allowed) Badge("Allowed", Tone.Ok) else Badge(call.action.label, call.action.tone, call.action.icon)
            }
        }
    }
}

@Composable
private fun EmptyCard(art: EmptyArt, title: String, sub: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    val c = LocalCeramic.current
    Box(modifier.fillMaxWidth().padding(horizontal = Gutter), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EmptyIllustration(art)
            Text(title, style = CType.h, color = c.text)
            Text(sub, style = CType.meta, color = c.text2, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            action?.invoke()
        }
    }
}
