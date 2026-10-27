package com.olirum.player.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olirum.player.R
import com.olirum.player.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val iconScale = remember { Animatable(0.75f) }
    val logoAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        iconScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
        delay(900)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B12)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Ícone Oficial Neon de Áudio com Play
            Image(
                painter = painterResource(id = R.drawable.olirum_icon_play),
                contentDescription = "Ícone OlirumPlay",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(130.dp)
                    .scale(iconScale.value)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Logotipo Oficial Horizontal 3D
            Image(
                painter = painterResource(id = R.drawable.olirum_logo_3d),
                contentDescription = "OlirumPlay 3D",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .height(68.dp)
                    .alpha(logoAlpha.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Linha equalizadora com degradê neon
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(OlirumGradientBrush)
                    .alpha(logoAlpha.value)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "MÚSICA EM ALTA FIDELIDADE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.85f),
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(logoAlpha.value)
            )
        }
    }
}
