package com.example.trongtuyen.carmap.activity.common

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.controllers.AppController
import com.example.trongtuyen.carmap.utils.AudioPlayer
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

    private var mAudioPlayer = AudioPlayer()

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
                btnSound.background = getDrawable(R.drawable.bg_btn_dismiss)
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
                    // Chạy audio
                    if (AppController.soundMode == 1) {
                        mAudioPlayer.play(this@SettingActivity, R.raw.an_danh_voi_tai_xe_khac)
                    }
                    AppController.settingInvisible = "true"
                } else {
                    // Chạy audio
                    if (AppController.soundMode == 1) {
                        mAudioPlayer.play(this@SettingActivity, R.raw.hien_thi_voi_tai_xe_khac)
                    }
                    AppController.settingInvisible = "false"
                }
            }
        })

        switchSocket.setOnToggledListener(object : OnToggledListener {
            override fun onSwitched(labeledSwitch: LabeledSwitch?, isOn: Boolean) {
                if (isOn) {
                    // Chạy audio
                    if (AppController.soundMode == 1) {
                        mAudioPlayer.play(this@SettingActivity, R.raw.giao_tiep_voi_tai_xe_khac)
                    }
                    AppController.settingSocket = "true"
                } else {
                    // Chạy audio
                    if (AppController.soundMode == 1) {
                        mAudioPlayer.play(this@SettingActivity, R.raw.khong_giao_tiep_voi_tai_xe_khac)
                    }
                    AppController.settingSocket = "false"
                }
            }
        })

        // Dùng với layout cũ
//        val mdisp = windowManager.defaultDisplay
//        val mdispSize = Point()
//        mdisp.getSize(mdispSize)
//        val maxX = mdispSize.x
//        val maxY = mdispSize.y

        btnUserRadius.setOnClickListener {

            // Chạy audio
            if (AppController.soundMode == 1) {
                mAudioPlayer.play(this@SettingActivity, R.raw.ban_kinh_hien_thi_tai_xe)
            }

            val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewPopup = inflater.inflate(R.layout.setting_radius_dialog_layout, null)
            // Dùng với layout cũ
//            mPopupRadiusWindow = PopupWindow(viewPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//            mPopupRadiusWindow!!.showAtLocation(it, Gravity.NO_GRAVITY, (btnUserRadius.x.toInt() / 2) + 10, (maxY / 2) - (maxY /10))

            // Layout mới
            mPopupRadiusWindow = PopupWindow(viewPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            mPopupRadiusWindow!!.showAtLocation(it, Gravity.CENTER, 0, 0)
            val tv2km = viewPopup.findViewById<TextView>(R.id.tv2km_seting_radius_dialog_layout)
            val tv5km = viewPopup.findViewById<TextView>(R.id.tv5km_seting_radius_dialog_layout)
            val tv10km = viewPopup.findViewById<TextView>(R.id.tv10km_seting_radius_dialog_layout)
            val tv25km = viewPopup.findViewById<TextView>(R.id.tv25km_seting_radius_dialog_layout)
            val tvInfo = viewPopup.findViewById<TextView>(R.id.tvInfo_setting_radius_dialog_layout)
            val layoutOutside = viewPopup.findViewById<LinearLayout>(R.id.bg_to_remove_setting_radius_dialog_layout)

            tvInfo.text = "Bán kính hiển thị tài xế"

            tv2km.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                btnUserRadius.text = "2 Km"
                AppController.settingUserRadius = 2000
                mPopupRadiusWindow!!.dismiss()
            }
            tv5km.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                btnUserRadius.text = "5 Km"
                AppController.settingUserRadius = 5000
                mPopupRadiusWindow!!.dismiss()
            }
            tv10km.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                btnUserRadius.text = "10 Km"
                AppController.settingUserRadius = 10000
                mPopupRadiusWindow!!.dismiss()
            }
            tv25km.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                btnUserRadius.text = "25 Km"
                AppController.settingUserRadius = 25000
                mPopupRadiusWindow!!.dismiss()
            }
            layoutOutside.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                mPopupRadiusWindow!!.dismiss()
            }
        }

        btnReportRadius.setOnClickListener {

            // Chạy audio
            if (AppController.soundMode == 1) {
                mAudioPlayer.play(this@SettingActivity, R.raw.ban_kinh_hien_thi_bao_hieu)
            }

            val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewPopup = inflater.inflate(R.layout.setting_radius_dialog_layout, null)

            // Dùng với layout cũ
//            mPopupRadiusWindow = PopupWindow(viewPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//            mPopupRadiusWindow!!.showAtLocation(it, Gravity.NO_GRAVITY, (btnReportRadius.x.toInt() / 2) + 10, (maxY / 2) - (maxY /10))

            // Layout mới
            mPopupRadiusWindow = PopupWindow(viewPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            mPopupRadiusWindow!!.showAtLocation(it, Gravity.CENTER, 0, 0)
            val tv2km = viewPopup.findViewById<TextView>(R.id.tv2km_seting_radius_dialog_layout)
            val tv5km = viewPopup.findViewById<TextView>(R.id.tv5km_seting_radius_dialog_layout)
            val tv10km = viewPopup.findViewById<TextView>(R.id.tv10km_seting_radius_dialog_layout)
            val tv25km = viewPopup.findViewById<TextView>(R.id.tv25km_seting_radius_dialog_layout)
            val tvInfo = viewPopup.findViewById<TextView>(R.id.tvInfo_setting_radius_dialog_layout)
            val layoutOutside = viewPopup.findViewById<LinearLayout>(R.id.bg_to_remove_setting_radius_dialog_layout)

            tvInfo.text = "Bán kính hiển thị báo hiệu"

            tv2km.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                btnReportRadius.text = "2 Km"
                AppController.settingReportRadius = 2000
                mPopupRadiusWindow!!.dismiss()
            }
            tv5km.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                btnReportRadius.text = "5 Km"
                AppController.settingReportRadius = 5000
                mPopupRadiusWindow!!.dismiss()
            }
            tv10km.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                btnReportRadius.text = "10 Km"
                AppController.settingReportRadius = 10000
                mPopupRadiusWindow!!.dismiss()
            }
            tv25km.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                btnReportRadius.text = "25 Km"
                AppController.settingReportRadius = 25000
                mPopupRadiusWindow!!.dismiss()
            }
            layoutOutside.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                mPopupRadiusWindow!!.dismiss()
            }
        }

        btnSound.setOnClickListener {
            val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewPopup = inflater.inflate(R.layout.setting_sound_dialog_layout, null)
            // Dùng với layout cũ
//            mPopupSoundWindow = PopupWindow(viewPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//            mPopupSoundWindow!!.showAtLocation(it, Gravity.NO_GRAVITY, (btnReportRadius.x.toInt() / 2) + 10, (maxY / 2) - (maxY /10))

            // Layout mới
            mPopupSoundWindow = PopupWindow(viewPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            mPopupSoundWindow!!.showAtLocation(it, Gravity.CENTER, 0, 0)
            val layoutSoundOn = viewPopup.findViewById<LinearLayout>(R.id.layoutSoundOn_setting_sound_dialog_layout)
            val layoutSoundAlert = viewPopup.findViewById<LinearLayout>(R.id.layoutSoundAlert_setting_sound_dialog_layout)
            val layoutSoundOff = viewPopup.findViewById<LinearLayout>(R.id.layoutSoundOff_setting_sound_dialog_layout)
            val layoutOutside = viewPopup.findViewById<LinearLayout>(R.id.bg_to_remove_setting_sound_dialog_layout)

            layoutSoundOn.setOnClickListener {
                // Chạy audio
                if (AppController.soundMode == 1) {
                    mAudioPlayer.play(this@SettingActivity, R.raw.am_thanh_mo)
                }
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                btnSound.text = "MỞ"
                AppController.soundMode = 1
                btnSound.background = getDrawable(R.drawable.bg_btn_send)
                mPopupSoundWindow!!.dismiss()
            }
            layoutSoundAlert.setOnClickListener {
                // Chạy audio
                if (AppController.soundMode == 1) {
                    mAudioPlayer.play(this@SettingActivity, R.raw.am_thanh_chi_bao_hieu)
                }
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                btnSound.text = "BÁO HIỆU"
                AppController.soundMode = 2
                btnSound.background = getDrawable(R.drawable.bg_btn_dismiss)
                mPopupSoundWindow!!.dismiss()
            }
            layoutSoundOff.setOnClickListener {
                // Chạy audio
                if (AppController.soundMode == 1) {
                    mAudioPlayer.play(this@SettingActivity, R.raw.am_thanh_tat)
                }
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                btnSound.text = "TẮT"
                AppController.soundMode = 3
                btnSound.background = getDrawable(R.drawable.bg_btn_gray_dark)
                mPopupSoundWindow!!.dismiss()
            }
            layoutOutside.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                mPopupSoundWindow!!.dismiss()
            }
        }

        btnBack.setOnClickListener {
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
