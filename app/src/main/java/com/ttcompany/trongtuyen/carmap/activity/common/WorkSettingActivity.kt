package com.ttcompany.trongtuyen.carmap.activity.common

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.ttcompany.trongtuyen.carmap.R
import com.ttcompany.trongtuyen.carmap.controllers.AppController
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.sdsmdg.tastytoast.TastyToast

class WorkSettingActivity : AppCompatActivity() {

    @BindView(R.id.imBack_work_setting)
    lateinit var btnBack: ImageView
    @BindView(R.id.tvAddress_work_setting)
    lateinit var tvAddress: TextView
    @BindView(R.id.btnDismiss_work_setting)
    lateinit var btnDismiss: Button
    @BindView(R.id.btnChoose_work_setting)
    lateinit var btnChoose: Button

    // PlaceAutoCompleteFragment
    private var placeAutoComplete: PlaceAutocompleteFragment? = null

    private var isNewChosen = false

    private var chosenPlace: Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_setting)

        ButterKnife.bind(this)
        initComponents()
    }

    private fun initComponents() {

        if (intent.getStringExtra("work_location") != "") {
            tvAddress.text = intent.getStringExtra("work_location")
        }

        // Obtain placeAutoComplete fragment
        placeAutoComplete = fragmentManager.findFragmentById(R.id.place_autocomplete_work_setting) as PlaceAutocompleteFragment
        placeAutoComplete!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                tvAddress.text = place.address
                chosenPlace = place
                isNewChosen = true
            }

            override fun onError(status: Status) {
                Log.d("Maps", "An error occurred: $status")
            }
        })

        btnBack.setOnClickListener {
            finish()
        }

        btnDismiss.setOnClickListener {
            finish()
        }

        btnChoose.setOnClickListener {
            if (isNewChosen) {
                AppController.userProfile!!.latWorkLocation = chosenPlace!!.latLng.latitude
                AppController.userProfile!!.longWorkLocation = chosenPlace!!.latLng.longitude
                intent.putExtra("work_location_new", tvAddress.text.toString())
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                TastyToast.makeText(this, "Vui lòng chọn địa chỉ mới", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
            }

        }
    }
}