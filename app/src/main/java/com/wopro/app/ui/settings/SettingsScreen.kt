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
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wopro.app.ui.components.LabeledRow
import com.wopro.app.ui.components.SectionHeader
import com.wopro.app.security.BiometricHelper
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.LocalActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var bioAvailable by remember { mutableStateOf(BiometricHelper.isAvailable(context)) }

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
                    LabeledRow("Biometric Auth", if (bioAvailable) "Available" else "Not available")
                    LabeledRow("Database", "Encrypted (SQLCipher AES-256)")
                    LabeledRow("Passwords", "Salted SHA-256 (never plaintext)")
                    LabeledRow("Network", "HTTPS only (cleartext blocked)")
                }
            }

            SectionHeader("Account")
            Button(
                onClick = {
                    if (bioAvailable && activity != null) {
                        BiometricHelper.authenticate(
                            activity = activity,
                            onSuccess = onLogout
                        )
                    } else {
                        onLogout()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Text("  Logout", style = MaterialTheme.typography.titleMedium)
            }

            SectionHeader("About")
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp)) {
                    LabeledRow("App", "WO Pro v1.0.0")
                    LabeledRow("Architecture", "MVVM + Room + SQLCipher")
                    LabeledRow("Mode", "Demo (local data)")
                    LabeledRow("API", "HTTPS ready (configure in BuildConfig)")
                }
            }
        }
    }
}