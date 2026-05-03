package com.example.mydogspace.ui.screens

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
import com.example.mydogspace.network.EventDto
import com.example.mydogspace.network.NetworkModule
import com.example.mydogspace.ui.components.BrutalButton
import com.example.mydogspace.ui.components.BrutalCard
import com.example.mydogspace.ui.components.MainScaffold
import com.example.mydogspace.ui.theme.PrimaryRed
import com.example.mydogspace.ui.theme.TertiaryYellow
import com.example.mydogspace.ui.theme.SecondaryCyan
import com.example.mydogspace.ui.theme.BrutalBlack
import kotlinx.coroutines.launch

@Composable
fun EventsScreen(navController: NavController) {
    var events by remember { mutableStateOf<List<EventDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // New Event State
    var newName by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newStartTime by remember { mutableStateOf("2024-06-01T10:00:00Z") }
    var newEndTime by remember { mutableStateOf("2024-06-01T12:00:00Z") }
    var newType by remember { mutableStateOf("Walk") }

    val scope = rememberCoroutineScope()

    fun loadEvents() {
        isLoading = true
        error = null
        scope.launch {
            try {
                events = NetworkModule.apiService.getEvents()
                isLoading = false
            } catch (e: Exception) {
                error = "Не вдалося завантажити заходи"
                isLoading = false
            }
        }
    }

    fun addEvent() {
        scope.launch {
            try {
                val request = com.example.mydogspace.network.CreateUpdateEventDto(
                    name = newName,
                    description = newDescription,
                    startTime = newStartTime,
                    endTime = newEndTime,
                    type = newType,
                    latitude = 50.0, // Default coordinates
                    longitude = 36.23
                )
                NetworkModule.apiService.createEvent(request)
                showAddDialog = false
                loadEvents()
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun deleteEvent(id: Int) {
        scope.launch {
            try {
                NetworkModule.apiService.deleteEvent(id)
                loadEvents()
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun toggleJoin(event: EventDto) {
        scope.launch {
            try {
                if (event.isJoined) {
                    NetworkModule.apiService.leaveEvent(event.id)
                } else {
                    NetworkModule.apiService.joinEvent(event.id)
                }
                loadEvents()
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    LaunchedEffect(Unit) {
        loadEvents()
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("СТВОРИТИ ЗАХІД", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Назва") })
                    OutlinedTextField(value = newDescription, onValueChange = { newDescription = it }, label = { Text("Опис") })
                    OutlinedTextField(value = newStartTime, onValueChange = { newStartTime = it }, label = { Text("Початок (ISO)") })
                    OutlinedTextField(value = newEndTime, onValueChange = { newEndTime = it }, label = { Text("Кінець (ISO)") })
                    OutlinedTextField(value = newType, onValueChange = { newType = it }, label = { Text("Тип") })
                }
            },
            confirmButton = {
                BrutalButton(text = "СТВОРТИ", backgroundColor = TertiaryYellow, onClick = { addEvent() })
            },
            dismissButton = {
                BrutalButton(text = "ВІДМІНА", backgroundColor = Color.White, onClick = { showAddDialog = false })
            }
        )
    }

    MainScaffold(title = "Заходи", navController = navController) {
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
                    Text(
                        text = error!!, 
                        color = PrimaryRed, 
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    BrutalButton(
                        text = "СПРОБУВАТИ ЗНОВУ",
                        backgroundColor = TertiaryYellow,
                        onClick = { loadEvents() }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ЦІКАВІ ПОДІЇ",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            BrutalButton(
                                text = "НОВА",
                                backgroundColor = TertiaryYellow,
                                onClick = { showAddDialog = true }
                            )
                        }
                    }

                    if (events.isEmpty()) {
                        item {
                            BrutalCard(backgroundColor = Color.White, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Наразі немає активних заходів.", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    items(events) { event ->
                        BrutalCard(
                            backgroundColor = Color.White,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = event.name.uppercase(), fontWeight = FontWeight.Black, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = event.startTime.take(10), 
                                        fontWeight = FontWeight.Black, 
                                        color = PrimaryRed,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "📍 ${event.type}", 
                                        fontSize = 14.sp, 
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                BrutalButton(
                                    text = "❌",
                                    backgroundColor = PrimaryRed,
                                    onClick = { deleteEvent(event.id) },
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = event.description)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            BrutalButton(
                                text = if (event.isJoined) "ПОКИНУТИ" else "ПРИЄДНАТИСЯ",
                                backgroundColor = if (event.isJoined) Color.White else SecondaryCyan,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { toggleJoin(event) }
                            )
                        }
                    }
                }
            }
        }
    }
}
