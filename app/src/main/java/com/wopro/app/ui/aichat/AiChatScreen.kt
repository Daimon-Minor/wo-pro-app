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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.wopro.app.data.remote.AiApiClient
import com.wopro.app.data.remote.ChatMessageDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AiChatViewModel(repo: com.wopro.app.data.repository.WOProRepository) : androidx.lifecycle.ViewModel() {
    val ui = repo.observeChat()
    private val repoRef = repo

    private val _loading = androidx.compose.runtime.mutableStateOf(false)
    val loading: androidx.compose.runtime.State<Boolean> = _loading

    fun sendMessage(text: String) = kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        repoRef.addChatMessage(ChatMessageEntity(role = "user", content = text))
        _loading.value = true
        try {
            // Kirim riwayat percakapan terakhir (max 10 pesan) + system prompt
            val history = repoRef.observeChat().first().takeLast(10).map {
                ChatMessageDto(role = it.role, content = it.content)
            }
            val reply = AiApiClient.chat(listOf(ChatMessageDto("system", SYSTEM_PROMPT)) + history)
            repoRef.addChatMessage(ChatMessageEntity(role = "assistant", content = reply))
        } catch (t: Throwable) {
            repoRef.addChatMessage(
                ChatMessageEntity(
                    role = "assistant",
                    content = "⚠️ Gagal menghubungi AI: ${t.message ?: "error tidak diketahui"}. Coba lagi nanti."
                )
            )
        } finally {
            _loading.value = false
        }
    }

    fun clearChat() = kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        repoRef.clearChat()
    }

    companion object {
        private const val SYSTEM_PROMPT =
            "Kamu adalah AI Assistant untuk aplikasi WO Pro (manajemen work order & fasilitas hotel). " +
            "Bantu user membuat work order, memahami status (Open/On Progress/Pending/Done), " +
            "memberi tips energi, dan menjawab pertanyaan facility management. " +
            "Jawab singkat, jelas, dalam bahasa Indonesia (kecuali user pakai bahasa lain)."
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(factory: VMFactory, onBack: (() -> Unit)? = null) {
    val vm: AiChatViewModel = viewModel(factory = factory)
    val messages by vm.ui.collectAsStateWithLifecycle(initialValue = emptyList())
    val loading by vm.loading
    var input by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Assistant", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                    }
                },
                actions = {
                    IconButton(onClick = { vm.clearChat() }) {
                        Icon(Icons.Default.Delete, "Clear chat")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (messages.isEmpty() && !loading) {
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
                    if (loading) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .padding(end = 48.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Text(
                                    "AI sedang mengetik…",
                                    modifier = Modifier.padding(14.dp),
                                    color = MaterialTheme.colorScheme.outline,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
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
                    enabled = input.isNotBlank() && !loading
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}