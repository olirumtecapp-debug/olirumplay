package com.olirum.player

import android.app.Application
import com.olirum.player.data.local.AppDatabase
import com.olirum.player.data.local.MediaStoreScanner
import com.olirum.player.data.remote.RadioRepository
import com.olirum.player.data.repository.MusicRepository
import com.olirum.player.data.repository.UserPreferencesRepository

class OlirumApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var musicRepository: MusicRepository
        private set

    lateinit var radioRepository: RadioRepository
        private set

    lateinit var preferencesRepository: UserPreferencesRepository
        private set

    lateinit var youtubeRepository: com.olirum.player.data.remote.youtube.YouTubeRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getDatabase(this)
        val scanner = MediaStoreScanner(this)

        radioRepository = RadioRepository()
        preferencesRepository = UserPreferencesRepository(this)
        youtubeRepository = com.olirum.player.data.remote.youtube.YouTubeRepository(preferencesRepository)
        musicRepository = MusicRepository(
            context = this,
            database = database,
            scanner = scanner
        )
    }

    companion object {
        lateinit var instance: OlirumApplication
            private set
    }
}
