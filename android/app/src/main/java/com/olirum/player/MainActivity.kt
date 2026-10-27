package com.olirum.player

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.olirum.player.ui.navigation.OlirumNavGraph
import com.olirum.player.ui.theme.OlirumPlayTheme
import com.olirum.player.ui.viewmodel.MainPlayerViewModel

class MainActivity : ComponentActivity() {

    private val playerViewModel: MainPlayerViewModel by viewModels {
        val app = application as OlirumApplication
        MainPlayerViewModel.Factory(
            context = applicationContext,
            musicRepository = app.musicRepository,
            prefsRepository = app.preferencesRepository
        )
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Notificação autorizada para exibição dos controles na tela de bloqueio e barra de status
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicita permissão para notificação no Android 13+ (necessário para controles na barra de status)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val isDarkMode by playerViewModel.isDarkMode.collectAsState()
            OlirumPlayTheme(darkTheme = isDarkMode) {
                OlirumNavGraph(viewModel = playerViewModel)
            }
        }
    }
}
