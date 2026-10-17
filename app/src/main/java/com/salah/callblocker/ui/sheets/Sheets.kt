@file:OptIn(ExperimentalMaterial3Api::class)

package com.salah.callblocker.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.salah.callblocker.data.BlockRule
import com.salah.callblocker.data.BlockedCall
import com.salah.callblocker.ui.kit.BtnStyle
import com.salah.callblocker.ui.kit.CButton
import com.salah.callblocker.ui.kit.CListCard
import com.salah.callblocker.ui.kit.CRow
import com.salah.callblocker.ui.kit.CRowDivider
import com.salah.callblocker.ui.kit.Tile
import com.salah.callblocker.ui.kit.Tone
import com.salah.callblocker.ui.kit.dayLabel
import com.salah.callblocker.ui.kit.displayNumber
import com.salah.callblocker.ui.kit.likePrefix
import com.salah.callblocker.ui.kit.icon
import com.salah.callblocker.ui.kit.label
import com.salah.callblocker.ui.kit.pressable
import com.salah.callblocker.ui.kit.timeLabel
import com.salah.callblocker.ui.kit.tone
import com.salah.callblocker.ui.theme.CType
import com.salah.callblocker.ui.theme.LocalCeramic
import kotlinx.coroutines.launch

/** Ceramic bottom sheet shell: grab handle, gutter, nav-bar inset. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CSheet(onDismiss: () -> Unit, state: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), content: @Composable ColumnScope.(close: (after: () -> Unit) -> Unit) -> Unit) {
    val c = LocalCeramic.current
    val scope = rememberCoroutineScope()
    val close: (() -> Unit) -> Unit = { after -> scope.launch { state.hide() }.invokeOnCompletion { onDismiss(); after() } }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state, containerColor = c.surface, dragHandle = {
        Box(Modifier.padding(top = 10.dp, bottom = 6.dp).size(40.dp, 5.dp).clip(CircleShape).background(c.line))
    }) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp).navigationBarsPadding().padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) { content(close) }
    }
}

/** Destructive confirm: icon, title, body, red action + cancel. */
@Composable
fun ConfirmSheet(title: String, body: String, cta: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val c = LocalCeramic.current
    CSheet(onDismiss) { close ->
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Tile(Icons.Rounded.DeleteOutline, Tone.Red, size = 56.dp, iconSize = 26.dp)
            Text(title, style = CType.title, color = c.text, textAlign = TextAlign.Center)
            Text(body, style = CType.body, color = c.text2, textAlign = TextAlign.Center)
        }
        CButton(cta, { close(onConfirm) }, style = BtnStyle.Danger, modifier = Modifier.fillMaxWidth())
        CButton("Cancel", { close {} }, style = BtnStyle.Ghost, modifier = Modifier.fillMaxWidth())
    }
}

/** Blocked-call detail: what matched, plus allow / make rule / copy. */
@Composable
fun CallSheet(
    call: BlockedCall,
    rule: BlockRule?,
    timesBlocked: Int,
    allowed: Boolean,
    onToggleAllow: () -> Unit,
    onMakeRule: (prefix: String) -> Unit,
    onCopied: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = LocalCeramic.current
    val clip = LocalClipboardManager.current
    val like = likePrefix(call.number)
    CSheet(onDismiss) { close ->
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Tile(call.action.icon, call.action.tone, size = 64.dp, iconSize = 30.dp)
            Text(displayNumber(call.number), style = CType.title.copy(fontFamily = CType.mono.fontFamily), color = c.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${dayLabel(call.timestamp)} · ${timeLabel(call.timestamp)} · ${call.action.label}", style = CType.meta, color = c.text2)
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Fact("Matched rule", call.matchedPattern, Modifier.weight(1f), mono = true)
                Fact("Match type", call.matchedType.label, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Fact("Times blocked", timesBlocked.toString(), Modifier.weight(1f))
                Fact("Rule label", rule?.label?.ifBlank { null } ?: "—", Modifier.weight(1f))
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Action(if (allowed) Icons.AutoMirrored.Rounded.Undo else Icons.Rounded.Check, Tone.Ok,
                if (allowed) "Block this number again" else "Always allow this number",
                if (allowed) "Remove it from your allow list" else "Adds an exception above every rule") { close(onToggleAllow) }
            Action(Icons.Rounded.Add, Tone.Neutral, "Block numbers like this", "New rule · starts with $like", monoSub = true) { close { onMakeRule(like) } }
            Action(Icons.Rounded.ContentCopy, Tone.Info, "Copy number", "To report or look it up") {
                clip.setText(AnnotatedString(displayNumber(call.number))); close(onCopied)
            }
        }
    }
}

@Composable
private fun Fact(label: String, value: String, modifier: Modifier, mono: Boolean = false) {
    val c = LocalCeramic.current
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(c.surface2).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = CType.label, color = c.text2)
        Text(value, style = if (mono) CType.mono else CType.num, color = c.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun Action(icon: ImageVector, tone: Tone, title: String, sub: String, monoSub: Boolean = false, onClick: () -> Unit) {
    val c = LocalCeramic.current
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.surface2).pressable(onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Tile(icon, tone, size = 36.dp, iconSize = 18.dp)
        Column(Modifier.weight(1f)) {
            Text(title, style = CType.num, color = c.text)
            Text(sub, style = if (monoSub) CType.meta.copy(fontFamily = CType.mono.fontFamily) else CType.meta, color = c.text2, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = c.text2, modifier = Modifier.size(18.dp))
    }
}

/** Allow-list viewer with per-number removal. */
@Composable
fun AllowedSheet(numbers: Set<String>, onRemove: (String) -> Unit, onDismiss: () -> Unit) {
    val c = LocalCeramic.current
    CSheet(onDismiss) { close ->
        Text("Allowed numbers", style = CType.title, color = c.text)
        if (numbers.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Check, null, tint = c.text2, modifier = Modifier.size(32.dp))
                Text("No exceptions yet", style = CType.h, color = c.text)
                Text("Open a blocked call and tap “Always allow” to add one.", style = CType.meta, color = c.text2, textAlign = TextAlign.Center)
            }
        } else CListCard {
            numbers.sorted().forEachIndexed { i, n ->
                if (i > 0) CRowDivider()
                CRow({ Tile(Icons.Rounded.Check, Tone.Ok) }, n, "Always rings", titleStyle = CType.mono) {
                    Icon(Icons.Rounded.DeleteOutline, "Remove", tint = c.text2, modifier = Modifier.size(22.dp).pressable { onRemove(n) })
                }
            }
        }
        CButton("Done", { close {} }, style = BtnStyle.Ghost, modifier = Modifier.fillMaxWidth())
    }
}
