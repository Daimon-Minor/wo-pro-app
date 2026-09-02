package com.wopro.app.ui.block

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wopro.app.WOProApp
import com.wopro.app.data.local.BlockEntity
import com.wopro.app.ui.VMFactory
import com.wopro.app.ui.components.EmptyState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BlockViewModel(repo: com.wopro.app.data.repository.WOProRepository) : androidx.lifecycle.ViewModel() {
    val ui = repo.observeBlocks()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBlocksScreen(
    factory: VMFactory,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repo = (context.applicationContext as WOProApp).container.repository
    val vm: BlockViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val blocks by vm.ui.collectAsStateWithLifecycle(initialValue = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var editBlock by remember { mutableStateOf<BlockEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Blok", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editBlock = null; showDialog = true }) {
                Icon(Icons.Default.Add, "Tambah Blok")
            }
        }
    ) { padding ->
        if (blocks.isEmpty()) {
            EmptyState("Belum ada blok. Tap + untuk tambah blok dengan L1/L2/L3 range.", Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(blocks, key = { it.id }) { b ->
                    BlockCard(b, onEdit = { editBlock = it; showDialog = true }, onDelete = {
                        CoroutineScope(Dispatchers.IO).launch { repo.deleteBlock(b) }
                    })
                }
            }
        }
    }

    if (showDialog) {
        BlockDialog(block = editBlock, onDismiss = { showDialog = false }, onSave = { b ->
            CoroutineScope(Dispatchers.IO).launch { repo.saveBlock(b) }
            showDialog = false
        })
    }
}

@Composable
private fun BlockCard(b: BlockEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(b.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error) }
            }
            Spacer(Modifier.height(4.dp))
            listOfNotNull(
                if (b.l1Start > 0) "L1: ${b.l1Start}–${b.l1End}" else null,
                if (b.l2Start > 0) "L2: ${b.l2Start}–${b.l2End}" else null,
                if (b.l3Start > 0) "L3: ${b.l3Start}–${b.l3End}" else null
            ).forEach { range ->
                Text(range, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlockDialog(block: BlockEntity?, onDismiss: () -> Unit, onSave: (BlockEntity) -> Unit) {
    var name by remember { mutableStateOf(block?.name ?: "") }
    var l1Start by remember { mutableStateOf(block?.l1Start?.toString() ?: "") }
    var l1End by remember { mutableStateOf(block?.l1End?.toString() ?: "") }
    var l2Start by remember { mutableStateOf(block?.l2Start?.toString() ?: "") }
    var l2End by remember { mutableStateOf(block?.l2End?.toString() ?: "") }
    var l3Start by remember { mutableStateOf(block?.l3Start?.toString() ?: "") }
    var l3End by remember { mutableStateOf(block?.l3End?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (block != null) "Edit Blok" else "Blok Baru") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Blok") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("L1 Range Kamar", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = l1Start, onValueChange = { l1Start = it.filter(Char::isDigit) }, label = { Text("Dari") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = l1End, onValueChange = { l1End = it.filter(Char::isDigit) }, label = { Text("Sampai") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
                Text("L2 Range Kamar (opsional)", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = l2Start, onValueChange = { l2Start = it.filter(Char::isDigit) }, label = { Text("Dari") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = l2End, onValueChange = { l2End = it.filter(Char::isDigit) }, label = { Text("Sampai") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
                Text("L3 Range Kamar (opsional)", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = l3Start, onValueChange = { l3Start = it.filter(Char::isDigit) }, label = { Text("Dari") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = l3End, onValueChange = { l3End = it.filter(Char::isDigit) }, label = { Text("Sampai") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(BlockEntity(
                        id = block?.id ?: 0,
                        name = name.trim(),
                        l1Start = l1Start.toIntOrNull() ?: 0, l1End = l1End.toIntOrNull() ?: 0,
                        l2Start = l2Start.toIntOrNull() ?: 0, l2End = l2End.toIntOrNull() ?: 0,
                        l3Start = l3Start.toIntOrNull() ?: 0, l3End = l3End.toIntOrNull() ?: 0
                    ))
                }
            }, enabled = name.isNotBlank()) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}