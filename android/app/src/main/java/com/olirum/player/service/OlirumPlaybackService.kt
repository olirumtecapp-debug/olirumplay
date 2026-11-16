package com.olirum.player.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.PowerManager
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.olirum.player.MainActivity
import com.olirum.player.OlirumPlayerWidget
import com.olirum.player.data.local.MediaStoreScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OlirumPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null
    private var reconnectJob: Job? = null
    private var retryCount = 0

    companion object {
        var instance: OlirumPlaybackService? = null
            private set
        var currentPlayer: ExoPlayer? = null
            private set
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        instance = this

        // Inicializa locks dedicados para blindar a reprodução contínua com tela desligada
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "OlirumPlay:AudioWakeLock")
            wakeLock?.setReferenceCounted(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "OlirumPlay:AudioWifiLock")
            wifiLock?.setReferenceCounted(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Configura atributos de áudio profissionais com foco de áudio automático (pausa em ligações)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true) // Lida com AudioFocus
            .setHandleAudioBecomingNoisy(true) // Pausa ao desconectar fones
            .setWakeMode(C.WAKE_MODE_NETWORK) // WakeLock nativo do ExoPlayer para streaming
            .build()

        currentPlayer = player

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()

        // Listener ativo para sincronizar locks, widget e recuperação automática de falhas
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateLocks(isPlaying)
                if (isPlaying) {
                    retryCount = 0
                    reconnectJob?.cancel()
                }
                notifyWidget()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                retryCount = 0
                reconnectJob?.cancel()
                notifyWidget()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    updateLocks(false)
                }
                notifyWidget()
            }

            override fun onPlayerError(error: PlaybackException) {
                handlePlaybackError(error)
            }
        })
    }

    private fun updateLocks(isPlaying: Boolean) {
        try {
            if (isPlaying) {
                if (wakeLock?.isHeld == false) {
                    wakeLock?.acquire(24 * 60 * 60 * 1000L) // Seguro com timeout longo
                }
                if (wifiLock?.isHeld == false) {
                    wifiLock?.acquire()
                }
            } else {
                if (wakeLock?.isHeld == true) {
                    wakeLock?.release()
                }
                if (wifiLock?.isHeld == true) {
                    wifiLock?.release()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handlePlaybackError(error: PlaybackException) {
        val currentItem = player.currentMediaItem
        val isRadio = currentItem?.mediaMetadata?.albumTitle?.toString()?.contains("Rádio", ignoreCase = true) == true
                || player.mediaItemCount <= 1

        if (isRadio && retryCount < 5) {
            retryCount++
            val delayMs = (retryCount * 1500L).coerceAtMost(6000L)
            reconnectJob?.cancel()
            reconnectJob = CoroutineScope(Dispatchers.Main).launch {
                delay(delayMs)
                if (::player.isInitialized) {
                    try {
                        player.prepare()
                        player.play()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            // Música local com erro/arquivo ilegível: pula imediatamente para a próxima sem travar o usuário
            CoroutineScope(Dispatchers.Main).launch {
                if (::player.isInitialized) {
                    if (player.hasNextMediaItem()) {
                        player.seekToNextMediaItem()
                        player.prepare()
                        player.play()
                    } else if (player.repeatMode == Player.REPEAT_MODE_ALL && player.mediaItemCount > 0) {
                        player.seekToDefaultPosition(0)
                        player.prepare()
                        player.play()
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action != null) {
            handleWidgetAction(action)
        }
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED) {
            updateLocks(false)
            stopSelf()
        }
    }

    private fun handleWidgetAction(action: String) {
        if (!::player.isInitialized) return

        when (action) {
            OlirumPlayerWidget.ACTION_WIDGET_PLAY_PAUSE -> {
                if (player.isPlaying) {
                    player.pause()
                } else if (player.mediaItemCount > 0) {
                    player.play()
                } else {
                    // Se não há faixa na fila, carrega músicas locais e toca
                    loadAndPlayLocalTracks()
                }
            }
            OlirumPlayerWidget.ACTION_WIDGET_PREV -> {
                if (player.currentPosition > 3000) {
                    player.seekTo(0)
                } else if (player.hasPreviousMediaItem()) {
                    player.seekToPreviousMediaItem()
                }
            }
            OlirumPlayerWidget.ACTION_WIDGET_NEXT -> {
                if (player.hasNextMediaItem()) {
                    player.seekToNextMediaItem()
                }
            }
        }
    }

    private fun loadAndPlayLocalTracks() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val scanner = MediaStoreScanner(applicationContext)
                val tracks = scanner.scanLocalTracks()
                if (tracks.isNotEmpty()) {
                    val mediaItems = tracks.map { track ->
                        MediaItem.Builder()
                            .setMediaId(track.id)
                            .setUri(track.mediaUri)
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle(track.title)
                                    .setArtist(track.artist)
                                    .setAlbumTitle(track.album)
                                    .setArtworkUri(if (!track.artworkUri.isNullOrBlank()) Uri.parse(track.artworkUri) else null)
                                    .build()
                            )
                            .build()
                    }
                    withContext(Dispatchers.Main) {
                        if (::player.isInitialized) {
                            player.setMediaItems(mediaItems)
                            player.prepare()
                            player.play()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        val launchIntent = Intent(applicationContext, MainActivity::class.java).apply {
                            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        applicationContext.startActivity(launchIntent)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun notifyWidget() {
        if (!::player.isInitialized) return
        val item = player.currentMediaItem
        val title = item?.mediaMetadata?.title?.toString() ?: "OlirumPlay"
        val artist = item?.mediaMetadata?.artist?.toString() ?: "Toque para ouvir música"
        val isPlaying = player.isPlaying
        val artworkUri = item?.mediaMetadata?.artworkUri?.toString()

        OlirumPlayerWidget.updateAllWidgets(
            applicationContext,
            title,
            artist,
            isPlaying,
            artworkUri
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        updateLocks(false)
        reconnectJob?.cancel()
        reconnectJob = null
        currentPlayer = null
        instance = null
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
