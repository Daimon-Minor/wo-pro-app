package com.wopro.app.ui.team

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.wopro.app.WOProApp
import com.wopro.app.data.local.TeamEntity
import com.wopro.app.ui.VMFactory
import com.wopro.app.ui.components.EmptyState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TeamViewModel(repo: com.wopro.app.data.repository.WOProRepository) : androidx.lifecycle.ViewModel() {
    val ui = repo.observeTeams()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsListScreen(
    factory: VMFactory,
    onNew: () -> Unit,
    onEdit: (Long) -> Unit,
    onBack: () -> Unit
) {
    val vm: TeamViewModel = viewModel(factory = factory)
    val teams by vm.ui.collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current
    val repo = (context.applicationContext as WOProApp).container.repository

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tim & Kelompok", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = onNew) { Icon(Icons.Default.Add, "New Team") } }
    ) { padding ->
        if (teams.isEmpty()) EmptyState("Belum ada tim. Tambahkan tim untuk pembagian blok & ruang.", Modifier.fillMaxSize().padding(padding))
        else LazyColumn(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(teams, key = { it.id }) { t ->
                TeamCard(
                    team = t,
                    onClick = { onEdit(t.id) },
                    onDelete = {
                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                            repo.deleteTeam(t)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TeamCard(team: TeamEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(team.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Handle Blok ${team.block}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                val memberCount = team.memberEmails.split(",").count { it.isNotBlank() }
                if (memberCount > 0) {
                    Text(
                        "$memberCount anggota",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
