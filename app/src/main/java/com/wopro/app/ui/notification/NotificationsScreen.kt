package com.wopro.app.ui.notification

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.wopro.app.WOProApp
import com.wopro.app.data.local.NotificationEntity
import com.wopro.app.ui.VMFactory
import com.wopro.app.ui.components.EmptyState
import com.wopro.app.ui.home.formatDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationViewModel(repo: com.wopro.app.data.repository.WOProRepository) : androidx.lifecycle.ViewModel() {
    val ui = repo.observeNotifications()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    factory: VMFactory,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as WOProApp
    val repo = app.container.repository
    val encryption = app.container.encryptionManager
    val vm: NotificationViewModel = viewModel(factory = factory)
    val notifications by vm.ui.collectAsStateWithLifecycle(initialValue = emptyList())
    var currentEmail by remember { mutableStateOf("") }
    var currentRole by remember { mutableStateOf("") }
    var filtered by remember { mutableStateOf<List<NotificationEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        val id = encryption.getUserId()
        if (id > 0) {
            val user = kotlinx.coroutines.withContext(Dispatchers.IO) { repo.getUser(id) }
            currentEmail = user?.email?.trim()?.lowercase() ?: ""
            currentRole = user?.role ?: ""
        }
    }

    // Filter notifications: admin sees all, others see targeted + broadcast
    LaunchedEffect(notifications, currentEmail, currentRole) {
        filtered = if (currentRole.equals("Admin", ignoreCase = true)) {
            notifications
        } else {
            notifications.filter { n ->
                val t = n.targetEmail.trim().lowercase()
                t.isEmpty() || t == currentEmail
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifikasi", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = {
                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                            repo.markAllNotificationsRead()
                        }
                    }) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Tandai semua dibaca")
                    }
                    IconButton(onClick = {
                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                            repo.clearNotifications()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus semua")
                    }
                }
            )
        }
    ) { padding ->
        if (filtered.isEmpty()) EmptyState("Belum ada notifikasi", Modifier.fillMaxSize().padding(padding))
        else LazyColumn(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(filtered, key = { it.id }) { n ->
                Card(
                    onClick = {
                        if (!n.read) {
                            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                                repo.markNotificationRead(n.id)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (n.read) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(Modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (n.read) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(n.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text(n.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                formatDate(n.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}