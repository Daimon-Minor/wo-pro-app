package com.wopro.app.ui.workorder

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.wopro.app.WOProApp
import com.wopro.app.data.local.WorkOrderEntity
import com.wopro.app.ui.VMFactory
import com.wopro.app.ui.components.LabeledRow
import com.wopro.app.ui.components.LoadingBox
import com.wopro.app.ui.components.SectionHeader
import com.wopro.app.ui.components.StatusChip
import com.wopro.app.ui.home.formatDate
import com.wopro.app.ui.home.statusColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderDetailScreen(
    woId: Long,
    factory: VMFactory,
    onEdit: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as WOProApp
    val repo = app.container.repository
    val encryption = app.container.encryptionManager
    var wo by remember { mutableStateOf<WorkOrderEntity?>(null) }
    var loading by remember { mutableStateOf(true) }
    var currentUserName by remember { mutableStateOf("") }

    // Dialog pending reason
    var showPendingDialog by remember { mutableStateOf(false) }
    var pendingReason by remember { mutableStateOf("") }

    // Photo picker untuk Done
    val donePhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && wo != null) {
            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                repo.setDone(wo!!, uri.toString())
                wo = repo.getWorkOrder(woId)
            }
        }
    }

    LaunchedEffect(woId) {
        val id = encryption.getUserId()
        if (id > 0) {
            val user = withContext(Dispatchers.IO) { repo.getUser(id) }
            currentUserName = user?.name ?: ""
        }
        wo = withContext(Dispatchers.IO) { repo.getWorkOrder(woId) }
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Work Order Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        if (loading) { LoadingBox(Modifier.fillMaxSize().padding(padding)); return@Scaffold }
        val item = wo ?: return@Scaffold

        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        StatusChip(item.status, statusColor(item.status))
                    }
                    Spacer(Modifier.height(8.dp))
                    LabeledRow("Category", item.category)
                    LabeledRow("Priority", item.priority)
                    LabeledRow("Location", item.location)
                    if (item.block.isNotBlank()) {
                        LabeledRow("Blok / Kamar", "${item.block}-${item.roomNumber}")
                    }
                    LabeledRow("Due", formatDate(item.dueDate))
                }
            }

            // Keterangan
            if (item.description.isNotBlank()) {
                SectionHeader("Keterangan")
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(item.description)
                        // Edit keterangan
                        TextButton(onClick = onEdit, modifier = Modifier.padding(top = 8.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("  Edit Keterangan", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Foto lampiran saat create
            if (item.photoUri != null) {
                SectionHeader("Foto Lampiran")
                AsyncImage(
                    model = item.photoUri,
                    contentDescription = "Lampiran WO",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
            }

            // Status flow
            when (item.status) {
                "Open" -> {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                                repo.acceptWorkOrder(item, currentUserName.ifBlank { "User" })
                                wo = repo.getWorkOrder(woId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Text("  Accept Work Order", style = MaterialTheme.typography.titleMedium)
                    }
                }
                "Accepted" -> {
                    Spacer(Modifier.height(8.dp))
                    LabeledRow("Diterima oleh", item.acceptedBy.ifBlank { "-" })
                    Spacer(Modifier.height(8.dp))
                    // Tombol Pending + Done
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showPendingDialog = true },
                            modifier = Modifier.weight(1f).height(52.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Text("  Pending")
                        }
                        Button(
                            onClick = { donePhotoPicker.launch("image/*") },
                            modifier = Modifier.weight(1f).height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Text("  Done")
                        }
                    }
                }
                "Pending" -> {
                    Spacer(Modifier.height(8.dp))
                    SectionHeader("Alasan Pending")
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(item.pendingReason.ifBlank { "-" }, Modifier.padding(16.dp))
                    }
                    LabeledRow("Diterima oleh", item.acceptedBy.ifBlank { "-" })
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                                    repo.resumeWorkOrder(item)
                                    wo = repo.getWorkOrder(woId)
                                }
                            },
                            modifier = Modifier.weight(1f).height(52.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Text("  Resume")
                        }
                        Button(
                            onClick = { donePhotoPicker.launch("image/*") },
                            modifier = Modifier.weight(1f).height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Text("  Done")
                        }
                    }
                }
                "Done" -> {
                    Spacer(Modifier.height(8.dp))
                    SectionHeader("Diselesaikan oleh")
                    LabeledRow("Nama", item.acceptedBy.ifBlank { "-" })
                    if (item.doneAt != null) {
                        LabeledRow("Selesai pada", formatDate(item.doneAt))
                    }
                    if (item.donePhotoUri != null) {
                        SectionHeader("Foto Saat Selesai")
                        AsyncImage(
                            model = item.donePhotoUri,
                            contentDescription = "Foto Done",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            LabeledRow("Dibuat oleh", item.createdBy.ifBlank { "-" })
            LabeledRow("Dibuat pada", formatDate(item.createdAt))

            if (item.status != "Done") {
                // Edit only if not done
                Button(onClick = onEdit, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Text("  Edit Work Order")
                }
            }
        }
    }

    // Dialog alasan Pending
    if (showPendingDialog) {
        AlertDialog(
            onDismissRequest = { showPendingDialog = false },
            title = { Text("Alasan Pending") },
            text = {
                OutlinedTextField(
                    value = pendingReason,
                    onValueChange = { pendingReason = it },
                    label = { Text("Ketik alasan work order di-pending") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pendingReason.isNotBlank()) {
                            showPendingDialog = false
                            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                                repo.setPending(wo!!, pendingReason.trim())
                                wo = repo.getWorkOrder(woId)
                            }
                            pendingReason = ""
                        }
                    },
                    enabled = pendingReason.isNotBlank()
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPendingDialog = false; pendingReason = "" }) {
                    Text("Cancel")
                }
            }
        )
    }
}