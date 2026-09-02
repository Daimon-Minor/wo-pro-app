package com.wopro.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wopro.app.ui.components.LabeledRow
import com.wopro.app.ui.components.SectionHeader
import androidx.compose.ui.platform.LocalContext
import com.wopro.app.WOProApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    onManageLocations: (() -> Unit)? = null,
    onManageBlocks: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val app = context.applicationContext as WOProApp
    var isAdmin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val id = app.container.encryptionManager.getUserId()
        if (id > 0) {
            val user = withContext(Dispatchers.IO) { app.container.repository.getUser(id) }
            isAdmin = user?.role.equals("Admin", ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader("Security")
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp)) {
                    LabeledRow("Database", "Encrypted (SQLCipher AES-256)")
                    LabeledRow("Passwords", "Salted SHA-256 (never plaintext)")
                    LabeledRow("Network", "HTTPS only (cleartext blocked)")
                }
            }

            if (isAdmin) {
                SectionHeader("Admin")
                if (onManageBlocks != null) {
                    OutlinedButton(
                        onClick = onManageBlocks,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null)
                        Text("  Kelola Blok (L1/L2/L3)", style = MaterialTheme.typography.titleMedium)
                    }
                }
                if (onManageLocations != null) {
                    OutlinedButton(
                        onClick = onManageLocations,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null)
                        Text("  Kelola Lokasi", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            SectionHeader("Account")
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Text("  Logout", style = MaterialTheme.typography.titleMedium)
            }

            SectionHeader("About")
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp)) {
                    LabeledRow("App", "WO Pro v2.0.0")
                    LabeledRow("Architecture", "MVVM + Room + SQLCipher")
                    LabeledRow("Mode", "Demo (local data)")
                    LabeledRow("API", "HTTPS ready (configure in BuildConfig)")
                }
            }
        }
    }
}