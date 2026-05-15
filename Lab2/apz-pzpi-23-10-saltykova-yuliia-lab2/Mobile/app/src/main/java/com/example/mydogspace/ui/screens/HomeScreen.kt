package com.example.mydogspace.ui.screens
 
import java.util.Locale

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.navigation.NavController
import com.example.mydogspace.network.NetworkModule
import com.example.mydogspace.network.PartnerDto
import com.example.mydogspace.network.EventDto
import com.example.mydogspace.network.DogDto
import com.example.mydogspace.ui.components.BrutalCard
import com.example.mydogspace.ui.components.BrutalButton
import com.example.mydogspace.ui.components.MainScaffold
import com.example.mydogspace.ui.theme.*
import android.Manifest
import android.widget.Toast
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.BitmapDescriptor

fun getDogIcon(context: Context, isSelected: Boolean): BitmapDescriptor {
    val size = 120
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // Draw background circle
    val bgPaint = Paint().apply {
        color = if (isSelected) Color(0xFF00BCD4).toArgb() else Color(0xFFFF5252).toArgb()
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2.2f, bgPaint)
    
    // Draw border
    val borderPaint = Paint().apply {
        color = Color.Black.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2.2f, borderPaint)

    // Draw dog emoji
    val textPaint = Paint().apply {
        textSize = 70f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    val fontMetrics = textPaint.fontMetrics
    val yOffset = (fontMetrics.ascent + fontMetrics.descent) / 2
    canvas.drawText("🐶", size / 2f, size / 2f - yOffset, textPaint)
    
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}


fun sendSafeZoneAlert(context: Context, dogName: String) {
    val channelId = "safe_zone_alerts"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Safe Zone Alerts", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }
    
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle("Увага! $dogName покинув(ла) безпечну зону!")
        .setContentText("Ваша собака знаходиться за межами дозволеного радіусу.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()
        
    notificationManager.notify(dogName.hashCode(), notification)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var partners by remember { mutableStateOf<List<PartnerDto>>(emptyList()) }
    var events by remember { mutableStateOf<List<EventDto>>(emptyList()) }
    var dogs by remember { mutableStateOf<List<DogDto>>(emptyList()) }
    var selectedDog by remember { mutableStateOf<DogDto?>(null) }
    var isExpanded by remember { mutableStateOf(false) }
    var alertedDogs by remember { mutableStateOf(setOf<Int>()) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()

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
        val permissionsToRequest = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        if (fineGranted || coarseGranted) {
            locationPermissionGranted = true
            
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
            locationPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

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
                
                // Check Safe Zones
                val newAlertedDogs = alertedDogs.toMutableSet()
                fetchedDogs.forEach { dog ->
                    val safeCenterLat = if (dog.isFollowingPhone) userLocation?.latitude else dog.safeZoneLatitude
                    val safeCenterLon = if (dog.isFollowingPhone) userLocation?.longitude else dog.safeZoneLongitude

                    if (dog.latitude != null && dog.longitude != null && safeCenterLat != null && safeCenterLon != null && dog.safeRadius != null) {
                        val dogLoc = Location("").apply { latitude = dog.latitude; longitude = dog.longitude }
                        val safeLoc = Location("").apply { latitude = safeCenterLat; longitude = safeCenterLon }
                        val dist = dogLoc.distanceTo(safeLoc)
                        
                        if (dist > dog.safeRadius) {
                            if (!newAlertedDogs.contains(dog.id)) {
                                sendSafeZoneAlert(context, dog.name)
                                newAlertedDogs.add(dog.id)
                            }
                        } else {
                            newAlertedDogs.remove(dog.id)
                        }
                    }
                }
                alertedDogs = newAlertedDogs
            } catch (e: Exception) {
                e.printStackTrace()
            }
            kotlinx.coroutines.delay(5000) // Poll every 5 seconds
        }
    }

    // Effect to move camera when selected dog or user location changes
    LaunchedEffect(userLocation, selectedDog) {
        if (userLocation != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(userLocation!!.latitude, userLocation!!.longitude), 15f)
            )
        } else {
            selectedDog?.let { dog ->
                val targetLatLng = when {
                    dog.latitude != null && dog.longitude != null && dog.latitude != 0.0 -> 
                        LatLng(dog.latitude!!, dog.longitude!!)
                    dog.safeZoneLatitude != null && dog.safeZoneLongitude != null && dog.safeZoneLatitude != 0.0 -> 
                        LatLng(dog.safeZoneLatitude!!, dog.safeZoneLongitude!!)
                    else -> null
                }
                targetLatLng?.let {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
                }
            }
        }
    }

    MainScaffold(title = "Карта відстеження", navController = navController) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                contentPadding = PaddingValues(top = 160.dp, bottom = 120.dp),
                properties = MapProperties(
                    isMyLocationEnabled = locationPermissionGranted,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = true
                )
            ) {
                // Dog Markers
                dogs.forEach { dog ->
                    // Перевірка на null та на (0,0), щоб не ставити маркери в океані
                    if (dog.latitude != null && dog.longitude != null && 
                        dog.latitude != 0.0 && dog.longitude != 0.0) {
                        Marker(
                            state = MarkerState(position = LatLng(dog.latitude, dog.longitude)),
                            title = dog.name,
                            snippet = "Статус: В мережі",
                            icon = getDogIcon(context, dog.id == selectedDog?.id)
                        )
                        
                        // Draw line between user and selected dog
                        if (dog.id == selectedDog?.id && userLocation != null) {
                            Polyline(
                                points = listOf(
                                    LatLng(userLocation!!.latitude, userLocation!!.longitude),
                                    LatLng(dog.latitude, dog.longitude)
                                ),
                                color = BrutalBlack,
                                width = 8f
                            )
                        }
                        
                        // Draw safe zone
                        val safeCenter = if (dog.isFollowingPhone && userLocation != null) {
                            LatLng(userLocation!!.latitude, userLocation!!.longitude)
                        } else if (dog.safeZoneLatitude != null && dog.safeZoneLongitude != null) {
                            LatLng(dog.safeZoneLatitude, dog.safeZoneLongitude)
                        } else null

                        if (safeCenter != null && dog.safeRadius != null) {
                            Circle(
                                center = safeCenter,
                                radius = dog.safeRadius,
                                strokeColor = PrimaryRed,
                                strokeWidth = 5f,
                                fillColor = PrimaryRed.copy(alpha = 0.2f)
                            )
                        }
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
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedDog?.name ?: "Немає собак",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { 
                                    IconButton(onClick = { isExpanded = !isExpanded }) {
                                        Icon(
                                            if (isExpanded) Icons.Default.KeyboardArrowUp 
                                            else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrutalBlack,
                                    unfocusedBorderColor = BrutalBlack
                                )
                            )
                            
                            DropdownMenu(
                                expanded = isExpanded,
                                onDismissRequest = { isExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f).background(BrutalWhite).border(2.dp, BrutalBlack)
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
        }
    }
}
