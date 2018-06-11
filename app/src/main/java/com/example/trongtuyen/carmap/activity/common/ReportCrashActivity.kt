package com.example.trongtuyen.carmap.activity.common

import android.content.Intent
import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.HapticFeedbackConstants
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.controllers.AppController
import com.example.trongtuyen.carmap.models.Report
import com.example.trongtuyen.carmap.services.*
import com.sdsmdg.tastytoast.TastyToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

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

    private var sFileAudioName: String? = null

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
            val encoded = encodeAudioFile(sFileAudioName!!)
            if (encoded != "") {
                TastyToast.makeText(this, encoded, TastyToast.LENGTH_SHORT, TastyToast.INFO).show()
                decodeAudioFile(encoded, externalCacheDir!!.absolutePath + "/audio_decoded.3gp")
            }
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
            val mReport = Report("crash", subType1, "", textInputEdit.text.toString(), AppController.userProfile!!.homeLocation!!, AppController.userProfile!!._id.toString(), 1, 0, false)
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
        tvRecord.text = "Thu âm"
        if (resultCode == 1) {
            sFileAudioName = data?.getStringExtra("FileAudioPath")
            tvRecord.text = "Đã thu âm"
            TastyToast.makeText(this, sFileAudioName.toString(), TastyToast.LENGTH_SHORT, TastyToast.INFO).show()
        }
    }

    private fun encodeAudioFile(path: String): String {
        val audioBytes: ByteArray
        try {
            // Just to check file size.. Its is correct i-e; Not Zero
            val audioFile = File(path)
            val fileSize = audioFile.length()

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
            val _audioBase64 = Base64.encodeToString(audioBytes, Base64.DEFAULT)
            return _audioBase64
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    private fun decodeAudioFile(base64AudioData: String, filePath: String) {
        val decoded: ByteArray = Base64.decode(base64AudioData, Base64.DEFAULT)
        val fos: FileOutputStream = FileOutputStream(filePath)
        fos.write(decoded);
        fos.close();

        try {
            val mp: MediaPlayer = MediaPlayer()
            mp.setDataSource(filePath)
            mp.prepare()
            mp.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
