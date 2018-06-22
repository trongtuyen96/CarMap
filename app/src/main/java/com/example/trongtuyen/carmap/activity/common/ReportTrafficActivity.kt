package com.example.trongtuyen.carmap.activity.common

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.HapticFeedbackConstants
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.controllers.AppController
import com.example.trongtuyen.carmap.models.Report
import com.example.trongtuyen.carmap.services.APIServiceGenerator
import com.example.trongtuyen.carmap.services.ErrorUtils
import com.example.trongtuyen.carmap.services.ReportService
import com.example.trongtuyen.carmap.services.models.ReportResponse
import com.example.trongtuyen.carmap.utils.FileUtils
import com.sdsmdg.tastytoast.TastyToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException

class ReportTrafficActivity : AppCompatActivity() {

    @BindView(R.id.imvTrafficModerate_report_traffic)
    lateinit var btnTrafficModerate: LinearLayout
    @BindView(R.id.imvTrafficHeavy_report_traffic)
    lateinit var btnTrafficHeavy: LinearLayout
    @BindView(R.id.imvTrafficStandstill_report_traffic)
    lateinit var btnTrafficStandstill: LinearLayout
    @BindView(R.id.txtMess_report_traffic)
    lateinit var textInputEdit: EditText
    @BindView(R.id.btnSend_report_traffic)
    lateinit var btnSend: Button

    @BindView(R.id.btnClose_report_traffic)
    lateinit var btnCLose: ImageView
    @BindView(R.id.btnDismiss_report_traffic)
    lateinit var btnDismiss: Button

    @BindView(R.id.layout_record_report_traffic)
    lateinit var btnRecord: LinearLayout
    @BindView(R.id.layout_take_photo_report_traffic)
    lateinit var btnTakePhoto: LinearLayout

    @BindView(R.id.tvRecord_report_traffic)
    lateinit var tvRecord: TextView
    @BindView(R.id.tvTakePhoto_report_traffic)
    lateinit var tvTakePhoto: TextView

    @BindView(R.id.over_layout_report_traffic)
    lateinit var layoutReport: RelativeLayout

    private var subType1: String = ""

    private var sFileAudioPath: String = ""

    private var sBase64Image: String = ""

    // ==== Dùng cho lấy chất lượng ảnh JPEG gốc, bằng cách chụp xong lưu file ảnh lại
    private lateinit var photoURI: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_traffic)

        ButterKnife.bind(this)
        initComponents()
    }

    private fun initComponents() {

        // Các nút báo cáo
        btnTrafficModerate.setOnClickListener {
            subType1 = "moderate"
            btnTrafficModerate.background = getDrawable(R.color.button_bg_inactive)
            btnTrafficHeavy.background = null
            btnTrafficStandstill.background = null
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
        btnTrafficHeavy.setOnClickListener {
            subType1 = "heavy"
            btnTrafficHeavy.background = getDrawable(R.color.button_bg_inactive)
            btnTrafficModerate.background = null
            btnTrafficStandstill.background = null
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
        btnTrafficStandstill.setOnClickListener {
            subType1 = "standstill"
            btnTrafficStandstill.background = getDrawable(R.color.button_bg_inactive)
            btnTrafficHeavy.background = null
            btnTrafficModerate.background = null
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
        btnSend.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onSend()
        }
        btnCLose.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onClose()
        }
        btnDismiss.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onClose()
        }

        btnRecord.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val intent = Intent(this, AudioRecordActivity::class.java)
            startActivityForResult(intent, 0)
        }

        btnTakePhoto.setOnClickListener {
//            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            startActivityForResult(intent, 1)

            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            // Xoá ảnh cũ
            if (mCurrentPhotoPath != "") {
                val oldFile = File(mCurrentPhotoPath)
                oldFile.delete()
            }

//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            startActivityForResult(intent, 1)

//             ==== Dùng cho lấy chất lượng ảnh JPEG gốc, bằng cách chụp xong lưu file ảnh lại
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

        layoutReport.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            finish()
        }
    }

    private fun onClose() {
        finish()
    }

    private fun onSend() {
        if (subType1 == "") {
            TastyToast.makeText(this, "Vui lòng chọn mức độ kẹt xe", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
        } else {
//            TastyToast.makeText(this, "Loại: " + subType1 + " " + textInputEdit.text.toString(), TastyToast.LENGTH_SHORT).show()
            if (sFileAudioPath == "" || sBase64Image == "") {
                // Encode file ghi âm
                val encoded = FileUtils.encodeAudioFile(sFileAudioPath)
                val mReport = Report("traffic", subType1, "", textInputEdit.text.toString(), AppController.userProfile!!.currentLocation!!, AppController.userProfile!!._id.toString(), 1, 0, false, encoded, sBase64Image)
                onAddNewReportTraffic(mReport, false)
//                Toast.makeText(this, "Chạy 1", Toast.LENGTH_SHORT).show()
            } else {
                val mReport = Report("traffic", subType1, "", textInputEdit.text.toString(), AppController.userProfile!!.currentLocation!!, AppController.userProfile!!._id.toString(), 1, 0, false, "", sBase64Image)
                onAddNewReportTraffic(mReport, true)
//                Toast.makeText(this, "Chạy 2", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onAddNewReportTraffic(report: Report, bothAudioAndImage: Boolean) {
        val service = APIServiceGenerator.createService(ReportService::class.java)

        val call = service.addNewReport(report)
        call.enqueue(object : Callback<Report> {
            override fun onFailure(call: Call<Report>?, t: Throwable?) {
                TastyToast.makeText(this@ReportTrafficActivity, "Gửi báo cáo thất bại!", TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
            }

            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                if (response.isSuccessful) {
                    if (bothAudioAndImage == true) {
                        // Encode file ghi âm
                        val encoded = FileUtils.encodeAudioFile(sFileAudioPath)
                        onUpdateBase64Voice(response.body()!!._id.toString(), encoded)
                        finish()
                    } else {
                        TastyToast.makeText(this@ReportTrafficActivity, "Gửi báo cáo thành công!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show()
                        finish()
                    }
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    TastyToast.makeText(this@ReportTrafficActivity, "" + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }
        })
    }

    private fun onUpdateBase64Voice(id: String, base64Voice: String) {
        val service = APIServiceGenerator.createService(ReportService::class.java)
        val call = service.updateBase64Voice(id, base64Voice)
        call.enqueue(object : Callback<ReportResponse> {
            override fun onResponse(call: Call<ReportResponse>, response: Response<ReportResponse>) {
                if (response.isSuccessful) {
                    TastyToast.makeText(this@ReportTrafficActivity, "Gửi báo cáo thành công!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show()
//                    Toast.makeText(this@ReportTrafficActivity, "Xong 2", Toast.LENGTH_SHORT).show()
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    TastyToast.makeText(this@ReportTrafficActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            override fun onFailure(call: Call<ReportResponse>, t: Throwable) {
                Log.e("Failure", "Error: " + t.message)
            }
        })
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when (requestCode) {
//            0 -> {
//                tvRecord.text = "Thu âm"
//                if (resultCode == Activity.RESULT_OK) {
//                    sFileAudioPath = data!!.getStringExtra("FileAudioPath")
//                    tvRecord.text = "Đã thu âm"
////                    TastyToast.makeText(this, sFileAudioPath, TastyToast.LENGTH_SHORT, TastyToast.INFO).show()
//                }
//            }
//            1 -> {
//                tvTakePhoto.text = "Chụp ảnh"
//                if (resultCode == Activity.RESULT_OK) {
//                    tvTakePhoto.text = "Đã chụp ảnh"
//                    val bitmap: Bitmap = data!!.extras.get("data") as Bitmap
//                    val matrix = Matrix()
//                    matrix.postRotate(90f)
//                    val newBitmap: Bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//                    if (bitmap.density > 320) {
//                        sBase64Image = FileUtils.encodeImageFile(newBitmap, "large")
//                    } else {
//                        sBase64Image = FileUtils.encodeImageFile(newBitmap, "normal")
//                    }
////                TastyToast.makeText(this, sBase64Image, TastyToast.LENGTH_SHORT, TastyToast.INFO).show()
//                }
//            }
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            0 -> {
                tvRecord.text = "Thu âm"
                if (resultCode == Activity.RESULT_OK) {
                    sFileAudioPath = data!!.getStringExtra("FileAudioPath")
                    tvRecord.text = "Đã thu âm"
//                    TastyToast.makeText(this, sFileAudioPath, TastyToast.LENGTH_SHORT, TastyToast.INFO).show()
                }
            }
            1 -> {
                tvTakePhoto.text = "Chụp ảnh"
                if (resultCode == Activity.RESULT_OK) {
                    tvTakePhoto.text = "Đã chụp ảnh"

                    // Chỉ lấy thumbnail nên chất lượng ảnh không cao
//                    val bitmap: Bitmap = data!!.extras.get("data") as Bitmap
//                    Toast.makeText(this, "BEFORE: " + bitmap.density.toString() + " " + bitmap.height.toString() + " " + bitmap.width.toString(), Toast.LENGTH_SHORT).show()
//                    val matrix = Matrix()
//                    matrix.postRotate(90f)
//                    val newBitmap: Bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//                    Toast.makeText(this, "AFTER: " + newBitmap.density.toString() + " " + newBitmap.height.toString() + " " + newBitmap.width.toString(), Toast.LENGTH_SHORT).show()
//                    sBase64Image = FileUtils.encodeImageFile(newBitmap)


                    // ==== Dùng cho lấy chất lượng ảnh JPEG gốc, bằng cách chụp xong lưu file ảnh lại
//                    val imageStream = contentResolver.openInputStream(photoURI)
//                    val bitmap = BitmapFactory.decodeStream(imageStream)

                    val options = BitmapFactory.Options()
                    // Số inSampleSize là ảnh mới sẽ bằng 1 / inSampleSize của ảnh gốc, tức chiều dài và rộng giảm đi inSampleSize lần
                    // inJustRebound = true là sẽ đọc resource của ảnh chứ ko laod ảnh vào bộ nhớ, sẽ giảm bộ nhớ sử dụng

                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(mCurrentPhotoPath, options)
                    options.inSampleSize = calculateInSampleSize(options)
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


//                    TastyToast.makeText(this, sBase64Image, TastyToast.LENGTH_SHORT, TastyToast.INFO).show()
                }
            }
        }
    }

    val TARGET_IMAGE_WIDTH: Int = 614
    val TARGET_IMAGE_HEIGHT: Int = 818
    // This method is used to calculate largest inSampleSize
    //which is used to decode bitmap in required bitmap.
    private fun calculateInSampleSize(bmOptions: BitmapFactory.Options): Int {
        // Raw height and width of image
        val photoWidth = bmOptions.outWidth
        val photoHeight = bmOptions.outHeight

        Toast.makeText(this, "BEFORE: " + photoWidth + " " + photoHeight, Toast.LENGTH_SHORT).show()
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
        Toast.makeText(this, (photoWidth / scaleFactor).toString() + "  " + (photoHeight / scaleFactor).toString(), Toast.LENGTH_SHORT).show()
        return scaleFactor
    }

    private var mCurrentPhotoPath: String = ""
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
}
