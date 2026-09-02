package com.wopro.app.ui.location

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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wopro.app.WOProApp
import com.wopro.app.data.local.LocationEntity
import com.wopro.app.ui.components.EmptyState
import com.wopro.app.ui.components.LoadingBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocationViewModel(repo: com.wopro.app.data.repository.WOProRepository) : androidx.lifecycle.ViewModel() {
    val ui = repo.observeLocations()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageLocationsScreen(
    factory: com.wopro.app.ui.VMFactory,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as WOProApp
    val repo = app.container.repository
    val vm: LocationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val locations by vm.ui.collectAsStateWithLifecycle(initialValue = emptyList())
    var newName by remember { mutableStateOf("") }

    // Hanya admin yang boleh tambah/edit/hapus lokasi
    var checked by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val id = app.container.encryptionManager.getUserId()
        val user = if (id > 0) withContext(Dispatchers.IO) { repo.getUser(id) } else null
        isAdmin = user?.role.equals("Admin", ignoreCase = true)
        checked = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Lokasi", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = {
                    val name = newName.trim()
                    if (name.isNotBlank()) {
                        newName = ""
                        CoroutineScope(Dispatchers.IO).launch {
                            repo.addLocation(name)
                        }
                    }
                }) { Icon(Icons.Default.Add, "Tambah Lokasi") }
            }
        }
    ) { padding ->
        when {
            !checked -> LoadingBox(Modifier.fillMaxSize().padding(padding))
            !isAdmin -> AdminOnlyNotice(onBack, Modifier.fillMaxSize().padding(padding))
            else -> Column(Modifier.fillMaxSize().padding(padding)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nama lokasi baru") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    "Tap + untuk menambah lokasi",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
                if (locations.isEmpty()) {
                    EmptyState("Belum ada lokasi", Modifier.fillMaxSize())
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(locations, key = { it.id }) { loc ->
                            LocationRow(loc, onDelete = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    repo.deleteLocation(loc)
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AdminOnlyNotice(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
        Text(
            "Khusus Admin",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            "Hanya admin/superuser yang dapat menambah, mengedit, atau menghapus data ini.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
        )
        Button(onClick = onBack) { Text("Kembali") }
    }
}

@Composable
private fun LocationRow(loc: LocationEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                loc.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 12.dp).weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
