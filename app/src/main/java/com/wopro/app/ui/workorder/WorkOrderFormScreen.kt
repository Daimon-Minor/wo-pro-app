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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.wopro.app.ui.components.LoadingBox
import com.wopro.app.ui.components.rememberCameraLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
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
    var locationDropdownExpanded by remember { mutableStateOf(false) }
    var locationOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var blockDropdownExpanded by remember { mutableStateOf(false) }
    var blockOptions by remember { mutableStateOf<List<String>>(emptyList()) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUri = uri?.toString()
    }
    val cameraLauncher = rememberCameraLauncher { uri ->
        photoUri = uri
    }

    LaunchedEffect(woId) {
        val userId = app.container.encryptionManager.getUserId()
        if (userId > 0) {
            val user = withContext(Dispatchers.IO) { app.container.repository.getUser(userId) }
            currentUserName = user?.name ?: ""
        }
        // Load lokasi dari database
        val savedLocations = withContext(Dispatchers.IO) {
            app.container.repository.observeLocations().firstOrNull()?.map { it.name }
        }
        if (!savedLocations.isNullOrEmpty()) {
            locationOptions = savedLocations
        } else {
            // Fallback ke preset
            locationOptions = listOf(
                "Kamar", "Lobby", "Kolam Renang", "Restoran", "Spa",
                "Gym", "Parkir", "Ruang Rapat", "Kantor", "Dapur"
            )
        }
        // Load blok dari database (kelola admin)
        val savedBlocks = withContext(Dispatchers.IO) {
            app.container.repository.observeBlocks().firstOrNull()?.map { it.name }
        }
        if (!savedBlocks.isNullOrEmpty()) blockOptions = savedBlocks
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

            // Location — dropdown (admin kelola di Settings → Kelola Lokasi)
            Text("Location", style = MaterialTheme.typography.labelLarge)
            ExposedDropdownMenuBox(
                expanded = locationDropdownExpanded,
                onExpandedChange = { locationDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Pilih Lokasi") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationDropdownExpanded)
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = locationDropdownExpanded,
                    onDismissRequest = { locationDropdownExpanded = false }
                ) {
                    locationOptions.forEach { loc ->
                        DropdownMenuItem(
                            text = { Text(loc) },
                            onClick = {
                                location = loc
                                locationDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Block + Room (routing ke team)
            Text("Blok (kelola admin)", style = MaterialTheme.typography.labelLarge)
            ExposedDropdownMenuBox(
                expanded = blockDropdownExpanded,
                onExpandedChange = { blockDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = block,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Pilih Blok") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = blockDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = blockDropdownExpanded,
                    onDismissRequest = { blockDropdownExpanded = false }
                ) {
                    if (blockOptions.isEmpty()) {
                        DropdownMenuItem(text = { Text("Belum ada blok — minta admin membuat blok") }, onClick = { blockDropdownExpanded = false })
                    }
                    blockOptions.forEach { b ->
                        DropdownMenuItem(
                            text = { Text(b) },
                            onClick = { block = b; blockDropdownExpanded = false }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = roomNumber,
                onValueChange = { roomNumber = it.filter(Char::isDigit).take(4) },
                label = { Text("No. Kamar") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
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
            // Tombol kamera untuk foto bukti pembuatan
            OutlinedButton(onClick = { cameraLauncher() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Text("  ${if (photoUri != null) "Ambil Ulang" else "Ambil Foto"}")
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
            val scope = rememberCoroutineScope()
            Button(
                onClick = {
                    val wo = WorkOrderEntity(
                        id = woId,
                        title = title, category = category,
                        description = description, location = location,
                        priority = priority,
                        block = block,
                        roomNumber = roomNumber.toIntOrNull() ?: 0,
                        photoUri = photoUri,
                        createdBy = currentUserName
                    )
                    scope.launch {
                        repo.saveWorkOrder(wo)
                        onSaved()
                    }
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
