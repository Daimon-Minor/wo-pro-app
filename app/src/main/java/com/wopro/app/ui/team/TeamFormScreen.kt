package com.wopro.app.ui.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wopro.app.WOProApp
import com.wopro.app.data.local.TeamEntity
import com.wopro.app.data.local.UserEntity
import com.wopro.app.ui.VMFactory
import com.wopro.app.ui.components.LoadingBox
import com.wopro.app.ui.components.SectionHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamFormScreen(
    teamId: Long,
    factory: VMFactory,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val repo = (context.applicationContext as WOProApp).container.repository
    var name by remember { mutableStateOf("") }
    var block by remember { mutableStateOf("") }
    var roomStart by remember { mutableStateOf("") }
    var roomEnd by remember { mutableStateOf("") }
    var selectedEmails by remember { mutableStateOf(setOf<String>()) }
    var users by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
    var loading by remember { mutableStateOf(teamId > 0) }

    val usersFlow = repo.observeUsers().collectAsStateWithLifecycle(initialValue = emptyList()).value
    // keep local mirror so the list is always fresh
    LaunchedEffect(usersFlow) { users = usersFlow }

    LaunchedEffect(teamId) {
        if (teamId > 0) {
            val t = withContext(Dispatchers.IO) { repo.getTeam(teamId) }
            t?.let {
                name = it.name
                block = it.block
                roomStart = it.roomStart.toString()
                roomEnd = it.roomEnd.toString()
                selectedEmails = it.memberEmails.split(",").map { s -> s.trim() }.filter { s -> s.isNotBlank() }.toSet()
            }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (teamId > 0) "Edit Tim" else "Tim Baru", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onSaved) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        if (loading) { LoadingBox(Modifier.fillMaxSize().padding(padding)); return@Scaffold }
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Tim") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = block, onValueChange = { block = it.uppercase().take(3) }, label = { Text("Blok (misal: A, B, C)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = roomStart, onValueChange = { roomStart = it.filter { c -> c.isDigit() } },
                    label = { Text("Ruang Mulai") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = roomEnd, onValueChange = { roomEnd = it.filter { c -> c.isDigit() } },
                    label = { Text("Ruang Akhir") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            SectionHeader("Anggota Tim (pilih user)")
            if (users.isEmpty()) {
                Text(
                    "Belum ada user terdaftar. User mendaftar lewat halaman Register.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            } else {
                users.forEach { u ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = u.email in selectedEmails,
                                onCheckedChange = { checked ->
                                    selectedEmails = if (checked) selectedEmails + u.email else selectedEmails - u.email
                                }
                            )
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                Text(u.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(u.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val start = roomStart.toIntOrNull() ?: 1
                    val end = roomEnd.toIntOrNull() ?: 99
                    val members = selectedEmails.sorted().joinToString(",")
                    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                        repo.saveTeam(TeamEntity(id = teamId, name = name, block = block, roomStart = start, roomEnd = end, memberEmails = members))
                    }
                    onSaved()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = name.isNotBlank() && block.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Text("  Simpan Tim", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
