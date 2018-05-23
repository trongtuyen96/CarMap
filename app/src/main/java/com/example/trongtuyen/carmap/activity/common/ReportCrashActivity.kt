package com.example.trongtuyen.carmap.activity.common

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.controllers.AppController
import com.example.trongtuyen.carmap.models.Report
import com.example.trongtuyen.carmap.models.User
import com.example.trongtuyen.carmap.services.*
import com.example.trongtuyen.carmap.services.models.ReportResponse
import com.example.trongtuyen.carmap.services.models.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        }
        btnCrashMajor.setOnClickListener {
            subType1 = "major"
            btnCrashMajor.background = getDrawable(R.color.button_bg_inactive)
            btnCrashMinor.background = null
            btnCrashOtherSide.background = null
        }
        btnCrashOtherSide.setOnClickListener {
            subType1 = "other_side"
            btnCrashOtherSide.background = getDrawable(R.color.button_bg_inactive)
            btnCrashMajor.background = null
            btnCrashMinor.background = null
        }
        btnSend.setOnClickListener { onSend() }
        btnCLose.setOnClickListener { onClose() }
    }

    private fun onClose() {
        finish()
    }

    private fun onSend() {
        if (subType1 == "") {
            Toast.makeText(this, "Vui lòng chọn loại tai nạn", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Loại: " + subType1 + " " + textInputEdit.text.toString(), Toast.LENGTH_SHORT).show()
            val mReport = Report("crash",subType1,"",textInputEdit.text.toString(),AppController.userProfile!!.homeLocation!!,AppController.userProfile!!._id.toString(),1,0,false)
            onAddNewReportCrash(mReport)
        }
    }

    private fun onAddNewReportCrash(report: Report) {
        val service = APIServiceGenerator.createService(ReportService::class.java)

        val call = service.addNewReport(report)
        call.enqueue(object : Callback<Report> {
            override fun onFailure(call: Call<Report>?, t: Throwable?) {
                Toast.makeText(this@ReportCrashActivity, "Failed!", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                if (response.isSuccessful()) {
                    Toast.makeText(this@ReportCrashActivity, "Gửi báo cáo thành công!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    Toast.makeText(this@ReportCrashActivity, "" + apiError.message(), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
