package com.wopro.app.ui.workorder

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.wopro.app.WOProApp
import com.wopro.app.data.local.WorkOrderEntity
import com.wopro.app.ui.VMFactory
import com.wopro.app.ui.components.EmptyState
import com.wopro.app.ui.home.formatDateTime
import com.wopro.app.ui.theme.Gray500
import com.wopro.app.ui.theme.TealPrimary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val DoneColor = TealPrimary
private val NewColor = Gray500
private val PendingColor = Color(0xFFFFB300) // amber
private val AcceptedColor = Color(0xFF1976D2) // blue

private val filterOptions = listOf("All", "New", "On Progress", "Pending", "Done")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderListScreen(
    factory: VMFactory,
    onNew: () -> Unit,
    onDetail: (Long) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val app = context.applicationContext as WOProApp
    val repo = app.container.repository
    val vm: WorkOrderViewModel = viewModel(factory = factory)
    val ui by vm.ui.collectAsStateWithLifecycle()
    var currentUserName by remember { mutableStateOf("") }

    // Set current user name once
    LaunchedEffect(Unit) {
        val id = app.container.encryptionManager.getUserId()
        if (id > 0) {
            val user = withContext(Dispatchers.IO) { repo.getUser(id) }
            currentUserName = user?.name ?: ""
            vm.setCurrentUserName(currentUserName)
        }
    }

    fun accept(wo: WorkOrderEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            repo.acceptWorkOrder(wo, currentUserName.ifBlank { "User" })
        }
    }

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                    }
                    TabButton(text = "WO In", active = ui.tab == "In", onClick = { vm.setTab("In") })
                    Spacer(Modifier.width(8.dp))
                    TabButton(text = "WO Out", active = ui.tab == "Out", onClick = { vm.setTab("Out") })
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Work Orders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = ui.search,
                        onValueChange = { vm.setSearch(it) },
                        placeholder = { Text("Search Work Order...", color = Gray500, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Gray500) },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFD5DCDD),
                            focusedBorderColor = TealPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                // Filter status: All / New (belum accept) / On Progress / Pending / Done
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterOptions) { opt ->
                        FilterChipWo(opt, selected = ui.filter == opt, onSelect = { vm.setFilter(opt) })
                    }
                }
            }
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FloatingActionButton(
                    onClick = onNew,
                    containerColor = TealPrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New WO")
                }
                FloatingActionButton(
                    onClick = { /* phone call */ },
                    containerColor = TealPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(20.dp))
                }
            }
        }
    ) { padding ->
        if (ui.loading) {
            EmptyState("Loading...", Modifier.fillMaxSize().padding(padding))
        } else if (ui.items.isEmpty()) {
            EmptyState("No work orders found", Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(ui.items, key = { it.id }) { wo ->
                    WoCardRef(
                        wo = wo,
                        onClick = { onDetail(wo.id) },
                        onAccept = { accept(wo) }
                    )
                }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }
    }
}

@Composable
private fun TabButton(text: String, active: Boolean, onClick: () -> Unit) {
    val textColor = if (active) TealPrimary else Gray500
    val underline = if (active) TealPrimary else Color.Transparent
    Column(Modifier.clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 2.dp)) {
        Text(
            text,
            color = textColor,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp
        )
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().height(3.dp).background(underline, RoundedCornerShape(2.dp)))
    }
}

@Composable
private fun FilterChipWo(text: String, selected: Boolean, onSelect: () -> Unit) {
    val bg = if (selected) TealPrimary else Color(0xFFF0F2F2)
    val textColor = if (selected) Color.White else Gray500
    AssistChip(
        onClick = onSelect,
        label = { Text(text, color = textColor, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, fontSize = 13.sp) },
        colors = AssistChipDefaults.assistChipColors(containerColor = bg),
        border = null
    )
}

@Composable
private fun WoCardRef(wo: WorkOrderEntity, onClick: () -> Unit, onAccept: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Row(Modifier.fillMaxWidth().padding(10.dp)) {
                // Thumbnail
                Box(
                    Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF0F2F2)),
                    contentAlignment = Alignment.Center
                ) {
                    if (wo.photoUri != null) {
                        AsyncImage(
                            model = wo.photoUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("no image", color = Gray500, fontSize = 11.sp)
                    }
                    if (wo.status == "Done") {
                        Box(
                            Modifier.fillMaxSize().background(Color(0x99007A6B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "WO. ${wo.id}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        StatusPill(wo.status)
                    }
                    Spacer(Modifier.height(1.dp))
                    Text(
                        wo.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    DetailRow(Icons.Default.LocationOn, locationText(wo))
                    DetailRow(Icons.Default.Person, "Dibuat oleh: ${wo.createdBy.ifBlank { "-" }}")
                    DetailRow(Icons.Default.AccessTime, formatDateTime(wo.createdAt))
                    if (wo.priority.isNotBlank() && wo.priority != "Low") {
                        DetailRow(Icons.Default.PriorityHigh, wo.priority, textColor = priorityColor(wo.priority))
                    }
                }
            }
            // Note / activity log
            if (wo.activityLog.isNotBlank()) {
                val lastNotes = wo.activityLog.split("\n").takeLast(2)
                Column(Modifier.padding(start = 10.dp, end = 10.dp, top = 0.dp, bottom = 4.dp)) {
                    Text("Note:", fontWeight = FontWeight.SemiBold, color = Gray500, fontSize = 11.sp)
                    lastNotes.forEach { line ->
                        Text(
                            line.trim(),
                            color = Color(0xFF4A5457),
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            // Accept button untuk status Open
            if (wo.status == "Open") {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp).padding(top = 0.dp, bottom = 8.dp).height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AcceptedColor)
                ) {
                    Text("Accept", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}

private fun locationText(wo: WorkOrderEntity): String = when {
    wo.block.isNotBlank() && wo.roomNumber > 0 -> "${wo.block}-${wo.roomNumber}"
    wo.block.isNotBlank() -> wo.block
    wo.location.isNotBlank() -> wo.location
    else -> "-"
}

@Composable
private fun StatusPill(status: String) {
    val (bg, text) = when (status) {
        "Done" -> Color(0xFFE0F2F1) to DoneColor
        "Pending" -> Color(0xFFFFF8E1) to PendingColor
        "On Progress" -> Color(0xFFE3F2FD) to AcceptedColor
        "Open" -> Color(0xFFF0F2F2) to NewColor
        else -> Color(0xFFF0F2F2) to Gray500
    }
    Box(
        Modifier.background(bg, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(status, color = text, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

@Composable
private fun DetailRow(icon: ImageVector, text: String, textColor: Color = Gray500) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 1.dp)) {
        Icon(icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, color = textColor, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun priorityColor(p: String): Color = when (p) {
    "High", "Critical" -> Color(0xFFE53935)
    "Medium" -> Color(0xFFFFB300)
    else -> Gray500
}
