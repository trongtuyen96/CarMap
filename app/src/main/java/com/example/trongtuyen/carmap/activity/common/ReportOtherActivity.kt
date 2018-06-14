package com.example.trongtuyen.carmap.activity.common

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.R
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.openalpr.OpenALPR
import org.openalpr.model.Results
import org.openalpr.model.ResultsError
import java.io.File
import java.io.IOException

class ReportOtherActivity : AppCompatActivity() {

    @BindView(R.id.layoutTakePhoto_report_other)
    lateinit var btnTakePhoto: LinearLayout
    @BindView(R.id.layoutLicensePlate_report_other)
    lateinit var btnLicensePlate: LinearLayout
    @BindView(R.id.txtPlate_report_other)
    lateinit var txtPlate: EditText

    @BindView(R.id.btnSend_report_other)
    lateinit var btnSend: Button
    @BindView(R.id.btnDismiss_report_other)
    lateinit var btnDismiss: Button

    @BindView(R.id.imVerified_report_other)
    lateinit var imVerified: ImageView

    @BindView(R.id.imTakePhoto_report_other)
    lateinit var imTakePhoto: ImageView

    // ==== Dùng cho lấy chất lượng ảnh JPEG gốc, bằng cách chụp xong lưu file ảnh lại
    private lateinit var photoURI: Uri

    private var mCurrentPhotoPath: String = ""

    private var ANDROID_DATA_DIR: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_other)

        ButterKnife.bind(this)
        initComponents()
    }

    private fun initComponents() {

        ANDROID_DATA_DIR = this.applicationInfo.dataDir

        imVerified.visibility = View.INVISIBLE

        btnTakePhoto.setOnClickListener {

            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            // Xoá ảnh cũ
            if (mCurrentPhotoPath != "") {
                val oldFile = File(mCurrentPhotoPath)
                oldFile.delete()
            }
            // ==== Dùng cho lấy chất lượng ảnh JPEG gốc, bằng cách chụp xong lưu file ảnh lại
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                var photoFile: File? = null
                try {
                    photoFile = createImageFile();
                } catch (e: IOException) {
                    // Error occurred while creating the File
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    photoURI = FileProvider.getUriForFile(this,
                            "com.example.android.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, 1);
                }
            }
        }
        btnLicensePlate.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onALPR()
        }
        btnDismiss.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            // Xoá ảnh cũ
            if (mCurrentPhotoPath != "") {
                val oldFile = File(mCurrentPhotoPath)
                oldFile.delete()
            }
            finish()
        }
        btnSend.setOnClickListener() {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            // Xoá ảnh cũ
            if (mCurrentPhotoPath != "") {
                val oldFile = File(mCurrentPhotoPath)
                oldFile.delete()
            }
        }
        imVerified.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            val intent = Intent(this, CustomCameraActivity::class.java)
            intent.putExtra("imageUri", photoURI)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> {
                if (resultCode == Activity.RESULT_OK) {
                    imVerified.visibility = View.VISIBLE

                    // ==== Dùng cho lấy chất lượng ảnh JPEG gốc, bằng cách chụp xong lưu file ảnh lại
//                    val imageStream = contentResolver.openInputStream(photoURI)
//                    val bitmap = BitmapFactory.decodeStream(imageStream)
//                    Toast.makeText(this, "BEFORE: " + bitmap.density.toString() + " " + bitmap.width.toString() + " " + bitmap.height.toString(), Toast.LENGTH_SHORT).show()
//                    val matrix = Matrix()
//                    matrix.postRotate(90f)
//                    val newBitmap: Bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//                    Toast.makeText(this, "AFTER: " + newBitmap.density.toString() + " " + newBitmap.width.toString() + " " + newBitmap.height.toString(), Toast.LENGTH_LONG).show()
//
//                    imTakePhoto.setImageBitmap(newBitmap)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
//        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        val imageFileName = "JPEG_" + timeStamp + "_"
        val imageFileName = "image"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )

        Log.e("PATH", image.absolutePath)
//        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.absolutePath
        return image
    }

    private fun onALPR(){
        val progress = ProgressDialog.show(this, "Loading", "Parsing result...", true)
        val openAlprConfFile = ANDROID_DATA_DIR + File.separatorChar + "runtime_data" + File.separatorChar + "openalpr.conf"
        val options = BitmapFactory.Options()
        options.inSampleSize = 10

//                    resultTextView.setText("Processing")

        AsyncTask.execute {
            val result = OpenALPR.Factory.create(this, ANDROID_DATA_DIR).recognizeWithCountryRegionNConfig("eu", "", mCurrentPhotoPath, openAlprConfFile, 10)

            Log.d("OPEN ALPR", result)

            try {
                val results = Gson().fromJson(result, Results::class.java)
                runOnUiThread {
                    if (results == null || results.results == null || results.results.size == 0) {
                        Toast.makeText(this, "It was not possible to detect the licence plate.", Toast.LENGTH_LONG).show()
//                                    resultTextView.setText("It was not possible to detect the licence plate.")
                    } else {
                        Toast.makeText(this, "Plate: " + results.results[0].plate
                                // Trim confidence to two decimal places
                                + " Confidence: " + String.format("%.2f", results.results[0].confidence) + "%"
                                // Convert processing time to seconds and trim to two decimal places
                                + " Processing time: " + String.format("%.2f", results.processingTimeMs!! / 1000.0 % 60) + " seconds", Toast.LENGTH_LONG).show()

                        txtPlate.setText(results.results[0].plate.toString(), TextView.BufferType.EDITABLE)
                    }
                }

            } catch (exception: JsonSyntaxException) {
                val resultsError = Gson().fromJson(result, ResultsError::class.java)

                runOnUiThread {
                    //                                resultTextView.setText(resultsError.msg)
                    Toast.makeText(this, resultsError.msg, Toast.LENGTH_SHORT).show()
                }
            }

            progress.dismiss()
        }
    }
}
