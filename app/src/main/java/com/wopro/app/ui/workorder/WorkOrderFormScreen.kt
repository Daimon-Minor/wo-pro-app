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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.wopro.app.WOProApp
import com.wopro.app.data.local.WorkOrderEntity
import com.wopro.app.ui.VMFactory
import com.wopro.app.ui.components.LoadingBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Lokasi preset yang bisa dipilih user saat membuat work order. */
val LOCATION_PRESETS = listOf(
    "Kamar", "Lobby", "Kolam Renang", "Restoran", "Spa",
    "Gym", "Parkir", "Ruang Rapat", "Kantor", "Dapur"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderFormScreen(
    woId: Long,
    factory: VMFactory,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as WOProApp
    val repo = app.container.repository
    val vm: WorkOrderFormViewModel = viewModel(factory = factory)
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var block by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(woId > 0) }
    var currentUserName by remember { mutableStateOf("") }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUri = uri?.toString()
    }

    LaunchedEffect(woId) {
        val userId = app.container.encryptionManager.getUserId()
        if (userId > 0) {
            val user = withContext(Dispatchers.IO) { app.container.repository.getUser(userId) }
            currentUserName = user?.name ?: ""
        }
        if (woId > 0) {
            val wo = withContext(Dispatchers.IO) { repo.getWorkOrder(woId) }
            wo?.let {
                title = it.title; category = it.category
                description = it.description; location = it.location
                priority = it.priority
                block = it.block
                roomNumber = if (it.roomNumber > 0) it.roomNumber.toString() else ""
                photoUri = it.photoUri
            }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (woId > 0) "Edit Work Order" else "New Work Order", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        if (loading) { LoadingBox(Modifier.fillMaxSize().padding(padding)); return@Scaffold }
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            // Location — user tinggal pilih dari preset
            Text("Location", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                LOCATION_PRESETS.take(6).forEach { loc ->
                    androidx.compose.material3.FilterChip(
                        selected = location == loc,
                        onClick = { location = loc },
                        label = { Text(loc) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                LOCATION_PRESETS.drop(6).forEach { loc ->
                    androidx.compose.material3.FilterChip(
                        selected = location == loc,
                        onClick = { location = loc },
                        label = { Text(loc) }
                    )
                }
            }
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location (ketik custom jika perlu)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Block + Room (routing ke team)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = block,
                    onValueChange = { block = it.uppercase().take(3) },
                    label = { Text("Blok") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it.filter(Char::isDigit).take(4) },
                    label = { Text("No. Kamar") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description / Keterangan") }, minLines = 3, modifier = Modifier.fillMaxWidth())

            // Foto lampiran saat membuat WO
            Text("Attachment Foto", style = MaterialTheme.typography.labelLarge)
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Lampiran",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
            }
            OutlinedButton(onClick = { photoPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Text("  ${if (photoUri != null) "Ganti Foto" else "Pilih Foto"}")
            }

            // Priority buttons
            Text("Priority", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Low", "Medium", "High", "Critical").forEach { p ->
                    androidx.compose.material3.FilterChip(
                        selected = priority == p,
                        onClick = { priority = p },
                        label = { Text(p) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    vm.save(WorkOrderEntity(
                        id = woId,
                        title = title, category = category,
                        description = description, location = location,
                        priority = priority,
                        block = block,
                        roomNumber = roomNumber.toIntOrNull() ?: 0,
                        photoUri = photoUri,
                        createdBy = currentUserName
                    ))
                    onSaved()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = title.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Text("  Save Work Order", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
