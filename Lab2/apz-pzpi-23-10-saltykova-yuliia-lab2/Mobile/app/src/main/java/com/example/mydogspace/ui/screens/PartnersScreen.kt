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

@Composable
fun PartnersScreen(navController: NavController) {
    var partners by remember { mutableStateOf<List<PartnerDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun loadPartners() {
        isLoading = true
        error = null
        scope.launch {
            try {
                partners = NetworkModule.apiService.getPartners()
                isLoading = false
            } catch (e: Exception) {
                error = "Не вдалося завантажити партнерів"
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadPartners()
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
                        onClick = { loadPartners() }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    item {
                        Text(
                            text = "НАШІ ПАРТНЕРИ",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
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
