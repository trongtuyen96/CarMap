package com.example.trongtuyen.carmap.activity.common

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.EditText
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.activity.MainActivity
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.controllers.AppController
import com.example.trongtuyen.carmap.models.User
import com.example.trongtuyen.carmap.services.APIServiceGenerator
import com.example.trongtuyen.carmap.services.models.AuthenticationResponse
import com.example.trongtuyen.carmap.services.AuthenticationService
import com.example.trongtuyen.carmap.services.ErrorUtils
import com.sdsmdg.tastytoast.TastyToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SignUpActivity : AppCompatActivity() {

    // Kotlin support this
    @BindView(R.id.txtEmail_signup)
    lateinit var txtEmail: EditText
    @BindView(R.id.txtPassword_signup)
    lateinit var txtPassword: EditText
    @BindView(R.id.txtFullName_signup)
    lateinit var txtFullName: EditText
    @BindView(R.id.btnSignUp_signup)
    lateinit var btnSignUp: View
    @BindView(R.id.btnSignIn_signup)
    lateinit var btnSignIn: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        ButterKnife.bind(this)

        initComponents()
    }

    private fun initComponents() {
        btnSignUp.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onSignUpWithEmail()
        }
        btnSignIn.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onSignIn()
        }
    }


    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic
        return email.contains("@")
    }

    internal fun onSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    internal fun onSignUpWithEmail() {
        // Reset errors.
        txtEmail.setError(null)
        txtPassword.setError(null)
        txtFullName.setError(null)

        // Store values at the time of the login attempt.
        val email = txtEmail.text.toString()
        val password = txtPassword.text.toString()
        val fullName = txtFullName.text.toString()
        var isValidateOk = true

        // Validate email
        if (TextUtils.isEmpty(email)) {
            txtEmail.setError("Email không thể bỏ trống!")
            isValidateOk = false
        } else if (!isEmailValid(email)) {
            txtEmail.setError("Email không hợp lệ!")
            isValidateOk = false
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            txtPassword.setError("Password không thể bỏ trống!")
            isValidateOk = false
        }

        // Validate full name
        if (TextUtils.isEmpty(fullName)) {
            txtFullName.setError("Họ và tên không thể bỏ trống!")
            isValidateOk = false
        }

        if (!isValidateOk)
            return

        val service = APIServiceGenerator.createService(AuthenticationService::class.java)

        val call = service.registerWithEmail(email, password, fullName, "1996-01-01")
        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    TastyToast.makeText(this@SignUpActivity, "Đăng ký thành công!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show()
                    signInWithEmail(email, password)
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    TastyToast.makeText(this@SignUpActivity, "" + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                TastyToast.makeText(this@SignUpActivity, "Failed!", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
            }
        })
    }

    internal fun signInWithEmail(email: String, password: String) {
        val service = APIServiceGenerator.createService(AuthenticationService::class.java)
        val call = service.authWithEmail(email, password)
        call.enqueue(object : Callback<AuthenticationResponse> {
            override fun onResponse(call: Call<AuthenticationResponse>, response: Response<AuthenticationResponse>) {
                if (response.isSuccessful) {
                    onAuthenticationSuccess(response.body())
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    TastyToast.makeText(this@SignUpActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                TastyToast.makeText(this@SignUpActivity, "Không có kết nối Internet", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
            }
        })
    }

    private fun onAuthenticationSuccess(response: AuthenticationResponse) {
        AppController.userProfile = response.user
        AppController.accessToken = response.token //Save access token

        // Tạo mới dữ liệu cài đặt
        AppController.settingFilterCar = "true"
        AppController.settingFilterReport = "true"

        //        Intent returnIntent = new Intent();
        //        setResult(Activity.RESULT_OK,returnIntent);
        //        finish();
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
