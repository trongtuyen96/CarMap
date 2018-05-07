package com.example.trongtuyen.carmap.activity.common

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.MainActivity
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.controllers.AppController
import com.example.trongtuyen.carmap.services.APIServiceGenerator
import com.example.trongtuyen.carmap.services.AuthenticationResponse
import com.example.trongtuyen.carmap.services.AuthenticationService
import com.example.trongtuyen.carmap.services.ErrorUtils
import com.example.trongtuyen.carmap.utils.SharePrefs
import kotlinx.android.synthetic.main.activity_signin.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by tuyen on 07/05/2018.
 */

class SignInActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        ButterKnife.bind(this)

        // Init SharePreference
        SharePrefs.Initialize(applicationContext)
        initComponents()
    }


    private fun onAuthenticationSuccess(response: AuthenticationResponse) {
        AppController.userProfile = response.user
        AppController.accessToken = response.token //Save access token

        //        Intent returnIntent = new Intent();
        //        setResult(Activity.RESULT_OK,returnIntent);
        //        finish();

        // Start Main Activity
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun initComponents() {

        // Sign in buttons
        btnSignInWithEmail.setOnClickListener(this)
        btnSignInWithFacebook.setOnClickListener(this)
        btnSignInWithGoogle.setOnClickListener(this)

        //
        btnClose.setOnClickListener(this)
        btnSignUp.setOnClickListener(this)

        progress1.visibility = View.GONE
        progress2.visibility = View.GONE
    }

    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic
        return email.contains("@")
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnSignInWithEmail -> onSignInWithEmail()
            R.id.btnSignInWithFacebook -> onSignInWithFacebook()
            R.id.btnSignInWithGoogle -> onSignInWithGoogle()
            R.id.btnClose -> onClose()
//            R.id.btnSignUp -> onSignUp()
        }
    }

    private fun onClose() {
        finish()
    }

//    private fun onSignUp() {
//        val intent = Intent(this, SignUpActivity::class.java)
//        startActivity(intent)
//    }

    private fun onSignInWithGoogle() {
        Toast.makeText(this, "Chúng tôi sẽ sớm hoàn thiện chức năng này!", Toast.LENGTH_SHORT).show()
    }

    private fun onSignInWithFacebook() {
        Toast.makeText(this, "Chúng tôi sẽ sớm hoàn thiện chức năng này!", Toast.LENGTH_SHORT).show()
    }

    private fun onSignInWithEmail() {
        // Reset errors.
        txtEmail!!.error = null
        txtPassword!!.error = null

        // Store values at the time of the login attempt.
        val email = txtEmail!!.text.toString()
        val password = txtPassword!!.text.toString()
        var isValidateOk = true

        // Validate email
        if (TextUtils.isEmpty(email)) {
            txtEmail!!.error = "Email không thể bỏ trống!"
            isValidateOk = false
        } else if (!isEmailValid(email)) {
            txtEmail!!.error = "Email không hợp lệ!"
            isValidateOk = false
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            txtPassword!!.error = "Mật khẩu không thể bỏ trống!"
            isValidateOk = false
        }

        if (!isValidateOk)
            return

        // Create and call service API
        val service = APIServiceGenerator.createService(AuthenticationService::class.java)
        val call = service.authWithEmail(email, password)
        call.enqueue(object : Callback<AuthenticationResponse> {
            override fun onResponse(call: Call<AuthenticationResponse>, response: Response<AuthenticationResponse>) {
                if (response.isSuccessful) {
                    onAuthenticationSuccess(response.body())
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    Toast.makeText(this@SignInActivity, "Lỗi: " + apiError.message(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                Toast.makeText(this@SignInActivity, "Không có kết nối Internet", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
