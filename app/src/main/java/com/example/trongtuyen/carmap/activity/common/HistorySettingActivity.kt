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
import com.example.trongtuyen.carmap.controllers.AppController
import com.google.android.gms.location.places.Place

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
    @BindView(R.id.tvName1_history_setting)
    lateinit var tvName1: TextView
    @BindView(R.id.tvName2_history_setting)
    lateinit var tvName2: TextView
    @BindView(R.id.tvName3_history_setting)
    lateinit var tvName3: TextView
    @BindView(R.id.tvAddress1_history_setting)
    lateinit var tvAddress1: TextView
    @BindView(R.id.tvAddress2_history_setting)
    lateinit var tvAddress2: TextView
    @BindView(R.id.tvAddress3_history_setting)
    lateinit var tvAddress3: TextView

    private var selectedPlace: Place? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_setting)

        ButterKnife.bind(this)
        initComponents()
    }

    private fun initComponents() {
        btnBack.setOnClickListener {
            finish()
        }

        btnDismiss.setOnClickListener {
            finish()
        }

        when (AppController.listHistoryPlace.size) {
            0 -> {
                layoutAll.visibility = View.INVISIBLE
            }
            1 -> {
                tvInfo.visibility = View.INVISIBLE
                layoutNum2.visibility = View.INVISIBLE
                layoutNum3.visibility = View.INVISIBLE
                divider2.visibility = View.INVISIBLE
                divider3.visibility = View.INVISIBLE
                tvName1.text = AppController.listHistoryPlace[0].name
                tvAddress1.text = AppController.listHistoryPlace[0].address
            }
            2 -> {
                tvInfo.visibility = View.INVISIBLE
                layoutNum3.visibility = View.INVISIBLE
                divider3.visibility = View.INVISIBLE
                tvName1.text = AppController.listHistoryPlace[0].name
                tvAddress1.text = AppController.listHistoryPlace[0].address
                tvName2.text = AppController.listHistoryPlace[1].name
                tvAddress2.text = AppController.listHistoryPlace[1].address
            }
            3 -> {
                tvInfo.visibility = View.INVISIBLE
                tvName1.text = AppController.listHistoryPlace[0].name
                tvAddress1.text = AppController.listHistoryPlace[0].address
                tvName2.text = AppController.listHistoryPlace[1].name
                tvAddress2.text = AppController.listHistoryPlace[1].address
                tvName3.text = AppController.listHistoryPlace[2].name
                tvAddress3.text = AppController.listHistoryPlace[2].address
            }
        }

        layoutNum1.setOnClickListener {
            layoutNum1.background = getDrawable(R.color.divider)
            layoutNum2.background = getDrawable(R.color.background_front)
            layoutNum3.background = getDrawable(R.color.background_front)
            selectedPlace = AppController.listHistoryPlace[0]
        }

        layoutNum2.setOnClickListener {
            layoutNum2.background = getDrawable(R.color.divider)
            layoutNum1.background = getDrawable(R.color.background_front)
            layoutNum3.background = getDrawable(R.color.background_front)
            selectedPlace = AppController.listHistoryPlace[1]
        }

        layoutNum3.setOnClickListener {
            layoutNum3.background = getDrawable(R.color.divider)
            layoutNum1.background = getDrawable(R.color.background_front)
            layoutNum2.background = getDrawable(R.color.background_front)
            selectedPlace = AppController.listHistoryPlace[2]
        }

        btnChoose.setOnClickListener { }
    }
}
