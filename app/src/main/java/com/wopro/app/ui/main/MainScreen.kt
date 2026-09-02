package com.wopro.app.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wopro.app.WOProApp
import com.wopro.app.ui.VMFactory
import com.wopro.app.ui.audit.AuditListScreen
import com.wopro.app.ui.home.HomeScreen
import com.wopro.app.ui.navigation.Tabs
import com.wopro.app.ui.utility.MeterListScreen
import com.wopro.app.ui.workorder.WorkOrderListScreen

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun MainScreen(onNavTo: (String) -> Unit, onLogout: () -> Unit) {
    var selectedTab by rememberSaveable { mutableStateOf(Tabs.HOME) }
    val tabs = listOf(
        TabItem(Tabs.HOME, "Home", Icons.Default.Home),
        TabItem(Tabs.WOS, "Work Orders", Icons.Default.Work),
        TabItem(Tabs.AUDIT, "Audit", Icons.AutoMirrored.Filled.List),
        TabItem(Tabs.METERS, "Meters", Icons.Default.Speed),
        TabItem(Tabs.MORE, "More", Icons.Default.MoreVert)
    )
    val factory = VMFactory(
        (LocalContext.current.applicationContext as WOProApp).container.repository,
        (LocalContext.current.applicationContext as WOProApp).container.encryptionManager
    )
    val app = LocalContext.current.applicationContext as WOProApp
    var isAdmin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val id = app.container.encryptionManager.getUserId()
        if (id > 0) {
            val user = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                app.container.repository.getUser(id)
            }
            isAdmin = user?.role.equals("Admin", ignoreCase = true)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab.route,
                        onClick = { selectedTab = tab.route },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, maxLines = 1) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (selectedTab) {
                Tabs.HOME -> HomeScreen(factory = factory, onNavTo = onNavTo, onLogout = onLogout)
                Tabs.WOS -> WorkOrderListScreen(
                    factory = factory,
                    onNew = { onNavTo("wo_form") },
                    onDetail = { id -> onNavTo("wo_detail/$id") },
                    onBack = { selectedTab = Tabs.HOME }
                )
                Tabs.AUDIT -> AuditListScreen(
                    factory = factory,
                    onNew = { onNavTo("audit_form") },
                    onDetail = { id -> onNavTo("audit_form?reportId=$id") },
                    onBack = { selectedTab = Tabs.HOME }
                )
                Tabs.METERS -> MeterListScreen(
                    factory = factory,
                    onNew = { type -> onNavTo("meter_form?meterType=$type") },
                    onBack = { selectedTab = Tabs.HOME }
                )
                Tabs.MORE -> MoreMenu(
                    onProjects = { onNavTo("project_list") },
                    onLogbook = { onNavTo("logbook_list") },
                    onReports = { onNavTo("reports") },
                    onAiChat = { onNavTo("ai_chat") },
                    onSettings = { onNavTo("settings") },
                    onTeams = { onNavTo("teams_list") },
                    isAdmin = isAdmin
                )
            }
        }
    }
}

@Composable
private fun MoreMenu(
    onProjects: () -> Unit,
    onLogbook: () -> Unit,
    onReports: () -> Unit,
    onAiChat: () -> Unit,
    onSettings: () -> Unit,
    onTeams: () -> Unit,
    isAdmin: Boolean
) {
    val items = mutableListOf(
        MenuItem("Logbook", "Catat pekerjaan harian & riwayat", Icons.Default.Book, onLogbook),
        MenuItem("Projects", "Capital projects & maintenance plans", Icons.Default.Work, onProjects),
        MenuItem("Reports", "Export CSV & filter laporan", Icons.Default.Assessment, onReports),
        MenuItem("AI Assistant", "Ask about procedures & energy tips", Icons.Default.Chat, onAiChat),
        MenuItem("Settings", "Profile, security & data", Icons.Default.Settings, onSettings)
    )
    if (isAdmin) {
        items.add(0, MenuItem("Teams", "Kelola tim & handle blok (Admin)", Icons.Default.Work, onTeams))
    }
    Column(Modifier.fillMaxSize().padding(top = 24.dp)) {
        items.forEach { item ->
            ListItem(
                headlineContent = { Text(item.title) },
                supportingContent = { Text(item.desc) },
                leadingContent = { Icon(item.icon, contentDescription = null) },
                modifier = Modifier.clickable(onClick = item.onClick)
            )
        }
    }
}

private data class MenuItem(
    val title: String,
    val desc: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)