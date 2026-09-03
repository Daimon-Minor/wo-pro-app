package com.wopro.app.ui.workorder

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PriorityHigh
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wopro.app.WOProApp
import com.wopro.app.data.local.WorkOrderEntity
import com.wopro.app.ui.VMFactory
import com.wopro.app.ui.components.PhotoViewerDialog
import com.wopro.app.ui.components.SectionHeader
import com.wopro.app.ui.components.rememberCameraLauncher
import com.wopro.app.ui.home.formatDateTime
import com.wopro.app.ui.theme.Gray500
import com.wopro.app.ui.theme.TealPrimary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val DoneColor = TealPrimary
private val NewColor = Gray500
private val PendingColor = Color(0xFFFFB300)
private val AcceptedColor = Color(0xFF1976D2)

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

    var showPendingDialog by remember { mutableStateOf(false) }
    var pendingReason by remember { mutableStateOf("") }
    var selectedPhoto by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val donePhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val current = wo
        if (uri != null && current != null) {
            scope.launch {
                repo.setDone(current, uri.toString())
                wo = repo.getWorkOrder(woId)
            }
        }
    }

    // Kamera untuk foto bukti selesai (permission otomatis diminta)
    val doneCamera = rememberCameraLauncher { uri ->
        val current = wo
        if (current != null) {
            scope.launch {
                repo.setDone(current, uri)
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
                title = { Text("WO. $woId", fontWeight = FontWeight.Bold, color = TealPrimary) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        if (loading) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { androidx.compose.material3.CircularProgressIndicator() }; return@Scaffold }
        val item = wo ?: return@Scaffold

        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header card
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            item.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        StatusPillDetail(item.status)
                    }
                    Spacer(Modifier.height(12.dp))
                    // Foto besar jika ada — tap untuk lihat full-screen
                    if (item.photoUri != null) {
                        AsyncImage(
                            model = item.photoUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedPhoto = item.photoUri }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    DetailRowDetail(Icons.Default.LocationOn, locationTextDetail(item))
                    DetailRowDetail(Icons.Default.Person, "Dibuat oleh: ${item.createdBy.ifBlank { "-" }}")
                    DetailRowDetail(Icons.Default.AccessTime, "Dibuat: ${formatDateTime(item.createdAt)}")
                    if (item.acceptedBy.isNotBlank()) {
                        DetailRowDetail(Icons.Default.Person, "Diterima oleh: ${item.acceptedBy}")
                        if (item.acceptedAt != null) {
                            DetailRowDetail(Icons.Default.AccessTime, "Diterima: ${formatDateTime(item.acceptedAt)}")
                        }
                    }
                    DetailRowDetail(Icons.Default.PriorityHigh, item.priority, textColor = priorityColorDetail(item.priority))
                    if (item.dueDate != null) {
                        DetailRowDetail(Icons.Default.AccessTime, "Due: ${formatDateTime(item.dueDate)}")
                    }
                }
            }

            // Keterangan
            if (item.description.isNotBlank()) {
                SectionHeader("Keterangan")
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F8F8))) {
                    Column(Modifier.padding(14.dp)) {
                        Text(item.description)
                        TextButton(onClick = onEdit, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = TealPrimary)
                            Text("  Edit Keterangan", color = TealPrimary)
                        }
                    }
                }
            }

            // Foto saat done — tap untuk lihat full-screen
            if (item.status == "Done" && item.donePhotoUri != null) {
                SectionHeader("Foto Saat Selesai")
                AsyncImage(
                    model = item.donePhotoUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedPhoto = item.donePhotoUri }
                )
            }

            // Activity log
            if (item.activityLog.isNotBlank()) {
                SectionHeader("Riwayat / Note")
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F8F8))) {
                    Column(Modifier.padding(14.dp)) {
                        item.activityLog.split("\n").filter { it.isNotBlank() }.forEach { line ->
                            Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(13.dp).padding(top = 2.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(line.trim(), style = MaterialTheme.typography.bodySmall, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Action flow
            when (item.status) {
                "Open" -> {
                    Spacer(Modifier.height(6.dp))
                    Button(
                        onClick = {
                            val current = item
                            scope.launch {
                                repo.acceptWorkOrder(current, currentUserName.ifBlank { "User" })
                                wo = repo.getWorkOrder(woId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AcceptedColor)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Text("  Accept Work Order", style = MaterialTheme.typography.titleMedium)
                    }
                }
                "On Progress" -> {
                    Spacer(Modifier.height(6.dp))
                    DetailRowDetail(Icons.Default.Person, "Diterima oleh: ${item.acceptedBy.ifBlank { "-" }}")
                    if (item.acceptedAt != null) {
                        DetailRowDetail(Icons.Default.AccessTime, "Diterima: ${formatDateTime(item.acceptedAt)}")
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { showPendingDialog = true }, modifier = Modifier.weight(1f).height(52.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Text("  Pending")
                        }
                        Button(
                            onClick = { doneCamera() },
                            modifier = Modifier.weight(1f).height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                            Text("  Done")
                        }
                    }
                    // Alternatif pilih foto dari galeri untuk bukti selesai
                    TextButton(onClick = { donePhotoPicker.launch("image/*") }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("  atau pilih foto dari galeri")
                    }
                }
                "Pending" -> {
                    Spacer(Modifier.height(6.dp))
                    SectionHeader("Alasan Pending")
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))) {
                        Text(item.pendingReason.ifBlank { "-" }, Modifier.padding(14.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                val current = item
                                scope.launch {
                                    repo.resumeWorkOrder(current)
                                    wo = repo.getWorkOrder(woId)
                                }
                            },
                            modifier = Modifier.weight(1f).height(52.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Text("  Resume")
                        }
                        Button(
                            onClick = { doneCamera() },
                            modifier = Modifier.weight(1f).height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                            Text("  Done")
                        }
                    }
                    // Alternatif pilih foto dari galeri untuk bukti selesai
                    TextButton(onClick = { donePhotoPicker.launch("image/*") }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("  atau pilih foto dari galeri")
                    }
                }
                "Done" -> {
                    Spacer(Modifier.height(6.dp))
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))) {
                        Column(Modifier.padding(14.dp)) {
                            Text("✔ Selesai", color = DoneColor, fontWeight = FontWeight.Bold)
                            if (item.acceptedBy.isNotBlank()) {
                                Text("Dikerjakan oleh: ${item.acceptedBy}", style = MaterialTheme.typography.bodySmall, color = Gray500)
                            }
                            if (item.doneAt != null) {
                                Text("Selesai pada: ${formatDateTime(item.doneAt)}", style = MaterialTheme.typography.bodySmall, color = Gray500)
                            }
                        }
                    }
                }
            }

            if (item.status != "Done") {
                Button(onClick = onEdit, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Text("  Edit Work Order")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

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
                        val reason = pendingReason.trim()
                        if (reason.isNotBlank()) {
                            showPendingDialog = false
                            pendingReason = ""
                            val current = wo
                            if (current != null) {
                                scope.launch {
                                    repo.setPending(current, reason)
                                    wo = repo.getWorkOrder(woId)
                                }
                            }
                        }
                    },
                    enabled = pendingReason.isNotBlank()
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showPendingDialog = false; pendingReason = "" }) { Text("Cancel") }
            }
        )
    }

    // Tampilkan foto full-screen saat foto di tap
    selectedPhoto?.let { uri ->
        PhotoViewerDialog(imageUri = uri, onDismiss = { selectedPhoto = null })
    }
}

private fun locationTextDetail(wo: WorkOrderEntity): String = when {
    wo.block.isNotBlank() && wo.roomNumber > 0 -> "${wo.block}-${wo.roomNumber}"
    wo.block.isNotBlank() -> wo.block
    wo.location.isNotBlank() -> wo.location
    else -> "-"
}

@Composable
private fun StatusPillDetail(status: String) {
    val (bg, text) = when (status) {
        "Done" -> Color(0xFFE0F2F1) to DoneColor
        "Pending" -> Color(0xFFFFF8E1) to PendingColor
        "On Progress" -> Color(0xFFE3F2FD) to AcceptedColor
        "Open" -> Color(0xFFF0F2F2) to NewColor
        else -> Color(0xFFF0F2F2) to Gray500
    }
    Box(Modifier.background(bg, RoundedCornerShape(12.dp)).padding(horizontal = 14.dp, vertical = 6.dp)) {
        Text(status, color = text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun DetailRowDetail(icon: ImageVector, text: String, textColor: Color = Color(0xFF3A4143)) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
        Icon(icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = textColor, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

private fun priorityColorDetail(p: String): Color = when (p) {
    "High", "Critical" -> Color(0xFFE53935)
    "Medium" -> Color(0xFFFFB300)
    else -> Gray500
}
