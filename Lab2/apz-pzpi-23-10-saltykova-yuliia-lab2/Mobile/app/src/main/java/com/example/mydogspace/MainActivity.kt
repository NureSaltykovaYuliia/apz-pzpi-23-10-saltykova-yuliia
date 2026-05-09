package com.example.mydogspace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.mydogspace.ui.navigation.NavGraph
import com.example.mydogspace.ui.theme.MyDogSpaceTheme

import com.example.mydogspace.network.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        SessionManager.init(this)
        
        // Встановлюємо кастомний OkHttpClient для завантаження картинок через Coil, 
        // щоб картинки також мали заголовок Bypass-Tunnel-Reminder та обробку помилок з state: 0
        val imageLoader = coil.ImageLoader.Builder(this)
            .okHttpClient(com.example.mydogspace.network.NetworkModule.client)
            .build()
        coil.Coil.setImageLoader(imageLoader)

        setContent {
            MyDogSpaceTheme(darkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}