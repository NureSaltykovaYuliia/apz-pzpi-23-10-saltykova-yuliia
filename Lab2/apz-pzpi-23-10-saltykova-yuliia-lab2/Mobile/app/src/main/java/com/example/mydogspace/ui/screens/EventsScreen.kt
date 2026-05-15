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

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun EventsScreen(navController: NavController) {
    var events by remember { mutableStateOf<List<EventDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedEventForDetails by remember { mutableStateOf<EventDto?>(null) }

    val eventTypes = listOf("Walk", "Training", "Exhibition", "Playdate", "Other")

    // New Event State
    var newName by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newStartTime by remember { mutableStateOf("2024-06-01T10:00:00Z") }
    var newEndTime by remember { mutableStateOf("2024-06-01T12:00:00Z") }
    var newType by remember { mutableStateOf(eventTypes[0]) }
    var selectedLocation by remember { mutableStateOf(LatLng(49.9935, 36.2304)) }
    var isTypeMenuExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
                    latitude = selectedLocation.latitude,
                    longitude = selectedLocation.longitude
                )
                NetworkModule.apiService.createEvent(request)
                showAddDialog = false
                loadEvents()
                Toast.makeText(context, "Захід створено!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Помилка створення", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteEvent(id: Int) {
        scope.launch {
            try {
                NetworkModule.apiService.deleteEvent(id)
                loadEvents()
            } catch (e: Exception) {
                Toast.makeText(context, "Помилка видалення", Toast.LENGTH_SHORT).show()
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

    // Add Event Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("СТВОРИТИ ЗАХІД", fontWeight = FontWeight.Black) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Назва") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newDescription, onValueChange = { newDescription = it }, label = { Text("Опис") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newStartTime, onValueChange = { newStartTime = it }, label = { Text("Початок (ISO)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newEndTime, onValueChange = { newEndTime = it }, label = { Text("Кінець (ISO)") }, modifier = Modifier.fillMaxWidth())
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        OutlinedTextField(
                            value = newType,
                            onValueChange = {},
                            label = { Text("Тип заходу") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { isTypeMenuExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = isTypeMenuExpanded,
                            onDismissRequest = { isTypeMenuExpanded = false }
                        ) {
                            eventTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        newType = type
                                        isTypeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("ЛОКАЦІЯ НА КАРТІ:", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp).border(2.dp, BrutalBlack)) {
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(selectedLocation, 12f)
                        }
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            onMapClick = { selectedLocation = it }
                        ) {
                            Marker(state = MarkerState(position = selectedLocation), title = "Місце заходу")
                        }
                    }
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

    // Event Details Dialog
    if (selectedEventForDetails != null) {
        val event = selectedEventForDetails!!
        AlertDialog(
            onDismissRequest = { selectedEventForDetails = null },
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(event.name.uppercase(), fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                    IconButton(onClick = { selectedEventForDetails = null }) {
                        Icon(Icons.Default.Close, "Закрити", tint = BrutalBlack)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("📅 ${event.startTime.take(10)} | ${event.startTime.drop(11).take(5)}", fontWeight = FontWeight.Bold)
                    Text("🏷️ Тип: ${event.type}", fontWeight = FontWeight.Bold)
                    Text("👥 Учасників: ${event.participantCount}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(event.description)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp).border(2.dp, BrutalBlack)) {
                        val eventPos = LatLng(event.latitude, event.longitude)
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(eventPos, 14f)
                        }
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(isMyLocationEnabled = false)
                        ) {
                            Marker(state = MarkerState(position = eventPos), title = event.name)
                        }
                    }
                }
            },
            confirmButton = {
                BrutalButton(
                    text = if (event.isJoined) "ПОКИНУТИ" else "ПРИЄДНАТИСЯ",
                    backgroundColor = if (event.isJoined) Color.White else SecondaryCyan,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { 
                        toggleJoin(event)
                        selectedEventForDetails = null
                    }
                )
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
                                Column(horizontalAlignment = Alignment.End) {
                                    BrutalButton(
                                        text = "✕",
                                        backgroundColor = PrimaryRed,
                                        onClick = { deleteEvent(event.id) },
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = event.description, maxLines = 2)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                BrutalButton(
                                    text = "ДЕТАЛІ",
                                    backgroundColor = TertiaryYellow,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedEventForDetails = event }
                                )
                                BrutalButton(
                                    text = if (event.isJoined) "ПОКИНУТИ" else "ПРИЄДНАТИСЯ",
                                    backgroundColor = if (event.isJoined) Color.White else SecondaryCyan,
                                    modifier = Modifier.weight(1f),
                                    onClick = { toggleJoin(event) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
