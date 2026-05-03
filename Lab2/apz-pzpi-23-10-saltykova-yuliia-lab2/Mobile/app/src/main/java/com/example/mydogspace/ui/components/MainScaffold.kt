package com.example.mydogspace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mydogspace.ui.navigation.Screen
import com.example.mydogspace.ui.theme.*

data class NavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    title: String,
    navController: NavController,
    content: @Composable () -> Unit
) {
    val navItems = listOf(
        NavItem("Головна", Screen.Home.route, Icons.Default.Home),
        NavItem("Заходи", Screen.Events.route, Icons.Default.DateRange),
        NavItem("Партнери", Screen.Partners.route, Icons.Default.Place),
        NavItem("Профіль", Screen.Profile.route, Icons.Default.Person)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        title.uppercase(), 
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SecondaryCyan,
                    titleContentColor = BrutalBlack
                ),
                modifier = Modifier.drawBehind {
                    val strokeWidth = 2.dp.toPx()
                    drawLine(
                        color = BrutalBlack,
                        start = androidx.compose.ui.geometry.Offset(0f, size.height),
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                        strokeWidth = strokeWidth
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = BrutalWhite,
                modifier = Modifier.drawBehind {
                    val strokeWidth = 2.dp.toPx()
                    drawLine(
                        color = BrutalBlack,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                        strokeWidth = strokeWidth
                    )
                },
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry = navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry.value?.destination?.route

                navItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontWeight = FontWeight.Bold) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrutalBlack,
                            selectedTextColor = BrutalBlack,
                            indicatorColor = TertiaryYellow,
                            unselectedIconColor = BrutalBlack.copy(alpha = 0.6f),
                            unselectedTextColor = BrutalBlack.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.padding(innerPadding),
            color = Gray100
        ) {
            content()
        }
    }
}
