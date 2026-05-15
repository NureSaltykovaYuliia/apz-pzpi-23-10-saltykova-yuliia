package com.example.mydogspace.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mydogspace.network.*
import com.example.mydogspace.ui.components.BrutalButton
import com.example.mydogspace.ui.components.BrutalCard
import com.example.mydogspace.ui.components.MainScaffold
import com.example.mydogspace.ui.theme.*
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DogDetailScreen(navController: NavController, dogId: Int) {
    var dog by remember { mutableStateOf<DogDto?>(null) }
    var device by remember { mutableStateOf<SmartDeviceDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isEditing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Edit states
    var editName by remember { mutableStateOf("") }
    var editBreed by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editBirthDate by remember { mutableStateOf("") }
    
    // Safe Zone states
    var editSafeRadius by remember { mutableStateOf("100") }
    var safeZoneCenter by remember { mutableStateOf<LatLng?>(null) }
    var isFollowingPhone by remember { mutableStateOf(false) }

    // Bind state
    var showBindDialog by remember { mutableStateOf(false) }
    var deviceGuid by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var userLocation by remember { mutableStateOf<Location?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationPermissionGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    userLocation = loc
                }
            } catch (_: SecurityException) { }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 5000
            ).build()

            val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                    result.lastLocation?.let { userLocation = it }
                }
            }

            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    android.os.Looper.getMainLooper()
                )
            } catch (_: SecurityException) { }
        } else {
            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    fun loadData() {
        isLoading = true
        scope.launch {
            try {
                dog = NetworkModule.apiService.getDogById(dogId)
                dog?.let {
                    editName = it.name
                    editBreed = it.breed
                    editDescription = it.description
                    editBirthDate = it.dateOfBirth.take(10)
                    editSafeRadius = it.safeRadius?.toString() ?: "100"
                    safeZoneCenter = if (it.safeZoneLatitude != null && it.safeZoneLongitude != null) 
                        LatLng(it.safeZoneLatitude, it.safeZoneLongitude) 
                    else null
                    isFollowingPhone = it.isFollowingPhone
                }
                
                // Try to get device info
                try {
                    device = NetworkModule.apiService.getDeviceByDogId(dogId)
                } catch (e: Exception) {
                    device = null
                }
                
                isLoading = false
            } catch (e: Exception) {
                error = "Не вдалося завантажити дані"
                isLoading = false
            }
        }
    }

    LaunchedEffect(dogId) {
        loadData()
    }

    fun handleUpdate() {
        scope.launch {
            try {
                val request = CreateUpdateDogDto(editName, editBreed, editBirthDate + "T00:00:00Z", editDescription)
                NetworkModule.apiService.updateDog(dogId, request)
                isEditing = false
                loadData()
                Toast.makeText(context, "Оновлено успішно!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Помилка оновлення", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun handleBind() {
        if (deviceGuid.isBlank()) return
        scope.launch {
            try {
                NetworkModule.apiService.assignDevice(deviceGuid.trim(), AssignDeviceDto(dogId))
                showBindDialog = false
                deviceGuid = ""
                loadData()
                Toast.makeText(context, "Пристрій прив'язано!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Помилка припуску GUID", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun handleUnbind() {
        scope.launch {
            try {
                NetworkModule.apiService.unassignDevice(dogId)
                loadData()
                Toast.makeText(context, "Пристрій відв'язано", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Помилка відв'язки", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showBindDialog) {
        AlertDialog(
            onDismissRequest = { showBindDialog = false },
            title = { Text("ПРИВ'ЯЗКА ПРИСТРОЮ", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text("Введіть GUID вашого розумного нашийника:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = deviceGuid,
                        onValueChange = { deviceGuid = it },
                        label = { Text("GUID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                BrutalButton(text = "ПРИВ'ЯЗАТИ", backgroundColor = TertiaryYellow, onClick = { handleBind() })
            },
            dismissButton = {
                BrutalButton(text = "СКАСУВАТИ", backgroundColor = Color.White, onClick = { showBindDialog = false })
            }
        )
    }

    MainScaffold(title = "Деталі собаки", navController = navController) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrutalBlack)
            }
        } else if (error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = error!!, color = PrimaryRed)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Card
                BrutalCard(backgroundColor = Color.White, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(SecondaryCyan)
                                    .border(2.dp, BrutalBlack),
                                contentAlignment = Alignment.Center
                            ) {
                                val imageUrl = NetworkModule.getImageUrl(dog?.photoUrl)
                                if (imageUrl != null) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text("🐶", fontSize = 40.sp)
                                }
                            }
                            
                            Column(modifier = Modifier.padding(start = 16.dp)) {
                                Text(
                                    text = (dog?.name ?: "").uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = dog?.breed ?: "",
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryRed
                                )
                            }
                        }
                        
                        BrutalButton(
                            text = if (isEditing) "💾" else "✏️",
                            backgroundColor = if (isEditing) SecondaryCyan else TertiaryYellow,
                            onClick = { if (isEditing) handleUpdate() else isEditing = true },
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Information Section
                BrutalCard(backgroundColor = Color.White, modifier = Modifier.fillMaxWidth()) {
                    Text("ОСНОВНА ІНФОРМАЦІЯ", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isEditing) {
                        OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Кличка") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = editBreed, onValueChange = { editBreed = it }, label = { Text("Порода") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = editBirthDate, onValueChange = { editBirthDate = it }, label = { Text("Дата народження (РРРР-ММ-ДД)") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = editDescription, onValueChange = { editDescription = it }, label = { Text("Опис") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    } else {
                        DetailRow("КЛИЧКА", dog?.name ?: "")
                        DetailRow("ПОРОДА", dog?.breed ?: "")
                        DetailRow("ДАТА НАРОДЖЕННЯ", dog?.dateOfBirth?.take(10) ?: "")
                        DetailRow("ОПИС", dog?.description ?: "Опис відсутній")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Device Section
                BrutalCard(
                    backgroundColor = if (device != null) QuaternaryPink else Gray200,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeviceHub, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("РОЗУМНИЙ ПРИСТРІЙ", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    if (device != null) {
                        DetailRow("GUID", device!!.deviceGuid)
                        DetailRow("ЗАРЯД", "${device!!.batteryLevel.toInt()}%")

                        var distanceText = "Невідомо"
                        if (userLocation != null && device!!.lastLatitude != 0.0 && device!!.lastLongitude != 0.0) {
                            val dogLoc = Location("").apply {
                                latitude = device!!.lastLatitude
                                longitude = device!!.lastLongitude
                            }
                            val dist = userLocation!!.distanceTo(dogLoc)
                            distanceText = if (dist > 1000) {
                                String.format("%.1f км", dist / 1000)
                            } else {
                                "${dist.toInt()} м"
                            }
                        }
                        DetailRow("ДИСТАНЦІЯ ДО СОБАКИ", distanceText)
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        if (device!!.lastLatitude != 0.0 && device!!.lastLongitude != 0.0) {
                            val dogLatLng = LatLng(device!!.lastLatitude, device!!.lastLongitude)
                            val cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(dogLatLng, 15f)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .border(2.dp, BrutalBlack)
                            ) {
                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = cameraPositionState,
                                    properties = MapProperties(isMyLocationEnabled = locationPermissionGranted)
                                ) {
                                    Marker(
                                        state = MarkerState(position = dogLatLng),
                                        title = dog?.name ?: "Собака",
                                        snippet = "Поточна локація"
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        BrutalButton(
                            text = "ВІДВ'ЯЗАТИ ПРИСТРІЙ",
                            backgroundColor = PrimaryRed,
                            onClick = { handleUnbind() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Пристрій не підключений", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        BrutalButton(
                            text = "ПРИВ'ЯЗАТИ ПРИСТРІЙ",
                            backgroundColor = TertiaryYellow,
                            onClick = { showBindDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // Safe Zone Section
                BrutalCard(backgroundColor = Color.White, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("БЕЗПЕЧНА ЗОНА", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = editSafeRadius,
                        onValueChange = { editSafeRadius = it },
                        label = { Text("Радіус безпечної зони (метрів)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("ЦЕНТР ЗОНИ (НАТИСНІТЬ НА КАРТУ АБО КНОПКУ НИЖЧЕ)", fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))

                    val initialCenter = safeZoneCenter ?: LatLng(49.9935, 36.2304)
                    val mapCameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(initialCenter, 15f)
                    }
                    
                    // Update camera if safeZoneCenter changes externally (e.g. from button)
                    LaunchedEffect(safeZoneCenter) {
                        safeZoneCenter?.let {
                            mapCameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
                        }
                    }

                    // Handle "Follow Phone" logic: if enabled and user moves, update safeZoneCenter
                    LaunchedEffect(isFollowingPhone, userLocation) {
                        if (isFollowingPhone && userLocation != null) {
                            safeZoneCenter = LatLng(userLocation!!.latitude, userLocation!!.longitude)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .border(2.dp, BrutalBlack)
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = mapCameraPositionState,
                            onMapClick = { latLng ->
                                if (!isFollowingPhone) {
                                    safeZoneCenter = latLng
                                } else {
                                    Toast.makeText(context, "Вимкніть режим стеження для ручного вибору", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            safeZoneCenter?.let { center ->
                                Marker(state = MarkerState(position = center), title = "Центр безпечної зони")
                                Circle(
                                    center = center,
                                    radius = editSafeRadius.toDoubleOrNull() ?: 100.0,
                                    strokeColor = PrimaryRed,
                                    strokeWidth = 5f,
                                    fillColor = PrimaryRed.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    BrutalButton(
                        text = if (isFollowingPhone) "✅ СЛІДКУВАТИ ЗА ТЕЛЕФОНОМ" else "⬜ СЛІДКУВАТИ ЗА ТЕЛЕФОНОМ",
                        backgroundColor = if (isFollowingPhone) SecondaryCyan else Color.White,
                        onClick = { isFollowingPhone = !isFollowingPhone },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        BrutalButton(
                            text = "ЦЕНТР ТУТ",
                            backgroundColor = SecondaryCyan,
                            enabled = !isFollowingPhone,
                            onClick = {
                                userLocation?.let {
                                    safeZoneCenter = LatLng(it.latitude, it.longitude)
                                } ?: Toast.makeText(context, "Геолокація недоступна", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BrutalButton(
                            text = "ЗБЕРЕГТИ ЗОНУ",
                            backgroundColor = TertiaryYellow,
                            onClick = {
                                val radius = editSafeRadius.toDoubleOrNull()
                                val finalCenter = safeZoneCenter ?: userLocation?.let { LatLng(it.latitude, it.longitude) }
                                
                                if (radius != null && finalCenter != null) {
                                    scope.launch {
                                        try {
                                            NetworkModule.apiService.updateSafeZone(
                                                dogId,
                                                UpdateSafeZoneDto(
                                                    finalCenter.latitude,
                                                    finalCenter.longitude,
                                                    radius,
                                                    isFollowingPhone
                                                )
                                            )
                                            Toast.makeText(context, "Зону оновлено!", Toast.LENGTH_SHORT).show()
                                            loadData()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Помилка оновлення", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    if (radius == null) {
                                        Toast.makeText(context, "Вкажіть радіус", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Центр зони не визначено. Увімкніть GPS", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                BrutalButton(
                    text = "НАЗАД",
                    backgroundColor = Color.White,
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray)
        Text(text = value.uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Black)
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
    }
}
