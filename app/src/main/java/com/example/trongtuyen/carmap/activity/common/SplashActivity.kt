package com.example.trongtuyen.carmap.activity.common

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.make
import android.support.v4.widget.SwipeRefreshLayout
import android.widget.Toast
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.activity.MainActivity
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.R.id.swipeRefresh
import com.example.trongtuyen.carmap.controllers.AppController
import com.example.trongtuyen.carmap.services.APIServiceGenerator
import com.example.trongtuyen.carmap.services.AuthenticationService
import com.example.trongtuyen.carmap.services.ErrorUtils
import com.example.trongtuyen.carmap.services.models.RefreshTokenResponse
import com.example.trongtuyen.carmap.utils.SharePrefs
import com.sdsmdg.tastytoast.TastyToast
import kotlinx.android.synthetic.main.activity_splash.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SplashActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Init SharePreference
        SharePrefs.Initialize(applicationContext)
        ButterKnife.bind(this)
        initComponents()
    }

    private fun initComponents() {
        swipeRefresh.setOnRefreshListener(this)
        swipeRefresh.setRefreshing(true)
        verifyAndRefreshNewToken()
    }

    private fun verifyAndRefreshNewToken() {
        if (AppController.accessToken != null && AppController.accessToken.toString().length > 0) {
            // Refresh new token
            val service = APIServiceGenerator.createService(AuthenticationService::class.java)
            val call = service.refreshToken(AppController.accessToken.toString())
            call.enqueue(object : Callback<RefreshTokenResponse> {
                override fun onResponse(call: Call<RefreshTokenResponse>, response: Response<RefreshTokenResponse>) {
                    swipeRefresh.isRefreshing = false

                    if (response.isSuccessful) {
                        val accessToken = response.body().token
                        AppController.accessToken = accessToken

                        startMainActivity()
                    } else {
                        val apiError = ErrorUtils.parseError(response)

                        //                        Snackbar snackbar = Snackbar.make(findViewById(R.id.splashLayout), apiError.message(), Snackbar.LENGTH_INDEFINITE);
                        //                        snackbar.setAction("OK", new View.OnClickListener() {
                        //                            @Override
                        //                            public void onClick(View v) {
                        //                                snackbar.dismiss();
                        //                            }
                        //                        }).show();
                        TastyToast.makeText(this@SplashActivity, apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()

                        //startMainActivity();
                        val intent = Intent(this@SplashActivity, SignInActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }

                override fun onFailure(call: Call<RefreshTokenResponse>, t: Throwable) {
                    make(findViewById(R.id.splashLayout), "Không có kết nối Internet", Snackbar.LENGTH_INDEFINITE).show()
                    //startMainActivity();
                    swipeRefresh.isRefreshing = false
                }
            })
        } else {
            swipeRefresh.isRefreshing = false
            startSignInActivity()
        }
    }

    private fun startMainActivity() {
        val h = Handler()
        h.postDelayed({
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 1000)
    }

    private fun startSignInActivity() {
        val h = Handler()
        h.postDelayed({
            val intent = Intent(applicationContext, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }, 1000)
    }

    override fun onRefresh() {
        swipeRefresh.isRefreshing = true
        verifyAndRefreshNewToken()
    }
}
