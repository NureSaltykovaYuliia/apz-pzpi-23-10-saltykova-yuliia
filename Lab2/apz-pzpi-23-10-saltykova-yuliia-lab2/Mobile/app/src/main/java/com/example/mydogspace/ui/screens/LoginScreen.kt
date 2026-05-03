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
                Text(
                    text = "ВХІД В АКАУНТ",
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

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
                        color = PrimaryRed,
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
                                val response = NetworkModule.apiService.login(UserForLoginDto(trimmedEmail, trimmedPassword))
                                SessionManager.token = response.token
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            } catch (e: retrofit2.HttpException) {
                                error = if (e.code() == 401) "Невірний email або пароль" 
                                        else "Помилка сервера: ${e.code()}"
                            } catch (e: Exception) {
                                error = "Помилка з'єднання: ${e.localizedMessage}"
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
