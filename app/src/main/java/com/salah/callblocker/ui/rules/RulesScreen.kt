package com.salah.callblocker.ui.rules

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.salah.callblocker.data.PatternType
import com.salah.callblocker.ui.kit.AlertMsg
import com.salah.callblocker.ui.kit.Badge
import com.salah.callblocker.ui.kit.BtnStyle
import com.salah.callblocker.ui.kit.CButton
import com.salah.callblocker.ui.kit.CCard
import com.salah.callblocker.ui.kit.CToggle
import com.salah.callblocker.ui.kit.ChipRow
import com.salah.callblocker.ui.kit.Gutter
import com.salah.callblocker.ui.kit.LocalAlerts
import com.salah.callblocker.ui.kit.RoundIconButton
import com.salah.callblocker.ui.kit.ScreenHeader
import com.salah.callblocker.ui.kit.SearchField
import com.salah.callblocker.ui.kit.TextTile
import com.salah.callblocker.ui.kit.Tone
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
    contentPadding: PaddingValues,
    vm: RulesViewModel = viewModel(),
) {
    val rules by vm.rules.collectAsStateWithLifecycle()
    val hits by vm.hits.collectAsStateWithLifecycle()
    val alerts = LocalAlerts.current
    val c = LocalCeramic.current
    var q by rememberSaveable { mutableStateOf("") }
    var f by rememberSaveable { mutableStateOf(F_ALL) }
    var byHits by rememberSaveable { mutableStateOf(true) }

    val filters = listOf(F_ALL to "All", F_ON to "Active", F_OFF to "Paused") + com.salah.callblocker.ui.kit.PatternTypesUi.map { it.name to it.label }
    val visible = rules.filter { r ->
        (q.isBlank() || r.pattern.contains(q, true) || r.label.contains(q, true)) &&
            when (f) { F_ALL -> true; F_ON -> r.enabled; F_OFF -> !r.enabled; else -> r.type.name == f }
    }.let { l -> if (byHits) l.sortedByDescending { hits[it.id] ?: 0 } else l }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + 8.dp, bottom = contentPadding.calculateBottomPadding() + 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Box(Modifier.padding(horizontal = Gutter)) {
                    ScreenHeader("Rules", "${rules.count { it.enabled }} of ${rules.size} active · first match wins") {
                        RoundIconButton(Icons.Rounded.SwapVert, "Sort") {
                            byHits = !byHits
                            alerts.show(AlertMsg(if (byHits) "Sorted by most hits" else "Sorted by your order", icon = Icons.Rounded.SwapVert, tone = Tone.Info))
                        }
                    }
                }
            }
            item { Box(Modifier.padding(horizontal = Gutter)) { SearchField(q, { q = it }, "Search pattern or label") } }
            item { ChipRow(filters, f, { f = it }) }
            when {
                rules.isEmpty() -> item {
                    Empty(Icons.Rounded.FilterAlt, "No rules yet", "Add a number or pattern to start blocking calls.") {
                        CButton("Add your first rule", { onEdit(null) }, icon = Icons.Rounded.Add)
                    }
                }
                visible.isEmpty() -> item {
                    Empty(Icons.Rounded.Search, "No rules match", "Try a different search or filter.") {
                        CButton("Clear filters", { q = ""; f = F_ALL }, style = BtnStyle.Ghost, small = true)
                    }
                }
                else -> items(visible, key = { it.id }) { r ->
                    Box(Modifier.padding(horizontal = Gutter)) {
                        RuleCard(r, hits[r.id] ?: 0, onClick = { onEdit(r) }) {
                            vm.toggle(r)
                            alerts.show(AlertMsg(if (r.enabled) "Rule paused" else "Rule active", r.pattern,
                                if (r.enabled) Icons.Rounded.NotificationsOff else Icons.Rounded.Check, if (r.enabled) Tone.Info else Tone.Ok))
                        }
                    }
                }
            }
        }
        Row(
            Modifier.align(Alignment.BottomEnd).padding(end = Gutter, bottom = contentPadding.calculateBottomPadding() + 16.dp)
                .shadow(14.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(c.ink)
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
private fun RuleCard(r: BlockRule, hits: Int, onClick: () -> Unit, onToggle: () -> Unit) {
    val c = LocalCeramic.current
    CCard(Modifier.fillMaxWidth().alpha(if (r.enabled) 1f else 0.55f), onClick = onClick, padding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextTile(r.type.glyph)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(r.pattern, style = CType.monoL, color = c.text, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    textDecoration = if (r.enabled) null else TextDecoration.LineThrough)
                Text(r.label.ifBlank { r.type.label }, style = CType.meta, color = c.text2, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Badge(r.action.label, r.action.tone, r.action.icon)
                    Badge(r.type.label)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CToggle(r.enabled, { onToggle() })
                Text("$hits hits", style = CType.label, color = c.text2)
            }
        }
    }
}

@Composable
internal fun Empty(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    sub: String,
    action: @Composable () -> Unit,
) {
    val c = LocalCeramic.current
    CCard(Modifier.fillMaxWidth().padding(horizontal = Gutter)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, null, tint = c.text2, modifier = Modifier.size(32.dp))
            Text(title, style = CType.h, color = c.text)
            Text(sub, style = CType.meta, color = c.text2)
            action()
        }
    }
}
