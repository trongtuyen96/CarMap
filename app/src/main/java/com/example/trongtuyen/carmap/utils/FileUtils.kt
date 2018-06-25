package com.example.trongtuyen.carmap.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaPlayer
import android.os.Environment
import android.util.Base64
import android.os.Environment.DIRECTORY_PICTURES
import android.os.Environment.getExternalStorageDirectory
import android.util.Log
import android.widget.Toast
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


object FileUtils {
    fun encodeAudioFile(path: String): String {
        val audioBytes: ByteArray
        try {
            // Just to check file size.. Its is correct i-e; Not Zero
//            val audioFile = File(path)
//            val fileSize = audioFile.length()

            val baos = ByteArrayOutputStream()
            val fis = FileInputStream(File(path))
            val buf = ByteArray(1024)
            var n: Int = fis.read(buf)
            while (-1 != n) {
                baos.write(buf, 0, n)
                n = fis.read(buf)
            }
            audioBytes = baos.toByteArray()

            // Here goes the Base64 string
            return Base64.encodeToString(audioBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun decodeAudioFile(base64AudioData: String, filePath: String) {
        val decoded: ByteArray = Base64.decode(base64AudioData, Base64.DEFAULT)
        val fos = FileOutputStream(filePath)
        fos.write(decoded)
        fos.close()

        try {
            val mp = MediaPlayer()
            mp.setDataSource(filePath)
            mp.prepare()
            mp.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun encodeImageFile(bitmap: Bitmap, type: String): String {
        val baos = ByteArrayOutputStream()
        // Dùng với thumbnail khi lấy thẳng data từ result

        if (type == "normal") {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        }
        if (type == "large") {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        }
        val byte = baos.toByteArray()
        return Base64.encodeToString(byte, Base64.DEFAULT)
    }

    fun decodeImageFile(base64ImageData: String): Bitmap {
        val decoded: ByteArray = Base64.decode(base64ImageData, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
    }


    fun playAudio(filePath: String) {
        try {
            val mp = MediaPlayer()
            mp.setDataSource(filePath)
            mp.prepare()
            mp.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}