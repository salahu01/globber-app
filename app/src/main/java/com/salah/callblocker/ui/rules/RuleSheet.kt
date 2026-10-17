package com.salah.callblocker.ui.rules

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.salah.callblocker.data.BlockRule
import com.salah.callblocker.data.PatternType
import com.salah.callblocker.data.RuleAction
import com.salah.callblocker.domain.RuleMatcher
import com.salah.callblocker.ui.kit.CButton
import com.salah.callblocker.ui.kit.ChipRow
import com.salah.callblocker.ui.kit.RoundIconButton
import com.salah.callblocker.ui.kit.Tile
import com.salah.callblocker.ui.kit.Tone
import com.salah.callblocker.ui.kit.icon
import com.salah.callblocker.ui.kit.label
import com.salah.callblocker.ui.kit.past
import com.salah.callblocker.ui.kit.pressable
import com.salah.callblocker.ui.theme.CType
import com.salah.callblocker.ui.theme.LocalCeramic
import kotlinx.coroutines.launch

/** What the rule sheet opens with: an existing rule to edit, or a prefilled new one. */
data class RuleDraft(
    val existing: BlockRule? = null,
    val pattern: String = "",
    val type: PatternType = PatternType.STARTS_WITH,
    val label: String = "",
    val test: String = "+91 140 4482 917",
)

fun BlockRule.toDraft() = RuleDraft(existing = this, pattern = pattern, type = type, label = label)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleSheet(
    draft: RuleDraft,
    allRules: List<BlockRule>,
    onSave: (BlockRule) -> Unit,
    onDelete: (BlockRule) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = LocalCeramic.current
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var pattern by remember { mutableStateOf(draft.pattern) }
    var type by remember { mutableStateOf(draft.type) }
    var action by remember { mutableStateOf(draft.existing?.action ?: RuleAction.REJECT) }
    var label by remember { mutableStateOf(draft.label) }
    var test by remember { mutableStateOf(draft.test) }
    var error by remember { mutableStateOf<String?>(null) }
    val shake = remember { Animatable(0f) }

    val probe = BlockRule(pattern = pattern.trim(), type = type, action = action)
    val hit = pattern.isNotBlank() && RuleMatcher.matches(test, probe)

    fun close(then: () -> Unit = {}) = scope.launch { state.hide() }.invokeOnCompletion { onDismiss(); then() }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state, containerColor = c.surface, dragHandle = {
        Box(Modifier.padding(top = 10.dp, bottom = 6.dp).size(40.dp, 5.dp).clip(CircleShape).background(c.line))
    }) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).navigationBarsPadding().padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (draft.existing != null) "Edit rule" else "New rule", style = CType.title, color = c.text, modifier = Modifier.weight(1f))
                draft.existing?.let { r -> RoundIconButton(Icons.Rounded.DeleteOutline, "Delete rule") { close { onDelete(r) } } }
            }

            Field("Pattern") {
                InputPill(
                    pattern, { pattern = it; error = null }, Icons.Rounded.Tag,
                    placeholder = if (type == PatternType.REGEX) "^\\+91 ?140" else "+91 140*",
                    style = CType.monoL, error = error != null,
                    modifier = Modifier.offset(x = shake.value.dp),
                    keyboard = if (type == PatternType.REGEX) KeyboardType.Text else KeyboardType.Phone,
                )
                error?.let {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.WarningAmber, null, tint = c.redInk, modifier = Modifier.size(14.dp))
                        Text(it, style = CType.meta, color = c.redInk)
                    }
                }
            }

            Field("Match type") {
                Box(Modifier.offset(x = (-20).dp).fillMaxWidth()) {
                    ChipRow(com.salah.callblocker.ui.kit.PatternTypesUi.map { it to it.label }, type, { type = it; error = null })
                }
            }

            Field("Test a number") {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(if (hit) c.redBg else c.surface2)
                        .then(if (hit) Modifier.border(1.dp, c.red.copy(alpha = 0.4f), RoundedCornerShape(16.dp)) else Modifier)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Tile(if (hit) Icons.Rounded.Block else Icons.Rounded.Call, if (hit) Tone.Red else Tone.Ok, size = 36.dp, iconSize = 18.dp)
                    Column(Modifier.weight(1f)) {
                        BasicTextField(test, { test = it }, singleLine = true, textStyle = CType.mono.copy(color = c.text), cursorBrush = SolidColor(c.red),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                        Text(
                            when { pattern.isBlank() -> "Type a pattern to test"; hit -> "Would be ${action.past}"; else -> "Would ring normally" },
                            style = CType.meta, color = c.text2,
                        )
                    }
                }
            }

            Field("When it matches") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RuleAction.entries.forEach { a ->
                        val on = a == action
                        Column(
                            Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                                .background(if (on) c.surface else c.surface2)
                                .border(if (on) 1.5.dp else 1.dp, if (on) c.ink else c.hair, RoundedCornerShape(16.dp))
                                .pressable { action = a }.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(a.icon, null, tint = if (on) c.red else c.text2, modifier = Modifier.size(22.dp))
                            Text(a.label, style = CType.label, color = if (on) c.text else c.text2)
                        }
                    }
                }
            }

            Field("Label (optional)") {
                InputPill(label, { label = it }, Icons.Rounded.Edit, placeholder = "Loan offers")
            }

            CButton(
                if (draft.existing != null) "Save changes" else "Add rule",
                icon = Icons.Rounded.Check,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val p = pattern.trim()
                    val msg = when {
                        p.isEmpty() -> "Enter a number or pattern"
                        type == PatternType.REGEX && runCatching { Regex(p) }.isFailure -> "That regex isn't valid"
                        type != PatternType.REGEX && p.count { it.isDigit() } < 3 -> "Use at least 3 digits"
                        allRules.any { it.id != draft.existing?.id && it.pattern == p && it.type == type } -> "You already have this rule"
                        else -> null
                    }
                    if (msg != null) {
                        error = msg
                        scope.launch { for (x in listOf(-8f, 8f, -6f, 4f, 0f)) shake.animateTo(x) }
                        return@CButton
                    }
                    val rule = (draft.existing ?: BlockRule(pattern = p, type = type)).copy(pattern = p, type = type, label = label.trim(), action = action)
                    close { onSave(rule) }
                },
            )
        }
    }
}

@Composable
private fun Field(label: String, content: @Composable () -> Unit) {
    val c = LocalCeramic.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label.uppercase(), style = CType.label.copy(letterSpacing = CType.label.fontSize * 0.08f), color = c.text2)
        content()
    }
}

@Composable
private fun InputPill(
    value: String,
    onChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String,
    modifier: Modifier = Modifier,
    style: TextStyle = CType.body,
    error: Boolean = false,
    keyboard: KeyboardType = KeyboardType.Text,
) {
    val c = LocalCeramic.current
    Row(
        modifier.fillMaxWidth().height(52.dp).clip(CircleShape).background(c.surface2)
            .then(if (error) Modifier.border(1.5.dp, c.red, CircleShape) else Modifier)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, null, tint = c.text2, modifier = Modifier.size(18.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(placeholder, style = style, color = c.text2)
            BasicTextField(value, onChange, singleLine = true, textStyle = style.copy(color = c.text), cursorBrush = SolidColor(c.red),
                keyboardOptions = KeyboardOptions(keyboardType = keyboard), modifier = Modifier.fillMaxWidth())
        }
    }
}
