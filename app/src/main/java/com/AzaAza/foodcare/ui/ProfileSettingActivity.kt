package com.AzaAza.foodcare.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore

import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.api.RetrofitClient
import com.AzaAza.foodcare.models.SignUpRequest

import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileSettingActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                profileImageView.setImageURI(it)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            profileImageView.setImageBitmap(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setting)

        profileImageView = findViewById(R.id.profileImage)
        nameEditText = findViewById(R.id.inputName)
        emailEditText = findViewById(R.id.inputEmail)

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            onBackPressed()
        }

        profileImageView.setOnClickListener {
            showImagePickOptions()
        }

        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            Toast.makeText(this, "저장됨: $name / $email", Toast.LENGTH_SHORT).show()
            // TODO: 저장 처리
        }

        loadUserInfo()
    }

    private fun loadUserInfo() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val loginId = prefs.getString("USER_LOGIN_ID", null)

        if (loginId == null) {
            Toast.makeText(this, "로그인 정보 없음", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.userApiService.getUserListAsSignUpRequest()
            .enqueue(object : Callback<List<SignUpRequest>> {
                override fun onResponse(
                call: Call<List<SignUpRequest>>,
                response: Response<List<SignUpRequest>>
            ) {
                if (response.isSuccessful) {
                    val user = response.body()?.find { it.login_id == loginId }
                    if (user != null) {
                        nameEditText.setText(user.username)
                        emailEditText.setText(user.email)
                    } else {
                        Toast.makeText(this@ProfileSettingActivity, "사용자 정보 없음", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ProfileSettingActivity, "서버 오류", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<SignUpRequest>>, t: Throwable) {
                Toast.makeText(this@ProfileSettingActivity, "통신 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showImagePickOptions() {
        val options = arrayOf("갤러리에서 선택", "카메라로 촬영")
        android.app.AlertDialog.Builder(this)
            .setTitle("프로필 사진 선택")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickImageFromGallery()
                    1 -> takePictureFromCamera()
                }
            }.show()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun takePictureFromCamera() {
        cameraLauncher.launch(null)
    }
}