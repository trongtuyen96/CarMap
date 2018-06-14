package com.example.trongtuyen.carmap.activity.common

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaPlayer
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.util.Base64
import android.util.Log
import android.view.HapticFeedbackConstants
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.controllers.AppController
import com.example.trongtuyen.carmap.models.Report
import com.example.trongtuyen.carmap.services.*
import com.example.trongtuyen.carmap.utils.FileUtils
import com.sdsmdg.tastytoast.TastyToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.net.URI


class ReportCrashActivity : AppCompatActivity() {

    @BindView(R.id.imvCrashMinor_report_crash)
    lateinit var btnCrashMinor: LinearLayout
    @BindView(R.id.imvCrashMajor_report_crash)
    lateinit var btnCrashMajor: LinearLayout
    @BindView(R.id.imvCrashOtherSide_report_crash)
    lateinit var btnCrashOtherSide: LinearLayout
    @BindView(R.id.txtMess_report_crash)
    lateinit var textInputEdit: EditText
    @BindView(R.id.btnSend_report_crash)
    lateinit var btnSend: Button

    @BindView(R.id.btnClose_report_crash)
    lateinit var btnCLose: ImageView
    @BindView(R.id.btnDismiss_report_crash)
    lateinit var btnDismiss: Button

    @BindView(R.id.layout_record_report_crash)
    lateinit var btnRecord: LinearLayout
    @BindView(R.id.layout_take_photo_report_crash)
    lateinit var btnTakePhoto: LinearLayout

    @BindView(R.id.tvRecord_report_crash)
    lateinit var tvRecord: TextView
    @BindView(R.id.tvTakePhoto_report_crash)
    lateinit var tvTakePhoto: TextView

    @BindView(R.id.over_layout_report_crash)
    lateinit var layoutReport: RelativeLayout

    private var sFileAudioPath: String = ""

    private var sBase64Image: String = ""

    // ==== Dùng cho lấy chất lượng ảnh JPEG gốc, bằng cách chụp xong lưu file ảnh lại
    private lateinit var photoURI: Uri

    private var subType1: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_crash)

        ButterKnife.bind(this)
        initComponents()
    }

    private fun initComponents() {

        // Các nút báo cáo
        btnCrashMinor.setOnClickListener {
            subType1 = "minor"
            btnCrashMinor.background = getDrawable(R.color.button_bg_inactive)
            btnCrashMajor.background = null
            btnCrashOtherSide.background = null
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
        btnCrashMajor.setOnClickListener {
            subType1 = "major"
            btnCrashMajor.background = getDrawable(R.color.button_bg_inactive)
            btnCrashMinor.background = null
            btnCrashOtherSide.background = null
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
        btnCrashOtherSide.setOnClickListener {
            subType1 = "other_side"
            btnCrashOtherSide.background = getDrawable(R.color.button_bg_inactive)
            btnCrashMajor.background = null
            btnCrashMinor.background = null
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
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 1)

            // ==== Dùng cho lấy chất lượng ảnh JPEG gốc, bằng cách chụp xong lưu file ảnh lại
//            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            // Ensure that there's a camera activity to handle the intent
//            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                // Create the File where the photo should go
//                var photoFile: File? = null
//                try {
//                    photoFile = createImageFile();
//                } catch (e: IOException) {
//                    // Error occurred while creating the File
//                }
//                // Continue only if the File was successfully created
//                if (photoFile != null) {
//                    photoURI = FileProvider.getUriForFile(this,
//                            "com.example.android.fileprovider",
//                            photoFile);
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                    startActivityForResult(takePictureIntent, 1);
//                }
//            }

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
            TastyToast.makeText(this, "Vui lòng chọn loại tai nạn", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
        } else {
//            TastyToast.makeText(this, "Loại: " + subType1 + " " + textInputEdit.text.toString(), TastyToast.LENGTH_SHORT, TastyToast.).show()
            // Encode file ghi âm
            val encoded = FileUtils.encodeAudioFile(sFileAudioPath)
            val mReport = Report("crash", subType1, "", textInputEdit.text.toString(), AppController.userProfile!!.homeLocation!!, AppController.userProfile!!._id.toString(), 1, 0, false, encoded, sBase64Image)
            onAddNewReportCrash(mReport)
        }
    }

    private fun onAddNewReportCrash(report: Report) {
        val service = APIServiceGenerator.createService(ReportService::class.java)

        val call = service.addNewReport(report)
        call.enqueue(object : Callback<Report> {
            override fun onFailure(call: Call<Report>?, t: Throwable?) {
                TastyToast.makeText(this@ReportCrashActivity, "Gửi báo cáo thất bại!", TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
            }

            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                if (response.isSuccessful) {
                    TastyToast.makeText(this@ReportCrashActivity, "Gửi báo cáo thành công!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show()
                    finish()
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    TastyToast.makeText(this@ReportCrashActivity, "" + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }
        })
    }

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
                    val bitmap: Bitmap = data!!.extras.get("data") as Bitmap
                    Toast.makeText(this, "BEFORE: " + bitmap.density.toString() + " " + bitmap.height.toString() + " " + bitmap.width.toString(), Toast.LENGTH_SHORT).show()
                    val matrix = Matrix()
                    matrix.postRotate(90f)
                    val newBitmap: Bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    Toast.makeText(this, "AFTER: " + newBitmap.density.toString() + " " + newBitmap.height.toString() + " " + newBitmap.width.toString(), Toast.LENGTH_SHORT).show()
                    sBase64Image = FileUtils.encodeImageFile(newBitmap)


                    // ==== Dùng cho lấy chất lượng ảnh JPEG gốc, bằng cách chụp xong lưu file ảnh lại
//                    val imageStream = contentResolver.openInputStream(photoURI)
//                    val bitmap = BitmapFactory.decodeStream(imageStream)
//                    Toast.makeText(this, "BEFORE: " + bitmap.density.toString() + " " + bitmap.width.toString() + " " + bitmap.height.toString(), Toast.LENGTH_SHORT).show()
//                    val matrix = Matrix()
//                    matrix.postRotate(90f)
//                    val newBitmap: Bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width / 8, bitmap.height / 8, matrix, true)
//                    Toast.makeText(this, "AFTER: " + newBitmap.density.toString() + " " + newBitmap.width.toString() + " " + newBitmap.height.toString(), Toast.LENGTH_LONG).show()
//                    sBase64Image = FileUtils.encodeImageFile(newBitmap)

//                TastyToast.makeText(this, sBase64Image, TastyToast.LENGTH_SHORT, TastyToast.INFO).show()
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
//        mCurrentPhotoPath = image.absolutePath
        return image
    }
}
