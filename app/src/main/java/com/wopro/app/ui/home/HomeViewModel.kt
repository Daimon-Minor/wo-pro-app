package com.wopro.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wopro.app.data.local.UserEntity
import com.wopro.app.data.local.WorkOrderEntity
import com.wopro.app.data.repository.WOProRepository
import com.wopro.app.security.EncryptionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

data class HomeUiState(
    val loading: Boolean = true,
    val user: UserEntity? = null,
    val openCount: Int = 0,
    val acceptedCount: Int = 0,
    val pendingCount: Int = 0,
    val doneCount: Int = 0,
    val recent: List<WorkOrderEntity> = emptyList(),
    val weeklyCounts: List<Int> = List(7) { 0 },
    val unreadCount: Int = 0
)

class HomeViewModel(
    private val repo: WOProRepository,
    private val encryption: EncryptionManager
) : ViewModel() {

    val ui: StateFlow<HomeUiState> =
        combine(repo.observeWorkOrders(), initUser(), repo.observeUnread()) { wos, user, unread ->
            val myEmail = user?.email?.trim()?.lowercase() ?: ""
            val isAdmin = user?.role.equals("Admin", ignoreCase = true)
            val visibleUnread = if (isAdmin) unread.size else unread.count {
                val t = it.targetEmail.trim().lowercase()
                t.isEmpty() || t == myEmail
            }
            val open = wos.count { it.status == "Open" }
            val accepted = wos.count { it.status == "On Progress" }
            val pending = wos.count { it.status == "Pending" }
            val done = wos.count { it.status == "Done" }
            HomeUiState(
                loading = false,
                user = user,
                openCount = open,
                acceptedCount = accepted,
                pendingCount = pending,
                doneCount = done,
                recent = wos.take(5),
                weeklyCounts = weeklyCreated(wos),
                unreadCount = visibleUnread
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    private fun initUser() = kotlinx.coroutines.flow.flow {
        val id = encryption.getUserId()
        emit(if (id > 0) repo.getUser(id) else null)
    }

    private fun weeklyCreated(wos: List<WorkOrderEntity>): List<Int> {
        val today = LocalDate.now()
        val week = today.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1)
        val counts = IntArray(7)
        wos.forEach { wo ->
            val date = Instant.ofEpochMilli(wo.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
            val idx = java.time.temporal.ChronoUnit.DAYS.between(week, date).toInt()
            if (idx in 0..6) counts[idx]++
        }
        return counts.toList()
    }
}
