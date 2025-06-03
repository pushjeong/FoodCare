package com.AzaAza.foodcare.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.api.RetrofitClient
import com.AzaAza.foodcare.models.PasswordChangeRequestDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import retrofit2.HttpException
import java.io.IOException

class PasswordChangeActivity : AppCompatActivity() {

    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var prefs: SharedPreferences

    private var loadingDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_change)

        // 툴바 세팅
        findViewById<TextView>(R.id.topBarTitle).text = "비밀번호 변경"
        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        // SharedPreferences 초기화
        prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        // 뷰 초기화
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnChangePassword = findViewById(R.id.btnChangePassword)

        //비밀번호 변경 버튼 클릭 리스너
        btnChangePassword.setOnClickListener {
            if (validateInput()) {
                changePassword()
            }
        }
    }

    // 입력 검증
    private fun validateInput(): Boolean {
        val currentPassword = etCurrentPassword.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (currentPassword.isEmpty()) {
            Toast.makeText(this, "현재 비밀번호를 입력해주세요.\n(비밀번호 찾기로 받은 임시 비밀번호)", Toast.LENGTH_LONG).show()
            etCurrentPassword.requestFocus()
            return false
        }

        if (newPassword.isEmpty()) {
            Toast.makeText(this, "새 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            etNewPassword.requestFocus()
            return false
        }

        if (newPassword.length < 6) {
            Toast.makeText(this, "새 비밀번호는 6자리 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
            etNewPassword.requestFocus()
            return false
        }

        if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "비밀번호 확인을 입력해주세요.", Toast.LENGTH_SHORT).show()
            etConfirmPassword.requestFocus()
            return false
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            etConfirmPassword.requestFocus()
            return false
        }

        if (currentPassword == newPassword) {
            Toast.makeText(this, "현재 비밀번호와 새 비밀번호가 동일합니다.", Toast.LENGTH_SHORT).show()
            etNewPassword.requestFocus()
            return false
        }

        return true
    }

    private fun showLoading(message: String) {
        if (loadingDialog?.isShowing == true) {
            loadingDialog?.dismiss()
        }

        loadingDialog = AlertDialog.Builder(this)
            .setMessage(message)
            .setCancelable(false)
            .create()
        loadingDialog?.show()
    }

    private fun hideLoading() {
        loadingDialog?.dismiss()
    }

    // 로그인 ID 찾기 (여러 방법 시도)
    private fun getLoginId(): String {
        // 1. SharedPreferences에서 찾기
        val savedLoginId = prefs.getString("login_id", null)
        if (!savedLoginId.isNullOrEmpty()) {
            Log.d("PasswordChange", "SharedPreferences에서 로그인 ID 발견: $savedLoginId")
            return savedLoginId
        }

        // 2. 다른 가능한 키들 시도
        val possibleKeys = listOf("loginId", "user_id", "username", "email")
        for (key in possibleKeys) {
            val value = prefs.getString(key, null)
            if (!value.isNullOrEmpty()) {
                Log.d("PasswordChange", "키 '$key'에서 로그인 ID 발견: $value")
                return value
            }
        }
        /*아래 부분은 디버깅용 하드코딩임. 실 배포/운영에선 쓸모 없음.
        *조치: 진짜 fallback이 필요하다면 ‘null’이나 안전한 기본값(예: “”)으로 대체.
        * 실사용에서는 SharedPreferences에서 못 찾으면 오류 알림이 더 적절.
         */
        // 3. 비밀번호 찾기에서 사용했던 로그인 ID (화면에서 확인한 것)
        val tempLoginId = "12345"
        Log.d("PasswordChange", "임시 로그인 ID 사용: $tempLoginId")

        return tempLoginId
    }

    // 비밀번호 변경 API 호출
    private fun changePassword() {
        val loginId = getLoginId()
        val currentPassword = etCurrentPassword.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()

        showLoading("비밀번호를 변경 중입니다...")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val requestBody = PasswordChangeRequestDto(
                    login_id = loginId,
                    current_password = currentPassword,
                    new_password = newPassword
                )

                Log.d("PasswordChange", "비밀번호 변경 요청:")
                Log.d("PasswordChange", "- 로그인 ID: $loginId")
                Log.d("PasswordChange", "- 현재 비밀번호: ${currentPassword.take(3)}... (일부만 표시)")
                Log.d("PasswordChange", "- 새 비밀번호: ${newPassword.take(3)}... (일부만 표시)")

                val response = RetrofitClient.userApiService.changePassword(requestBody)

                withContext(Dispatchers.Main) {
                    hideLoading()

                    if (response.success) {
                        showSuccessDialog()
                    } else {
                        handlePasswordChangeError(response.message)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideLoading()
                    handleNetworkError(e)
                }
            }
        }
    }

    // 비밀번호 변경 에러 처리
    private fun handlePasswordChangeError(message: String) {
        when {
            message.contains("현재 비밀번호") -> {
                AlertDialog.Builder(this)
                    .setTitle("현재 비밀번호 오류")
                    .setMessage("현재 비밀번호가 일치하지 않습니다.\n\n💡 확인사항:\n• 비밀번호 찾기로 받은 임시 비밀번호를 입력하셨나요?\n• 임시 비밀번호: 영문 대소문자 + 숫자 10자리")
                    .setPositiveButton("확인") { _, _ ->
                        etCurrentPassword.selectAll()
                        etCurrentPassword.requestFocus()
                    }
                    .show()
            }
            message.contains("사용자") -> {
                Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // 네트워크 오류 처리
    private fun handleNetworkError(e: Exception) {
        Log.e("PasswordChange", "네트워크 오류", e)
        when (e) {
            is SocketTimeoutException -> {
                Toast.makeText(this, "서버 응답 시간이 초과되었습니다.", Toast.LENGTH_SHORT).show()
            }
            is IOException -> {
                Toast.makeText(this, "네트워크 연결에 문제가 있습니다.", Toast.LENGTH_SHORT).show()
            }
            is HttpException -> {
                when (e.code()) {
                    401 -> {
                        AlertDialog.Builder(this)
                            .setTitle("인증 오류")
                            .setMessage("현재 비밀번호가 올바르지 않습니다.\n\n비밀번호 찾기로 받은 임시 비밀번호를 정확히 입력해주세요.")
                            .setPositiveButton("확인") { _, _ ->
                                etCurrentPassword.selectAll()
                                etCurrentPassword.requestFocus()
                            }
                            .show()
                    }
                    404 -> Toast.makeText(this, "사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(this, "서버 오류: ${e.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 성공 다이얼로그 - 자동 로그아웃 포함
    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("🎉 비밀번호 변경 완료!")
            .setMessage("비밀번호가 성공적으로 변경되었습니다!\n\n🔐 보안을 위해 자동으로 로그아웃됩니다.\n✨ 새로운 비밀번호로 다시 로그인해주세요.")
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
                logoutAndGoToLogin()
            }
            .setCancelable(false)
            .show()
    }

    // 로그아웃 후 로그인 화면으로 이동
    private fun logoutAndGoToLogin() {
        try {
            // 1. SharedPreferences 완전 삭제 (모든 세션 정보 제거)
            prefs.edit().clear().apply()
            Log.d("PasswordChange", "SharedPreferences 완전 삭제 완료")

            // 2. 로그인 화면으로 이동하고 모든 이전 액티비티 제거
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            // 4. 약간의 지연 후 로그인 화면으로 이동 (Toast가 보이도록)
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(intent)
                finish()
            }, 1000) // 1초 지연

            Log.d("PasswordChange", "로그인 화면으로 리다이렉션 완료")

        } catch (e: Exception) {
            Log.e("PasswordChange", "로그아웃 처리 중 오류", e)
            // 오류 발생 시에도 기본적으로 앱을 종료
            finish()
        }
    }

    // 뒤로가기 버튼 처리 (비밀번호 변경 중에는 막기)
    override fun onBackPressed() {
        if (loadingDialog?.isShowing == true) {
            Toast.makeText(this, "비밀번호 변경 중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingDialog?.dismiss()
    }
}