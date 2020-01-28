package com.example.lab7_start_service_app

import android.app.Service
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import kotlinx.coroutines.*
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random


class DownloadImageService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("DownloadImageService", "service is called")
        if (intent != null) {
            coroutineScope.launch(Dispatchers.IO) {
                handleWork(intent)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private suspend fun handleWork(receivedIntent: Intent) {
        val url = receivedIntent.getStringExtra("EXTRA_URL")
        if (url != null) {
            Log.i(
                "DownloadImageService",
                "is image downloading in main thread ${Looper.myLooper() == Looper.getMainLooper()}"
            )
            var path = ""
            val originalBitmap =
                downloadImageAsync(url).await()
            val cw = ContextWrapper(applicationContext)
            val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
            var file = File(directory, "downloadedimg" + Random.nextInt(0, 10000) + ".jpg")
            while (file.exists()) {
                file = File(directory, "downloadedimg" + Random.nextInt(0, 10000) + ".jpg")
            }
            path = file.toString()
            Log.i("DownloadImageService", "Path is $path")
            val fos: FileOutputStream?
            try {
                fos = FileOutputStream(file)
                originalBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                Log.i("DownloadImageService", "error occurred")
                e.printStackTrace()
            }
            if (path != "") {
                Intent().also { intent ->
                    intent.action = "com.example.lab7_start_service_app.IMAGE_DOWNLOAD_COMPLETE"
                    intent.putExtra("PATH_TO_IMAGE", path)
                    sendBroadcast(intent)
                    Log.i("DownloadImageService", "broadcast sent")
                }
            }
        }
        stopSelf()
    }

    private val parentJob = Job()

    private val coroutineScope = CoroutineScope(Dispatchers.Main + parentJob)


    private fun downloadImageAsync(url: String): Deferred<Bitmap?> =
        coroutineScope.async(Dispatchers.IO) {
            return@async try {
                val imageUrl = URL(url)
                val conn = imageUrl.openConnection() as HttpsURLConnection
                conn.doInput = true
                conn.connect()
                val stream = conn.inputStream
                BitmapFactory.decodeStream(stream)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }

        }
}