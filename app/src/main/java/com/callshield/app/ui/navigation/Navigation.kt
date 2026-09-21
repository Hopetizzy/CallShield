package com.callshield.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.callshield.app.ui.MainViewModel
import com.callshield.app.ui.screens.DashboardScreen
import com.callshield.app.ui.screens.RulesScreen
import com.callshield.app.ui.screens.SandboxScreen
import com.callshield.app.ui.screens.ThreatLogScreen
import com.callshield.app.ui.theme.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Shield", Icons.Filled.Shield)
    object Rules : Screen("rules", "Matrix", Icons.Filled.FilterAlt)
    object Threats : Screen("threats", "Ledger", Icons.Filled.ListAlt)
    object Sandbox : Screen("sandbox", "Sandbox", Icons.Filled.Science)
}

@Composable
fun MainNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = CyberBackground,
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(24.dp))
            ) {
                val items = listOf(
                    Screen.Dashboard,
                    Screen.Rules,
                    Screen.Threats,
                    Screen.Sandbox
                )

                items.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = if (isSelected) NeonCyan else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = screen.title.uppercase(),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonCyan else TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CyberSurfaceElevated
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToRules = { navController.navigate(Screen.Rules.route) },
                    onNavigateToThreats = { navController.navigate(Screen.Threats.route) },
                    onNavigateToSandbox = { navController.navigate(Screen.Sandbox.route) }
                )
            }
            composable(Screen.Rules.route) {
                RulesScreen(viewModel = viewModel)
            }
            composable(Screen.Threats.route) {
                ThreatLogScreen(viewModel = viewModel)
            }
            composable(Screen.Sandbox.route) {
                SandboxScreen(viewModel = viewModel)
            }
        }
    }
}
