package com.example.trongtuyen.carmap.activity.common

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.utils.FileUtils
import com.sdsmdg.tastytoast.TastyToast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.jar.Manifest

class CustomCameraActivity : AppCompatActivity() {
    companion object {
        private const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 1
    }

    @BindView(R.id.imCamera_custom_camera)
    lateinit var imCamera: ImageView
    @BindView(R.id.imBack_custom_camera)
    lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_camera)

        ButterKnife.bind(this)
        initComponents()
    }

    private fun initComponents() {
        btnBack.setOnClickListener {
            finish()
        }
        if (intent.getStringExtra("base64Image") != null) {
            val base64Image = intent.getStringExtra("base64Image")
            val newBitmap = FileUtils.decodeImageFile(base64Image)
            imCamera.setImageBitmap(newBitmap)

//            var decodedFile : File = savebitmap(newBitmap)

        }
        if (intent.getParcelableExtra<Uri>("imageUri") != null){
            val imageStream = contentResolver.openInputStream(intent.getParcelableExtra<Uri>("imageUri"))
            val bitmap = BitmapFactory.decodeStream(imageStream)
//            Toast.makeText(this, "BEFORE: " + bitmap.density.toString() + " " + bitmap.width.toString() + " " + bitmap.height.toString(), Toast.LENGTH_SHORT).show()
            val matrix = Matrix()
            matrix.postRotate(90f)
            val newBitmap: Bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            imCamera.setImageBitmap(newBitmap)
        }
//        Toast.makeText(this,newBitmap.density.toString() + " " + newBitmap.height.toString() + " " + newBitmap.width.toString(), Toast.LENGTH_SHORT).show()

    }

    @Throws(IOException::class)
    fun savebitmap(bmp: Bitmap): File {
        val bytes = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val f = File(Environment.getExternalStorageDirectory().toString()
                + File.separator + "testimage.jpg")
        f.createNewFile()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permission = ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)
            if (permission == PackageManager.PERMISSION_GRANTED) {
                Log.i("PermissionStorage", "Permission to record enabled")
                val fo = FileOutputStream(f)
                fo.write(bytes.toByteArray())
                fo.close()
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        READ_EXTERNAL_STORAGE_REQUEST_CODE)
            }
        }

        return f
    }
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        val bitmap: Bitmap = data!!.extras.get("data") as Bitmap
//        val matrix = Matrix()
//        matrix.postRotate(90f)
//        val pBitmap: Bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//        imCamera.setImageBitmap(pBitmap)
//    }
}
