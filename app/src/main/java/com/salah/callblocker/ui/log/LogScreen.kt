package com.salah.callblocker.ui.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salah.callblocker.data.BlockedCall
import com.salah.callblocker.data.RuleAction
import com.salah.callblocker.domain.RuleMatcher
import com.salah.callblocker.ui.kit.AlertMsg
import com.salah.callblocker.ui.kit.BlockedCallRow
import com.salah.callblocker.ui.kit.BtnStyle
import com.salah.callblocker.ui.kit.CButton
import com.salah.callblocker.ui.kit.CCard
import com.salah.callblocker.ui.kit.CListCard
import com.salah.callblocker.ui.kit.CRowDivider
import com.salah.callblocker.ui.kit.ChipRow
import com.salah.callblocker.ui.kit.Gutter
import com.salah.callblocker.ui.kit.LocalAlerts
import com.salah.callblocker.ui.kit.RoundIconButton
import com.salah.callblocker.ui.kit.ScreenHeader
import com.salah.callblocker.ui.kit.SearchField
import com.salah.callblocker.ui.kit.SectionLabel
import com.salah.callblocker.ui.kit.Tone
import com.salah.callblocker.ui.kit.dayLabel
import com.salah.callblocker.ui.theme.CType
import com.salah.callblocker.ui.theme.LocalCeramic

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
    var q by rememberSaveable { mutableStateOf("") }
    var f by rememberSaveable { mutableStateOf<String?>(null) }

    val filters = listOf<Pair<String?, String>>(null to "All", RuleAction.REJECT.name to "Rejected", RuleAction.SILENCE.name to "Silenced", RuleAction.VOICEMAIL.name to "Voicemail")
    val qq = q.filterNot { it.isWhitespace() }
    val visible = calls.filter { call ->
        (qq.isEmpty() || call.number.filterNot { it.isWhitespace() }.contains(qq, true) || call.matchedPattern.filterNot { it.isWhitespace() }.contains(qq, true)) &&
            (f == null || call.action.name == f)
    }
    val groups = visible.groupBy { dayLabel(it.timestamp) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + 8.dp, bottom = contentPadding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Box(Modifier.padding(horizontal = Gutter)) {
                ScreenHeader("Blocked calls", "${calls.size} screened") {
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
        item { Box(Modifier.padding(horizontal = Gutter)) { SearchField(q, { q = it }, "Search number or pattern") } }
        item { ChipRow(filters, f, { f = it }) }
        when {
            calls.isEmpty() -> item { EmptyCard(Icons.Rounded.History, "No blocked calls yet", "Blocked calls will show up here.") }
            visible.isEmpty() -> item {
                EmptyCard(Icons.Rounded.Search, "Nothing matches", "No blocked call fits that search.") {
                    CButton("Clear search", { q = ""; f = null }, style = BtnStyle.Ghost, small = true)
                }
            }
            else -> groups.forEach { (day, list) ->
                item(key = "h$day") { Box(Modifier.padding(horizontal = Gutter + 2.dp)) { SectionLabel(day, right = "${list.size} blocked") } }
                item(key = "g$day") {
                    Box(Modifier.padding(horizontal = Gutter)) {
                        CListCard {
                            list.forEachIndexed { i, call ->
                                if (i > 0) CRowDivider()
                                BlockedCallRow(call, RuleMatcher.national(call.number) in allowList) { onOpenCall(call) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, sub: String, action: (@Composable () -> Unit)? = null) {
    val c = LocalCeramic.current
    CCard(Modifier.fillMaxWidth().padding(horizontal = Gutter)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, null, tint = c.text2, modifier = Modifier.size(32.dp))
            Text(title, style = CType.h, color = c.text)
            Text(sub, style = CType.meta, color = c.text2)
            action?.invoke()
        }
    }
}
