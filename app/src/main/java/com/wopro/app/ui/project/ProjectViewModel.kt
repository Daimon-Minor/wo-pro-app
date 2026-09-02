package com.wopro.app.ui.project

import androidx.lifecycle.ViewModel
import com.wopro.app.data.repository.WOProRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map

class ProjectViewModel(repo: WOProRepository) : ViewModel() {
    val ui = repo.observeProjects()
        .map { it }
        .stateIn(this, SharingStarted.WhileSubscribed(5000), emptyList())
}
