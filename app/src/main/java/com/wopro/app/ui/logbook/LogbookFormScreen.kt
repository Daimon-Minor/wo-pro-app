package com.wopro.app.ui.logbook

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.wopro.app.WOProApp
import com.wopro.app.data.local.LogbookEntity
import com.wopro.app.ui.VMFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DEPARTMENTS = listOf(
    "Engineering", "Housekeeping", "F&B", "Security", "Front Office",
    "Maintenance", "Spa", "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogbookFormScreen(
    entryId: Long,
    factory: VMFactory,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as WOProApp
    val repo = app.container.repository

    var date by remember { mutableStateOf(formatToday()) }
    var department by remember { mutableStateOf("Engineering") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var currentUserName by remember { mutableStateOf("") }
    var locationOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var locDropdownExpanded by remember { mutableStateOf(false) }
    var deptDropdownExpanded by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUri = uri?.toString()
    }

    LaunchedEffect(entryId) {
        val userId = app.container.encryptionManager.getUserId()
        if (userId > 0) {
            val user = withContext(Dispatchers.IO) { repo.getUser(userId) }
            currentUserName = user?.name ?: ""
        }
        val locs = withContext(Dispatchers.IO) {
            repo.observeLocations().firstOrNull()?.map { it.name }
        }
        if (!locs.isNullOrEmpty()) locationOptions = locs
        if (entryId > 0) {
            val e = withContext(Dispatchers.IO) { repo.getLogbookEntry(entryId) }
            e?.let {
                date = formatLogDate(it.date)
                department = it.department
                location = it.location
                description = it.description
                photoUri = it.photoUri
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (entryId > 0) "Edit Catatan" else "Catat Pekerjaan", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Date
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Tanggal (yyyy-MM-dd)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Department dropdown
            Text("Departemen", style = MaterialTheme.typography.labelLarge)
            ExposedDropdownMenuBox(
                expanded = deptDropdownExpanded,
                onExpandedChange = { deptDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = department,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Pilih Departemen") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = deptDropdownExpanded,
                    onDismissRequest = { deptDropdownExpanded = false }
                ) {
                    DEPARTMENTS.forEach { d ->
                        DropdownMenuItem(
                            text = { Text(d) },
                            onClick = { department = d; deptDropdownExpanded = false }
                        )
                    }
                }
            }

            // Location dropdown
            Text("Lokasi", style = MaterialTheme.typography.labelLarge)
            ExposedDropdownMenuBox(
                expanded = locDropdownExpanded,
                onExpandedChange = { locDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Pilih Lokasi") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = locDropdownExpanded,
                    onDismissRequest = { locDropdownExpanded = false }
                ) {
                    locationOptions.forEach { loc ->
                        DropdownMenuItem(
                            text = { Text(loc) },
                            onClick = { location = loc; locDropdownExpanded = false }
                        )
                    }
                }
            }

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi Pekerjaan") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            // Photo
            Text("Foto", style = MaterialTheme.typography.labelLarge)
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Foto pekerjaan",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
            }
            OutlinedButton(onClick = { photoPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Text("  ${if (photoUri != null) "Ganti Foto" else "Pilih Foto"}")
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val dateMillis = parseDate(date) ?: System.currentTimeMillis()
                        repo.saveLogbookEntry(
                            LogbookEntity(
                                id = entryId,
                                date = dateMillis,
                                department = department,
                                location = location,
                                description = description,
                                photoUri = photoUri,
                                createdBy = currentUserName
                            )
                        )
                    }
                    onSaved()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = description.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Text("  Simpan Catatan", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

fun formatToday(): String {
    return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(java.time.LocalDate.now())
}

private fun parseDate(text: String): Long? {
    return try {
        val date = java.time.LocalDate.parse(text.trim())
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    } catch (t: Throwable) {
        null
    }
}
