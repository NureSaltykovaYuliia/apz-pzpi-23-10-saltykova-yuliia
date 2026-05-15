package com.example.mydogspace.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mydogspace.network.NetworkModule
import com.example.mydogspace.network.PartnerDto
import com.example.mydogspace.ui.components.BrutalButton
import com.example.mydogspace.ui.components.BrutalCard
import com.example.mydogspace.ui.components.MainScaffold
import com.example.mydogspace.ui.theme.*
import kotlinx.coroutines.launch

import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import com.example.mydogspace.network.CreateUpdatePartnerDto
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun PartnersScreen(navController: NavController) {
    var partners by remember { mutableStateOf<List<PartnerDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var userProfile by remember { mutableStateOf<com.example.mydogspace.network.UserProfileDto?>(null) }
    
    // Add Partner State
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newAddress by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newWebsite by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf(LatLng(49.9935, 36.2304)) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun loadData() {
        isLoading = true
        error = null
        scope.launch {
            try {
                userProfile = NetworkModule.apiService.getProfile()
                partners = NetworkModule.apiService.getPartners()
                isLoading = false
            } catch (e: Exception) {
                error = "Не вдалося завантажити дані"
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("НОВИЙ ПАРТНЕР", fontWeight = FontWeight.Black) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Назва") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newDescription, onValueChange = { newDescription = it }, label = { Text("Опис") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newAddress, onValueChange = { newAddress = it }, label = { Text("Адреса") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newPhone, onValueChange = { newPhone = it }, label = { Text("Телефон") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newWebsite, onValueChange = { newWebsite = it }, label = { Text("Вебсайт") }, modifier = Modifier.fillMaxWidth())
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("ОБЕРІТЬ ЛОКАЦІЮ НА КАРТІ:", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp).border(2.dp, BrutalBlack)) {
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(selectedLocation, 12f)
                        }
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            onMapClick = { selectedLocation = it }
                        ) {
                            Marker(state = MarkerState(position = selectedLocation), title = "Місце партнера")
                        }
                    }
                }
            },
            confirmButton = {
                BrutalButton(
                    text = "ЗБЕРЕГТИ", 
                    backgroundColor = TertiaryYellow, 
                    onClick = {
                        scope.launch {
                            try {
                                val request = CreateUpdatePartnerDto(
                                    name = newName,
                                    description = newDescription,
                                    address = newAddress,
                                    phoneNumber = newPhone,
                                    website = newWebsite,
                                    latitude = selectedLocation.latitude,
                                    longitude = selectedLocation.longitude
                                )
                                NetworkModule.apiService.createPartner(request)
                                showAddDialog = false
                                loadData()
                                Toast.makeText(context, "Партнера додано!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Помилка додавання", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            },
            dismissButton = {
                BrutalButton(text = "ВІДМІНА", backgroundColor = Color.White, onClick = { showAddDialog = false })
            }
        )
    }

    MainScaffold(title = "Партнери", navController = navController) {
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
                        onClick = { loadData() }
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
                                text = "НАШІ ПАРТНЕРИ",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            if (userProfile?.role == "Admin") {
                                BrutalButton(
                                    text = "ДОДАТИ",
                                    backgroundColor = QuaternaryPink,
                                    onClick = { showAddDialog = true }
                                )
                            }
                        }
                    }

                    items(partners) { partner ->
                        BrutalCard(
                            backgroundColor = Color.White,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                    val imageUrl = NetworkModule.getImageUrl(partner.photoUrl)
                                    if (imageUrl != null) {
                                        BrutalCard(
                                            backgroundColor = QuaternaryPink,
                                            modifier = Modifier.size(80.dp),
                                        ) {
                                            AsyncImage(
                                                model = imageUrl,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    } else {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .background(QuaternaryPink)
                                            .border(2.dp, BrutalBlack),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🐾", fontSize = 32.sp)
                                    }
                                }
                                
                                Column(modifier = Modifier.padding(start = 16.dp)) {
                                    Text(
                                        text = partner.name.uppercase(), 
                                        fontWeight = FontWeight.Black, 
                                        fontSize = 18.sp
                                    )
                                    if (partner.website.isNotBlank()) {
                                        Text(
                                            text = partner.website, 
                                            fontWeight = FontWeight.Black, 
                                            color = PrimaryRed,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "📍 ${partner.address}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = partner.description, maxLines = 3)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            BrutalButton(
                                text = "ЗВ'ЯЗАТИСЯ",
                                backgroundColor = TertiaryYellow,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { /* TODO */ }
                            )
                        }
                    }
                }
            }
        }
    }
}
