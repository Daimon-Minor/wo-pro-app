package com.wopro.app.ui.workorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wopro.app.data.local.WorkOrderEntity
import com.wopro.app.data.repository.WOProRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WorkOrderFormViewModel(private val repo: WOProRepository) : ViewModel() {

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved

    fun save(wo: WorkOrderEntity) = viewModelScope.launch {
        repo.saveWorkOrder(wo)
        _saved.value = true
    }
}
