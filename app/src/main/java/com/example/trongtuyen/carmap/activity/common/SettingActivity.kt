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
import com.github.angads25.toggle.LabeledSwitch
import com.github.angads25.toggle.interfaces.OnToggledListener

class SettingActivity : AppCompatActivity() {

    @BindView(R.id.switchInvisible_setting)
    lateinit var switchInvisible: LabeledSwitch
    @BindView(R.id.switchSocket_setting)
    lateinit var switchSocket: LabeledSwitch
    @BindView(R.id.btnDismiss_history_setting)
    lateinit var btnDismiss: Button
    @BindView(R.id.btnRadius_setting)
    lateinit var btnRadius: Button
    @BindView(R.id.btnSound_setting)
    lateinit var btnSound: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        ButterKnife.bind(this)
        initComponents()
    }

    private fun initComponents(){

        if (AppController.settingInvisible == "true") {
            switchInvisible.isOn = true
        } else {
            switchInvisible.isOn = false
        }

        if (AppController.settingSocket == "true") {
            switchSocket.isOn = true
        } else {
            switchSocket.isOn = false
        }

        btnRadius.text = AppController.settingRadius.toString() + " km"

        when (AppController.soundMode) {
            1 -> {
                btnSound.background = getDrawable(R.drawable.bg_btn_send)
                btnSound.text = "MỞ"
            }
            2 -> {
                btnSound.background = getDrawable(R.drawable.bg_btn_orange)
                btnSound.text = "BÁO HIỆU"
            }
            3 -> {
                btnSound.background = getDrawable(R.drawable.bg_btn_gray_dark)
                btnSound.text = "TẮT"
            }
        }

        switchInvisible.setOnToggledListener(object : OnToggledListener {
            override fun onSwitched(labeledSwitch: LabeledSwitch?, isOn: Boolean) {
                if (isOn) {
                    AppController.settingInvisible = "true"
                } else {
                    AppController.settingInvisible = "false"
                }
            }
        })

        switchInvisible.setOnToggledListener(object : OnToggledListener {
            override fun onSwitched(labeledSwitch: LabeledSwitch?, isOn: Boolean) {
                if (isOn) {
                    AppController.settingInvisible = "true"
                } else {
                    AppController.settingInvisible = "false"
                }
            }
        })
    }
}
