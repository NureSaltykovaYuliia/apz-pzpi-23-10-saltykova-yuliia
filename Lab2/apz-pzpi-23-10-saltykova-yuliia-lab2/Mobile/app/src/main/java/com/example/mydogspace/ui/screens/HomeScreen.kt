package com.example.mydogspace.ui.screens
 
import java.util.Locale

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mydogspace.network.NetworkModule
import com.example.mydogspace.network.PartnerDto
import com.example.mydogspace.network.EventDto
import com.example.mydogspace.network.DogDto
import com.example.mydogspace.ui.components.BrutalCard
import com.example.mydogspace.ui.components.MainScaffold
import com.example.mydogspace.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var partners by remember { mutableStateOf<List<PartnerDto>>(emptyList()) }
    var events by remember { mutableStateOf<List<EventDto>>(emptyList()) }
    var dogs by remember { mutableStateOf<List<DogDto>>(emptyList()) }
    var selectedDog by remember { mutableStateOf<DogDto?>(null) }
    var isExpanded by remember { mutableStateOf(false) }
    
    val kharkiv = LatLng(49.9935, 36.2304)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(kharkiv, 12f)
    }

    LaunchedEffect(Unit) {
        while(true) {
            try {
                partners = NetworkModule.apiService.getPartners()
                events = NetworkModule.apiService.getEvents()
                val fetchedDogs = NetworkModule.apiService.getDogs()
                dogs = fetchedDogs
                
                // Update selectedDog from the new list to get fresh coordinates
                if (fetchedDogs.isNotEmpty()) {
                    if (selectedDog == null) {
                        selectedDog = fetchedDogs.first()
                    } else {
                        selectedDog = fetchedDogs.find { it.id == selectedDog?.id } ?: fetchedDogs.first()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            kotlinx.coroutines.delay(5000) // Poll every 5 seconds
        }
    }

    // Effect to move camera when selected dog changes
    LaunchedEffect(selectedDog) {
        selectedDog?.let { dog ->
            if (dog.latitude != null && dog.longitude != null && (dog.latitude != 0.0 || dog.longitude != 0.0)) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(dog.latitude, dog.longitude), 15f
                    )
                )
            }
        }
    }

    MainScaffold(title = "Карта відстеження", navController = navController) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = false,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                // Dog Markers
                dogs.forEach { dog ->
                    if (dog.latitude != null && dog.longitude != null) {
                        Marker(
                            state = MarkerState(position = LatLng(dog.latitude, dog.longitude)),
                            title = dog.name,
                            snippet = "Статус: В мережі",
                            icon = BitmapDescriptorFactory.defaultMarker(
                                if (dog == selectedDog) BitmapDescriptorFactory.HUE_AZURE else BitmapDescriptorFactory.HUE_RED
                            )
                        )
                    }
                }

                // Partner Markers
                partners.forEach { partner ->
                    if (partner.latitude != null && partner.longitude != null) {
                        Marker(
                            state = MarkerState(position = LatLng(partner.latitude, partner.longitude)),
                            title = partner.name,
                            snippet = partner.description,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        )
                    }
                }
            }

            // Floating Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                BrutalCard(
                    backgroundColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "ОБЕРІТЬ СОБАКУ ДЛЯ ВІДСТЕЖЕННЯ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        ExposedDropdownMenuBox(
                            expanded = isExpanded,
                            onExpandedChange = { isExpanded = !isExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedDog?.name ?: "Немає собак",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = BrutalBlack,
                                    unfocusedBorderColor = BrutalBlack
                                )
                            )
                            
                            ExposedDropdownMenu(
                                expanded = isExpanded,
                                onDismissRequest = { isExpanded = false }
                            ) {
                                dogs.forEach { dog ->
                                    DropdownMenuItem(
                                        text = { Text(dog.name, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            selectedDog = dog
                                            isExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Bottom Info Card (Optional)
            if (selectedDog != null) {
                BrutalCard(
                    backgroundColor = TertiaryYellow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ОСТАННЯ ГЕОЛОКАЦІЯ: ",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                        selectedDog?.let { dog ->
                            if (dog.latitude != null && (dog.latitude != 0.0 || dog.longitude != 0.0)) {
                                Text(
                                    text = "${String.format(Locale.US, "%.4f", dog.latitude)}, ${String.format(Locale.US, "%.4f", dog.longitude)}",
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(text = "НЕМАЄ ДАНИХ", color = PrimaryRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
