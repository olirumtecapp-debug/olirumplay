package com.olirum.player.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.olirum.player.OlirumApplication
import com.olirum.player.ui.components.MiniPlayer
import com.olirum.player.ui.screens.*
import com.olirum.player.ui.theme.*
import com.olirum.player.ui.viewmodel.MainPlayerViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Início", Icons.Rounded.Home)
    object Explore : Screen("explore", "Explorar", Icons.Rounded.Explore)
    object Radios : Screen("radios", "Rádios", Icons.Rounded.Radio)
    object Videos : Screen("videos", "Vídeos", Icons.Rounded.SmartDisplay)
    object Library : Screen("library", "Biblioteca", Icons.Rounded.LibraryMusic)
    object Device : Screen("device", "Músicas", Icons.Rounded.FolderOpen)
    object Lyrics : Screen("lyrics", "Letras", Icons.Rounded.Mic)
    object Widgets : Screen("widgets", "Ajustes", Icons.Rounded.GraphicEq)
}

@Composable
fun OlirumNavGraph(
    viewModel: MainPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalOlirumColors.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()

    var showFullPlayer by remember { mutableStateOf(false) }

    // 4 abas principais da navegação inferior conforme a nova especificação
    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Explore,
        Screen.Radios,
        Screen.Videos,
        Screen.Library
    )

    val progressFraction = remember(currentPositionMs, durationMs) {
        if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = colors.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = false
                                        }
                                        launchSingleTop = true
                                        restoreState = false
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OlirumCyan,
                                selectedTextColor = OlirumCyan,
                                indicatorColor = OlirumCyan.copy(alpha = 0.2f),
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary
                            ),
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToDevice = { navController.navigate(Screen.Device.route) },
                            onNavigateToExplore = { navController.navigate(Screen.Explore.route) },
                            onNavigateToRadios = { navController.navigate(Screen.Radios.route) },
                            onNavigateToLibrary = { navController.navigate(Screen.Library.route) },
                            onNavigateToWidgets = { navController.navigate(Screen.Widgets.route) },
                            onNavigateToVideos = { navController.navigate(Screen.Videos.route) },
                            onCategoryClick = { cat ->
                                navController.navigate("category/${cat.id}")
                            }
                        )
                    }
                    composable(Screen.Explore.route) {
                        ExploreScreen(
                            viewModel = viewModel,
                            onCategoryClick = { cat ->
                                navController.navigate("category/${cat.id}")
                            }
                        )
                    }
                    composable(Screen.Radios.route) {
                        RadiosScreen(
                            viewModel = viewModel,
                            onCategoryClick = { cat ->
                                navController.navigate("category/${cat.id}")
                            }
                        )
                    }
                    composable(Screen.Videos.route) {
                        VideoclipesScreen(mainPlayerViewModel = viewModel)
                    }
                    composable(Screen.Library.route) {
                        LibraryScreen(
                            viewModel = viewModel,
                            onNavigateToDevice = { navController.navigate(Screen.Device.route) }
                        )
                    }
                    composable(Screen.Device.route) {
                        DeviceLibraryScreen(viewModel = viewModel)
                    }
                    composable(
                        route = "category/{categoryId}",
                        arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                        val category = OlirumApplication.instance.radioRepository.categories.find { it.id == categoryId }
                            ?: OlirumApplication.instance.radioRepository.categories.first()
                        CategoryStationsScreen(
                            category = category,
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.Lyrics.route) {
                        LyricsScreen(viewModel = viewModel)
                    }
                    composable(Screen.Widgets.route) {
                        WidgetsScreen(viewModel = viewModel)
                    }
                }

                // MiniPlayer persistente flutuante
                AnimatedVisibility(
                    visible = currentTrack != null && !showFullPlayer,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    MiniPlayer(
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        progressFraction = progressFraction,
                        onPlayPauseClick = { viewModel.togglePlayPause() },
                        onSkipNextClick = { viewModel.skipNext() },
                        onClick = { showFullPlayer = true }
                    )
                }
            }
        }

        // Full Player Modal
        if (showFullPlayer && currentTrack != null) {
            FullPlayerScreen(
                viewModel = viewModel,
                onDismiss = { showFullPlayer = false },
                onNavigateToLyrics = {
                    showFullPlayer = false
                    navController.navigate(Screen.Lyrics.route)
                }
            )
        }
    }
}
