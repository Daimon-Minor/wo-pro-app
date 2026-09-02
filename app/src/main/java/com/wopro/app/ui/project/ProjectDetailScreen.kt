package com.wopro.app.ui.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wopro.app.data.local.ProjectEntity
import com.wopro.app.ui.VMFactory
import com.wopro.app.ui.components.LabeledRow
import com.wopro.app.ui.components.LoadingBox
import com.wopro.app.ui.components.SectionHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import com.wopro.app.WOProApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: Long,
    factory: VMFactory,
    onEdit: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repo = (context.applicationContext as WOProApp).container.repository
    var p by remember { mutableStateOf<ProjectEntity?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(projectId) {
        p = withContext(Dispatchers.IO) { repo.getProject(projectId) }
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        if (loading) { LoadingBox(Modifier.fillMaxSize().padding(padding)); return@Scaffold }
        val item = p ?: return@Scaffold
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp)) {
                    Text(item.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    LabeledRow("Status", item.status)
                    LabeledRow("Location", item.location)
                    LabeledRow("Budget", item.budget.toString())
                    LabeledRow("Start", com.wopro.app.ui.home.formatDate(item.startDate))
                }
            }
            if (item.description.isNotBlank()) {
                SectionHeader("Description")
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Text(item.description, Modifier.padding(16.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Text("  Edit Project")
            }
        }
    }
}