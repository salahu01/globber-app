package com.salah.callblocker.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.salah.callblocker.CallBlockerApp
import com.salah.callblocker.data.BlockRule
import com.salah.callblocker.data.BlockedCall
import com.salah.callblocker.data.RuleRepository
import com.salah.callblocker.ui.kit.startOfDay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DayBar(val label: String, val count: Int, val today: Boolean)

data class DashboardUiState(
    val blockedToday: Int = 0,
    val blockedWeek: Int = 0,
    val activeRules: Int = 0,
    val totalRules: Int = 0,
    val topRule: BlockRule? = null,
    val topRuleHits: Int = 0,
    val week: List<DayBar> = emptyList(),
    val recent: List<BlockedCall> = emptyList(),
)

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: RuleRepository = CallBlockerApp.repository(app)

    val state: StateFlow<DashboardUiState> =
        combine(repo.rules, repo.blockedCalls) { rules, calls ->
            val today = startOfDay(System.currentTimeMillis())
            val day = 86_400_000L
            val weekStart = today - 6 * day
            val fmt = SimpleDateFormat("EEEEE", Locale.getDefault())
            val week = (6 downTo 0).map { back ->
                val start = today - back * day
                DayBar(
                    label = fmt.format(Date(start)),
                    count = calls.count { it.timestamp in start until start + day },
                    today = back == 0,
                )
            }
            val hits = calls.groupingBy { it.matchedRuleId }.eachCount()
            val top = rules.maxByOrNull { hits[it.id] ?: 0 }?.takeIf { (hits[it.id] ?: 0) > 0 }
            DashboardUiState(
                blockedToday = calls.count { it.timestamp >= today },
                blockedWeek = calls.count { it.timestamp >= weekStart },
                activeRules = rules.count { it.enabled },
                totalRules = rules.size,
                topRule = top,
                topRuleHits = top?.let { hits[it.id] } ?: 0,
                week = week,
                recent = calls.sortedByDescending { it.timestamp }.take(3),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
}
