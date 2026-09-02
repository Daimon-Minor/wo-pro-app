package com.wopro.app.ui.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wopro.app.data.local.ProjectEntity
import com.wopro.app.ui.VMFactory
import com.wopro.app.ui.components.EmptyState
import com.wopro.app.ui.components.StatusChip
import com.wopro.app.ui.home.formatDate
import com.wopro.app.ui.home.statusColor

class ProjectViewModel(repo: com.wopro.app.data.repository.WOProRepository) : androidx.lifecycle.ViewModel() {
    val ui = repo.observeProjects()
        .let { it }
    // simple: expose flow directly; handled in screen with collectAsStateWithLifecycle
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    factory: VMFactory,
    onNew: () -> Unit,
    onDetail: (Long) -> Unit
) {
    val vm: ProjectViewModel = viewModel(factory = factory)
    val projects by vm.ui.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Projects", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = { FloatingActionButton(onClick = onNew) { Icon(Icons.Default.Add, "New Project") } }
    ) { padding ->
        if (projects.isEmpty()) EmptyState("No projects yet", Modifier.fillMaxSize().padding(padding))
        else LazyColumn(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(projects, key = { it.id }) { p ->
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    onClick = { onDetail(p.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(p.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text("${p.location} · ${formatDate(p.startDate)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        StatusChip(p.status, statusColor(p.status))
                    }
                }
            }
        }
    }
}