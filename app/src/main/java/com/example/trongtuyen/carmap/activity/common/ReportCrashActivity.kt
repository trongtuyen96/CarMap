package com.example.trongtuyen.carmap.activity.common

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.R

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

    private var subType1 : Number = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_crash)


        ButterKnife.bind(this)
        initComponents()
    }

    private fun initComponents() {

        // Các nút báo cáo
        btnCrashMinor.setOnClickListener {
            subType1 = 1
            btnCrashMinor.background = getDrawable(R.color.colorPrimaryLight)
            btnCrashMajor.background = null
            btnCrashOtherSide.background = null
        }
        btnCrashMajor.setOnClickListener {
            subType1 = 2
            btnCrashMajor.background = getDrawable(R.color.colorPrimaryLight)
            btnCrashMinor.background = null
            btnCrashOtherSide.background = null
        }
        btnCrashOtherSide.setOnClickListener {
            subType1 = 3
            btnCrashOtherSide.background = getDrawable(R.color.colorPrimaryLight)
            btnCrashMajor.background = null
            btnCrashMinor.background = null
        }
        btnSend.setOnClickListener { onSend() }
        btnCLose.setOnClickListener { onClose() }
    }

    private fun onClose(){
        finish()
    }

    private fun onSend() {
        if (subType1 == 0){
            Toast.makeText(this, "Vui lòng chọn loại tai nạn", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(this, "Loại: " + subType1 + " " + textInputEdit.text.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}
