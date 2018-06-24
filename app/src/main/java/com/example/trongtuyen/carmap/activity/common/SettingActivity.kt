package com.example.trongtuyen.carmap.activity.common

import android.app.Activity
import android.content.Context
import android.graphics.Point
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
    @BindView(R.id.imBack_setting)
    lateinit var btnBack: ImageView
    @BindView(R.id.btnUserRadius_setting)
    lateinit var btnUserRadius: Button
    @BindView(R.id.btnReportRadius_setting)
    lateinit var btnReportRadius: Button
    @BindView(R.id.btnSound_setting)
    lateinit var btnSound: Button

    private var mPopupRadiusWindow: PopupWindow? = null

    private var mPopupSoundWindow: PopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        ButterKnife.bind(this)
        initComponents()
    }

    private fun initComponents() {

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

        when (AppController.settingUserRadius) {
            2000 -> {
                btnUserRadius.text = "2 Km"
            }
            5000 -> {
                btnUserRadius.text = "5 Km"
            }
            10000 -> {
                btnUserRadius.text = "10 Km"
            }
            25000 -> {
                btnUserRadius.text = "25 Km"
            }
        }

        when (AppController.settingReportRadius) {
            2000 -> {
                btnReportRadius.text = "2 Km"
            }
            5000 -> {
                btnReportRadius.text = "5 Km"
            }
            10000 -> {
                btnReportRadius.text = "10 Km"
            }
            25000 -> {
                btnReportRadius.text = "25 Km"
            }
        }

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

        val mdisp = windowManager.defaultDisplay
        val mdispSize = Point()
        mdisp.getSize(mdispSize)
        val maxX = mdispSize.x
        val maxY = mdispSize.y

        btnUserRadius.setOnClickListener {
            val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewPopup = inflater.inflate(R.layout.setting_radius_dialog_layout, null)
            mPopupRadiusWindow = PopupWindow(viewPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            mPopupRadiusWindow!!.showAtLocation(it, Gravity.NO_GRAVITY, (btnUserRadius.x.toInt() / 2) + 10, (maxY / 2) - (maxY /10))

            val tv2km = viewPopup.findViewById<TextView>(R.id.tv2km_seting_radius_dialog_layout)
            val tv5km = viewPopup.findViewById<TextView>(R.id.tv5km_seting_radius_dialog_layout)
            val tv10km = viewPopup.findViewById<TextView>(R.id.tv10km_seting_radius_dialog_layout)
            val tv25km = viewPopup.findViewById<TextView>(R.id.tv25km_seting_radius_dialog_layout)
            val tvInfo = viewPopup.findViewById<TextView>(R.id.tvInfo_setting_radius_dialog_layout)

            tvInfo.text = "Bán kính hiển thị tài xế"

            tv2km.setOnClickListener {
                btnUserRadius.text = "2 Km"
                AppController.settingUserRadius = 2000
                mPopupRadiusWindow!!.dismiss()
            }
            tv5km.setOnClickListener {
                btnUserRadius.text = "5 Km"
                AppController.settingUserRadius = 5000
                mPopupRadiusWindow!!.dismiss()
            }
            tv10km.setOnClickListener {
                btnUserRadius.text = "10 Km"
                AppController.settingUserRadius = 10000
                mPopupRadiusWindow!!.dismiss()
            }
            tv25km.setOnClickListener {
                btnUserRadius.text = "25 Km"
                AppController.settingUserRadius = 25000
                mPopupRadiusWindow!!.dismiss()
            }
        }

        btnReportRadius.setOnClickListener {
            val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewPopup = inflater.inflate(R.layout.setting_radius_dialog_layout, null)
            mPopupRadiusWindow = PopupWindow(viewPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            mPopupRadiusWindow!!.showAtLocation(it, Gravity.NO_GRAVITY, (btnReportRadius.x.toInt() / 2) + 10, (maxY / 2) - (maxY /10))

            val tv2km = viewPopup.findViewById<TextView>(R.id.tv2km_seting_radius_dialog_layout)
            val tv5km = viewPopup.findViewById<TextView>(R.id.tv5km_seting_radius_dialog_layout)
            val tv10km = viewPopup.findViewById<TextView>(R.id.tv10km_seting_radius_dialog_layout)
            val tv25km = viewPopup.findViewById<TextView>(R.id.tv25km_seting_radius_dialog_layout)
            val tvInfo = viewPopup.findViewById<TextView>(R.id.tvInfo_setting_radius_dialog_layout)

            tvInfo.text = "Bán kính hiển thị báo hiệu"

            tv2km.setOnClickListener {
                btnReportRadius.text = "2 Km"
                AppController.settingReportRadius = 2000
                mPopupRadiusWindow!!.dismiss()
            }
            tv5km.setOnClickListener {
                btnReportRadius.text = "5 Km"
                AppController.settingReportRadius = 5000
                mPopupRadiusWindow!!.dismiss()
            }
            tv10km.setOnClickListener {
                btnReportRadius.text = "10 Km"
                AppController.settingReportRadius = 10000
                mPopupRadiusWindow!!.dismiss()
            }
            tv25km.setOnClickListener {
                btnReportRadius.text = "25 Km"
                AppController.settingReportRadius = 25000
                mPopupRadiusWindow!!.dismiss()
            }
        }

        btnSound.setOnClickListener {
            val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewPopup = inflater.inflate(R.layout.setting_sound_dialog_layout, null)
            mPopupSoundWindow = PopupWindow(viewPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            mPopupSoundWindow!!.showAtLocation(it, Gravity.NO_GRAVITY, (btnReportRadius.x.toInt() / 2) + 10, (maxY / 2) - (maxY /10))

            val layoutSoundOn = viewPopup.findViewById<LinearLayout>(R.id.layoutSoundOn_setting_sound_dialog_layout)
            val layoutSoundAlert = viewPopup.findViewById<LinearLayout>(R.id.layoutSoundAlert_setting_sound_dialog_layout)
            val layoutSoundOff = viewPopup.findViewById<LinearLayout>(R.id.layoutSoundOff_setting_sound_dialog_layout)

            layoutSoundOn.setOnClickListener {
                btnSound.text = "MỞ"
                AppController.soundMode = 1
                btnSound.background = getDrawable(R.drawable.bg_btn_send)
                mPopupSoundWindow!!.dismiss()
            }
            layoutSoundAlert.setOnClickListener {
                btnSound.text = "BÁO CÁO"
                AppController.soundMode = 2
                btnSound.background = getDrawable(R.drawable.bg_btn_dismiss)
                mPopupSoundWindow!!.dismiss()
            }
            layoutSoundOff.setOnClickListener {
                btnSound.text = "TẮT"
                AppController.soundMode = 3
                btnSound.background = getDrawable(R.drawable.bg_btn_gray_dark)
                mPopupSoundWindow!!.dismiss()
            }
        }

        btnBack.setOnClickListener {
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}
