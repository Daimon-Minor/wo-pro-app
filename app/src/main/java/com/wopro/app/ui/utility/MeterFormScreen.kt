package com.wopro.app.ui.utility

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
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.wopro.app.data.local.MeterReadingEntity
import com.wopro.app.ui.VMFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import com.wopro.app.WOProApp
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeterFormScreen(
    meterType: String,
    factory: VMFactory,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val repo = (context.applicationContext as WOProApp).container.repository
    var name by remember { mutableStateOf("") }
    var reading by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var tariff by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$meterType Reading", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onSaved) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Meter type: $meterType", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Meter Name / ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = reading, onValueChange = { reading = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Reading Value") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (e.g. kWh, L, bar)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = tariff, onValueChange = { tariff = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Tariff (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        repo.addMeterReading(MeterReadingEntity(
                            meterType = meterType, meterName = name,
                            reading = reading.toDoubleOrNull() ?: 0.0,
                            unit = unit, tariff = tariff.toDoubleOrNull() ?: 0.0,
                            note = note
                        ))
                    }
                    onSaved()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = reading.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Text("  Save Reading", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}