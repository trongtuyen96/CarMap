package com.example.trongtuyen.carmap.activity.common

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.utils.FileUtils

class CustomCameraActivity : AppCompatActivity() {

    @BindView(R.id.imCamera_custom_camera)
    lateinit var imCamera: ImageView
    @BindView(R.id.btnOpen_custom_camera)
    lateinit var btnOpen: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_camera)

        ButterKnife.bind(this)
        initComponents()
    }

    private fun initComponents() {
        btnOpen.setOnClickListener {
//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            startActivityForResult(intent, 0)
        }
        val base64 = intent.getStringExtra("base64Image")
        val newBitmap = FileUtils.decodeImageFile(base64)
        imCamera.setImageBitmap(newBitmap)
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
