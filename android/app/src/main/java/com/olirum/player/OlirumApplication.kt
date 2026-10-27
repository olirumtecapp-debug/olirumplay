package com.olirum.player

import android.app.Application
import com.olirum.player.data.local.AppDatabase
import com.olirum.player.data.local.MediaStoreScanner
import com.olirum.player.data.repository.MusicRepository
import com.olirum.player.data.repository.UserPreferencesRepository

class OlirumApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var musicRepository: MusicRepository
        private set

    lateinit var preferencesRepository: UserPreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getDatabase(this)
        val scanner = MediaStoreScanner(this)

        preferencesRepository = UserPreferencesRepository(this)
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
