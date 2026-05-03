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
import com.example.mydogspace.network.UserForRegistrationDto
import com.example.mydogspace.ui.components.BrutalButton
import com.example.mydogspace.ui.components.BrutalCard
import com.example.mydogspace.ui.navigation.Screen
import com.example.mydogspace.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var adminCode by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(QuaternaryPink)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "РЕЄСТРАЦІЯ",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = BrutalBlack,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            BrutalCard(backgroundColor = Color.White, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("ІМ'Я", fontWeight = FontWeight.Black) },
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

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = adminCode,
                    onValueChange = { adminCode = it },
                    label = { Text("КОД АДМІНА (ОПЦІОНАЛЬНО)", fontWeight = FontWeight.Black) },
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
                    text = "СТВОРИТИ АКАУНТ",
                    backgroundColor = TertiaryYellow,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val trimmedUsername = username.trim()
                        val trimmedEmail = email.trim()
                        val trimmedPassword = password.trim()
                        val trimmedAdminCode = adminCode.trim().ifBlank { null }

                        if (trimmedUsername.isBlank() || trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
                            error = "Заповніть всі обов'язкові поля"
                            return@BrutalButton
                        }

                        scope.launch {
                            try {
                                NetworkModule.apiService.register(
                                    UserForRegistrationDto(trimmedUsername, trimmedEmail, trimmedPassword, trimmedAdminCode)
                                )
                                navController.navigate(Screen.Login.route)
                            } catch (e: retrofit2.HttpException) {
                                error = "Помилка сервера: ${e.code()}"
                            } catch (e: Exception) {
                                error = "Помилка з'єднання: ${e.localizedMessage}"
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            BrutalButton(
                text = "ВЖЕ Є АКАУНТ? УВІЙТИ",
                backgroundColor = SecondaryCyan,
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(Screen.Login.route) }
            )
        }
    }
}
