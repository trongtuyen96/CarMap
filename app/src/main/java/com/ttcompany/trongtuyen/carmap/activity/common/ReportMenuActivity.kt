package com.ttcompany.trongtuyen.carmap.activity.common

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.support.design.widget.BottomSheetDialog
import android.view.HapticFeedbackConstants
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.ttcompany.trongtuyen.carmap.R
import com.ttcompany.trongtuyen.carmap.controllers.AppController
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
    @BindView(R.id.report_menu_layout)
    lateinit var layoutMenu: RelativeLayout
    @BindView(R.id.btnClose_activity_report)
    lateinit var btnCLose: ImageView

    @BindView(R.id.imvReportOther_activity_report)
    lateinit var btnReportOther: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_menu)

        ButterKnife.bind(this)
        initComponents()

        // Tự động tắt activity sau 7 giây
        val countDownTimer = object : CountDownTimer(7000, 500) {
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
        btnReportTraffic.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onReportTraffic()
        }
        btnReportCrash.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onReportCrash()
        }
        btnReportHazard.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onReportHazard()
        }
        btnReportAssist.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onReportAssist()
        }
        layoutMenu.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            finish()
        }

        btnCLose.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onClose()
        }

        btnReportOther.setOnClickListener {
            val customBottomSheetView = layoutInflater.inflate(R.layout.custom_bottom_sheet_dialog_2_items, null)

            val btnCarelessDriver = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame1_2_items)
            val btnPiggy = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame2_2_items)

            val bottomSheetDialog = BottomSheetDialog(this)
            bottomSheetDialog.setContentView(customBottomSheetView)

            bottomSheetDialog.setCanceledOnTouchOutside(true)

            btnCarelessDriver.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                AppController.typeReportOther = "careless_driver"
                onReportOtherCarelessDriver()
                bottomSheetDialog.dismiss()
            }
            btnPiggy.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                AppController.typeReportOther = "piggy"
                onReportOtherPiggy()
                bottomSheetDialog.dismiss()
            }
            bottomSheetDialog.show()
        }

    }

    private fun onClose() {
        finish()
    }

    private fun onReportTraffic() {
        val intent = Intent(this, ReportTrafficActivity::class.java)
        startActivity(intent)
    }

    private fun onReportCrash() {
        val intent = Intent(this, ReportCrashActivity::class.java)
        startActivity(intent)
    }

    private fun onReportHazard() {
        val intent = Intent(this, ReportHazardActivity::class.java)
        startActivity(intent)
    }

    private fun onReportAssist() {
        val intent = Intent(this, ReportHelpActivity::class.java)
        startActivity(intent)
    }

    private fun onReportOtherCarelessDriver() {
        val intent = Intent(this, ReportOtherActivity::class.java)
        startActivity(intent)
    }

    private fun onReportOtherPiggy() {
        val intent = Intent(this, ReportOtherActivity::class.java)
        startActivity(intent)
    }
}
