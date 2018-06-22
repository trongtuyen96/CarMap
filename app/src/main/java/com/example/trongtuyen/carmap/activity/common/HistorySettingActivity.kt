package com.example.trongtuyen.carmap.activity.common

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.R

class HistorySettingActivity : AppCompatActivity() {

    @BindView(R.id.imBack_history_setting)
    lateinit var btnBack: ImageView
    @BindView(R.id.tvInfo_history_setting)
    lateinit var tvInfo: TextView
    @BindView(R.id.btnDismiss_history_setting)
    lateinit var btnDismiss: Button
    @BindView(R.id.btnChoose_history_setting)
    lateinit var btnChoose: Button
    @BindView(R.id.layoutNumberAll_history_setting)
    lateinit var layoutAll: LinearLayout
    @BindView(R.id.divider0_history_setting)
    lateinit var divider0: View
    @BindView(R.id.divider1_history_setting)
    lateinit var divider1: View
    @BindView(R.id.divider2_history_setting)
    lateinit var divider2: View
    @BindView(R.id.divider3_history_setting)
    lateinit var divider3: View
    @BindView(R.id.layoutNum1_history_setting)
    lateinit var layoutNum1: LinearLayout
    @BindView(R.id.layoutNum2_history_setting)
    lateinit var layoutNum2: LinearLayout
    @BindView(R.id.layoutNum3_history_setting)
    lateinit var layoutNum3: LinearLayout
    @BindView(R.id.tvAddress1_history_setting)
    lateinit var tvAddress1: TextView
    @BindView(R.id.tvAddress2_history_setting)
    lateinit var tvAddress2: TextView
    @BindView(R.id.tvAddress3_history_setting)
    lateinit var tvAddress3: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_setting)

        ButterKnife.bind(this)
        initComponents()
    }

    private fun initComponents(){
        btnBack.setOnClickListener {
            finish()
        }

        btnDismiss.setOnClickListener{
            finish()
        }
    }
}
