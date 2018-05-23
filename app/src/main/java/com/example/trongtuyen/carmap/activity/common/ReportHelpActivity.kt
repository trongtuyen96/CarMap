package com.example.trongtuyen.carmap.activity.common

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.view.View
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

class ReportHelpActivity : AppCompatActivity() {

    @BindView(R.id.imvOthers_report_help)
    lateinit var btnOthers: LinearLayout
    @BindView(R.id.imvCall_report_help)
    lateinit var btnCall: LinearLayout

    @BindView(R.id.txtMess_report_help)
    lateinit var textInputEdit: EditText
    @BindView(R.id.btnSend_report_help)
    lateinit var btnSend: Button

    @BindView(R.id.imOthers_report_help)
    lateinit var imOthers: ImageView
    @BindView(R.id.tvOthers_report_help)
    lateinit var tvOthers: TextView

    @BindView(R.id.btnClose_report_help)
    lateinit var btnCLose: ImageView

    private var subType1: String = ""
    private var subType2: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_help)

        ButterKnife.bind(this)
        initComponents()
    }

    private fun initComponents() {

        // Các nút báo cáo
        btnOthers.setOnClickListener {
            subType1 = "others"
            btnOthers.background = getDrawable(R.color.button_bg_inactive)
            btnCall.background = null
            onOpenOthers()
        }
        btnCall.setOnClickListener {
            subType1 = "call"
            btnCall.background = getDrawable(R.color.button_bg_inactive)
            btnOthers.background = null
            onCall()
        }
        btnSend.setOnClickListener { onSend() }
        btnCLose.setOnClickListener { onClose() }
    }

    private fun onClose() {
        finish()
    }

    private fun onSend() {
        if (subType1 == "" || subType2 == "") {
            Toast.makeText(this, "Vui lòng chọn loại giúp đỡ", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Loại: " + subType1 + " " + subType2 + " " + textInputEdit.text.toString(), Toast.LENGTH_SHORT).show()
            val mReport = Report("help", subType1, subType2, textInputEdit.text.toString(), AppController.userProfile!!.homeLocation!!, AppController.userProfile!!._id.toString(), 1, 0, false)
            onAddNewReportHazard(mReport)
        }
    }

    private fun onAddNewReportHazard(report: Report) {
        val servMedical = APIServiceGenerator.createService(ReportService::class.java)

        val call = servMedical.addNewReport(report)
        call.enqueue(object : Callback<Report> {
            override fun onFailure(call: Call<Report>?, t: Throwable?) {
                Toast.makeText(this@ReportHelpActivity, "Failed!", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                if (response.isSuccessful()) {
                    Toast.makeText(this@ReportHelpActivity, "Gửi báo cáo thành công!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    Toast.makeText(this@ReportHelpActivity, "" + apiError.message(), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun onOpenOthers(){
        val customBottomSheetView = layoutInflater.inflate(R.layout.custom_bottom_sheet_dialog_6_items, null)

        val btnNoGas = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame1_6_items)
        val btnFlatTire = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame2_6_items)
        val btnBattery = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame3_6_items)
        val btnMedical = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame4_6_items)
        val btn5 = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame5_6_items)
        val btn6 = customBottomSheetView.findViewById<LinearLayout>(R.id.imvFrame6_6_items)

        val imNoGas = customBottomSheetView.findViewById<ImageView>(R.id.imFrame1_6_items)
        val imFlatTire = customBottomSheetView.findViewById<ImageView>(R.id.imFrame2_6_items)
        val imBattery = customBottomSheetView.findViewById<ImageView>(R.id.imFrame3_6_items)
        val imMedical = customBottomSheetView.findViewById<ImageView>(R.id.imFrame4_6_items)
        val im5 = customBottomSheetView.findViewById<ImageView>(R.id.imFrame5_6_items)
        val im6 = customBottomSheetView.findViewById<ImageView>(R.id.imFrame6_6_items)

        val tvNoGas = customBottomSheetView.findViewById<TextView>(R.id.tvFrame1_6_items)
        val tvFlatTire = customBottomSheetView.findViewById<TextView>(R.id.tvFrame2_6_items)
        val tvBattery = customBottomSheetView.findViewById<TextView>(R.id.tvFrame3_6_items)
        val tvMedical = customBottomSheetView.findViewById<TextView>(R.id.tvFrame4_6_items)
        val tv5 = customBottomSheetView.findViewById<TextView>(R.id.tvFrame5_6_items)
        val tv6 = customBottomSheetView.findViewById<TextView>(R.id.tvFrame6_6_items)
        val tvTitleSub = customBottomSheetView.findViewById<TextView>(R.id.tvTitle_report_6_items)

        btn5.isClickable = false
        btn6.isClickable = false
        im5.visibility = View.INVISIBLE
        im6.visibility = View.INVISIBLE
        tv5.visibility = View.INVISIBLE
        tv6.visibility = View.INVISIBLE

        imNoGas.setImageResource(R.drawable.ic_report_sos_no_gas)
        imFlatTire.setImageResource(R.drawable.ic_report_sos_flat_tire)
        imBattery.setImageResource(R.drawable.ic_report_sos_no_battery)
        imMedical.setImageResource(R.drawable.ic_report_sos_medical_care)

        tvNoGas.text = "Hết xăng"
        tvFlatTire.text = "Xẹp lốp"
        tvBattery.text = "Hết bình"
        tvMedical.text = "Y tế"

        tvTitleSub.text = "Bạn đang gặp vấn đề gì ?"

        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(customBottomSheetView)
        btnNoGas.setOnClickListener {
            subType2 = "no_gas"
            bottomSheetDialog.dismiss()
        }
        btnFlatTire.setOnClickListener {
            subType2 = "flat_tire"
            bottomSheetDialog.dismiss()
        }
        btnBattery.setOnClickListener {
            subType2 = "no_battery"
            bottomSheetDialog.dismiss()
        }
        btnMedical.setOnClickListener {
            subType2 = "medical_care"
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.setCanceledOnTouchOutside(true)

        bottomSheetDialog.setOnDismissListener {
            when (subType2) {
                "no_gas" -> {
                    imOthers.setImageResource(R.drawable.ic_report_sos_no_gas)
                    tvOthers.text = "Hết xăng"
                }
                "flat_tire" -> {
                    imOthers.setImageResource(R.drawable.ic_report_sos_flat_tire)
                    tvOthers.text = "Xẹp lốp"
                }
                "no_battery" -> {
                    imOthers.setImageResource(R.drawable.ic_report_sos_no_battery)
                    tvOthers.text = "Hết bình"
                }
                "medical_care" -> {
                    imOthers.setImageResource(R.drawable.ic_report_sos_medical_care)
                    tvOthers.text = "Y tế"
                }
            }
        }
        bottomSheetDialog.show()
    }

    private fun onCall(){
        Toast.makeText(this,"Chúng tôi đang hoàn thiện tính năng này!", Toast.LENGTH_SHORT).show()
    }
}
