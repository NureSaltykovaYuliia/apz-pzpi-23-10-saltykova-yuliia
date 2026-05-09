package com.example.mydogspace.ui.screens

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
import com.example.mydogspace.network.SessionManager
import com.example.mydogspace.network.UserForLoginDto
import com.example.mydogspace.ui.components.BrutalButton
import com.example.mydogspace.ui.components.BrutalCard
import com.example.mydogspace.ui.navigation.Screen
import com.example.mydogspace.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showServerSettings by remember { mutableStateOf(false) }
    var serverUrlInput by remember { mutableStateOf(NetworkModule.BASE_URL) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SecondaryCyan)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "MY DOG SPACE",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = BrutalBlack,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            BrutalCard(backgroundColor = Color.White, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ВХІД В АКАУНТ",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    )
                    IconButton(onClick = {
                        showServerSettings = !showServerSettings
                        if (showServerSettings) serverUrlInput = NetworkModule.BASE_URL
                    }) {
                        Text("⚙️", fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Панель налаштувань сервера (розгортається за ⚙️)
                if (showServerSettings) {
                    BrutalCard(backgroundColor = Color(0xFFF0F0F0), modifier = Modifier.fillMaxWidth()) {
                        Text("URL СЕРВЕРА", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = serverUrlInput,
                            onValueChange = { serverUrlInput = it },
                            label = { Text("https://... або http://10.0.2.2:5000/", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrutalBlack,
                                unfocusedBorderColor = BrutalBlack
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BrutalButton(
                                text = "ЗБЕРЕГТИ",
                                backgroundColor = TertiaryYellow,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val url = serverUrlInput.trim().let {
                                        if (it.endsWith("/")) it else "$it/"
                                    }
                                    SessionManager.serverUrl = url
                                    NetworkModule.resetClient()
                                    showServerSettings = false
                                    error = "URL змінено: $url"
                                }
                            )
                            BrutalButton(
                                text = "ЕМУЛЯТОР",
                                backgroundColor = QuaternaryPink,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    serverUrlInput = "http://10.0.2.2:5000/"
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("EMAIL", fontWeight = FontWeight.Black) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrutalBlack,
                        unfocusedBorderColor = BrutalBlack,
                        focusedLabelColor = BrutalBlack,
                        cursorColor = BrutalBlack
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("ПАРОЛЬ", fontWeight = FontWeight.Black) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrutalBlack,
                        unfocusedBorderColor = BrutalBlack,
                        focusedLabelColor = BrutalBlack,
                        cursorColor = BrutalBlack
                    )
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = if (error!!.startsWith("URL змінено")) Color(0xFF2E7D32) else PrimaryRed,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                BrutalButton(
                    text = "УВІЙТИ",
                    backgroundColor = TertiaryYellow,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val trimmedEmail = email.trim()
                        val trimmedPassword = password.trim()

                        if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
                            error = "Заповніть всі поля"
                            return@BrutalButton
                        }

                        scope.launch {
                            try {
                                val response = NetworkModule.apiService.login(
                                    UserForLoginDto(trimmedEmail, trimmedPassword)
                                )
                                SessionManager.token = response.token
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            } catch (e: retrofit2.HttpException) {
                                error = when (e.code()) {
                                    401 -> "Невірний email або пароль"
                                    400 -> "Помилка 400 (Bad Request). Перевірте URL в налаштуваннях ⚙️.\nМожливо, домен Localtunnel застарів.\nПоточний: ${NetworkModule.BASE_URL}"
                                    404 -> "Помилка 404 (Not Found).\nСервер не знайдено за адресою:\n${NetworkModule.BASE_URL}"
                                    else -> "Помилка сервера: ${e.code()} — ${e.message()}\nURL: ${NetworkModule.BASE_URL}"
                                }
                            } catch (e: Exception) {
                                error = "Помилка з'єднання: ${e.localizedMessage}\nПеревірте URL в налаштуваннях ⚙️.\nПоточний: ${NetworkModule.BASE_URL}"
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            BrutalButton(
                text = "ЗАРЕЄСТРУВАТИСЯ",
                backgroundColor = QuaternaryPink,
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(Screen.Register.route) }
            )
        }
    }
}
