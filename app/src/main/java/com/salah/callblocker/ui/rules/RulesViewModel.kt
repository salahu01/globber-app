package com.salah.callblocker.ui.rules

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.salah.callblocker.CallBlockerApp
import com.salah.callblocker.data.BlockRule
import com.salah.callblocker.data.PatternType
import com.salah.callblocker.data.RuleAction
import com.salah.callblocker.data.RuleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RulesViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: RuleRepository = CallBlockerApp.repository(app)

    val rules: StateFlow<List<BlockRule>> = repo.rules.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList(),
    )

    /** Hits per rule id, from the blocked-call log. */
    val hits: StateFlow<Map<Long, Int>> = combine(repo.rules, repo.blockedCalls) { _, calls ->
        calls.mapNotNull { it.matchedRuleId }.groupingBy { it }.eachCount()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun addRule(pattern: String, type: PatternType, label: String, action: RuleAction) {
        viewModelScope.launch {
            repo.addRule(pattern, type, label, action)
        }
    }

    fun updateRule(rule: BlockRule) {
        viewModelScope.launch {
            repo.updateRule(rule)
        }
    }

    fun toggle(rule: BlockRule) {
        viewModelScope.launch {
            repo.setEnabled(rule.id, !rule.enabled)
        }
    }

    fun delete(rule: BlockRule) {
        viewModelScope.launch {
            repo.deleteRule(rule)
        }
    }

    fun restore(rule: BlockRule) {
        viewModelScope.launch {
            repo.updateRule(rule)
        }
    }

    fun moveUp(rule: BlockRule) {
        viewModelScope.launch {
            repo.move(rule, true)
        }
    }

    fun moveDown(rule: BlockRule) {
        viewModelScope.launch {
            repo.move(rule, false)
        }
    }
}
