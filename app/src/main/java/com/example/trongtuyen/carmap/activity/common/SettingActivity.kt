package com.example.trongtuyen.carmap.activity.common

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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

        switchSocket.setOnToggledListener(object : OnToggledListener {
            override fun onSwitched(labeledSwitch: LabeledSwitch?, isOn: Boolean) {
                if (isOn) {
                    AppController.settingSocket = "true"
                } else {
                    AppController.settingSocket = "false"
                }
            }
        })

        btnRadius.setOnClickListener {
            val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewPopup = inflater.inflate(R.layout.setting_radius_dialog_layout, null)
            val mPopupWindow = PopupWindow(viewPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            mPopupWindow.showAtLocation(this.currentFocus, Gravity.BOTTOM, 0, 0)

            val tv2km = viewPopup.findViewById<TextView>(R.id.tv2km_seting_radius_dialog_layout)
            val tv5km = viewPopup.findViewById<TextView>(R.id.tv5km_seting_radius_dialog_layout)
            val tv10km = viewPopup.findViewById<TextView>(R.id.tv10km_seting_radius_dialog_layout)
            val tv25km = viewPopup.findViewById<TextView>(R.id.tv25km_seting_radius_dialog_layout)

            tv2km.setOnClickListener {
                btnRadius.text = "2 Km"
                AppController.settingRadius = 2
                mPopupWindow.dismiss()
            }
            tv5km.setOnClickListener {
                btnRadius.text = "5 Km"
                AppController.settingRadius = 5
                mPopupWindow.dismiss()
            }
            tv10km.setOnClickListener {
                btnRadius.text = "10 Km"
                AppController.settingRadius = 10
                mPopupWindow.dismiss()
            }
            tv25km.setOnClickListener {
                btnRadius.text = "25 Km"
                AppController.settingRadius = 25
                mPopupWindow.dismiss()
            }
        }

        btnSound.setOnClickListener {
            val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewPopup = inflater.inflate(R.layout.setting_sound_dialog_layout, null)
            val mPopupWindow = PopupWindow(viewPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            mPopupWindow.showAtLocation(this.currentFocus, Gravity.BOTTOM, 0, 0)

            val layoutSoundOn = viewPopup.findViewById<LinearLayout>(R.id.layoutSoundOn_setting_sound_dialog_layout)
            val layoutSoundAlert = viewPopup.findViewById<LinearLayout>(R.id.layoutSoundAlert_setting_sound_dialog_layout)
            val layoutSoundOff = viewPopup.findViewById<LinearLayout>(R.id.layoutSoundOff_setting_sound_dialog_layout)

            layoutSoundOn.setOnClickListener {
                btnSound.text = "MỞ"
                AppController.soundMode = 1
                btnSound.background = getDrawable(R.drawable.bg_btn_send)
                mPopupWindow.dismiss()
            }
            layoutSoundAlert.setOnClickListener {
                btnSound.text = "BÁO CÁO"
                AppController.soundMode = 2
                btnSound.background = getDrawable(R.drawable.bg_btn_dismiss)
                mPopupWindow.dismiss()
            }
            layoutSoundOff.setOnClickListener {
                btnSound.text = "TẮT"
                AppController.soundMode = 3
                btnSound.background = getDrawable(R.drawable.bg_btn_gray_dark)
                mPopupWindow.dismiss()
            }
        }
    }
}
