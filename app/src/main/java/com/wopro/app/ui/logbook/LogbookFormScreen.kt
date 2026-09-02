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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import java.util.Locale
import java.util.TimeZone

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

    // Simpan tanggal sebagai epoch millis (local midnight)
    var dateMillis by remember { mutableLongStateOf(todayAtMidnight()) }
    var department by remember { mutableStateOf("Engineering") }
    var location by remember { mutableStateOf("") }
    var activity by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var currentUserName by remember { mutableStateOf("") }
    var locationOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var locDropdownExpanded by remember { mutableStateOf(false) }
    var deptDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

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
                dateMillis = it.date
                department = it.department
                location = it.location
                activity = it.description
                photoUri = it.photoUri
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Logbook", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Date — tappable, buka DatePicker
            Text("Date", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
            OutlinedTextField(
                value = friendlyDate(dateMillis),
                onValueChange = { },
                readOnly = true,
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.Edit, contentDescription = "Pilih Tanggal") } }
            )

            // Department dropdown
            Text("Department", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
            ExposedDropdownMenuBox(
                expanded = deptDropdownExpanded,
                onExpandedChange = { deptDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = department,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Department") },
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

            // Location (optional) — dropdown dari lokasi admin
            Text("Location (optional)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
            ExposedDropdownMenuBox(
                expanded = locDropdownExpanded,
                onExpandedChange = { locDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Location") },
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

            // Activity / notes
            Text("Activity", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
            OutlinedTextField(
                value = activity,
                onValueChange = { activity = it },
                label = { Text("Activity / notes") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            // Photo (opsional)
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
                Text("  ${if (photoUri != null) "Ganti Foto" else "Tambah Foto"}")
            }

            Spacer(Modifier.height(8.dp))

            // Batal / Save
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    Text("Batal", style = MaterialTheme.typography.titleMedium)
                }
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            repo.saveLogbookEntry(
                                LogbookEntity(
                                    id = entryId,
                                    date = dateMillis,
                                    department = department,
                                    location = location,
                                    description = activity,
                                    photoUri = photoUri,
                                    createdBy = currentUserName
                                )
                            )
                        }
                        onSaved()
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    enabled = activity.isNotBlank()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Text("  Save", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    // Date Picker dialog
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = toUtcMillis(dateMillis)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { utc ->
                        dateMillis = utcToLocal(utc)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

fun friendlyDate(epoch: Long): String {
    return try {
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
            .format(Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()))
    } catch (t: Throwable) {
        "today"
    }
}

private fun todayAtMidnight(): Long {
    return java.time.LocalDate.now()
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

/** DatePicker pakai UTC midnight; konversi ke local. */
private fun toUtcMillis(localEpoch: Long): Long {
    return localEpoch - TimeZone.getDefault().getOffset(localEpoch)
}

private fun utcToLocal(utcEpoch: Long): Long {
    return utcEpoch + TimeZone.getDefault().getOffset(utcEpoch)
}
