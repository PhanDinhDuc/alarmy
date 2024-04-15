package com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.AudioModel

object AudioUtils {
    //    private var tmpList: List<AudioModel>? = null
    fun getAllAudioFromDevice(context: Context, isMusic: Boolean): List<AudioModel> {
        var tmpList: List<AudioModel>? = null
        if (tmpList != null) return tmpList
        val tempAudioList: MutableList<AudioModel> = ArrayList<AudioModel>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.ArtistColumns.ARTIST,
        )

        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val cursor: Cursor? = if (isMusic) {
            context.contentResolver.query(
                uri,
                projection,
                selection,
                null,
                null
            )
        } else {
            context.contentResolver.query(uri, projection, null, null, null)
        }


        if (cursor != null) {
            while (cursor.moveToNext()) {
                val audioModel = AudioModel()
                val album =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM))
                val artist =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.ArtistColumns.ARTIST))
                val name =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME))
                val path =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA))
                audioModel.aName = name
                audioModel.aAlbum = album
                audioModel.aArtist = artist
                audioModel.aPath = path
                tempAudioList.add(audioModel)

            }
            cursor.close()
        }
        tmpList = tempAudioList
        return tempAudioList
    }

}