package com.wopro.app.ui.utility

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wopro.app.data.local.MeterReadingEntity
import com.wopro.app.ui.VMFactory
import com.wopro.app.ui.components.EmptyState
import com.wopro.app.ui.home.formatDate

class MeterViewModel(repo: com.wopro.app.data.repository.WOProRepository) : androidx.lifecycle.ViewModel() {
    val ui = repo.observeMeters()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeterListScreen(
    factory: VMFactory,
    onNew: (String) -> Unit
) {
    val vm: MeterViewModel = viewModel(factory = factory)
    val meters by vm.ui.collectAsStateWithLifecycle(initialValue = emptyList())
    val types = listOf("Chiller", "Freezer", "Heat Pump", "Water Tank", "Fuel", "Gas", "KWH")

    Scaffold(
        topBar = { TopAppBar(title = { Text("Utility Meters", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            item { Text("Choose meter type to add reading:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp)) }
            types.forEach { type ->
                item {
                    androidx.compose.material3.ListItem(
                        headlineContent = { Text(type) },
                        leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                        modifier = Modifier.clickable(onClick = { onNew(type) }),
                        supportingContent = {
                            val count = meters.count { it.meterType == type }
                            Text("$count readings")
                        }
                    )
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
            item { Text("Recent Readings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp)) }
            if (meters.isEmpty()) {
                item { EmptyState("No readings yet. Tap a meter type above to add one.") }
            } else {
                items(meters.take(20), key = { it.id }) { m ->
                    MeterCard(m)
                }
            }
        }
    }
}

private fun Modifier.clickable(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))

@Composable
private fun MeterCard(m: MeterReadingEntity) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${m.meterType} · ${m.meterName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("${m.reading} ${m.unit}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Text(formatDate(m.readingDate), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}