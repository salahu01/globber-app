package com.salah.callblocker.ui.kit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Voicemail
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.salah.callblocker.data.BlockedCall
import com.salah.callblocker.data.PatternType
import com.salah.callblocker.data.RuleAction
import com.salah.callblocker.ui.theme.CType
import com.salah.callblocker.ui.theme.LocalCeramic
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val PatternType.label: String
    get() = when (this) {
        PatternType.STARTS_WITH -> "Starts with"; PatternType.CONTAINS -> "Contains"
        PatternType.ENDS_WITH -> "Ends with"; PatternType.EXACT -> "Exact"; PatternType.REGEX -> "Regex"
    }

/** Short glyph shown in the rule-type tile. */
val PatternType.glyph: String
    get() = when (this) {
        PatternType.STARTS_WITH -> "↦"; PatternType.CONTAINS -> "∗"; PatternType.ENDS_WITH -> "↤"
        PatternType.EXACT -> "="; PatternType.REGEX -> ".*"
    }

val RuleAction.label: String
    get() = when (this) { RuleAction.REJECT -> "Reject"; RuleAction.SILENCE -> "Silence"; RuleAction.VOICEMAIL -> "Voicemail" }

val RuleAction.past: String
    get() = when (this) { RuleAction.REJECT -> "rejected"; RuleAction.SILENCE -> "silenced"; RuleAction.VOICEMAIL -> "sent to voicemail" }

val RuleAction.icon: ImageVector
    get() = when (this) { RuleAction.REJECT -> Icons.Rounded.Block; RuleAction.SILENCE -> Icons.Rounded.NotificationsOff; RuleAction.VOICEMAIL -> Icons.Rounded.Voicemail }

val RuleAction.tone: Tone
    get() = when (this) { RuleAction.REJECT -> Tone.Red; RuleAction.SILENCE -> Tone.Info; RuleAction.VOICEMAIL -> Tone.Neutral }

fun startOfDay(ts: Long): Long = Calendar.getInstance().apply {
    timeInMillis = ts; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

fun dayLabel(ts: Long, now: Long = System.currentTimeMillis()): String {
    val diff = ((startOfDay(now) - startOfDay(ts)) / 86_400_000L).toInt()
    return when (diff) {
        0 -> "Today"; 1 -> "Yesterday"
        else -> SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date(ts))
    }
}

/** Display form of a caller ID, e.g. "+911404482917" → "+91 14044 82917" (locale formatting). */
fun displayNumber(raw: String): String =
    android.telephony.PhoneNumberUtils.formatNumber(raw, Locale.getDefault().country.ifEmpty { "IN" }) ?: raw

/** Narrow "numbers like this" prefix: country code + first five national digits. */
fun likePrefix(raw: String): String {
    val n = com.salah.callblocker.domain.RuleMatcher.national(raw)
    val full = com.salah.callblocker.domain.RuleMatcher.normalize(raw)
    val cc = if (full.startsWith("+") && full.length > n.length) full.dropLast(n.length) else ""
    return (if (cc.isNotEmpty()) "$cc " else "") + n.take(5) + "*"
}

/** Ordered for the UI: most useful match type first. */
val PatternTypesUi = listOf(PatternType.STARTS_WITH, PatternType.CONTAINS, PatternType.ENDS_WITH, PatternType.EXACT, PatternType.REGEX)

fun timeLabel(ts: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))

/** One blocked call: action tile · number + matched pattern · time + action badge. */
@Composable
fun BlockedCallRow(call: BlockedCall, allowed: Boolean, onClick: () -> Unit) {
    val c = LocalCeramic.current
    CRow(
        lead = { Tile(call.action.icon, call.action.tone) },
        title = displayNumber(call.number),
        titleStyle = CType.mono,
        sub = "${call.matchedPattern} · ${call.matchedType.label}",
        onClick = onClick,
    ) {
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(timeLabel(call.timestamp), style = CType.label, color = c.text2)
            if (allowed) Badge("Allowed", Tone.Ok) else Badge(call.action.label, call.action.tone)
        }
    }
}

/** Pattern text with glob wildcards (*) and regex metacharacters highlighted in signal red. */
@Composable
fun globText(pattern: String): androidx.compose.ui.text.AnnotatedString {
    val red = LocalCeramic.current.red
    return androidx.compose.ui.text.buildAnnotatedString {
        pattern.forEach { ch ->
            if (ch == '*' || ch == '^' || ch == '$' || ch == '?') {
                pushStyle(androidx.compose.ui.text.SpanStyle(color = red)); append(ch); pop()
            } else append(ch)
        }
    }
}

/** Tiny 7-bar sparkline; last bar (today) in red. */
@Composable
fun Spark(values: List<Int>, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
    val c = LocalCeramic.current
    val max = (values.maxOrNull() ?: 0).coerceAtLeast(1)
    androidx.compose.foundation.Canvas(modifier) {
        val n = values.size.coerceAtLeast(1)
        val gap = size.width * 0.08f
        val w = (size.width - gap * (n - 1)) / n
        values.forEachIndexed { i, v ->
            val h = (0.12f + 0.88f * v / max) * size.height
            drawRoundRect(
                color = if (i == n - 1 && v > 0) c.red else if (v > 0) c.text2.copy(alpha = 0.55f) else c.line,
                topLeft = androidx.compose.ui.geometry.Offset(i * (w + gap), size.height - h),
                size = androidx.compose.ui.geometry.Size(w, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w / 2),
            )
        }
    }
}
