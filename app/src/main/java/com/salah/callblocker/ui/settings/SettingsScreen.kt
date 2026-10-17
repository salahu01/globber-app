package com.salah.callblocker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PhoneDisabled
import androidx.compose.material.icons.rounded.ShieldMoon
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salah.callblocker.BuildConfig
import com.salah.callblocker.data.ThemeMode
import com.salah.callblocker.ui.kit.BtnStyle
import com.salah.callblocker.ui.kit.CButton
import com.salah.callblocker.ui.kit.CCard
import com.salah.callblocker.ui.kit.CListCard
import com.salah.callblocker.ui.kit.CRow
import com.salah.callblocker.ui.kit.CRowDivider
import com.salah.callblocker.ui.kit.CToggle
import com.salah.callblocker.ui.kit.GlobIcon
import com.salah.callblocker.ui.kit.Gutter
import com.salah.callblocker.ui.kit.SectionLabel
import com.salah.callblocker.ui.kit.Segmented
import com.salah.callblocker.ui.kit.Tile
import com.salah.callblocker.ui.kit.Tone
import com.salah.callblocker.ui.kit.VSpace
import com.salah.callblocker.ui.theme.CType
import com.salah.callblocker.ui.theme.LocalCeramic

@Composable
fun SettingsScreen(
    vm: SettingsViewModel,
    roleHeld: Boolean,
    ruleCount: Int,
    onRequestRole: () -> Unit,
    onLetContacts: (Boolean) -> Unit,
    onHiddenChanged: (Boolean) -> Unit,
    onOpenAllowed: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    contentPadding: PaddingValues,
) {
    val c = LocalCeramic.current
    val notify by vm.notifyOnBlock.collectAsStateWithLifecycle()
    val hidden by vm.blockUnknown.collectAsStateWithLifecycle()
    val theme by vm.themeMode.collectAsStateWithLifecycle()
    val contacts by vm.letContacts.collectAsStateWithLifecycle()
    val allow by vm.allowList.collectAsStateWithLifecycle()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(start = Gutter, end = Gutter, top = contentPadding.calculateTopPadding() + 8.dp, bottom = contentPadding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Settings", style = CType.title, color = c.text)

        // protection card
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(Brush.radialGradient(listOf(if (c.isDark) c.surface2 else c.surface, c.surface)))
                .background(c.surface.copy(alpha = 0f)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (roleHeld) GlobIcon(56.dp, corner = 18.dp) else Tile(Icons.Rounded.ShieldMoon, Tone.Red, size = 56.dp, iconSize = 26.dp)
                Column(Modifier.weight(1f)) {
                    Text(if (roleHeld) "Call screening is on" else "Call screening is off", style = CType.h, color = c.text)
                    Text(if (roleHeld) "Globber checks every incoming call" else "Globber can't block until it's the screening app", style = CType.meta, color = c.text2)
                }
            }
            CButton(if (roleHeld) "Change screening app" else "Set as call screening app", onRequestRole,
                style = if (roleHeld) BtnStyle.Ghost else BtnStyle.Ink, modifier = Modifier.fillMaxWidth())
        }

        SectionLabel("Calls")
        CListCard {
            CRow({ Tile(Icons.Rounded.Group, Tone.Info) }, "Let contacts through", if (contacts) "Saved contacts always ring" else "Needs contacts permission") {
                CToggle(contacts, onLetContacts)
            }
            CRowDivider()
            CRow({ Tile(Icons.Rounded.PhoneDisabled, Tone.Red) }, "Block hidden numbers", "Private or withheld caller ID") {
                CToggle(hidden, { vm.setBlockUnknown(it); onHiddenChanged(it) })
            }
            CRowDivider()
            CRow({ Tile(Icons.Rounded.NotificationsNone) }, "Notify when blocked", "One quiet notification per call") {
                CToggle(notify, vm::setNotifyOnBlock)
            }
            CRowDivider()
            CRow({ Tile(Icons.Rounded.Check, Tone.Ok) }, "Allowed numbers", if (allow.isEmpty()) "None yet" else "${allow.size} always ring", onClick = onOpenAllowed) {
                Icon(Icons.Rounded.ChevronRight, null, tint = c.text2, modifier = Modifier.size(18.dp))
            }
        }

        SectionLabel("Appearance")
        CCard(Modifier.fillMaxWidth()) {
            Segmented(listOf(ThemeMode.SYSTEM to "System", ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark"), theme, vm::setThemeMode)
        }

        SectionLabel("Backup", right = "$ruleCount rules")
        CListCard {
            CRow({ Tile(Icons.Rounded.FileUpload) }, "Export rules", "Save all rules to a JSON file", onClick = onExport) {
                Icon(Icons.Rounded.ChevronRight, null, tint = c.text2, modifier = Modifier.size(18.dp))
            }
            CRowDivider()
            CRow({ Tile(Icons.Rounded.FileDownload, Tone.Info) }, "Import rules", "Add rules from an exported file", onClick = onImport) {
                Icon(Icons.Rounded.ChevronRight, null, tint = c.text2, modifier = Modifier.size(18.dp))
            }
        }

        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(c.surface2).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Tile(Icons.Rounded.Lock, Tone.Ok)
            Text("No internet permission, no analytics, no ads. Everything stays on this phone.", style = CType.meta, color = c.text2, modifier = Modifier.weight(1f))
        }
        Text("Globber ${BuildConfig.VERSION_NAME} · ${BuildConfig.APPLICATION_ID}", style = CType.label, color = c.text2,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        VSpace(4.dp)
        Box(Modifier)
    }
}
