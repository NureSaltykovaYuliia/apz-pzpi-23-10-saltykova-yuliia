package com.example.mydogspace.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.mydogspace.network.ConversationDto
import com.example.mydogspace.network.NetworkModule
import com.example.mydogspace.ui.components.BrutalButton
import com.example.mydogspace.ui.components.BrutalCard
import com.example.mydogspace.ui.components.MainScaffold
import com.example.mydogspace.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ChatListScreen(navController: NavController) {
    var conversations by remember { mutableStateOf<List<ConversationDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun loadConversations() {
        isLoading = true
        error = null
        scope.launch {
            try {
                conversations = NetworkModule.apiService.getConversations()
                isLoading = false
            } catch (e: Exception) {
                error = "Не вдалося завантажити чати"
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadConversations()
    }

    MainScaffold(title = "Повідомлення", navController = navController) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrutalBlack)
                }
            } else if (error != null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = error!!, color = PrimaryRed, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(16.dp))
                    BrutalButton(text = "СПРОБУВАТИ ЗНОВУ", backgroundColor = TertiaryYellow, onClick = { loadConversations() })
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    item {
                        Text(
                            text = "ВАШІ ЧАТИ",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    if (conversations.isEmpty()) {
                        item {
                            BrutalCard(backgroundColor = Color.White, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "У вас поки немає активних переписок.", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    items(conversations) { chat ->
                        BrutalCard(
                            backgroundColor = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable { navController.navigate("chat_detail/${chat.id}") }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(SecondaryCyan)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("💬", fontSize = 24.sp)
                                }
                                Column(modifier = Modifier.padding(start = 16.dp)) {
                                    val name = chat.name ?: chat.participantNames.joinToString(", ")
                                    Text(text = name.uppercase(), fontWeight = FontWeight.Black, fontSize = 18.sp)
                                    Text(text = "Останнє повідомлення...", color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
