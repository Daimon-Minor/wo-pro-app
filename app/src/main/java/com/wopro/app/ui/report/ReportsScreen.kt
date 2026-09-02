package com.wopro.app.ui.report

import android.content.Intent
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.wopro.app.WOProApp
import com.wopro.app.data.local.WorkOrderEntity
import com.wopro.app.ui.components.EmptyState
import com.wopro.app.ui.components.FilterChips
import com.wopro.app.ui.components.StatusChip
import com.wopro.app.ui.home.formatDate
import com.wopro.app.ui.home.statusColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(factory: com.wopro.app.ui.VMFactory, onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = (context.applicationContext as WOProApp).container.repository

    var statusFilter by remember { mutableStateOf("All") }
    var roomFilter by remember { mutableStateOf("") }
    var rows by remember { mutableStateOf<List<WorkOrderEntity>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }

    val statuses = listOf("All", "Pending", "Done", "Open", "Accepted")

    LaunchedEffect(statusFilter, roomFilter) {
        val room = roomFilter.toIntOrNull() ?: 0
        rows = withContext(Dispatchers.IO) {
            repo.exportWorkOrders(statusFilter, room)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Spacer(Modifier.height(8.dp))
            FilterChips(statuses, statusFilter, { statusFilter = it })

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = roomFilter,
                    onValueChange = { roomFilter = it.filter(Char::isDigit).take(4) },
                    label = { Text("Filter No. Kamar") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        val csv = buildCsv(rows)
                        val uri = writeCsvToFile(context, csv)
                        if (uri != null) {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(send, "Bagikan Laporan"))
                        } else {
                            message = "Gagal membuat file laporan"
                        }
                    },
                    enabled = rows.isNotEmpty()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Text("  Export CSV")
                }
            }

            message?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(4.dp))
            }

            Text(
                "${rows.size} data terfilter",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(4.dp))

            if (rows.isEmpty()) {
                EmptyState("Tidak ada work order sesuai filter")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(rows, key = { it.id }) { wo ->
                        ReportRow(wo)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportRow(wo: WorkOrderEntity) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(wo.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                StatusChip(wo.status, statusColor(wo.status))
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${wo.category} · ${if (wo.block.isNotBlank()) "Blok ${wo.block}-${wo.roomNumber}" else wo.location}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                "Dibuat: ${formatDate(wo.createdAt)} · Due: ${formatDate(wo.dueDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            if (wo.status == "Pending" && wo.pendingReason.isNotBlank()) {
                Text("Alasan: ${wo.pendingReason}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/** Build CSV string from work orders. */
private fun buildCsv(rows: List<WorkOrderEntity>): String {
    val header = listOf(
        "ID", "Title", "Category", "Priority", "Status", "Location", "Block", "Room",
        "Due Date", "Created By", "Created At", "Accepted By", "Pending Reason", "Done At"
    )
    val sb = StringBuilder()
    sb.append(header.joinToString(",")).append("\n")
    rows.forEach { w ->
        val line = listOf(
            w.id.toString(), w.title, w.category, w.priority, w.status, w.location, w.block,
            w.roomNumber.toString(), formatDate(w.dueDate), w.createdBy, formatDate(w.createdAt),
            w.acceptedBy, w.pendingReason, if (w.doneAt != null) formatDate(w.doneAt) else ""
        ).map { escapeCsv(it) }
        sb.append(line.joinToString(",")).append("\n")
    }
    return sb.toString()
}

private fun escapeCsv(value: String): String {
    val v = value.replace("\"", "\"\"")
    return if (v.contains(",") || v.contains("\"") || v.contains("\n")) "\"$v\"" else v
}

private fun writeCsvToFile(context: android.content.Context, csv: String): android.net.Uri? {
    return try {
        val dir = File(context.filesDir, "documents").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "wo_report_$stamp.csv")
        FileOutputStream(file).use { it.write(csv.toByteArray(Charsets.UTF_8)) }
        FileProvider.getUriForFile(context, "com.wopro.app.fileprovider", file)
    } catch (t: Throwable) {
        null
    }
}
