package com.example.trongtuyen.carmap.activity.common

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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
import android.view.View
import android.widget.LinearLayout


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

    private var subType1: String = ""
    private var subType2: String = ""

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
            onOpenHazardOnRoad()
        }
        btnHazardShoulder.setOnClickListener {
            subType1 = "shoulder"
            btnHazardShoulder.background = getDrawable(R.color.button_bg_inactive)
            btnHazardOnRoad.background = null
            btnHazardWeather.background = null
            tvTitle.text = "LỀ ĐƯỜNG"
            onOpenHazardShoulder()
        }
        btnHazardWeather.setOnClickListener {
            subType1 = "weather"
            btnHazardWeather.background = getDrawable(R.color.button_bg_inactive)
            btnHazardShoulder.background = null
            btnHazardOnRoad.background = null
            tvTitle.text = "THỜI TIẾT"
            onOpenHazardWeather()
        }
        btnSend.setOnClickListener { onSend() }
        btnCLose.setOnClickListener { onClose() }
    }

    private fun onClose() {
        finish()
    }

    private fun onSend() {
        if (subType1 == "" || subType2 == "") {
            Toast.makeText(this, "Vui lòng chọn loại nguy hiểm", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Loại: " + subType1 + " " + textInputEdit.text.toString(), Toast.LENGTH_SHORT).show()
            val mReport = Report("hazard", subType1, subType2, textInputEdit.text.toString(), AppController.userProfile!!.homeLocation!!, AppController.userProfile!!._id.toString(), 1, 0, false)
            onAddNewReportHazard(mReport)
        }
    }

    private fun onAddNewReportHazard(report: Report) {
        val service = APIServiceGenerator.createService(ReportService::class.java)

        val call = service.addNewReport(report)
        call.enqueue(object : Callback<Report> {
            override fun onFailure(call: Call<Report>?, t: Throwable?) {
                Toast.makeText(this@ReportHazardActivity, "Failed!", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                if (response.isSuccessful()) {
                    Toast.makeText(this@ReportHazardActivity, "Gửi báo cáo thành công!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    Toast.makeText(this@ReportHazardActivity, "" + apiError.message(), Toast.LENGTH_SHORT).show()
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
            subType2 = "object"
            bottomSheetDialog.dismiss()
        }
        btnConstruction.setOnClickListener {
            subType2 = "construction"
            bottomSheetDialog.dismiss()
        }
        btnBrokenLight.setOnClickListener {
            subType2 = "broken_light"
            bottomSheetDialog.dismiss()
        }
        btnPothole.setOnClickListener {
            subType2 = "pothole"
            bottomSheetDialog.dismiss()
        }
        btnVehicleStop.setOnClickListener {
            subType2 = "vehicle_stop"
            bottomSheetDialog.dismiss()
        }
        btnRoadkill.setOnClickListener {
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
                    tvHazardOnRoad.text = "Động vật"
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

        tvTitleSub.text = "Nguy hiểm bên lè"

        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(customBottomSheetView)
        btnVehicleStop.setOnClickListener {
            subType2 = "vehicle_stop"
            bottomSheetDialog.dismiss()
        }
        btnAnimal.setOnClickListener {
            subType2 = "animal"
            bottomSheetDialog.dismiss()
        }
        btnMissingSign.setOnClickListener {
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
            subType2 = "fog"
            bottomSheetDialog.dismiss()
        }
        btnHail.setOnClickListener {
            subType2 = "hail"
            bottomSheetDialog.dismiss()
        }
        btnFlood.setOnClickListener {
            subType2 = "flood"
            bottomSheetDialog.dismiss()
        }
        btnIce.setOnClickListener {
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

}
