package com.example.trongtuyen.carmap.activity.common

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.activity.MainActivity
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.controllers.AppController
import com.example.trongtuyen.carmap.services.APIServiceGenerator
import com.example.trongtuyen.carmap.services.models.AuthenticationResponse
import com.example.trongtuyen.carmap.services.AuthenticationService
import com.example.trongtuyen.carmap.services.ErrorUtils
import com.sdsmdg.tastytoast.TastyToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by tuyen on 07/05/2018.
 */

class SignInActivity : AppCompatActivity() {

    @BindView(R.id.txtEmail_signin)
    lateinit var txtEmail: EditText
    @BindView(R.id.txtPassword_signin)
    lateinit var txtPassword: EditText

    @BindView(R.id.btnSignInWithEmail_signin)
    lateinit var btnSignInWithEmail: View
    @BindView(R.id.btnSignInWithFacebook_signin)
    lateinit var btnSignInWithFacebook: View
    @BindView(R.id.btnSignInWithGoogle_signin)
    lateinit var btnSignInWithGoogle: View

    @BindView(R.id.btnClose_signin)
    lateinit var btnClose: View

    @BindView(R.id.btnSignUp_signin)
    lateinit var btnSignUp: TextView

    @BindView(R.id.progress1_signin)
    lateinit var progress1: ProgressBar
    @BindView(R.id.progress2_signin)
    lateinit var progress2: ProgressBar

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

        // Tạo mới dữ liệu cài đặt
        AppController.settingFilterCar = "true"
        AppController.settingFilterReport = "true"
        AppController.soundMode = 1

        // Notify sign in successfully
        TastyToast.makeText(this, "Đăng nhập thành công!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show()

        // Start Main Activity
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun initComponents() {

        // Sign in buttons
        btnSignInWithEmail.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onSignInWithEmail()
        }
        btnSignInWithFacebook.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onSignInWithFacebook()
        }
        btnSignInWithGoogle.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onSignInWithGoogle()
        }

        //
        btnClose.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onClose()
        }
        btnSignUp.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onSignUp()
        }

        progress1.visibility = View.GONE
        progress2.visibility = View.GONE
    }

    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic
        return email.contains("@")
    }

    private fun onClose() {
        finish()
    }

    private fun onSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun onSignInWithGoogle() {
        TastyToast.makeText(this, "Chúng tôi sẽ sớm hoàn thiện chức năng này!", TastyToast.LENGTH_SHORT, TastyToast.INFO).show()
    }

    private fun onSignInWithFacebook() {
        TastyToast.makeText(this, "Chúng tôi sẽ sớm hoàn thiện chức năng này!", TastyToast.LENGTH_SHORT, TastyToast.INFO).show()
    }

    private fun onSignInWithEmail() {
        // Reset errors.
        txtEmail.error = null
        txtPassword.error = null

        // Store values at the time of the login attempt.
        val email = txtEmail.text.toString()
        val password = txtPassword.text.toString()
        var isValidateOk = true

        // Validate email
        if (TextUtils.isEmpty(email)) {
            txtEmail.error = "Email không thể bỏ trống!"
            isValidateOk = false
        } else if (!isEmailValid(email)) {
            txtEmail.error = "Email không hợp lệ!"
            isValidateOk = false
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            txtPassword.error = "Mật khẩu không thể bỏ trống!"
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
                    onAuthenticationSuccess(response.body()!!)
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    TastyToast.makeText(this@SignInActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                TastyToast.makeText(this@SignInActivity, "Không có kết nối Internet", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
