package com.alarm.clock.reminder.alarmclock.simplealarm.data.local

import android.content.Context
import androidx.annotation.RawRes
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.convertRawToPath
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

data class AudioModel(
    @RawRes
    var raw: Int? = null,
    var aPath: String? = null,
    var aName: String? = null,
    var aAlbum: String? = null,
    var aArtist: String? = null,
    var realPath: String? = null
) {
    fun handlePath(context: Context) {
        realPath = this.getPath(context)
    }
}


fun AudioModel.getPath(context: Context): String {
    return aPath ?: raw?.convertRawToPath(context)
        .toString()
}

fun getPathFileFromRaw(raw: Int, context: Context, aName: String): String {
    val inputStream: InputStream = context.resources.openRawResource(raw)
    val outputFile = File(context.filesDir, aName)
    val outputStream: OutputStream = FileOutputStream(outputFile)

    val buffer = ByteArray(1024)
    var read: Int
    while (inputStream.read(buffer).also { read = it } != -1) {
        outputStream.write(buffer, 0, read)
    }

    outputStream.flush()
    outputStream.close()
    inputStream.close()
    return outputFile.absolutePath
}