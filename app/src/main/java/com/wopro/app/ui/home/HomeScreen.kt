package com.wopro.app.ui.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wopro.app.data.local.WorkOrderEntity
import com.wopro.app.ui.VMFactory
import com.wopro.app.ui.components.EmptyState
import com.wopro.app.ui.components.LoadingBox
import com.wopro.app.ui.components.SectionHeader
import com.wopro.app.ui.components.StatusChip
import com.wopro.app.ui.theme.Amber400
import com.wopro.app.ui.theme.Green600
import com.wopro.app.ui.theme.Red500
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    factory: VMFactory,
    onNavTo: (String) -> Unit,
    onLogout: () -> Unit
) {
    val vm: HomeViewModel = viewModel(factory = factory)
    val ui by vm.ui.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Dashboard", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        ui.user?.let {
                            Text(
                                "Welcome, ${it.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { onNavTo("notifications") }) {
                        BadgedBox(
                            badge = {
                                if (ui.unreadCount > 0) {
                                    Badge { Text(if (ui.unreadCount > 99) "99+" else ui.unreadCount.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavTo("wo_form") }) {
                Icon(Icons.Default.Add, contentDescription = "New Work Order")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (ui.loading) {
                item { LoadingBox() }
                return@LazyColumn
            }

            // Stat cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Open", ui.openCount, Amber400, Modifier.weight(1f))
                    StatCard("In Progress", ui.inProgressCount, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Completed", ui.completedCount, Green600, Modifier.weight(1f))
                    StatCard("Overdue", ui.overdueCount, Red500, Modifier.weight(1f))
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item { SectionHeader("Weekly Activity") }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    WeeklyChart(ui.weeklyCounts)
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("Recent Work Orders", Modifier)
                    androidx.compose.material3.TextButton(onClick = { onNavTo("tab_wos") }) {
                        Text("See All")
                    }
                }
            }

            if (ui.recent.isEmpty()) {
                item { EmptyState("No work orders yet. Tap + to create one.") }
            } else {
                items(ui.recent, key = { it.id }) { wo ->
                    RecentWoRow(wo, onClick = { onNavTo("wo_detail/${wo.id}") })
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatCard(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(96.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
            Text(
                value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/** Minimal bar chart (no external chart lib). */
@Composable
fun WeeklyChart(counts: List<Int>) {
    val max = (counts.maxOrNull() ?: 1).coerceAtLeast(1)
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp).height(120.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        counts.forEachIndexed { index, value ->
            val barHeight = (value.toFloat() / max) * 100f
            val barColor = if (index % 2 == 0) MaterialTheme.colorScheme.primary else Amber400
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(80.dp)
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val h = size.height * (barHeight / 100f)
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(0f, size.height - h),
                            size = androidx.compose.ui.geometry.Size(size.width, h),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(days.getOrElse(index) { "" }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun RecentWoRow(wo: WorkOrderEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(wo.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${wo.category} · ${formatDate(wo.dueDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            StatusChip(wo.status, statusColor(wo.status))
        }
    }
}

@Composable
fun statusColor(status: String): Color = when (status) {
    "Completed" -> Green600
    "In Progress" -> MaterialTheme.colorScheme.primary
    "Overdue" -> Red500
    "Open" -> Amber400
    else -> MaterialTheme.colorScheme.outline
}

fun formatDate(epoch: Long?): String {
    if (epoch == null) return "No due date"
    val formatter = DateTimeFormatter.ofPattern("dd MMM")
    return Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).format(formatter)
}

fun daysOverdue(epoch: Long?): Long {
    if (epoch == null) return 0
    return ChronoUnit.DAYS.between(
        Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalDate(),
        java.time.LocalDate.now()
    )
}