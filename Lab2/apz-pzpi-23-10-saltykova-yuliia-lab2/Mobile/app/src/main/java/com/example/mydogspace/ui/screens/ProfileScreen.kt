package com.example.mydogspace.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.mydogspace.network.*
import com.example.mydogspace.ui.components.BrutalButton
import com.example.mydogspace.ui.components.BrutalCard
import com.example.mydogspace.ui.components.MainScaffold
import com.example.mydogspace.ui.navigation.Screen
import com.example.mydogspace.ui.theme.*
import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    var profile by remember { mutableStateOf<UserProfileDto?>(null) }
    var friends by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var dogs by remember { mutableStateOf<List<DogDto>>(emptyList()) }
    var conversations by remember { mutableStateOf<List<ConversationDto>>(emptyList()) }
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val tabs = listOf("ПРОФІЛЬ", "ДРУЗІ", "ЧАТИ", "СОБАКИ")

    fun loadData() {
        isLoading = true
        error = null
        scope.launch {
            try {
                val userProfile = NetworkModule.apiService.getProfile()
                profile = userProfile
                SessionManager.userId = userProfile.id
                
                friends = NetworkModule.apiService.getFriends()
                dogs = NetworkModule.apiService.getDogs()
                conversations = NetworkModule.apiService.getConversations()
                
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

    MainScaffold(title = "Профіль", navController = navController) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = BrutalWhite,
                contentColor = BrutalBlack,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = TertiaryYellow,
                        height = 4.dp
                    )
                },
                divider = { Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(BrutalBlack)) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Black, fontSize = 12.sp) }
                    )
                }
            }

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
                    BrutalButton(text = "ПОВТОРИТИ", backgroundColor = TertiaryYellow, onClick = { loadData() })
                }
            } else {
                when (selectedTab) {
                    0 -> ProfileInfoTab(profile, navController)
                    1 -> FriendsTab(friends, navController) { loadData() }
                    2 -> ChatsTab(conversations, navController)
                    3 -> DogsTab(dogs, navController) { loadData() }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoTab(profile: UserProfileDto?, navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(QuaternaryPink)
                    .border(3.dp, BrutalBlack),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 64.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            BrutalCard(backgroundColor = Color.White, modifier = Modifier.fillMaxWidth()) {
                Text(text = "ВАША ІНФОРМАЦІЯ", fontWeight = FontWeight.Black, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileField(label = "ЛОГІН", value = profile?.username ?: "N/A")
                ProfileField(label = "EMAIL", value = profile?.email ?: "N/A")
                ProfileField(label = "РОЛЬ", value = profile?.role ?: "USER")
            }

            Spacer(modifier = Modifier.height(32.dp))

            BrutalButton(
                text = "ВИЙТИ З АКАУНТУ",
                backgroundColor = PrimaryRed,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    SessionManager.token = null
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun FriendsTab(friends: List<UserDto>, navController: NavController, onRefresh: () -> Unit) {
    val scope = rememberCoroutineScope()
    
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text(text = "МОЇ ДРУЗІ", fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))
        }
        
        if (friends.isEmpty()) {
            item {
                BrutalCard(backgroundColor = Color.White, modifier = Modifier.fillMaxWidth()) {
                    Text("У вас поки немає друзів.", fontWeight = FontWeight.Bold)
                }
            }
        }

        items(friends) { friend ->
            BrutalCard(backgroundColor = Color.White, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = friend.username.uppercase(), fontWeight = FontWeight.Black)
                    BrutalButton(
                        text = "ЧАТ", 
                        backgroundColor = SecondaryCyan, 
                        onClick = {
                            scope.launch {
                                try {
                                    val conv = NetworkModule.apiService.createPrivateConversation(friend.id)
                                    navController.navigate(Screen.ChatDetail.createRoute(conv.id))
                                } catch (e: Exception) {}
                            }
                        },
                        modifier = Modifier.height(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatsTab(conversations: List<ConversationDto>, navController: NavController) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text(text = "ОСТАННІ ПЕРЕПИСКИ", fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))
        }

        if (conversations.isEmpty()) {
            item {
                BrutalCard(backgroundColor = Color.White, modifier = Modifier.fillMaxWidth()) {
                    Text("Активних чатів не знайдено.", fontWeight = FontWeight.Bold)
                }
            }
        }

        items(conversations) { chat ->
            BrutalCard(
                backgroundColor = Color.White,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { 
                    navController.navigate("chat_detail/${chat.id}") 
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(50.dp).background(SecondaryCyan).padding(8.dp), contentAlignment = Alignment.Center) {
                        Text("💬", fontSize = 24.sp)
                    }
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        val name = chat.name ?: chat.participantNames.joinToString(", ")
                        Text(text = name.uppercase(), fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text(text = "Переглянути повідомлення", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DogsTab(dogs: List<DogDto>, navController: NavController, onRefresh: () -> Unit) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newBreed by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newBirthDate by remember { mutableStateOf("2020-01-01") }
    val scope = rememberCoroutineScope()

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("ДОДАТИ СОБАКУ") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    TextField(value = newName, onValueChange = { newName = it }, label = { Text("Кличка") })
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(value = newBreed, onValueChange = { newBreed = it }, label = { Text("Порода") })
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(value = newDescription, onValueChange = { newDescription = it }, label = { Text("Опис") })
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(value = newBirthDate, onValueChange = { newBirthDate = it }, label = { Text("Дата (РРРР-ММ-ДД)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        try {
                            val dateStr = if (newBirthDate.contains("T")) newBirthDate else newBirthDate + "T00:00:00Z"
                            NetworkModule.apiService.createDog(CreateUpdateDogDto(newName, newBreed, dateStr, newDescription))
                            showAddDialog = false
                            onRefresh()
                            Toast.makeText(context, "Собаку додано!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Помилка: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }) { Text("ЗБЕРЕГТИ") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("ВІДМІНА") }
            }
        )
    }

    var showLinkDialog by remember { mutableStateOf<Int?>(null) }
    var deviceGuid by remember { mutableStateOf("") }

    if (showLinkDialog != null) {
        AlertDialog(
            onDismissRequest = { showLinkDialog = null },
            title = { Text("ПРИВ'ЯЗАТИ ПРИСТРІЙ") },
            text = {
                Column {
                    Text("Введіть GUID пристрою (наприклад: SIM-DOG-001)")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(value = deviceGuid, onValueChange = { deviceGuid = it }, label = { Text("GUID пристрою") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        try {
                            val response = NetworkModule.apiService.assignDevice(deviceGuid, AssignDeviceDto(showLinkDialog!!))
                            Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                            showLinkDialog = null
                            deviceGuid = ""
                            onRefresh()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Помилка: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }) { Text("ПРИВ'ЯЗАТИ") }
            },
            dismissButton = {
                TextButton(onClick = { showLinkDialog = null }) { Text("ВІДМІНА") }
            }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "МОЇ УЛЮБЛЕНЦІ", fontWeight = FontWeight.Black, fontSize = 20.sp)
                Button(onClick = { showAddDialog = true }) {
                    Text("ДОДАТИ")
                }
            }
        }

        items(dogs) { dog ->
            BrutalCard(backgroundColor = Color.White, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(60.dp).background(SecondaryCyan).border(2.dp, BrutalBlack), contentAlignment = Alignment.Center) {
                            Text("🐶", fontSize = 24.sp)
                        }
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(text = dog.name.uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Text(text = dog.breed, fontWeight = FontWeight.Black, color = PrimaryRed, fontSize = 12.sp)
                        }
                    }
                    Row {
                        Button(
                            onClick = { showLinkDialog = dog.id },
                            colors = ButtonDefaults.buttonColors(containerColor = TertiaryYellow),
                            modifier = Modifier.size(width = 50.dp, height = 40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("🔗", color = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        NetworkModule.apiService.deleteDog(dog.id)
                                        onRefresh()
                                        Toast.makeText(context, "Видалено", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Помилка", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                            modifier = Modifier.size(width = 50.dp, height = 40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("❌", color = Color.White)
                        }
                    }
                }
            }
        }
        }
}

@Composable
fun ProfileField(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray)
        Text(text = value.uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Black)
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
    }
}
