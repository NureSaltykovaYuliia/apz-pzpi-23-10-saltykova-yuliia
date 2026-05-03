package com.example.mydogspace.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mydogspace.network.DogDto
import com.example.mydogspace.network.NetworkModule
import com.example.mydogspace.ui.components.BrutalButton
import com.example.mydogspace.ui.components.BrutalCard
import com.example.mydogspace.ui.components.MainScaffold
import com.example.mydogspace.ui.theme.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch

@Composable
fun DogsScreen(navController: NavController) {
    var dogs by remember { mutableStateOf<List<DogDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    // New Dog State
    var newName by remember { mutableStateOf("") }
    var newBreed by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newBirthDate by remember { mutableStateOf("2020-01-01") }

    val scope = rememberCoroutineScope()

    fun loadDogs() {
        isLoading = true
        error = null
        scope.launch {
            try {
                dogs = NetworkModule.apiService.getDogs()
                isLoading = false
            } catch (e: Exception) {
                error = "Не вдалося завантажити список собак"
                isLoading = false
            }
        }
    }

    fun addDog() {
        scope.launch {
            try {
                val request = com.example.mydogspace.network.CreateUpdateDogDto(
                    name = newName,
                    breed = newBreed,
                    description = newDescription,
                    dateOfBirth = newBirthDate + "T00:00:00Z"
                )
                NetworkModule.apiService.createDog(request)
                showAddDialog = false
                loadDogs()
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun deleteDog(id: Int) {
        scope.launch {
            try {
                NetworkModule.apiService.deleteDog(id)
                loadDogs()
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    LaunchedEffect(Unit) {
        loadDogs()
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("ДОДАТИ НОВУ СОБАКУ", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Кличка") })
                    OutlinedTextField(value = newBreed, onValueChange = { newBreed = it }, label = { Text("Порода") })
                    OutlinedTextField(value = newDescription, onValueChange = { newDescription = it }, label = { Text("Опис") })
                    OutlinedTextField(value = newBirthDate, onValueChange = { newBirthDate = it }, label = { Text("Дата народження (РРРР-ММ-ДД)") })
                }
            },
            confirmButton = {
                BrutalButton(text = "ЗБЕРЕГТИ", backgroundColor = TertiaryYellow, onClick = { addDog() })
            },
            dismissButton = {
                BrutalButton(text = "ВІДМІНА", backgroundColor = Color.White, onClick = { showAddDialog = false })
            }
        )
    }

    MainScaffold(title = "Собаки", navController = navController) {
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
                        onClick = { loadDogs() }
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
                                text = "МОЇ СОБАКИ",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            BrutalButton(
                                text = "ДОДАТИ",
                                backgroundColor = QuaternaryPink,
                                onClick = { showAddDialog = true }
                            )
                        }
                    }

                    if (dogs.isEmpty()) {
                        item {
                            BrutalCard(backgroundColor = Color.White, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "У вас поки немає доданих собак.", fontWeight = FontWeight.Bold)
                            }
                        }
                    }


                    items(dogs) { dog ->
                        BrutalCard(
                            backgroundColor = Color.White,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val imageUrl = NetworkModule.getImageUrl(dog.photoUrl)
                                    if (imageUrl != null) {
                                        BrutalCard(
                                            backgroundColor = SecondaryCyan,
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
                                                .background(SecondaryCyan)
                                                .border(2.dp, BrutalBlack),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🐶", fontSize = 32.sp)
                                        }
                                    }
                                    
                                    Column(modifier = Modifier.padding(start = 16.dp)) {
                                        Text(
                                            text = dog.name.uppercase(), 
                                            fontWeight = FontWeight.Black, 
                                            fontSize = 20.sp
                                        )
                                        Text(
                                            text = dog.breed, 
                                            fontWeight = FontWeight.Black, 
                                            color = PrimaryRed,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                BrutalButton(
                                    text = "❌",
                                    backgroundColor = PrimaryRed,
                                    onClick = { deleteDog(dog.id) },
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "НАРОДЖЕННЯ: ${dog.dateOfBirth.take(10)}", fontWeight = FontWeight.Black)
                                Text(text = "АКТИВНИЙ", fontWeight = FontWeight.Black, color = SecondaryCyan)
                            }
                        }
                    }
                }
            }
        }
    }
}
