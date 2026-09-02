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
import kotlinx.coroutines.launch

data class WoListState(
    val items: List<WorkOrderEntity> = emptyList(),
    val filter: String = "All",
    val loading: Boolean = true
)

class WorkOrderViewModel(private val repo: WOProRepository) : ViewModel() {

    private val filter = MutableStateFlow("All")

    val ui: StateFlow<WoListState> =
        repo.observeWorkOrders().combine(filter) { wos, f ->
            WoListState(
                items = if (f == "All") wos else wos.filter { it.status == f },
                filter = f,
                loading = false
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WoListState())

    fun setFilter(f: String) {
        filter.value = f
    }

    fun delete(wo: WorkOrderEntity) = viewModelScope.launch {
        repo.deleteWorkOrder(wo)
    }
}
