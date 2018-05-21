package com.example.trongtuyen.carmap.activity.common

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.R
import java.util.*
import java.util.concurrent.TimeUnit

class ReportMenuActivity : AppCompatActivity() {
    @BindView(R.id.imvReportTraffic_activity_report)
    lateinit var btnReportTraffic: ImageView
    @BindView(R.id.imvReportCrash_activity_report)
    lateinit var btnReportCrash: ImageView
    @BindView(R.id.imvReportHazard_activity_report)
    lateinit var btnReportHazard: ImageView
    @BindView(R.id.imvReportAssistance_activity_report)
    lateinit var btnReportAssist: ImageView

    @BindView(R.id.btnClose_activity_report)
    lateinit var btnCLose: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_menu)

        ButterKnife.bind(this)
        initComponents()

        // Tự động tắt activity sau 7 giây
        var countDownTimer= object : CountDownTimer(7000, 500) {
            override fun onTick(millisUntilFinished: Long) {
                findViewById<TextView>(R.id.tvClose_activity_report).text = String.format(Locale.getDefault(), "%s %d giây",
                        "Đóng sau",
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1)
            }

            override fun onFinish() {
                finish()
            }
        }
        countDownTimer.start()
    }

    private fun initComponents() {

        // Các nút báo cáo
        btnReportTraffic.setOnClickListener { onReportTraffic() }
        btnReportCrash.setOnClickListener { onReportCrash() }
        btnReportHazard.setOnClickListener { onReportHazard() }
        btnReportAssist.setOnClickListener { onReportAssist() }


        btnCLose.setOnClickListener { onClose() }

    }

    private fun onClose(){
        finish()
    }

    private fun onReportTraffic(){
        val intent = Intent(this, ReportTrafficActivity::class.java)
        startActivity(intent)
    }

    private fun onReportCrash() {
        val intent = Intent(this, ReportCrashActivity::class.java)
        startActivity(intent)
    }

    private fun onReportHazard() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun onReportAssist() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }
}
