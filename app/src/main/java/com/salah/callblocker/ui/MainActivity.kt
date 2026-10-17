package com.salah.callblocker.ui

import android.Manifest
import android.app.role.RoleManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PhoneDisabled
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SpaceDashboard
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salah.callblocker.CallBlockerApp
import com.salah.callblocker.data.BlockRule
import com.salah.callblocker.data.BlockedCall
import com.salah.callblocker.data.PatternType
import com.salah.callblocker.data.ThemeMode
import com.salah.callblocker.domain.RuleMatcher
import com.salah.callblocker.ui.dashboard.DashboardScreen
import com.salah.callblocker.ui.kit.AlertHost
import com.salah.callblocker.ui.kit.AlertMsg
import com.salah.callblocker.ui.kit.Alerts
import com.salah.callblocker.ui.kit.LocalAlerts
import com.salah.callblocker.ui.kit.Tone
import com.salah.callblocker.ui.kit.displayNumber
import com.salah.callblocker.ui.kit.label
import com.salah.callblocker.ui.log.LogScreen
import com.salah.callblocker.ui.onboarding.OnboardingScreen
import com.salah.callblocker.ui.rules.RuleDraft
import com.salah.callblocker.ui.rules.RuleSheet
import com.salah.callblocker.ui.rules.RulesScreen
import com.salah.callblocker.ui.rules.RulesViewModel
import com.salah.callblocker.ui.rules.toDraft
import com.salah.callblocker.ui.settings.SettingsScreen
import com.salah.callblocker.ui.settings.SettingsViewModel
import com.salah.callblocker.ui.sheets.AllowedSheet
import com.salah.callblocker.ui.sheets.CallSheet
import com.salah.callblocker.ui.sheets.ConfirmSheet
import com.salah.callblocker.ui.splash.SplashScreen
import com.salah.callblocker.ui.theme.CType
import com.salah.callblocker.ui.theme.CallBlockerTheme
import com.salah.callblocker.ui.theme.CeramicLight
import com.salah.callblocker.ui.theme.LocalCeramic
import kotlinx.coroutines.launch

private enum class Tab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.SpaceDashboard),
    RULES("Rules", Icons.AutoMirrored.Rounded.List),
    LOG("Log", Icons.Rounded.History),
    SETTINGS("Settings", Icons.Rounded.Settings),
}

private enum class Phase { SPLASH, ONBOARD, APP }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settings = CallBlockerApp.settings(this)
        setContent {
            val themeMode by settings.themeMode.collectAsStateWithLifecycle()
            val dark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            var phase by rememberSaveable { mutableStateOf(Phase.SPLASH) }
            val roleState = rememberRoleState()
            // Status/nav bar icons follow the app's theme (splash + onboarding are always light).
            val lightBars = phase != Phase.APP || !dark
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = lightBars
                    isAppearanceLightNavigationBars = lightBars
                }
            }
            CallBlockerTheme(darkTheme = dark) {
                Surface(Modifier.fillMaxSize(), color = LocalCeramic.current.bg) {
                    if (phase == Phase.APP) {
                        val c = LocalCeramic.current
                        val top = if (c.isDark) Color(0xFF36333C) else Color(0xFFFFFFFF)
                        val bottom = if (c.isDark) Color(0xFF2E100C) else Color(0xFFCDB39C)
                        Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(top, c.bg, bottom))))
                    }
                    Crossfade(phase, animationSpec = tween(450), label = "phase") { p ->
                        when (p) {
                            Phase.SPLASH -> SplashScreen(onFinished = {
                                phase = if (settings.onboarded.value) Phase.APP else Phase.ONBOARD
                            })
                            // Splash and onboarding are always the light ceramic look.
                            Phase.ONBOARD -> CompositionLocalProvider(LocalCeramic provides CeramicLight) {
                                OnboardingScreen(
                                    roleHeld = roleState.held,
                                    onRequestRole = roleState.request,
                                    onDone = { settings.setOnboarded(); phase = Phase.APP },
                                )
                            }
                            Phase.APP -> GlobberApp(roleState)
                        }
                    }
                }
            }
        }
    }
}

private class RoleState(val held: Boolean, val request: () -> Unit)

/** Tracks whether Globber holds ROLE_CALL_SCREENING; refreshes on resume. */
@Composable
private fun rememberRoleState(): RoleState {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val rm = remember { context.getSystemService(RoleManager::class.java) }
    fun isHeld() = rm?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
    var held by remember { mutableStateOf(isHeld()) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { held = isHeld() }
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) held = isHeld() }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }
    return RoleState(held) { rm?.let { launcher.launch(it.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)) } }
}

@Composable
private fun GlobberApp(role: RoleState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val alerts = remember { Alerts() }
    val settingsVm: SettingsViewModel = viewModel()
    val rulesVm: RulesViewModel = viewModel()
    val rules by rulesVm.rules.collectAsStateWithLifecycle()
    val allow by settingsVm.allowList.collectAsStateWithLifecycle()
    val calls by CallBlockerApp.repository(context).blockedCalls.collectAsStateWithLifecycle(emptyList())

    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var ruleDraft by remember { mutableStateOf<RuleDraft?>(null) }
    var openCall by remember { mutableStateOf<BlockedCall?>(null) }
    var confirm by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }
    var deleteRule by remember { mutableStateOf<BlockRule?>(null) }
    var showAllowed by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            val json = settingsVm.exportJson()
            context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            alerts.show(AlertMsg("Rules exported", "${rules.size} rules saved", Icons.Rounded.FileUpload, Tone.Ok))
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val json = uri?.let { context.contentResolver.openInputStream(it)?.bufferedReader()?.use { r -> r.readText() } }
        if (json != null) {
            settingsVm.importJson(json, replace = false)
            alerts.show(AlertMsg("Rules imported", "Added to your list", Icons.Rounded.FileDownload, Tone.Ok))
        }
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) { if (Build.VERSION.SDK_INT >= 33) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }

    BackHandler(enabled = tab != Tab.HOME) { tab = Tab.HOME }

    CompositionLocalProvider(LocalAlerts provides alerts) {
        val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 96.dp
        val pad = PaddingValues(top = top, bottom = bottom)
        Box(Modifier.fillMaxSize()) {
            AnimatedContent(tab, transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) }, label = "tab") { t ->
                when (t) {
                    Tab.HOME -> DashboardScreen(
                        roleHeld = role.held, allowList = allow, onRequestRole = role.request,
                        onOpenRules = { tab = Tab.RULES }, onOpenLog = { tab = Tab.LOG },
                        onOpenRule = { ruleDraft = it.toDraft() }, onOpenCall = { openCall = it }, contentPadding = pad,
                    )
                    Tab.RULES -> RulesScreen(onEdit = { ruleDraft = it?.toDraft() ?: RuleDraft() }, contentPadding = pad, vm = rulesVm)
                    Tab.LOG -> LogScreen(allow, onOpenCall = { openCall = it }, onConfirmClear = { n, ok -> confirm = "$n" to ok }, contentPadding = pad)
                    Tab.SETTINGS -> SettingsScreen(
                        vm = settingsVm, roleHeld = role.held, ruleCount = rules.size, onRequestRole = role.request,
                        onHiddenChanged = { on ->
                            alerts.show(AlertMsg(if (on) "Hidden numbers blocked" else "Hidden numbers allowed", "Applies to private and withheld IDs",
                                if (on) Icons.Rounded.PhoneDisabled else Icons.Rounded.Call, if (on) Tone.Red else Tone.Info))
                        },
                        onOpenAllowed = { showAllowed = true },
                        onExport = { exportLauncher.launch("globber-rules.json") },
                        onImport = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                        contentPadding = pad,
                    )
                }
            }
            BottomNav(tab, { tab = it }, Modifier.align(Alignment.BottomCenter))
            AlertHost(alerts)
        }

        ruleDraft?.let { d ->
            RuleSheet(
                draft = d, allRules = rules,
                onSave = { r ->
                    if (d.existing != null) rulesVm.updateRule(r) else rulesVm.addRule(r.pattern, r.type, r.label, r.action)
                    if (tab != Tab.RULES) tab = Tab.RULES
                    alerts.show(AlertMsg(if (d.existing != null) "Rule saved" else "Rule added", "${r.pattern} · ${r.action.label}", Icons.Rounded.Check, Tone.Ok))
                },
                onDelete = { deleteRule = it },
                onDismiss = { ruleDraft = null },
            )
        }
        deleteRule?.let { r ->
            ConfirmSheet("Delete this rule?", "${r.pattern} will stop blocking calls. Past log entries stay.", "Delete rule",
                onConfirm = {
                    rulesVm.delete(r)
                    alerts.show(AlertMsg("Rule deleted", r.pattern, Icons.Rounded.DeleteOutline, Tone.Red, "Undo") { rulesVm.restore(r) })
                },
                onDismiss = { deleteRule = null })
        }
        confirm?.let { (n, ok) ->
            ConfirmSheet("Clear the log?", "All $n blocked-call entries will be removed. Your rules stay.", "Clear log", onConfirm = ok, onDismiss = { confirm = null })
        }
        openCall?.let { call ->
            val nn = RuleMatcher.national(call.number)
            val allowed = nn in allow
            CallSheet(
                call = call,
                rule = rules.firstOrNull { it.id == call.matchedRuleId },
                timesBlocked = calls.count { RuleMatcher.national(it.number) == nn },
                allowed = allowed,
                onToggleAllow = {
                    if (allowed) {
                        settingsVm.disallow(nn)
                        alerts.show(AlertMsg("Blocking again", displayNumber(call.number), Icons.Rounded.Block, Tone.Red))
                    } else {
                        settingsVm.allow(nn)
                        alerts.show(AlertMsg("Number allowed", "${displayNumber(call.number)} will always ring", Icons.Rounded.Check, Tone.Ok, "Undo") { settingsVm.disallow(nn) })
                    }
                },
                onMakeRule = { p -> ruleDraft = RuleDraft(pattern = p, type = PatternType.STARTS_WITH, label = "From blocked call", test = call.number) },
                onCopied = { alerts.show(AlertMsg("Copied", displayNumber(call.number), Icons.Rounded.ContentCopy, Tone.Info)) },
                onDismiss = { openCall = null },
            )
        }
        if (showAllowed) AllowedSheet(allow, onRemove = settingsVm::disallow, onDismiss = { showAllowed = false })
    }
}

/**
 * macOS-style dock: compact glass bar centred at the bottom, square icon tiles,
 * a dot under the active tab, and dock magnification while a tile is pressed
 * (the pressed tile grows and lifts, its neighbours grow a little).
 */
@Composable
private fun BottomNav(current: Tab, onSelect: (Tab) -> Unit, modifier: Modifier = Modifier) {
    val c = LocalCeramic.current
    var pressed by remember { mutableStateOf<Int?>(null) }
    val shape = RoundedCornerShape(26.dp)
    Box(modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 14.dp), contentAlignment = Alignment.BottomCenter) {
        Row(
            Modifier
                .shadow(28.dp, shape, ambientColor = Color.Black.copy(alpha = 0.25f), spotColor = Color.Black.copy(alpha = 0.3f))
                // background(shape) instead of clip so magnified tiles can rise above the bar
                .background(c.surface.copy(alpha = if (c.isDark) 0.9f else 0.82f), shape)
                .border(1.dp, if (c.isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.9f), shape)
                .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Tab.entries.forEachIndexed { i, t ->
                val on = t == current
                val dist = pressed?.let { kotlin.math.abs(it - i) }
                val target = when (dist) { 0 -> 1.28f; 1 -> 1.1f; else -> 1f }
                val scale by animateFloatAsState(target, spring(dampingRatio = 0.55f, stiffness = 420f), label = "mag")
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .graphicsLayer {
                                scaleX = scale; scaleY = scale
                                transformOrigin = TransformOrigin(0.5f, 1f)
                                translationY = -(scale - 1f) * 18.dp.toPx()
                            }
                            .size(52.dp)
                            .shadow(if (on) 10.dp else 3.dp, RoundedCornerShape(15.dp))
                            .clip(RoundedCornerShape(15.dp))
                            .background(if (on) c.ink else c.surface2)
                            .pointerInput(i) {
                                detectTapGestures(
                                    onPress = { pressed = i; tryAwaitRelease(); pressed = null },
                                    onTap = { onSelect(t) },
                                )
                            }
                            .semantics { contentDescription = t.title; role = Role.Tab; selected = on },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(t.icon, null, tint = if (on) c.onInk else c.text, modifier = Modifier.size(24.dp))
                    }
                    Box(Modifier.height(6.dp))
                    Box(Modifier.size(5.dp).clip(CircleShape).background(if (on) c.text else Color.Transparent))
                }
            }
        }
    }
}
