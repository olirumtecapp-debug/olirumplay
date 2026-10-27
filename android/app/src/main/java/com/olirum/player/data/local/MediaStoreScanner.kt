package com.olirum.player.data.local

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.olirum.player.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreScanner(private val context: Context) {

    suspend fun scanLocalTracks(): List<Track> = withContext(Dispatchers.IO) {
        val trackList = mutableListOf<Track>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 15000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Sem Título"
                    val artist = cursor.getString(artistCol).takeIf { it != "<unknown>" } ?: "Artista Desconhecido"
                    val album = cursor.getString(albumCol).takeIf { it != "<unknown>" } ?: "Álbum do Dispositivo"
                    val durationMs = cursor.getLong(durCol)
                    val size = cursor.getLong(sizeCol)
                    val albumId = cursor.getLong(albumIdCol)

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()
                    val artworkUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId).toString()

                    trackList.add(
                        Track(
                            id = "local-$id",
                            title = title,
                            artist = artist,
                            album = album,
                            durationMs = durationMs,
                            mediaUri = contentUri,
                            artworkUri = artworkUri,
                            source = "local",
                            fileSize = size
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        trackList
    }
}
