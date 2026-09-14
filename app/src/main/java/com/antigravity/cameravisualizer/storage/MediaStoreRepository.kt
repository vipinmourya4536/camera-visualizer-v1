package com.antigravity.cameravisualizer.storage

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MediaStoreRepository(private val context: Context) {
    fun savePhoto(bitmap: Bitmap, styleName: String): Uri? {
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
        val filename = "Visualizer_${styleName}_$timestamp.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Visualizer")
            }
        }

        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        
        uri?.let {
            var outputStream: OutputStream? = null
            try {
                outputStream = context.contentResolver.openOutputStream(it)
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                outputStream?.close()
            }
        }
        return uri
    }
}
