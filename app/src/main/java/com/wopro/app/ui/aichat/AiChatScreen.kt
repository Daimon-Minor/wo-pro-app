package com.wopro.app.ui.aichat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
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
import com.wopro.app.data.local.ChatMessageEntity
import com.wopro.app.ui.VMFactory
import com.wopro.app.ui.components.EmptyState
import com.wopro.app.ui.components.LoadingBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import com.wopro.app.WOProApp
import kotlinx.coroutines.CoroutineScope

class AiChatViewModel(repo: com.wopro.app.data.repository.WOProRepository) : androidx.lifecycle.ViewModel() {
    val ui = repo.observeChat()
    private val repoRef = repo

    fun sendMessage(text: String) = kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        repoRef.addChatMessage(ChatMessageEntity(role = "user", content = text))
        // Demo: simple echo with a mock response
        val response = demoResponse(text)
        repoRef.addChatMessage(ChatMessageEntity(role = "assistant", content = response))
    }

    private fun demoResponse(query: String): String = when {
        query.contains("hello", true) || query.contains("hi", true) -> "Hello! How can I help with your facility today?"
        query.contains("energy", true) -> "Track energy readings in the Meters tab. Compare tariffs to optimise costs."
        query.contains("wo", true) || query.contains("work order", true) -> "Create work orders in the Work Orders tab. Set priority and due dates."
        query.contains("audit", true) -> "Use the Audit tab to run safety and compliance checklists."
        query.contains("report", true) -> "Audit reports are saved locally. You can review them in the Audit tab."
        query.contains("cooling", true) || query.contains("chiller", true) -> "Chiller readings are tracked in the Meters tab. Compare with tariff data."
        else -> "I'm a demo assistant. I can help with basic questions about work orders, energy readings, and audits. Try: 'how to create a work order'?"
    }

    fun clearChat() = kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        repoRef.clearChat()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(factory: VMFactory) {
    val vm: AiChatViewModel = viewModel(factory = factory)
    val messages by vm.ui.collectAsStateWithLifecycle(initialValue = emptyList())
    var input by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Assistant", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { vm.clearChat() }) {
                        Icon(Icons.Default.Delete, "Clear — but icon placeholder")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (messages.isEmpty()) {
                EmptyState("Ask me anything about facility management", Modifier.weight(1f))
            } else {
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(messages, key = { it.id }) { msg ->
                        val isUser = msg.role == "user"
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .let { if (isUser) it.padding(start = 48.dp) else it.padding(end = 48.dp) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                msg.content,
                                modifier = Modifier.padding(14.dp),
                                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Ask anything…") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        if (input.isNotBlank()) {
                            vm.sendMessage(input.trim())
                            input = ""
                        }
                    },
                    enabled = input.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}