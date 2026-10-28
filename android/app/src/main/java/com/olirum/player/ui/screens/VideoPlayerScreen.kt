package com.olirum.player.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.olirum.player.data.remote.youtube.VideoClip
import com.olirum.player.ui.theme.*
import com.olirum.player.ui.viewmodel.MainPlayerViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoPlayerScreen(
    video: VideoClip,
    mainPlayerViewModel: MainPlayerViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalOlirumColors.current

    // Pausa automaticamente o player de áudio local ao abrir o videoclipe
    DisposableEffect(video.id) {
        mainPlayerViewModel.pause()
        onDispose { }
    }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.apply {
                loadUrl("about:blank")
                stopLoading()
                destroy()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Barra superior com botão fechar e detalhes do canal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Fechar Player",
                        tint = colors.textPrimary
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = video.title,
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = video.channelTitle,
                        color = OlirumCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, video.title)
                            putExtra(Intent.EXTRA_TEXT, "${video.title} - ${video.youtubeUrl}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Videoclipe"))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Compartilhar",
                        tint = colors.textSecondary
                    )
                }
            }

            // Container 16:9 para o Player Oficial IFrame do YouTube
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setBackgroundColor(0xFF070B14.toInt())

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                allowFileAccess = false
                                allowContentAccess = false
                            }

                            webChromeClient = WebChromeClient()
                            webViewClient = WebViewClient()

                            val embedHtml = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                  <style>
                                    body { margin: 0; padding: 0; background-color: #070B14; display: flex; align-items: center; justify-content: center; height: 100vh; overflow: hidden; }
                                    .video-wrapper { position: relative; width: 100%; padding-bottom: 56.25%; height: 0; }
                                    .video-wrapper iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: 0; }
                                  </style>
                                </head>
                                <body>
                                  <div class="video-wrapper">
                                    <iframe id="ytplayer" type="text/html" width="100%" height="100%"
                                      src="https://www.youtube.com/embed/${video.id}?enablejsapi=1&autoplay=1&playsinline=1&rel=0&modestbranding=1"
                                      frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                                      allowfullscreen>
                                    </iframe>
                                  </div>
                                </body>
                                </html>
                            """.trimIndent()

                            loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "UTF-8", null)
                            webViewInstance = this
                        }
                    }
                )
            }

            // Informações e Ações Oficiais abaixo do vídeo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    text = video.title,
                    color = colors.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = video.channelTitle,
                        color = colors.textSecondary,
                        fontSize = 14.sp
                    )

                    if (video.durationFormatted != "--:--") {
                        Text(
                            text = " • ",
                            color = colors.textSecondary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = video.durationFormatted,
                            color = OlirumCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botão de Destaque: Abrir no YouTube Oficial
                Button(
                    onClick = {
                        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(video.youtubeUrl))
                        context.startActivity(appIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444) // Vermelho YouTube oficial
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.OpenInNew,
                        contentDescription = "Abrir no YouTube",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Assistir no App do YouTube",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card de Conformidade e Direitos
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = "Verificado",
                            tint = OlirumCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reprodução oficial via YouTube IFrame Player API. Todos os direitos e créditos pertencem aos respectivos artistas e ao YouTube.",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}
