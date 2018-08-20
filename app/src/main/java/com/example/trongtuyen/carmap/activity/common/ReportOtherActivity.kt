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
import android.view.WindowManager
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.controllers.AppController
import com.example.trongtuyen.carmap.models.Report
import com.example.trongtuyen.carmap.services.APIServiceGenerator
import com.example.trongtuyen.carmap.services.ErrorUtils
import com.example.trongtuyen.carmap.services.ReportService
import com.example.trongtuyen.carmap.utils.AudioPlayer
import com.example.trongtuyen.carmap.utils.FileUtils
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.sdsmdg.tastytoast.TastyToast
import org.openalpr.OpenALPR
import org.openalpr.model.Results
import org.openalpr.model.ResultsError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
    @BindView(R.id.btnClose_report_other)
    lateinit var btnClose: ImageView

    @BindView(R.id.imVerified_report_other)
    lateinit var imVerified: ImageView

    @BindView(R.id.imTakePhoto_report_other)
    lateinit var imTakePhoto: ImageView
    @BindView(R.id.imLicensePlate_report_other)
    lateinit var imLicensePlate: ImageView

    // ==== Dùng cho lấy chất lượng ảnh JPEG gốc, bằng cách chụp xong lưu file ảnh lại
    private lateinit var photoURI: Uri

    private var mCurrentPhotoPath: String = ""

    private var ANDROID_DATA_DIR: String? = null

    private var sBase64Image: String = ""

    // Audio Player
    private var mAudioPlayer = AudioPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_other)

//        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        ButterKnife.bind(this)
        initComponents()
    }

    private fun initComponents() {

        ANDROID_DATA_DIR = this.applicationInfo.dataDir

        imVerified.visibility = View.INVISIBLE

        imTakePhoto.setOnClickListener {

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
        imLicensePlate.setOnClickListener {
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
        btnClose.setOnClickListener {
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

            AppController.licensePlate = txtPlate.text.toString()
            onSend()
            finish()
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

                    // Chỉ lấy thumbnail nên chất lượng ảnh không cao
//                    val bitmap: Bitmap = data!!.extras.get("data") as Bitmap
//                    Toast.makeText(this, "BEFORE: " + bitmap.density.toString() + " " + bitmap.height.toString() + " " + bitmap.width.toString(), Toast.LENGTH_SHORT).show()
//                    val matrix = Matrix()
//                    matrix.postRotate(90f)
//                    val newBitmap: Bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//                    Toast.makeText(this, "AFTER: " + newBitmap.density.toString() + " " + newBitmap.height.toString() + " " + newBitmap.width.toString(), Toast.LENGTH_SHORT).show()


//                    // Solution cần bảo tồn
//                    val options = BitmapFactory.Options()
//                    // Số inSampleSize là ảnh mới sẽ bằng 1 / inSampleSize của ảnh gốc, twucs chiều dài và rộng giảm đi inSampleSize lần
//
//
//                    options.inSampleSize = 8
//                    val imageStream = contentResolver.openInputStream(photoURI)
//
//
//                    val bitmap = BitmapFactory.decodeStream(imageStream, null, options)
//                    Toast.makeText(this, "BEFORE: " + bitmap.density.toString() + " " + bitmap.width.toString() + " " + bitmap.height.toString(), Toast.LENGTH_SHORT).show()
//                    val matrix = Matrix()
//                    matrix.postRotate(90f)
//                    val newBitmap: Bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//
//                    if (bitmap.density > 320) {
//                        sBase64Image = FileUtils.encodeImageFile(newBitmap, "large")
//                    } else {
//                        sBase64Image = FileUtils.encodeImageFile(newBitmap, "normal")
//                    }
//                    AppController.base64ImageReportOther = sBase64Image

                    val options = BitmapFactory.Options()
                    // Số inSampleSize là ảnh mới sẽ bằng 1 / inSampleSize của ảnh gốc, tức chiều dài và rộng giảm đi inSampleSize lần
                    // inJustRebound = true là sẽ đọc resource của ảnh chứ ko laod ảnh vào bộ nhớ, sẽ giảm bộ nhớ sử dụng

                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(mCurrentPhotoPath, options)
//                    options.inSampleSize = calculateInSampleSize(options)
                    options.inSampleSize = 8
                    Toast.makeText(this, "SAMPLE: " + options.inSampleSize.toString(), Toast.LENGTH_SHORT).show()
//                    options.inDensity = 320
                    options.inJustDecodeBounds = false
                    val imageStream = contentResolver.openInputStream(photoURI)
//                    imageStream = contentResolver.openInputStream(photoURI)
                    val bitmap = BitmapFactory.decodeStream(imageStream, null, options)

                    val matrix = Matrix()
                    matrix.postRotate(90f)
                    val newBitmap: Bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    Toast.makeText(this, "AFTER: " + newBitmap.density.toString() + " " + newBitmap.width.toString() + " " + newBitmap.height.toString(), Toast.LENGTH_LONG).show()
                    if (bitmap.density > 320) {
                        sBase64Image = FileUtils.encodeImageFile(newBitmap, "large")
                    } else {
                        sBase64Image = FileUtils.encodeImageFile(newBitmap, "normal")
                    }
                    AppController.base64ImageReportOther = sBase64Image
                }
            }
        }
    }

    private fun onSend() {
        when (AppController.typeReportOther) {
            "careless_driver" -> {
                val mReport = Report("other", "careless_driver", "", txtPlate.text.toString(), AppController.userProfile!!.currentLocation!!, AppController.userProfile!!._id.toString(), 1, 0, false, "", sBase64Image, "")
                onAddNewReportOther(mReport)
            }
            "piggy" -> {
                val mReport = Report("other", "piggy", "", txtPlate.text.toString(), AppController.userProfile!!.currentLocation!!, AppController.userProfile!!._id.toString(), 1, 0, false, "", sBase64Image, "")
                onAddNewReportOther(mReport)
            }
        }
    }

    private fun onAddNewReportOther(report: Report) {
        val service = APIServiceGenerator.createService(ReportService::class.java)

        val call = service.addNewReport(report)
        call.enqueue(object : Callback<Report> {
            override fun onFailure(call: Call<Report>?, t: Throwable?) {
                TastyToast.makeText(this@ReportOtherActivity, "Gửi báo hiệu thất bại!", TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
            }

            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                if (response.isSuccessful) {

                    // Chạy audio
                    if (AppController.soundMode == 1) {
                        mAudioPlayer.play(this@ReportOtherActivity, R.raw.gui_bao_hieu_thanh_cong)
                    }
                    TastyToast.makeText(this@ReportOtherActivity, "Gửi báo hiệu thành công!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show()
                    finish()

                } else {
                    val apiError = ErrorUtils.parseError(response)
                    TastyToast.makeText(this@ReportOtherActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }
        })
    }

    val TARGET_IMAGE_WIDTH: Int = 614
    val TARGET_IMAGE_HEIGHT: Int = 818
    // This method is used to calculate largest inSampleSize
    //which is used to decode bitmap in required bitmap.
    private fun calculateInSampleSize(bmOptions: BitmapFactory.Options): Int {
        // Raw height and width of image
        val photoWidth = bmOptions.outWidth
        val photoHeight = bmOptions.outHeight
// Bỏ vì bớt toast
//        Toast.makeText(this, "BEFORE: " + photoWidth + " " + photoHeight, Toast.LENGTH_SHORT).show()
        var scaleFactor = 2
//        if (photoWidth > TARGET_IMAGE_WIDTH || photoHeight > TARGET_IMAGE_HEIGHT) {
//            val halfPhotoWidth = photoWidth / 2
//            val halfPhotoHeight = photoHeight / 2

        // Calculate the largest inSampleSize value that is a power of 2
        //and keeps both height and width larger than the requested height and width.

        // Test and replace with || ( or )
        while ((photoWidth / scaleFactor) >= TARGET_IMAGE_WIDTH && (photoHeight / scaleFactor) >= TARGET_IMAGE_HEIGHT) {

            scaleFactor *= 2
        }
//        }
        // Bỏ vì bớt toast
//        Toast.makeText(this, (photoWidth / scaleFactor).toString() + "  " + (photoHeight / scaleFactor).toString(), Toast.LENGTH_SHORT).show()
        return scaleFactor
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

    private fun onALPR() {
//        val mDialog = SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
//                .setTitleText("Nhận diện biển số xe tự động")
//                .setContentText("ALPR đang thực thi")
//                .showCancelButton(true)
//                .setCancelClickListener {
//                    it.dismiss()
//                }
        val progress = ProgressDialog.show(this, "Nhận diện biển số tự động", "Đang xử lý kết quả...", true)
        val openAlprConfFile = ANDROID_DATA_DIR + File.separatorChar + "runtime_data" + File.separatorChar + "openalpr.conf"
        val options = BitmapFactory.Options()
        options.inSampleSize = 10

        AsyncTask.execute {
            val result = OpenALPR.Factory.create(this, ANDROID_DATA_DIR).recognizeWithCountryRegionNConfig("us", "", mCurrentPhotoPath, openAlprConfFile, 10)
            val results = Gson().fromJson(result, Results::class.java)
            Log.d("OPEN ALPR", result)

            try {
                runOnUiThread {
                    if (results == null || results.results == null || results.results.size == 0) {
                        TastyToast.makeText(this, "Không thể nhận diện được biển số xe", TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
//                                    resultTextView.setText("It was not possible to detect the licence plate.")
                    } else {
                        TastyToast.makeText(this, "Biển số: " + results.results[0].plate
                                // Trim confidence to two decimal places
                                + " Độ tin cậy: " + String.format("%.2f", results.results[0].confidence) + "%"
                                // Convert processing time to seconds and trim to two decimal places
                                + " Thời gian thực thi: " + String.format("%.2f", results.processingTimeMs!! / 1000.0 % 60) + " giây", TastyToast.LENGTH_LONG, TastyToast.SUCCESS).show()

                        txtPlate.setText(results.results[0].plate.toString(), TextView.BufferType.EDITABLE)
                    }
                }

            } catch (exception: JsonSyntaxException) {
                val resultsError = Gson().fromJson(result, ResultsError::class.java)

                runOnUiThread {
                    //                                resultTextView.setText(resultsError.msg)
                    TastyToast.makeText(this, resultsError.msg, TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            progress.dismiss()
        }
    }
}
