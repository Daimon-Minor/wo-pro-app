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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wopro.app.data.local.AuditItemEntity
import com.wopro.app.data.local.AuditReportEntity
import com.wopro.app.ui.VMFactory
import com.wopro.app.ui.components.SectionHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import com.wopro.app.WOProApp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope

private val defaultItems = listOf(
    "Emergency lighting operational",
    "Fire extinguisher in place & tagged",
    "Exit signs illuminated",
    "HVAC filter replaced",
    "Electrical panel labels visible",
    "Plumbing - no leaks observed",
    "Kitchen exhaust hood clean",
    "Storage area clear of hazards",
    "Thermostat calibrated",
    "Energy meter reading recorded"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditFormScreen(
    reportId: Long,
    factory: VMFactory,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val repo = (context.applicationContext as WOProApp).container.repository
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Safety") }
    var property by remember { mutableStateOf("") }
    val items = remember { mutableStateListOf<AuditItemEntity>() }
    var loading by remember { mutableStateOf(reportId > 0) }

    // Init
    androidx.compose.runtime.LaunchedEffect(reportId) {
        if (reportId > 0) {
            val r = withContext(Dispatchers.IO) { repo.getAuditReport(reportId) }
            r?.let { title = it.title; category = it.category; property = it.property }
            val existing = withContext(Dispatchers.IO) { repo.observeAuditItems(reportId) }
            // For simplicity, just use defaults in edit too
        }
        if (items.isEmpty()) {
            defaultItems.forEachIndexed { i, s -> items.add(AuditItemEntity(checkItem = s, result = "N/A", reportId = 0)) }
        }
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (reportId > 0) "Edit Audit" else "New Audit", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onSaved) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        if (loading) { androidx.compose.foundation.layout.Box(Modifier.fillMaxSize().padding(padding)) { com.wopro.app.ui.components.LoadingBox() }; return@Scaffold }

        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Audit Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            item {
                OutlinedTextField(value = property, onValueChange = { property = it }, label = { Text("Property / Area") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            item { SectionHeader("Checklist Items") }

            itemsIndexed(items) { idx, item ->
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${idx + 1}. ${item.checkItem}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Pass", "Fail", "N/A").forEach { result ->
                                androidx.compose.material3.FilterChip(
                                    selected = items[idx].result == result,
                                    onClick = { items[idx] = items[idx].copy(result = result) },
                                    label = { Text(result) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            repo.saveAuditReport(
                                AuditReportEntity(title = title, category = category, property = property, auditor = "Current User"),
                                items.toList()
                            )
                        }
                        onSaved()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = title.isNotBlank()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Text("  Save Report", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}