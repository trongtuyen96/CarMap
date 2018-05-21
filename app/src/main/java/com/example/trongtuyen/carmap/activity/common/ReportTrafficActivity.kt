package com.example.trongtuyen.carmap.activity.common

import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.R

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

    private var subType1 : Number = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_traffic)

        ButterKnife.bind(this)
        initComponents()
    }

    private fun initComponents() {

        // Các nút báo cáo
        btnTrafficModerate.setOnClickListener {
            subType1 = 1
            btnTrafficModerate.background = getDrawable(R.color.colorPrimaryLight)
            btnTrafficHeavy.background = null
            btnTrafficStandstill.background = null
        }
        btnTrafficHeavy.setOnClickListener {
            subType1 = 2
            btnTrafficHeavy.background = getDrawable(R.color.colorPrimaryLight)
            btnTrafficModerate.background = null
            btnTrafficStandstill.background = null
        }
        btnTrafficStandstill.setOnClickListener {
            subType1 = 3
            btnTrafficStandstill.background = getDrawable(R.color.colorPrimaryLight)
            btnTrafficHeavy.background = null
            btnTrafficModerate.background = null
        }
        btnSend.setOnClickListener { onSend() }
        btnCLose.setOnClickListener { onClose() }
    }

    private fun onClose(){
        finish()
    }

    private fun onSend() {
        if (subType1 == 0){
            Toast.makeText(this, "Vui lòng chọn mức độ kẹt xe", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(this, "Loại: " + subType1 + " " + textInputEdit.text.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}
