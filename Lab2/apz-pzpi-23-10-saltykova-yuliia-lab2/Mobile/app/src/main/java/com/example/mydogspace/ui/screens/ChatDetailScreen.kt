package com.example.mydogspace.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mydogspace.network.MessageDto
import com.example.mydogspace.network.NetworkModule
import com.example.mydogspace.ui.components.BrutalButton
import com.example.mydogspace.ui.components.BrutalCard
import com.example.mydogspace.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatDetailScreen(navController: NavController, conversationId: Int) {
    var messages by remember { mutableStateOf<List<MessageDto>>(emptyList()) }
    var messageText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun loadMessages() {
        scope.launch {
            try {
                messages = NetworkModule.apiService.getMessages(conversationId)
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    fun sendMessage() {
        if (messageText.isBlank()) return
        scope.launch {
            try {
                NetworkModule.apiService.sendMessage(conversationId, messageText)
                messageText = ""
                loadMessages()
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    LaunchedEffect(Unit) {
        loadMessages()
        // Auto refresh messages every 5 seconds
        while(true) {
            delay(5000)
            loadMessages()
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(QuaternaryPink)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BrutalButton(
                        text = "←",
                        backgroundColor = Color.White,
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "ЧАТ #$conversationId",
                        modifier = Modifier.padding(start = 16.dp),
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Повідомлення...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrutalBlack,
                            unfocusedBorderColor = BrutalBlack
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BrutalButton(
                        text = "→",
                        backgroundColor = SecondaryCyan,
                        onClick = { sendMessage() },
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color.White)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrutalBlack)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    reverseLayout = true
                ) {
                    items(messages.reversed()) { msg ->
                        val isMe = msg.senderId == com.example.mydogspace.network.SessionManager.userId
                        
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                        ) {
                            BrutalCard(
                                backgroundColor = if (isMe) SecondaryCyan else Color.White,
                                modifier = Modifier.widthIn(max = 280.dp).padding(vertical = 4.dp)
                            ) {
                                Column {
                                    if (!isMe) {
                                        Text(
                                            text = msg.senderName, 
                                            fontWeight = FontWeight.Black, 
                                            fontSize = 12.sp,
                                            color = PrimaryRed
                                        )
                                    }
                                    Text(text = msg.content)
                                    Text(
                                        text = msg.timestamp.take(16).replace("T", " "),
                                        fontSize = 10.sp,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
