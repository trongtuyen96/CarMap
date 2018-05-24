package com.example.trongtuyen.carmap.activity.common

import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.controllers.AppController
import com.example.trongtuyen.carmap.models.Report
import com.example.trongtuyen.carmap.services.APIServiceGenerator
import com.example.trongtuyen.carmap.services.ErrorUtils
import com.example.trongtuyen.carmap.services.ReportService
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

    private var subType1 : String = ""
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
        }
        btnTrafficHeavy.setOnClickListener {
            subType1 = "heavy"
            btnTrafficHeavy.background = getDrawable(R.color.button_bg_inactive)
            btnTrafficModerate.background = null
            btnTrafficStandstill.background = null
        }
        btnTrafficStandstill.setOnClickListener {
            subType1 = "standstill"
            btnTrafficStandstill.background = getDrawable(R.color.button_bg_inactive)
            btnTrafficHeavy.background = null
            btnTrafficModerate.background = null
        }
        btnSend.setOnClickListener { onSend() }
        btnCLose.setOnClickListener { onClose() }
        btnDismiss.setOnClickListener { onClose() }
    }

    private fun onClose(){
        finish()
    }

    private fun onSend() {
        if (subType1 == ""){
            Toast.makeText(this, "Vui lòng chọn mức độ kẹt xe", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(this, "Loại: " + subType1 + " " + textInputEdit.text.toString(), Toast.LENGTH_SHORT).show()
            val mReport = Report("traffic",subType1,"",textInputEdit.text.toString(), AppController.userProfile!!.homeLocation!!, AppController.userProfile!!._id.toString(),1,0,false)
            onAddNewReportTraffic(mReport)
        }
    }

    private fun onAddNewReportTraffic(report: Report) {
        val service = APIServiceGenerator.createService(ReportService::class.java)

        val call = service.addNewReport(report)
        call.enqueue(object : Callback<Report> {
            override fun onFailure(call: Call<Report>?, t: Throwable?) {
                Toast.makeText(this@ReportTrafficActivity, "Failed!", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                if (response.isSuccessful()) {
                    Toast.makeText(this@ReportTrafficActivity, "Gửi báo cáo thành công!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    Toast.makeText(this@ReportTrafficActivity, "" + apiError.message(), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
