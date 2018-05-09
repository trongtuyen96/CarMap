package com.example.trongtuyen.carmap.activity.common

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.MainActivity
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.controllers.AppController
import com.example.trongtuyen.carmap.services.APIServiceGenerator
import com.example.trongtuyen.carmap.services.AuthenticationResponse
import com.example.trongtuyen.carmap.services.AuthenticationService
import com.example.trongtuyen.carmap.services.ErrorUtils
import kotlinx.android.synthetic.main.activity_signin.*
import kotlinx.android.synthetic.main.activity_signup.*
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

        initComponents()
    }

    private fun onAuthenticationSuccess(response: AuthenticationResponse) {
        AppController.userProfile = response.user
        AppController.accessToken = response.token //Save access token

        //        Intent returnIntent = new Intent();
        //        setResult(Activity.RESULT_OK,returnIntent);
        //        finish();

        // Notify sign in successfully
        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

        // Start Main Activity
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun initComponents() {

        // Sign in buttons
        btnSignInWithEmail_signin.setOnClickListener(this)
        btnSignInWithFacebook_signin.setOnClickListener(this)
        btnSignInWithGoogle_signin.setOnClickListener(this)

        //
        btnClose_signin.setOnClickListener(this)
        btnSignUp_signin.setOnClickListener(this)

        progress1_signin.visibility = View.GONE
        progress2_signin.visibility = View.GONE
    }

    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic
        return email.contains("@")
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnSignInWithEmail_signin -> onSignInWithEmail()
            R.id.btnSignInWithFacebook_signin -> onSignInWithFacebook()
            R.id.btnSignInWithGoogle_signin -> onSignInWithGoogle()
            R.id.btnClose_signin -> onClose()
            R.id.btnSignUp_signin -> onSignUp()
        }
    }

    private fun onClose() {
        finish()
    }

    private fun onSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun onSignInWithGoogle() {
        Toast.makeText(this, "Chúng tôi sẽ sớm hoàn thiện chức năng này!", Toast.LENGTH_SHORT).show()
    }

    private fun onSignInWithFacebook() {
        Toast.makeText(this, "Chúng tôi sẽ sớm hoàn thiện chức năng này!", Toast.LENGTH_SHORT).show()
    }

    private fun onSignInWithEmail() {
        // Reset errors.
        txtEmail_signin!!.error = null
        txtPassword_signin!!.error = null

        // Store values at the time of the login attempt.
        val email = txtEmail_signin!!.text.toString()
        val password = txtPassword_signin!!.text.toString()
        var isValidateOk = true

        // Validate email
        if (TextUtils.isEmpty(email)) {
            txtEmail_signin!!.error = "Email không thể bỏ trống!"
            isValidateOk = false
        } else if (!isEmailValid(email)) {
            txtEmail_signin!!.error = "Email không hợp lệ!"
            isValidateOk = false
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            txtPassword_signin!!.error = "Mật khẩu không thể bỏ trống!"
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
