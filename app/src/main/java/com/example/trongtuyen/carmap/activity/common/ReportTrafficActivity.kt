package com.example.trongtuyen.carmap.activity.common

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.TextInputEditText
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
import com.example.trongtuyen.carmap.utils.FileUtils
import com.sdsmdg.tastytoast.TastyToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    private var subType1: String = ""

    private var sFileAudioPath: String = ""

    private var sBase64Image: String = ""

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
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 1)
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
            // Encode file ghi âm
            val encoded = FileUtils.encodeAudioFile(sFileAudioPath)
            val mReport = Report("traffic", subType1, "", textInputEdit.text.toString(), AppController.userProfile!!.homeLocation!!, AppController.userProfile!!._id.toString(), 1, 0, false, encoded, sBase64Image)
            onAddNewReportTraffic(mReport)
        }
    }

    private fun onAddNewReportTraffic(report: Report) {
        val service = APIServiceGenerator.createService(ReportService::class.java)

        val call = service.addNewReport(report)
        call.enqueue(object : Callback<Report> {
            override fun onFailure(call: Call<Report>?, t: Throwable?) {
                TastyToast.makeText(this@ReportTrafficActivity, "Gửi báo cáo thất bại!", TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
            }

            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                if (response.isSuccessful) {
                    TastyToast.makeText(this@ReportTrafficActivity, "Gửi báo cáo thành công!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show()
                    finish()
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    TastyToast.makeText(this@ReportTrafficActivity, "" + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
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
                if(resultCode == Activity.RESULT_OK) {
                    tvTakePhoto.text = "Đã chụp ảnh"
                    val bitmap: Bitmap = data!!.extras.get("data") as Bitmap
                    val matrix = Matrix()
                    matrix.postRotate(90f)
                    val newBitmap: Bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    sBase64Image = FileUtils.encodeImageFile(newBitmap)
//                TastyToast.makeText(this, sBase64Image, TastyToast.LENGTH_SHORT, TastyToast.INFO).show()
                }
            }
        }
    }
}
