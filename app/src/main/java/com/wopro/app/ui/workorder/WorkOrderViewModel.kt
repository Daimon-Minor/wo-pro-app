package com.wopro.app.ui.workorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wopro.app.data.local.WorkOrderEntity
import com.wopro.app.data.repository.WOProRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class WoListState(
    val items: List<WorkOrderEntity> = emptyList(),
    val filter: String = "All",
    val tab: String = "In",           // "In" = semua, "Out" = dibuat user ini
    val search: String = "",
    val loading: Boolean = true
)

class WorkOrderViewModel(private val repo: WOProRepository) : ViewModel() {

    private val filter = MutableStateFlow("All")
    private val tab = MutableStateFlow("In")
    private val search = MutableStateFlow("")
    private val currentUserName = MutableStateFlow("")

    val ui: StateFlow<WoListState> =
        combine(repo.observeWorkOrders(), filter, tab, search, currentUserName) { wos, f, t, s, user ->
            val byStatus = if (f == "All") wos else wos.filter { it.status == f }
            val byTab = if (t == "Out") {
                byStatus.filter { it.createdBy.equals(user, ignoreCase = true) || (user.isBlank() && it.createdBy.isBlank()) }
            } else byStatus
            val q = s.trim()
            val bySearch = if (q.isEmpty()) byTab else byTab.filter {
                it.title.contains(q, ignoreCase = true) ||
                    it.location.contains(q, ignoreCase = true) ||
                    it.block.contains(q, ignoreCase = true) ||
                    it.roomNumber.toString().contains(q) ||
                    "wo. ${it.id}".contains(q, ignoreCase = true)
            }
            WoListState(
                items = bySearch,
                filter = f,
                tab = t,
                search = s,
                loading = false
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WoListState())

    fun setFilter(f: String) { filter.value = f }
    fun setTab(t: String) { tab.value = t }
    fun setSearch(s: String) { search.value = s }
    fun setCurrentUserName(name: String) { currentUserName.value = name }

    fun delete(wo: WorkOrderEntity) = viewModelScope.launch {
        repo.deleteWorkOrder(wo)
    }
}
