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
import android.widget.LinearLayout


class ReportHazardActivity : AppCompatActivity() {

    @BindView(R.id.imvHazardOnRoad_report_hazard)
    lateinit var btnHazardOnRoad: LinearLayout
    @BindView(R.id.imvHazardShoulder_report_hazard)
    lateinit var btnHazardShoulder: LinearLayout
    @BindView(R.id.imvHazardWeather_report_hazard)
    lateinit var btnHazardWeather: LinearLayout
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

    // CustomBottomSheetDialog
//    @BindView(R.id.imvObject_report_hazard_on_road)
//    lateinit var btnObject: LinearLayout
//    @BindView(R.id.imvConstruction_report_hazard_on_road)
//    lateinit var btnConstruction: LinearLayout
//    @BindView(R.id.imvBrokenLight_report_hazard_on_road)
//    lateinit var btnBrokenLight: LinearLayout
//    @BindView(R.id.imvPothole_report_hazard_on_road)
//    lateinit var btnPothole: LinearLayout
//    @BindView(R.id.imvVehicleStop_report_hazard_on_road)
//    lateinit var btnVehicleStop: LinearLayout
//    @BindView(R.id.imvRoadkill_report_hazard_on_road)
//    lateinit var btnRoadkill: LinearLayout

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
            btnHazardOnRoad.background = getDrawable(R.color.colorPrimaryLight)
            btnHazardShoulder.background = null
            btnHazardWeather.background = null
            onOpenHazardOnRoad()

//            val custom_bottom_sheet_view = layoutInflater.inflate(R.layout.custom_bottom_sheet_dialog_6_items, null)
//            val bottomSheetDialog = BottomSheetDialog(this)
//            bottomSheetDialog.setContentView(custom_bottom_sheet_view)
//            bottomSheetDialog.show()
//            Toast.makeText(this,"Show", Toast.LENGTH_SHORT).show()
//            bottomSheetDialog.dismiss()

//            imHazardOnRoad.visibility = View.GONE
//            imHazardOnRoad.visibility = View.VISIBLE
//            tvHazardOnRoad.visibility = View.GONE
//            tvHazardOnRoad.visibility = View.VISIBLE
//            btnHazardOnRoad.visibility = View.GONE
//            btnHazardOnRoad.visibility = View.VISIBLE
//            findViewById<ImageView>(R.id.imHazardOnRoad_report_hazard).invalidate()
//            btnHazardOnRoad.invalidate()
//            layout.invalidate()
//            layout.refreshDrawableState()
//            layout.postInvalidate()
        }
        btnHazardShoulder.setOnClickListener {
            subType1 = "shoulder"
            btnHazardShoulder.background = getDrawable(R.color.colorPrimaryLight)
            btnHazardOnRoad.background = null
            btnHazardWeather.background = null
        }
        btnHazardWeather.setOnClickListener {
            subType1 = "weather"
            btnHazardWeather.background = getDrawable(R.color.colorPrimaryLight)
            btnHazardShoulder.background = null
            btnHazardOnRoad.background = null
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

                } else {
                    val apiError = ErrorUtils.parseError(response)
                    Toast.makeText(this@ReportHazardActivity, "" + apiError.message(), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun onOpenHazardOnRoad() {
        val custom_bottom_sheet_view = layoutInflater.inflate(R.layout.custom_bottom_sheet_dialog_6_items, null)

        val btnObject = custom_bottom_sheet_view.findViewById<LinearLayout>(R.id.imvObject_report_hazard_on_road)
        val btnConstruction = custom_bottom_sheet_view.findViewById<LinearLayout>(R.id.imvConstruction_report_hazard_on_road)
        val btnBrokenLight = custom_bottom_sheet_view.findViewById<LinearLayout>(R.id.imvBrokenLight_report_hazard_on_road)
        val btnPothole = custom_bottom_sheet_view.findViewById<LinearLayout>(R.id.imvPothole_report_hazard_on_road)
        val btnVehicleStop = custom_bottom_sheet_view.findViewById<LinearLayout>(R.id.imvVehicleStop_report_hazard_on_road)
        val btnRoadkill = custom_bottom_sheet_view.findViewById<LinearLayout>(R.id.imvRoadkill_report_hazard_on_road)

        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(custom_bottom_sheet_view)
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
                "" -> {
                    Toast.makeText(this, "Vui lòng chọn loại nguy hiểm trên đường", Toast.LENGTH_SHORT).show()
                }
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
}
