package com.example.trongtuyen.carmap.activity.common

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.media.Image
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
import petrov.kristiyan.colorpicker.ColorPicker

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
    @BindView(R.id.btnVoice_setting)
    lateinit var btnVoice: Button
    @BindView(R.id.layoutMyCar_setting)
    lateinit var layoutMyCar: LinearLayout

    private var mPopupRadiusWindow: PopupWindow? = null

    private var mPopupSoundWindow: PopupWindow? = null

    private var mPopupVoiceWindow: PopupWindow? = null

    private var mPopupMyCarWindow: PopupWindow? = null

    private var mAudioPlayer = AudioPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        ButterKnife.bind(this)
        initComponents()
    }

    private fun initComponents() {

        if (AppController.settingInvisible == "invisible") {
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

        when (AppController.voiceType) {
            1 -> {
                btnVoice.background = getDrawable(R.drawable.bg_btn_send)
                btnVoice.text = "CHUẨN"
            }
            2 -> {
                btnVoice.background = getDrawable(R.drawable.bg_btn_dismiss)
                btnVoice.text = "NỮ"
            }
        }

        switchInvisible.setOnToggledListener(object : OnToggledListener {
            override fun onSwitched(labeledSwitch: LabeledSwitch?, isOn: Boolean) {
                if (isOn) {
                    // Chạy audio
                    if (AppController.soundMode == 1) {
                        when (AppController.voiceType) {
                            1 -> {
                                mAudioPlayer.play(this@SettingActivity, R.raw.an_danh_voi_tai_xe_khac)
                            }
                            2 -> {
                                mAudioPlayer.play(this@SettingActivity, R.raw.an_danh_voi_tai_xe_khac_2)
                            }
                        }
                    }
                    AppController.settingInvisible = "invisible"
                } else {
                    // Chạy audio
                    if (AppController.soundMode == 1) {
                        when (AppController.voiceType) {
                            1 -> {
                                mAudioPlayer.play(this@SettingActivity, R.raw.hien_thi_voi_tai_xe_khac)
                            }
                            2 -> {
                                mAudioPlayer.play(this@SettingActivity, R.raw.hien_thi_voi_tai_xe_khac_2)
                            }
                        }
                    }
                    AppController.settingInvisible = "visible"
                }
            }
        })

        switchSocket.setOnToggledListener(object : OnToggledListener {
            override fun onSwitched(labeledSwitch: LabeledSwitch?, isOn: Boolean) {
                if (isOn) {
                    // Chạy audio
                    if (AppController.soundMode == 1) {
                        when (AppController.voiceType) {
                            1 -> {
                                mAudioPlayer.play(this@SettingActivity, R.raw.giao_tiep_voi_tai_xe_khac)
                            }
                            2 -> {
                                mAudioPlayer.play(this@SettingActivity, R.raw.giao_tiep_voi_tai_xe_khac_2)
                            }
                        }
                    }
                    AppController.settingSocket = "true"
                } else {
                    // Chạy audio
                    if (AppController.soundMode == 1) {
                        when (AppController.voiceType) {
                            1 -> {
                                mAudioPlayer.play(this@SettingActivity, R.raw.khong_giao_tiep_voi_tai_xe_khac)
                            }
                            2 -> {
                                mAudioPlayer.play(this@SettingActivity, R.raw.khong_giao_tiep_voi_tai_xe_khac_2)
                            }
                        }
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
                when (AppController.voiceType) {
                    1 -> {
                        mAudioPlayer.play(this, R.raw.ban_kinh_hien_thi_tai_xe)
                    }
                    2 -> {
                        //CHƯA THAY
                        mAudioPlayer.play(this, R.raw.ban_kinh_hien_thi_tai_xe_2)
                    }
                }
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
                when (AppController.voiceType) {
                    1 -> {
                        mAudioPlayer.play(this, R.raw.ban_kinh_hien_thi_bao_hieu)
                    }
                    2 -> {
                        mAudioPlayer.play(this, R.raw.ban_kinh_hien_thi_bao_hieu_2)
                    }
                }
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
                    when (AppController.voiceType) {
                        1 -> {
                            mAudioPlayer.play(this, R.raw.am_thanh_mo)
                        }
                        2 -> {
                            mAudioPlayer.play(this, R.raw.am_thanh_mo_2)
                        }
                    }
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
                    when (AppController.voiceType) {
                        1 -> {
                            mAudioPlayer.play(this, R.raw.am_thanh_chi_bao_hieu)
                        }
                        2 -> {
                            mAudioPlayer.play(this, R.raw.am_thanh_chi_bao_hieu_2)
                        }
                    }
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
                    when (AppController.voiceType) {
                        1 -> {
                            mAudioPlayer.play(this, R.raw.am_thanh_tat)
                        }
                        2 -> {
                            mAudioPlayer.play(this, R.raw.am_thanh_tat_2)
                        }
                    }
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

        btnVoice.setOnClickListener {
            // Chạy audio
            if (AppController.soundMode == 1) {
                when (AppController.voiceType) {
                    1 -> {
                        mAudioPlayer.play(this, R.raw.giong_noi_huong_dan)
                    }
                    2 -> {
                        mAudioPlayer.play(this, R.raw.giong_noi_huong_dan_2)
                    }
                }
            }

            val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewPopup = inflater.inflate(R.layout.setting_voice_dialog_layout, null)
            // Dùng với layout cũ
//            mPopupSoundWindow = PopupWindow(viewPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//            mPopupSoundWindow!!.showAtLocation(it, Gravity.NO_GRAVITY, (btnReportRadius.x.toInt() / 2) + 10, (maxY / 2) - (maxY /10))

            // Layout mới
            mPopupVoiceWindow = PopupWindow(viewPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            mPopupVoiceWindow!!.showAtLocation(it, Gravity.CENTER, 0, 0)
            val layoutVoiceStandard = viewPopup.findViewById<LinearLayout>(R.id.layoutVoiceStandard_setting_voice_dialog_layout)
            val layoutVoiceGirl = viewPopup.findViewById<LinearLayout>(R.id.layoutVoiceGirl_setting_voice_dialog_layout)
            val layoutOutside = viewPopup.findViewById<LinearLayout>(R.id.bg_to_remove_setting_voice_dialog_layout)

            layoutVoiceStandard.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                btnVoice.text = "CHUẨN"
                AppController.voiceType = 1
                btnVoice.background = getDrawable(R.drawable.bg_btn_send)
                if (AppController.soundMode == 1) {
                    when (AppController.voiceType) {
                        1 -> {
                            mAudioPlayer.play(this, R.raw.chao_ban_den_voi_car_map)
                        }
                    }
                }
                mPopupVoiceWindow!!.dismiss()
            }
            layoutVoiceGirl.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                btnVoice.text = "NỮ"
                AppController.voiceType = 2
                btnVoice.background = getDrawable(R.drawable.bg_btn_dismiss)
                if (AppController.soundMode == 1) {
                    when (AppController.voiceType) {
                        2 -> {
                            mAudioPlayer.play(this, R.raw.chao_ban_den_voi_car_map_2)
                        }
                    }
                }
                mPopupVoiceWindow!!.dismiss()
            }
            layoutOutside.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                mPopupVoiceWindow!!.dismiss()
            }
        }

        layoutMyCar.setOnClickListener {

            // Chạy audio
            if (AppController.soundMode == 1) {
                when (AppController.voiceType) {
                    1 -> {
                        mAudioPlayer.play(this, R.raw.thong_tin_ve_xe_cua_ban)
                    }
                    2 -> {
                        mAudioPlayer.play(this, R.raw.thong_tin_ve_xe_cua_ban_2)
                    }
                }
            }

            val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewPopup = inflater.inflate(R.layout.setting_my_car_dialog_layout, null)

//            // Layout mới
//            mPopupMyCarWindow = PopupWindow(viewPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
//            mPopupMyCarWindow!!.showAtLocation(it, Gravity.CENTER, 0, 0)
//            val btnCon = viewPopup.findViewById<LinearLayout>(R.id.layout4_setting_my_car_dialog_layout)
//            val btnTai = viewPopup.findViewById<LinearLayout>(R.id.layout6_setting_my_car_dialog_layout)
//            val btnTai = viewPopup.findViewById<LinearLayout>(R.id.layoutTai_setting_my_car_dialog_layout)
//            val editTextModel = viewPopup.findViewById<EditText>(R.id.editTextModel_setting_my_car_dialog_layout)
//            val imColor = viewPopup.findViewById<ImageView>(R.id.imColor_setting_my_car_dialog_layout)
//            val btnChooseColor = viewPopup.findViewById<Button>(R.id.btnChooseColor_setting_my_car_dialog_layout)
//            val btnDismiss = viewPopup.findViewById<Button>(R.id.btnDismiss_setting_my_car_dialog_layout)
//            val btnConfirm = viewPopup.findViewById<Button>(R.id.btnConfirm_setting_my_car_dialog_layout)
//            val layoutOutside = viewPopup.findViewById<LinearLayout>(R.id.bg_to_remove_setting_my_car_dialog_layout)
//
////            editTextModel.clearFocus()
//            // Load dữ liệu có sẵn
//            if (AppController.userProfile!!.typeCar != "") {
//                when (AppController.userProfile!!.typeCar) {
//                    "4 cho" -> {
//                        btnCon.background = getDrawable(R.color.divider)
//                    }
//                    "68 cho" -> {
//                        btnTai.background = getDrawable(R.color.divider)
//                    }
//                    "xe tai" -> {
//                        btnTai.background = getDrawable(R.color.divider)
//                    }
//                }
//            }
//            if (AppController.userProfile!!.modelCar != "") {
//                editTextModel.setText(AppController.userProfile!!.modelCar, TextView.BufferType.EDITABLE)
//            }
//            if (AppController.userProfile!!.colorCar != "") {
//                imColor.setBackgroundColor(Color.parseColor(AppController.userProfile!!.colorCar))
//            }
//
//            var typeCar = AppController.userProfile!!.typeCar
//            btnCon.setOnClickListener {
//                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
//                btnCon.background = getDrawable(R.color.divider)
//                btnTai.background = getDrawable(R.color.background_front)
//                btnTai.background = getDrawable(R.color.background_front)
//                typeCar = "4 cho"
//            }
//            btnTai.setOnClickListener {
//                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
//                btnTai.background = getDrawable(R.color.divider)
//                btnCon.background = getDrawable(R.color.background_front)
//                btnTai.background = getDrawable(R.color.background_front)
//                typeCar = "68 cho"
//            }
//            btnTai.setOnClickListener {
//                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
//                btnTai.background = getDrawable(R.color.divider)
//                btnTai.background = getDrawable(R.color.background_front)
//                btnCon.background = getDrawable(R.color.background_front)
//                typeCar = "xe tai"
//            }
            // Layout mới
            mPopupMyCarWindow = PopupWindow(viewPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            mPopupMyCarWindow!!.showAtLocation(it, Gravity.CENTER, 0, 0)
            val btnCon = viewPopup.findViewById<LinearLayout>(R.id.layoutCon_setting_my_car_dialog_layout)
            val btnTai = viewPopup.findViewById<LinearLayout>(R.id.layoutTai_setting_my_car_dialog_layout)
            val btnKhach = viewPopup.findViewById<LinearLayout>(R.id.layoutKhach_setting_my_car_dialog_layout)
            val btnContainer = viewPopup.findViewById<LinearLayout>(R.id.layoutContainer_setting_my_car_dialog_layout)
            val editTextModel = viewPopup.findViewById<EditText>(R.id.editTextModel_setting_my_car_dialog_layout)
            val imColor = viewPopup.findViewById<ImageView>(R.id.imColor_setting_my_car_dialog_layout)
            val btnChooseColor = viewPopup.findViewById<Button>(R.id.btnChooseColor_setting_my_car_dialog_layout)
            val btnDismiss = viewPopup.findViewById<Button>(R.id.btnDismiss_setting_my_car_dialog_layout)
            val btnConfirm = viewPopup.findViewById<Button>(R.id.btnConfirm_setting_my_car_dialog_layout)
            val layoutOutside = viewPopup.findViewById<LinearLayout>(R.id.bg_to_remove_setting_my_car_dialog_layout)

//            editTextModel.clearFocus()
            // Load dữ liệu có sẵn
            if (AppController.userProfile!!.typeCar != "") {
                when (AppController.userProfile!!.typeCar) {
                    "xe con" -> {
                        btnCon.background = getDrawable(R.color.divider)
                    }
                    "xe tai" -> {
                        btnTai.background = getDrawable(R.color.divider)
                    }
                    "xe khach" -> {
                        btnKhach.background = getDrawable(R.color.divider)
                    }
                    "xe container" -> {
                        btnContainer.background = getDrawable(R.color.divider)
                    }
                }
            }
            if (AppController.userProfile!!.modelCar != "") {
                editTextModel.setText(AppController.userProfile!!.modelCar, TextView.BufferType.EDITABLE)
            }
            if (AppController.userProfile!!.colorCar != "") {
                imColor.setBackgroundColor(Color.parseColor(AppController.userProfile!!.colorCar))
            }


            var typeCar = AppController.userProfile!!.typeCar
            btnCon.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                btnCon.background = getDrawable(R.color.divider)
                btnTai.background = getDrawable(R.color.background_front)
                btnKhach.background = getDrawable(R.color.background_front)
                btnContainer.background = getDrawable(R.color.background_front)
                typeCar = "xe con"
            }
            btnTai.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                btnTai.background = getDrawable(R.color.divider)
                btnCon.background = getDrawable(R.color.background_front)
                btnKhach.background = getDrawable(R.color.background_front)
                btnContainer.background = getDrawable(R.color.background_front)
                typeCar = "xe tai"
            }
            btnKhach.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                btnKhach.background = getDrawable(R.color.divider)
                btnCon.background = getDrawable(R.color.background_front)
                btnTai.background = getDrawable(R.color.background_front)
                btnContainer.background = getDrawable(R.color.background_front)
                typeCar = "xe khach"
            }
            btnContainer.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                btnContainer.background = getDrawable(R.color.divider)
                btnCon.background = getDrawable(R.color.background_front)
                btnTai.background = getDrawable(R.color.background_front)
                btnKhach.background = getDrawable(R.color.background_front)
                typeCar = "xe container"
            }

            btnChooseColor.setOnClickListener {
                val colorsHexList = arrayListOf<String>("#000000", "#7a7172", "#c6c2c3", "#ff071c", "#ff0766", "#ff07ac", "#c507ff", "#6a07ff", "#2807ff", "#077bff", "#07bdff", "#07ffea", "#204734", "#39ff07", "#faff07", "#ffcd07", "#ff8b07", "#ffffff")
                val colorPicker: ColorPicker = ColorPicker(this)
                colorPicker.setOnChooseColorListener(object : ColorPicker.OnChooseColorListener {
                    override fun onChooseColor(position: Int, color: Int) {
                        if (position == -1) {
                            AppController.userProfile!!.colorCar = "#000000"
                        } else {
                            AppController.userProfile!!.colorCar = colorsHexList[position]
                        }
                        imColor.setBackgroundColor(Color.parseColor(AppController.userProfile!!.colorCar))
                    }

                    override fun onCancel() {
                        colorPicker.dismissDialog()
                    }

                }).setColumns(6).setColors(colorsHexList).show()
            }


            layoutOutside.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                mPopupMyCarWindow!!.dismiss()
            }

            btnDismiss.setOnClickListener {
                mPopupMyCarWindow!!.dismiss()
            }

            btnConfirm.setOnClickListener {
                AppController.userProfile!!.typeCar = typeCar
                AppController.userProfile!!.modelCar = editTextModel.text.toString()
                mPopupMyCarWindow!!.dismiss()
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
