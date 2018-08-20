package com.example.trongtuyen.carmap.activity.common

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.controllers.AppController
import com.example.trongtuyen.carmap.models.Report
import com.example.trongtuyen.carmap.services.APIServiceGenerator
import com.example.trongtuyen.carmap.services.ErrorUtils
import com.example.trongtuyen.carmap.services.ReportService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.LinearLayout
import com.example.trongtuyen.carmap.services.models.ReportResponse
import com.example.trongtuyen.carmap.utils.AudioPlayer
import com.example.trongtuyen.carmap.utils.FileUtils
import com.sdsmdg.tastytoast.TastyToast
import java.io.File
import java.io.IOException


class ReportHazardActivity : AppCompatActivity() {

    @BindView(R.id.imvHazardOnRoad_report_hazard)
    lateinit var btnHazardOnRoad: LinearLayout
    @BindView(R.id.imvHazardShoulder_report_hazard)
    lateinit var btnHazardShoulder: LinearLayout
    @BindView(R.id.imvHazardWeather_report_hazard)
    lateinit var btnHazardWeather: LinearLayout

    @BindView(R.id.tvTitle_report_hazard)
    lateinit var tvTitle: TextView

    @BindView(R.id.txtMess_report_hazard)
    lateinit var textInputEdit: EditText
    @BindView(R.id.btnSend_report_hazard)
    lateinit var btnSend: Button
    @BindView(R.id.imHazardOnRoad_report_hazard)
    lateinit var imHazardOnRoad: ImageView
    @BindView(R.id.imHazardShoulder_report_hazard)
    lateinit var imHazardShoulder: ImageView
    @BindView(R.id.imHazardWeather_report_hazard)
    lateinit var imHazardWeather: ImageView
    @BindView(R.id.tvHazardOnRoad_report_hazard)
    lateinit var tvHazardOnRoad: TextView
    @BindView(R.id.tvHazardShoulder_report_hazard)
    lateinit var tvHazardShoulder: TextView
    @BindView(R.id.tvHazardWeather_report_hazard)
    lateinit var tvHazardWeather: TextView

    @BindView(R.id.btnClose_report_hazard)
    lateinit var btnCLose: ImageView
    @BindView(R.id.btnDismiss_report_hazard)
    lateinit var btnDismiss: Button

    @BindView(R.id.layout_record_report_hazard)
    lateinit var btnRecord: LinearLayout
    @BindView(R.id.layout_take_photo_report_hazard)
    lateinit var btnTakePhoto: LinearLayout

    @BindView(R.id.tvRecord_report_hazard)
    lateinit var tvRecord: TextView
    @BindView(R.id.tvTakePhoto_report_hazard)
    lateinit var tvTakePhoto: TextView

    @BindView(R.id.over_layout_report_hazard)
    lateinit var layoutReport: RelativeLayout

    private var subType1: String = ""
    private var subType2: String = ""

    private var sFileAudioPath: String = ""

    private var sBase64Image: String = ""

    // ==== Dùng cho lấy chất lượng ảnh JPEG gốc, bằng cách chụp xong lưu file ảnh lại
    private lateinit var photoURI: Uri

    // Audio Player
    private var mAudioPlayer = AudioPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_hazard)
//        val mInflater = LayoutInflater.from(this)
//        val contentView = mInflater.inflate(R.layout.activity_report_hazard, null)
//        val mLayout = contentView.findViewById(R.id.relativeLayout_activity_report_hazard) as RelativeLayout
//        setContentView(mLayout)

//        val view : View = View.inflate(this,R.layout.custom_bottom_sheet_dialog_6_items, null)
//        this.addContentView(view, LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT))
//        view.visibility = View.GONE

        ButterKnife.bind(this)
        initComponents()
    }

    private fun initComponents() {

        // Các nút báo cáo
        btnHazardOnRoad.setOnClickListener {
            subType1 = "on_road"
            btnHazardOnRoad.background = getDrawable(R.color.button_bg_inactive)
            btnHazardShoulder.background = null
            btnHazardWeather.background = null
            tvTitle.text = "TRÊN ĐƯỜNG ĐI"

            // Đặt 2 bên còn lại về ban đầu
            imHazardShoulder.setImageResource(R.drawable.ic_report_hazard_shoulder)
            tvHazardShoulder.text = "Bên lề"
            imHazardWeather.setImageResource(R.drawable.ic_report_hazard_weather)
            tvHazardWeather.text = "Thời tiết"

            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onOpenHazardOnRoad()
        }
        btnHazardShoulder.setOnClickListener {
            subType1 = "shoulder"
            btnHazardShoulder.background = getDrawable(R.color.button_bg_inactive)
            btnHazardOnRoad.background = null
            btnHazardWeather.background = null
            tvTitle.text = "LỀ ĐƯỜNG"

            // Đặt 2 bên còn lại về ban đầu
            imHazardOnRoad.setImageResource(R.drawable.ic_report_hazard_road)
            tvHazardOnRoad.text = "Trên đường"
            imHazardWeather.setImageResource(R.drawable.ic_report_hazard_weather)
            tvHazardWeather.text = "Thời tiết"

            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onOpenHazardShoulder()
        }
        btnHazardWeather.setOnClickListener {
            subType1 = "weather"
            btnHazardWeather.background = getDrawable(R.color.button_bg_inactive)
            btnHazardShoulder.background = null
            btnHazardOnRoad.background = null
            tvTitle.text = "THỜI TIẾT"

            // Đặt 2 bên còn lại về ban đầu
            imHazardOnRoad.setImageResource(R.drawable.ic_report_hazard_road)
            tvHazardOnRoad.text = "Trên đường"
            imHazardShoulder.setImageResource(R.drawable.ic_report_hazard_shoulder)
            tvHazardShoulder.text = "Bên lề"

            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onOpenHazardWeather()
        }
        btnSend.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            if (mCurrentPhotoPath != "") {
                val oldFile = File(mCurrentPhotoPath)
                oldFile.delete()
            }
            onSend()
        }
        btnCLose.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            if (mCurrentPhotoPath != "") {
                val oldFile = File(mCurrentPhotoPath)
                oldFile.delete()
            }
            onClose()
        }
        btnDismiss.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            if (mCurrentPhotoPath != "") {
                val oldFile = File(mCurrentPhotoPath)
                oldFile.delete()
            }
            onClose()
        }

        btnRecord.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val intent = Intent(this, AudioRecordActivity::class.java)
            startActivityForResult(intent, 0)
        }

        btnTakePhoto.setOnClickListener {
            //            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
//            if (mCurrentPhotoPath != "") {
//                val oldFile = File(mCurrentPhotoPath)
//                oldFile.delete()
//            }
//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            startActivityForResult(intent, 1)

            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            // Xoá ảnh cũ
            if (mCurrentPhotoPath != "") {
                val oldFile = File(mCurrentPhotoPath)
                oldFile.delete()
            }

//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            startActivityForResult(intent, 1)

//             ==== Dùng cho lấy chất lượng ảnh JPEG gốc, bằng cách chụp xong lưu file ảnh lại
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                var photoFile: File? = null
                try {
                    photoFile = createImageFile();
                } catch (e: IOException) {
                    // Error occurred while creating the File
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    photoURI = FileProvider.getUriForFile(this,
                            "com.example.android.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, 1);
                }
            }
        }

        layoutReport.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            if (mCurrentPhotoPath != "") {
                val oldFile = File(mCurrentPhotoPath)
                oldFile.delete()
            }
            finish()
        }
    }

    private fun onClose() {
        finish()
    }

    private fun onSend() {
        if (subType1 == "" || subType2 == "") {
            TastyToast.makeText(this, "Vui lòng chọn loại nguy hiểm", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
        } else {
            when (subType1) {
                "on_road" -> {
                    if (subType2 == "object" || subType2 == "construction" || subType2 == "broken_light" || subType2 == "pothole" || subType2 == "vehicle_stop" || subType2 == "road_kill") {
//                        TastyToast.makeText(this, "Loại: " + subType1 + " " + subType2 + " " + textInputEdit.text.toString(), TastyToast.LENGTH_SHORT, TastyToast.).show()
                        if (sFileAudioPath == "" || sBase64Image == "") {
                            // Encode file ghi âm
                            val encoded = FileUtils.encodeAudioFile(sFileAudioPath)
                            val mReport = Report("hazard", subType1, subType2, textInputEdit.text.toString(), AppController.userProfile!!.currentLocation!!, AppController.userProfile!!._id.toString(), 1, 0, false, encoded, sBase64Image, "")
                            onAddNewReportHazard(mReport, false)
                        } else {
                            val mReport = Report("hazard", subType1, subType2, textInputEdit.text.toString(), AppController.userProfile!!.currentLocation!!, AppController.userProfile!!._id.toString(), 1, 0, false, "", sBase64Image, "")
                            onAddNewReportHazard(mReport, true)
                        }
                    } else {
                        TastyToast.makeText(this, "Vui lòng chọn loại nguy hiểm", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
                    }
                }
                "shoulder" -> {
                    if (subType2 == "vehicle_stop" || subType2 == "animal" || subType2 == "missing_sign") {
//                        TastyToast.makeText(this, "Loại: " + subType1 + " " + subType2 + " " + textInputEdit.text.toString(), TastyToast.LENGTH_SHORT, TastyToast.).show()
                        if (sFileAudioPath == "" || sBase64Image == "") {
                            // Encode file ghi âm
                            val encoded = FileUtils.encodeAudioFile(sFileAudioPath)
                            val mReport = Report("hazard", subType1, subType2, textInputEdit.text.toString(), AppController.userProfile!!.currentLocation!!, AppController.userProfile!!._id.toString(), 1, 0, false, encoded, sBase64Image, "")
                            onAddNewReportHazard(mReport, false)
                        } else {
                            val mReport = Report("hazard", subType1, subType2, textInputEdit.text.toString(), AppController.userProfile!!.currentLocation!!, AppController.userProfile!!._id.toString(), 1, 0, false, "", sBase64Image, "")
                            onAddNewReportHazard(mReport, true)
                        }
                    } else {
                        TastyToast.makeText(this, "Vui lòng chọn loại nguy hiểm", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
                    }
                }
                "weather" -> {
                    if (subType2 == "fog" || subType2 == "hail" || subType2 == "flood" || subType2 == "ice") {
//                        TastyToast.makeText(this, "Loại: " + subType1 + " " + subType2 + " " + textInputEdit.text.toString(), TastyToast.LENGTH_SHORT, TastyToast.).show()
                        if (sFileAudioPath == "" || sBase64Image == "") {
                            // Encode file ghi âm
                            val encoded = FileUtils.encodeAudioFile(sFileAudioPath)
                            val mReport = Report("hazard", subType1, subType2, textInputEdit.text.toString(), AppController.userProfile!!.currentLocation!!, AppController.userProfile!!._id.toString(), 1, 0, false, encoded, sBase64Image, "")
                            onAddNewReportHazard(mReport, false)
                        } else {
                            val mReport = Report("hazard", subType1, subType2, textInputEdit.text.toString(), AppController.userProfile!!.currentLocation!!, AppController.userProfile!!._id.toString(), 1, 0, false, "", sBase64Image, "")
                            onAddNewReportHazard(mReport, true)
                        }
                    } else {
                        TastyToast.makeText(this, "Vui lòng chọn loại nguy hiểm", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
                    }
                }
            }
        }
    }

    private fun onAddNewReportHazard(report: Report, bothAudioAndImage: Boolean) {
        val service = APIServiceGenerator.createService(ReportService::class.java)

        val call = service.addNewReport(report)
        call.enqueue(object : Callback<Report> {
            override fun onFailure(call: Call<Report>?, t: Throwable?) {
                TastyToast.makeText(this@ReportHazardActivity, "Gửi báo hiệu thất bại!", TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
            }

            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                if (response.isSuccessful) {
                    if (bothAudioAndImage == true) {
                        // Encode file ghi âm
                        val encoded = FileUtils.encodeAudioFile(sFileAudioPath)
                        onUpdateBase64Voice(response.body()!!._id.toString(), encoded)
                        finish()
                    } else {
                        // Chạy audio
                        if (AppController.soundMode == 1) {
                            mAudioPlayer.play(this@ReportHazardActivity, R.raw.gui_bao_hieu_thanh_cong)
                        }
                        TastyToast.makeText(this@ReportHazardActivity, "Gửi báo hiệu thành công!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show()
                        finish()
                    }
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    TastyToast.makeText(this@ReportHazardActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }
        })
    }

    private fun onOpenHazardOnRoad() {
        val customBottomSheetView = layoutInflater.inflate(R.layout.custom_bottom_sheet_dialog_6_items, null)

        val btnObject = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame1_6_items)
        val btnConstruction = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame2_6_items)
        val btnBrokenLight = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame3_6_items)
        val btnPothole = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame4_6_items)
        val btnVehicleStop = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame5_6_items)
        val btnRoadkill = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame6_6_items)

        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(customBottomSheetView)
        btnObject.setOnClickListener {
            // Chạy audio
            if (AppController.soundMode == 1) {
                mAudioPlayer.play(this, R.raw.vat_can)
            }
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            subType2 = "object"
            bottomSheetDialog.dismiss()
        }
        btnConstruction.setOnClickListener {
            // Chạy audio
            if (AppController.soundMode == 1) {
                mAudioPlayer.play(this, R.raw.cong_trinh)
            }
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            subType2 = "construction"
            bottomSheetDialog.dismiss()
        }
        btnBrokenLight.setOnClickListener {
            // Chạy audio
            if (AppController.soundMode == 1) {
                mAudioPlayer.play(this, R.raw.den_bao_hu)
            }
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            subType2 = "broken_light"
            bottomSheetDialog.dismiss()
        }
        btnPothole.setOnClickListener {
            // Chạy audio
            if (AppController.soundMode == 1) {
                mAudioPlayer.play(this, R.raw.ho_voi)
            }
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            subType2 = "pothole"
            bottomSheetDialog.dismiss()
        }
        btnVehicleStop.setOnClickListener {
            // Chạy audio
            if (AppController.soundMode == 1) {
                mAudioPlayer.play(this, R.raw.xe_dau)
            }
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            subType2 = "vehicle_stop"
            bottomSheetDialog.dismiss()
        }
        btnRoadkill.setOnClickListener {
            // Chạy audio
            if (AppController.soundMode == 1) {
                mAudioPlayer.play(this, R.raw.dong_vat_chet_tren_duong)
            }
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            subType2 = "road_kill"
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.setCanceledOnTouchOutside(true)

        bottomSheetDialog.setOnDismissListener {
            when (subType2) {
                "object" -> {
                    imHazardOnRoad.setImageResource(R.drawable.ic_report_hazard_object)
                    tvHazardOnRoad.text = "Vật cản"
                }
                "construction" -> {
                    imHazardOnRoad.setImageResource(R.drawable.ic_report_hazard_construction)
                    tvHazardOnRoad.text = "Công trình"
                }
                "broken_light" -> {
                    imHazardOnRoad.setImageResource(R.drawable.ic_report_broken_traffic_light)
                    tvHazardOnRoad.text = "Đèn hư"
                }
                "pothole" -> {
                    imHazardOnRoad.setImageResource(R.drawable.ic_report_hazard_pothole)
                    tvHazardOnRoad.text = "Ổ voi"
                }
                "vehicle_stop" -> {
                    imHazardOnRoad.setImageResource(R.drawable.ic_report_hazard_stopped)
                    tvHazardOnRoad.text = "Xe đậu"
                }
                "road_kill" -> {
                    imHazardOnRoad.setImageResource(R.drawable.ic_report_hazard_roadkill)
                    tvHazardOnRoad.text = "Động vật chết"
                }
            }
        }
        bottomSheetDialog.show()
    }

    private fun onOpenHazardShoulder() {
        val customBottomSheetView = layoutInflater.inflate(R.layout.custom_bottom_sheet_dialog_3_items, null)

        val btnVehicleStop = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame1_3_items)
        val btnAnimal = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame2_3_items)
        val btnMissingSign = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame3_3_items)

        val imVehicleStop = customBottomSheetView.findViewById<ImageView>(R.id.imFrame1_3_items)
        val imAnimal = customBottomSheetView.findViewById<ImageView>(R.id.imFrame2_3_items)
        val imMissingSign = customBottomSheetView.findViewById<ImageView>(R.id.imFrame3_3_items)

        val tvVehicleStop = customBottomSheetView.findViewById<TextView>(R.id.tvFrame1_3_items)
        val tvAnimal = customBottomSheetView.findViewById<TextView>(R.id.tvFrame2_3_items)
        val tvMissingSign = customBottomSheetView.findViewById<TextView>(R.id.tvFrame3_3_items)
        val tvTitleSub = customBottomSheetView.findViewById<TextView>(R.id.tvTitle_report_3_items)

        imVehicleStop.setImageResource(R.drawable.ic_report_hazard_stopped)
        imAnimal.setImageResource(R.drawable.ic_report_hazard_animals)
        imMissingSign.setImageResource(R.drawable.ic_report_hazard_missingsign)

        tvVehicleStop.text = "Xe đậu"
        tvAnimal.text = "Động vật"
        tvMissingSign.text = "Thiếu biển"

        tvTitleSub.text = "Nguy hiểm bên lề"

        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(customBottomSheetView)
        btnVehicleStop.setOnClickListener {
            // Chạy audio
            if (AppController.soundMode == 1) {
                mAudioPlayer.play(this, R.raw.xe_dau_ben_le)
            }
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            subType2 = "vehicle_stop"
            bottomSheetDialog.dismiss()
        }
        btnAnimal.setOnClickListener {
            // Chạy audio
            if (AppController.soundMode == 1) {
                mAudioPlayer.play(this, R.raw.dong_vat_nguy_hiem)
            }
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            subType2 = "animal"
            bottomSheetDialog.dismiss()
        }
        btnMissingSign.setOnClickListener {
            // Chạy audio
            if (AppController.soundMode == 1) {
                mAudioPlayer.play(this, R.raw.thieu_bien_bao)
            }
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            subType2 = "missing_sign"
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.setCanceledOnTouchOutside(true)

        bottomSheetDialog.setOnDismissListener {
            when (subType2) {
                "vehicle_stop" -> {
                    imHazardShoulder.setImageResource(R.drawable.ic_report_hazard_stopped)
                    tvHazardShoulder.text = "Xe đậu"
                }
                "animal" -> {
                    imHazardShoulder.setImageResource(R.drawable.ic_report_hazard_animals)
                    tvHazardShoulder.text = "Động vật"
                }
                "missing_sign" -> {
                    imHazardShoulder.setImageResource(R.drawable.ic_report_hazard_missingsign)
                    tvHazardShoulder.text = "Thiếu biển"
                }
            }
        }
        bottomSheetDialog.show()
    }

    private fun onOpenHazardWeather() {
        val customBottomSheetView = layoutInflater.inflate(R.layout.custom_bottom_sheet_dialog_6_items, null)

        val btnFog = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame1_6_items)
        val btnHail = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame2_6_items)
        val btnFlood = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame3_6_items)
        val btnIce = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame4_6_items)
        val btn5 = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame5_6_items)
        val btn6 = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame6_6_items)

        val imFog = customBottomSheetView.findViewById<ImageView>(R.id.imFrame1_6_items)
        val imHail = customBottomSheetView.findViewById<ImageView>(R.id.imFrame2_6_items)
        val imFlood = customBottomSheetView.findViewById<ImageView>(R.id.imFrame3_6_items)
        val imIce = customBottomSheetView.findViewById<ImageView>(R.id.imFrame4_6_items)
        val im5 = customBottomSheetView.findViewById<ImageView>(R.id.imFrame5_6_items)
        val im6 = customBottomSheetView.findViewById<ImageView>(R.id.imFrame6_6_items)

        val tvFog = customBottomSheetView.findViewById<TextView>(R.id.tvFrame1_6_items)
        val tvHail = customBottomSheetView.findViewById<TextView>(R.id.tvFrame2_6_items)
        val tvFlood = customBottomSheetView.findViewById<TextView>(R.id.tvFrame3_6_items)
        val tvIce = customBottomSheetView.findViewById<TextView>(R.id.tvFrame4_6_items)
        val tv5 = customBottomSheetView.findViewById<TextView>(R.id.tvFrame5_6_items)
        val tv6 = customBottomSheetView.findViewById<TextView>(R.id.tvFrame6_6_items)
        val tvTitleSub = customBottomSheetView.findViewById<TextView>(R.id.tvTitle_report_6_items)

        btn5.isClickable = false
        btn6.isClickable = false
        im5.visibility = View.INVISIBLE
        im6.visibility = View.INVISIBLE
        tv5.visibility = View.INVISIBLE
        tv6.visibility = View.INVISIBLE

        imFog.setImageResource(R.drawable.ic_hazard_weather_fog)
        imHail.setImageResource(R.drawable.ic_hazard_weather_hail)
        imFlood.setImageResource(R.drawable.ic_hazard_weather_flood)
        imIce.setImageResource(R.drawable.ic_hazard_weather_ice)

        tvFog.text = "Sương mù"
        tvHail.text = "Mưa đá"
        tvFlood.text = "Lũ lụt"
        tvIce.text = "Đá trơn"

        tvTitleSub.text = "Nguy hiểm thời tiết"

        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(customBottomSheetView)
        btnFog.setOnClickListener {
            // Chạy audio
            if (AppController.soundMode == 1) {
                mAudioPlayer.play(this, R.raw.suong_mu)
            }
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            subType2 = "fog"
            bottomSheetDialog.dismiss()
        }
        btnHail.setOnClickListener {
            // Chạy audio
            if (AppController.soundMode == 1) {
                mAudioPlayer.play(this, R.raw.mua_da)
            }
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            subType2 = "hail"
            bottomSheetDialog.dismiss()
        }
        btnFlood.setOnClickListener {
            // Chạy audio
            if (AppController.soundMode == 1) {
                mAudioPlayer.play(this, R.raw.lu_lut)
            }
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            subType2 = "flood"
            bottomSheetDialog.dismiss()
        }
        btnIce.setOnClickListener {
            // Chạy audio
            if (AppController.soundMode == 1) {
                mAudioPlayer.play(this, R.raw.da_tron_tren_duong)
            }
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            subType2 = "ice"
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.setCanceledOnTouchOutside(true)

        bottomSheetDialog.setOnDismissListener {
            when (subType2) {
                "fog" -> {
                    imHazardWeather.setImageResource(R.drawable.ic_hazard_weather_fog)
                    tvHazardWeather.text = "Sương mù"
                }
                "hail" -> {
                    imHazardWeather.setImageResource(R.drawable.ic_hazard_weather_hail)
                    tvHazardWeather.text = "Mưa đá"
                }
                "flood" -> {
                    imHazardWeather.setImageResource(R.drawable.ic_hazard_weather_flood)
                    tvHazardWeather.text = "Lũ lụt"
                }
                "ice" -> {
                    imHazardWeather.setImageResource(R.drawable.ic_hazard_weather_ice)
                    tvHazardWeather.text = "Đá trơn"
                }
            }
        }
        bottomSheetDialog.show()
    }

    private fun onUpdateBase64Voice(id: String, base64Voice: String) {
        val service = APIServiceGenerator.createService(ReportService::class.java)
        val call = service.updateBase64Voice(id, base64Voice)
        call.enqueue(object : Callback<ReportResponse> {
            override fun onResponse(call: Call<ReportResponse>, response: Response<ReportResponse>) {
                if (response.isSuccessful) {
                    // Chạy audio
                    if (AppController.soundMode == 1) {
                        mAudioPlayer.play(this@ReportHazardActivity, R.raw.gui_bao_hieu_thanh_cong)
                    }
                    TastyToast.makeText(this@ReportHazardActivity, "Gửi báo hiệu thành công!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show()
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    TastyToast.makeText(this@ReportHazardActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            override fun onFailure(call: Call<ReportResponse>, t: Throwable) {
                Log.e("Failure", "Error: " + t.message)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            0 -> {
                tvRecord.text = "Thu âm"
                if (resultCode == Activity.RESULT_OK) {
                    sFileAudioPath = data!!.getStringExtra("FileAudioPath")
                    tvRecord.text = "Đã thu âm"
//                    TastyToast.makeText(this, sFileAudioPath, TastyToast.LENGTH_SHORT, TastyToast.INFO).show()
                }
            }
            1 -> {
                tvTakePhoto.text = "Chụp ảnh"
                if (resultCode == Activity.RESULT_OK) {
                    tvTakePhoto.text = "Đã chụp ảnh"

                    // Chỉ lấy thumbnail nên chất lượng ảnh không cao
//                    val bitmap: Bitmap = data!!.extras.get("data") as Bitmap
//                    Toast.makeText(this, "BEFORE: " + bitmap.density.toString() + " " + bitmap.height.toString() + " " + bitmap.width.toString(), Toast.LENGTH_SHORT).show()
//                    val matrix = Matrix()
//                    matrix.postRotate(90f)
//                    val newBitmap: Bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//                    Toast.makeText(this, "AFTER: " + newBitmap.density.toString() + " " + newBitmap.height.toString() + " " + newBitmap.width.toString(), Toast.LENGTH_SHORT).show()
//                    sBase64Image = FileUtils.encodeImageFile(newBitmap)


                    // ==== Dùng cho lấy chất lượng ảnh JPEG gốc, bằng cách chụp xong lưu file ảnh lại
//                    val imageStream = contentResolver.openInputStream(photoURI)
//                    val bitmap = BitmapFactory.decodeStream(imageStream)

                    val options = BitmapFactory.Options()
                    // Số inSampleSize là ảnh mới sẽ bằng 1 / inSampleSize của ảnh gốc, tức chiều dài và rộng giảm đi inSampleSize lần
                    // inJustRebound = true là sẽ đọc resource của ảnh chứ ko laod ảnh vào bộ nhớ, sẽ giảm bộ nhớ sử dụng

                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(mCurrentPhotoPath, options)
                    options.inSampleSize = calculateInSampleSize(options)
                    // Bỏ vì bớt toast
//                    Toast.makeText(this, "SAMPLE: " + options.inSampleSize.toString(), Toast.LENGTH_SHORT).show()
//                    options.inDensity = 320
                    options.inJustDecodeBounds = false
                    val imageStream = contentResolver.openInputStream(photoURI)
//                    imageStream = contentResolver.openInputStream(photoURI)
                    val bitmap = BitmapFactory.decodeStream(imageStream, null, options)

                    val matrix = Matrix()
                    matrix.postRotate(90f)
                    val newBitmap: Bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    // Bỏ vì bớt toast
//                    Toast.makeText(this, "AFTER: " + newBitmap.density.toString() + " " + newBitmap.width.toString() + " " + newBitmap.height.toString(), Toast.LENGTH_LONG).show()
                    if (bitmap.density > 320) {
                        sBase64Image = FileUtils.encodeImageFile(newBitmap, "large")
                    } else {
                        sBase64Image = FileUtils.encodeImageFile(newBitmap, "normal")
                    }


//                    TastyToast.makeText(this, sBase64Image, TastyToast.LENGTH_SHORT, TastyToast.INFO).show()
                }
            }
        }
    }

    val TARGET_IMAGE_WIDTH: Int = 614
    val TARGET_IMAGE_HEIGHT: Int = 818
    // This method is used to calculate largest inSampleSize
    //which is used to decode bitmap in required bitmap.
    private fun calculateInSampleSize(bmOptions: BitmapFactory.Options): Int {
        // Raw height and width of image
        val photoWidth = bmOptions.outWidth
        val photoHeight = bmOptions.outHeight
// Bỏ vì bớt toast
//        Toast.makeText(this, "BEFORE: " + photoWidth + " " + photoHeight, Toast.LENGTH_SHORT).show()
        var scaleFactor = 2
//        if (photoWidth > TARGET_IMAGE_WIDTH || photoHeight > TARGET_IMAGE_HEIGHT) {
//            val halfPhotoWidth = photoWidth / 2
//            val halfPhotoHeight = photoHeight / 2

        // Calculate the largest inSampleSize value that is a power of 2
        //and keeps both height and width larger than the requested height and width.

        // Test and replace with || ( or )
        while ((photoWidth / scaleFactor) >= TARGET_IMAGE_WIDTH && (photoHeight / scaleFactor) >= TARGET_IMAGE_HEIGHT) {

            scaleFactor *= 2
        }
//        }
        // Bỏ vì bớt toast
//        Toast.makeText(this, (photoWidth / scaleFactor).toString() + "  " + (photoHeight / scaleFactor).toString(), Toast.LENGTH_SHORT).show()
        return scaleFactor
    }

    private var mCurrentPhotoPath: String = ""
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
//        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        val imageFileName = "JPEG_" + timeStamp + "_"
        val imageFileName = "image"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )

        Log.e("PATH", image.absolutePath)
//        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.absolutePath
        return image
    }
}
