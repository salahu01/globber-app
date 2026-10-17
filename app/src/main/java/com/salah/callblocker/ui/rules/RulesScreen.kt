package com.salah.callblocker.ui.rules

import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Pause
import com.salah.callblocker.ui.kit.Tile
import com.salah.callblocker.data.PatternType
import androidx.compose.foundation.lazy.rememberLazyListState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.salah.callblocker.ui.kit.SliverBar
import com.salah.callblocker.ui.kit.LocalHaze
import com.salah.callblocker.ui.kit.collapse
import com.salah.callblocker.ui.kit.largeTitle
import androidx.compose.runtime.remember
import com.salah.callblocker.ui.kit.EmptyArt
import com.salah.callblocker.ui.kit.premiumFill
import com.salah.callblocker.ui.kit.EmptyIllustration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salah.callblocker.data.BlockRule
import com.salah.callblocker.ui.kit.AlertMsg
import com.salah.callblocker.ui.kit.Badge
import com.salah.callblocker.ui.kit.BtnStyle
import com.salah.callblocker.ui.kit.CButton
import com.salah.callblocker.ui.kit.CCard
import com.salah.callblocker.ui.kit.CToggle
import com.salah.callblocker.ui.kit.ChipRow
import com.salah.callblocker.ui.kit.Gutter
import com.salah.callblocker.ui.kit.LocalAlerts
import com.salah.callblocker.ui.kit.PatternTypesUi
import com.salah.callblocker.ui.kit.RoundIconButton
import com.salah.callblocker.ui.kit.ScreenHeader
import com.salah.callblocker.ui.kit.SearchField
import com.salah.callblocker.ui.kit.SectionLabel
import com.salah.callblocker.ui.kit.Spark
import com.salah.callblocker.ui.kit.TextTile
import com.salah.callblocker.ui.kit.Tone
import com.salah.callblocker.ui.kit.ChipIcon
import com.salah.callblocker.ui.kit.ChipGlyph
import com.salah.callblocker.ui.kit.globText
import com.salah.callblocker.ui.kit.glyph
import com.salah.callblocker.ui.kit.icon
import com.salah.callblocker.ui.kit.label
import com.salah.callblocker.ui.kit.pressable
import com.salah.callblocker.ui.kit.tone
import com.salah.callblocker.ui.theme.CType
import com.salah.callblocker.ui.theme.LocalCeramic

private const val F_ALL = "all"
private const val F_ON = "on"
private const val F_OFF = "off"

@Composable
fun RulesScreen(
    onEdit: (BlockRule?) -> Unit,
    onDelete: (BlockRule) -> Unit,
    contentPadding: PaddingValues,
    vm: RulesViewModel = viewModel(),
) {
    val rules by vm.rules.collectAsStateWithLifecycle()
    val hits by vm.hits.collectAsStateWithLifecycle()
    val daily by vm.daily.collectAsStateWithLifecycle()
    val alerts = LocalAlerts.current
    val c = LocalCeramic.current
    var q by rememberSaveable { mutableStateOf("") }
    var f by rememberSaveable { mutableStateOf(F_ALL) }
    var byHits by rememberSaveable { mutableStateOf(true) }

    val filters = listOf(F_ALL to "All", F_ON to "Active", F_OFF to "Paused") + PatternTypesUi.map { it.name to it.label }
    val visible = rules.filter { r ->
        (q.isBlank() || r.pattern.contains(q, true) || r.label.contains(q, true)) &&
            when (f) { F_ALL -> true; F_ON -> r.enabled; F_OFF -> !r.enabled; else -> r.type.name == f }
    }.let { l -> if (byHits) l.sortedByDescending { hits[it.id] ?: 0 } else l }
    val (active, paused) = visible.partition { it.enabled }
    val weekStopped = daily.values.sumOf { it.sum() }

    fun toggle(r: BlockRule) {
        vm.toggle(r)
        alerts.show(AlertMsg(if (r.enabled) "Rule paused" else "Rule active", r.pattern,
            if (r.enabled) Icons.Rounded.NotificationsOff else Icons.Rounded.Check, if (r.enabled) Tone.Info else Tone.Ok))
    }

    Box(Modifier.fillMaxSize()) {
        val list = rememberLazyListState()
        val haze = LocalHaze.current
        val collapsed by list.collapse()
        LazyColumn(
            Modifier.fillMaxSize().hazeSource(haze),
            state = list,
            contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + 8.dp, bottom = contentPadding.calculateBottomPadding() + 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Box(Modifier.padding(horizontal = Gutter).largeTitle { collapsed }) {
                    ScreenHeader("Rules", "First match wins · swipe left to delete") {
                        RoundIconButton(Icons.Rounded.SwapVert, "Sort") {
                            byHits = !byHits
                            alerts.show(AlertMsg(if (byHits) "Sorted by most hits" else "Sorted by your order", icon = Icons.Rounded.SwapVert, tone = Tone.Info))
                        }
                    }
                }
            }
            if (rules.isNotEmpty()) item { Summary(rules.count { it.enabled }, rules.size, weekStopped) }
            item { Box(Modifier.padding(horizontal = Gutter)) { SearchField(q, { q = it }, "Search pattern or label") } }
            item { ChipRow(filters, f, { f = it }) { k ->
                    when (k) {
                        F_ALL -> ChipIcon(Icons.Rounded.Apps, Tone.Neutral)
                        F_ON -> ChipIcon(Icons.Rounded.Check, Tone.Ok)
                        F_OFF -> ChipIcon(Icons.Rounded.Pause, Tone.Info)
                        else -> ChipGlyph(PatternType.valueOf(k).glyph)
                    }
                } }
            when {
                rules.isEmpty() -> item {
                    Empty(EmptyArt.Rules, "Start with one pattern", "One rule like +91 140* stops a whole telemarketing series.", Modifier.fillParentMaxHeight(0.62f)) {
                        CButton("Add your first rule", { onEdit(null) }, icon = Icons.Rounded.Add)
                    }
                }
                visible.isEmpty() -> item {
                    Empty(EmptyArt.Search, "No rules match", "Try a different search or filter.", Modifier.fillParentMaxHeight(0.62f)) {
                        CButton("Clear filters", { q = ""; f = F_ALL }, style = BtnStyle.Ghost, small = true)
                    }
                }
                else -> {
                    if (active.isNotEmpty()) {
                        item(key = "h-on") { Box(Modifier.padding(horizontal = Gutter + 2.dp)) { SectionLabel("Active", right = "${active.size}") } }
                        items(active, key = { it.id }) { r ->
                            SwipeRule(r, Modifier.animateItem(), onDelete) { RuleCard(r, hits[r.id] ?: 0, daily[r.id], { onEdit(r) }) { toggle(r) } }
                        }
                    }
                    if (paused.isNotEmpty()) {
                        item(key = "h-off") { Box(Modifier.padding(horizontal = Gutter + 2.dp)) { SectionLabel("Paused", right = "${paused.size}") } }
                        items(paused, key = { it.id }) { r ->
                            SwipeRule(r, Modifier.animateItem(), onDelete) { RuleCard(r, hits[r.id] ?: 0, daily[r.id], { onEdit(r) }) { toggle(r) } }
                        }
                    }
                }
            }
        }
        SliverBar("Rules", collapsed, haze, contentPadding.calculateTopPadding())
        if (rules.isNotEmpty()) Row(
            Modifier.align(Alignment.BottomEnd).padding(end = Gutter, bottom = contentPadding.calculateBottomPadding() + 16.dp)
                .premiumFill(c.ink, RoundedCornerShape(18.dp))
                .pressable { onEdit(null) }.height(56.dp).padding(start = 16.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Rounded.Add, null, tint = c.onInk, modifier = Modifier.size(20.dp))
            Text("Add rule", style = CType.btn.copy(fontSize = CType.num.fontSize * 1.07f), color = c.onInk)
        }
    }
}

@Composable
private fun Summary(active: Int, total: Int, week: Int) {
    val c = LocalCeramic.current
    CCard(Modifier.fillMaxWidth().padding(horizontal = Gutter)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$active", style = CType.numL.copy(fontSize = CType.numL.fontSize * 1.4f), color = c.text)
                    Text(" / $total active", style = CType.meta, color = c.text2, modifier = Modifier.padding(bottom = 4.dp))
                }
                Text("patterns screening calls", style = CType.meta, color = c.text2)
            }
            Box(Modifier.width(1.dp).height(44.dp).background(c.line))
            Column(Modifier.weight(1f).padding(start = 16.dp)) {
                Text("$week", style = CType.numL.copy(fontSize = CType.numL.fontSize * 1.4f), color = c.red)
                Text("calls stopped · 7 days", style = CType.meta, color = c.text2)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeRule(r: BlockRule, modifier: Modifier, onDelete: (BlockRule) -> Unit, content: @Composable () -> Unit) {
    val c = LocalCeramic.current
    val state = rememberSwipeToDismissBoxState(confirmValueChange = {
        if (it == SwipeToDismissBoxValue.EndToStart) onDelete(r)
        false // the delete sheet confirms; the card springs back
    })
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        modifier = modifier.padding(horizontal = Gutter),
        backgroundContent = {
            Row(
                Modifier.fillMaxSize().clip(RoundedCornerShape(22.dp)).background(c.redBg).padding(end = 24.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Delete", style = CType.label, color = c.redInk)
                Icon(Icons.Rounded.DeleteOutline, null, tint = c.redInk, modifier = Modifier.padding(start = 6.dp).size(20.dp))
            }
        },
    ) { content() }
}

@Composable
private fun RuleCard(r: BlockRule, hits: Int, week: List<Int>?, onClick: () -> Unit, onToggle: () -> Unit) {
    val c = LocalCeramic.current
    CCard(Modifier.fillMaxWidth(), onClick = onClick, padding = PaddingValues(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextTile(r.type.glyph)
            Column(Modifier.weight(1f).alpha(if (r.enabled) 1f else 0.5f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(globText(r.pattern), style = CType.monoL.copy(fontSize = CType.monoL.fontSize * 1.1f), color = c.text, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, textDecoration = if (r.enabled) null else TextDecoration.LineThrough)
                Text(r.label.ifBlank { r.type.label }, style = CType.meta, color = c.text2, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            CToggle(r.enabled, { onToggle() })
        }
        Box(Modifier.padding(vertical = 12.dp).fillMaxWidth().height(1.dp).background(c.line))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Badge(r.action.label, r.action.tone, r.action.icon)
            Badge(r.type.label)
            Box(Modifier.weight(1f))
            Spark(week ?: List(7) { 0 }, Modifier.size(width = 44.dp, height = 18.dp))
            Text(if (hits == 1) "1 hit" else "$hits hits", style = CType.label, color = if (hits > 0) c.text else c.text2, modifier = Modifier.padding(start = 6.dp))
        }
    }
}

@Composable
internal fun Empty(
    art: EmptyArt,
    title: String,
    sub: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit,
) {
    val c = LocalCeramic.current
    Box(modifier.fillMaxWidth().padding(horizontal = Gutter), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EmptyIllustration(art)
            Text(title, style = CType.h, color = c.text)
            Text(sub, style = CType.meta, color = c.text2, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            action()
        }
    }
}
