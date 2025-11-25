package com.example.lab_week_11_b

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.util.concurrent.Executor

class ProviderFileManager(
    private val context: Context,
    private val fileHelper: FileHelper,
    private val contentResolver: ContentResolver,
    private val executor: Executor,
    private val mediaContentHelper: MediaContentHelper
) {

    fun generatePhotoUri(time: Long): FileInfo {
        val name = "img_$time.jpg"
        val file = File(
            context.getExternalFilesDir(fileHelper.getPicturesFolder()),
            name
        )
        return FileInfo(
            fileHelper.getUriFromFile(file),
            file,
            name,
            fileHelper.getPicturesFolder(),
            "image/jpeg"
        )
    }

    fun generateVideoUri(time: Long): FileInfo {
        val name = "video_$time.mp4"
        val file = File(
            context.getExternalFilesDir(fileHelper.getVideosFolder()),
            name
        )
        return FileInfo(
            fileHelper.getUriFromFile(file),
            file,
            name,
            fileHelper.getVideosFolder(),
            "video/mp4"
        )
    }

    fun insertImageToStore(fileInfo: FileInfo?) {
        fileInfo?.let {
            Log.d("ProviderFileManager", "Insert image to store: ${it.name}")
            insertToStore(it, mediaContentHelper.getImageContentUri(),
                mediaContentHelper.generateImageContentValues(it)
            )
            android.os.Handler(context.mainLooper).post {
                android.widget.Toast.makeText(context, "Image saved", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun insertVideoToStore(fileInfo: FileInfo?) {
        fileInfo?.let {
            Log.d("ProviderFileManager", "Insert video to store: ${it.name}")
            insertToStore(it, mediaContentHelper.getVideoContentUri(),
                mediaContentHelper.generateVideoContentValues(it)
            )
            android.os.Handler(context.mainLooper).post {
                android.widget.Toast.makeText(context, "Video saved", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun insertToStore(fileInfo: FileInfo, contentUri: Uri, contentValues: ContentValues) {
        executor.execute {
            Log.d("ProviderFileManager", "Inserting file: ${fileInfo.name} into $contentUri")
            val insertedUri = contentResolver.insert(contentUri, contentValues)
            insertedUri?.let { uri ->
                val inputStream = contentResolver.openInputStream(fileInfo.uri)
                val outputStream = contentResolver.openOutputStream(uri)

                if (inputStream != null && outputStream != null) {
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    Log.d("ProviderFileManager", "File copied to store: $uri")
                } else {
                    Log.e("ProviderFileManager", "Stream null, cannot copy file: ${fileInfo.name}")
                }
            } ?: run {
                Log.e("ProviderFileManager", "Failed to insert into MediaStore for ${fileInfo.name}")
            }
        }
    }
}
