package com.wopro.app.ui.audit

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
import com.wopro.app.data.local.AuditReportEntity
import com.wopro.app.ui.VMFactory
import com.wopro.app.ui.components.EmptyState
import com.wopro.app.ui.components.StatusChip
import com.wopro.app.ui.home.formatDate
import com.wopro.app.ui.home.statusColor

class AuditViewModel(repo: com.wopro.app.data.repository.WOProRepository) : androidx.lifecycle.ViewModel() {
    val ui = repo.observeAuditReports()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditListScreen(
    factory: VMFactory,
    onNew: () -> Unit,
    onDetail: (Long) -> Unit
) {
    val vm: AuditViewModel = viewModel(factory = factory)
    val reports by vm.ui.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Audit Reports", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = { FloatingActionButton(onClick = onNew) { Icon(Icons.Default.Add, "New Audit") } }
    ) { padding ->
        if (reports.isEmpty()) EmptyState("No audit reports yet", Modifier.fillMaxSize().padding(padding))
        else LazyColumn(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(reports, key = { it.id }) { r ->
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    onClick = { onDetail(r.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(r.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text("${r.category} · ${r.property} · ${formatDate(r.createdAt)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        StatusChip(r.status, statusColor(r.status))
                    }
                }
            }
        }
    }
}