package com.AzaAza.foodcare.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.api.RetrofitClient
import com.AzaAza.foodcare.models.SignUpRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.app.AlertDialog
import com.AzaAza.foodcare.models.InviteRequest
import com.AzaAza.foodcare.models.InviteResponse
import com.AzaAza.foodcare.models.MemberResponse


class MemberActivity : AppCompatActivity() {

    private lateinit var memberListContainer: LinearLayout
    private lateinit var btnAddMember: Button
    private lateinit var btnManageMember: Button

    // 관리모드 여부
    private var isManageMode = false

    // 내 user id (대표 id), 실제론 SharedPreferences 등에서 불러오기!
    private val myUserId: Int by lazy {
        val id = getMyUserIdFromPrefs()
        Log.d("MemberActivity", "내 user id: $id")
        id
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member)

        memberListContainer = findViewById(R.id.memberListContainer)
        btnAddMember = findViewById(R.id.btnAddMember)
        btnManageMember = findViewById(R.id.btnManageMember)

        findViewById<ImageView>(R.id.backButton).setOnClickListener { onBackPressed() }

        btnAddMember.setOnClickListener { showAddMemberDialog() }
        btnManageMember.setOnClickListener {
            isManageMode = !isManageMode
            btnManageMember.text = if (isManageMode) "관리 종료" else "구성원 관리"
            refreshMemberList()
        }

        refreshMemberList()
    }

    private fun refreshMemberList() {
        RetrofitClient.userApiService.getMembers(myUserId)
            .enqueue(object : Callback<List<MemberResponse>> {
                override fun onResponse(call: Call<List<MemberResponse>>, response: Response<List<MemberResponse>>) {
                    memberListContainer.removeAllViews()
                    response.body()?.forEach { member ->
                        addMemberView(member)
                    }
                }
                override fun onFailure(call: Call<List<MemberResponse>>, t: Throwable) {}
            })
    }


    private fun showAddMemberDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null)
        val input = view.findViewById<EditText>(R.id.inputId)
        AlertDialog.Builder(this)
            .setTitle("구성원 추가")
            .setView(view)
            .setPositiveButton("초대") { _, _ ->
                val loginId = input.text.toString()
                if (loginId.isNotBlank()) inviteMember(loginId)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun inviteMember(loginId: String) {
        val req = InviteRequest(owner_id = myUserId, member_login_id = loginId)
        Log.d("초대", "owner_id=${myUserId}") // 0이 아닌 값인지 꼭 로그로 확인!

        RetrofitClient.userApiService.inviteMember(req)
            .enqueue(object : Callback<InviteResponse> {
                override fun onResponse(call: Call<InviteResponse>, response: Response<InviteResponse>) {
                    val res = response.body()
                    if (res?.success == true) {
                        Toast.makeText(this@MemberActivity, "초대 전송 완료", Toast.LENGTH_SHORT).show()
                        refreshMemberList()
                    } else {
                        Toast.makeText(this@MemberActivity, res?.message ?: "초대 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<InviteResponse>, t: Throwable) {
                    Toast.makeText(this@MemberActivity, "통신 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun addMemberView(member: MemberResponse) {
        val inflater = LayoutInflater.from(this)
        val memberView = inflater.inflate(R.layout.member_item, memberListContainer, false)
        memberView.findViewById<TextView>(R.id.memberName).text = member.username

        val roleText = when {
            member.is_owner -> "대표"
            member.status == "pending" -> "초대함"
            else -> "구성원"
        }
        memberView.findViewById<TextView>(R.id.memberRole).text = roleText
        val btnDelete = memberView.findViewById<Button>(R.id.btnDelete)
        val btnCancelInvite = memberView.findViewById<Button>(R.id.btnCancelInvite)

        // 관리 모드에서만 버튼 표시
        if (isManageMode && !member.is_owner) {
            if (member.status == "pending") {
                btnCancelInvite.visibility = View.VISIBLE
                btnDelete.visibility = View.GONE
            } else {
                btnCancelInvite.visibility = View.GONE
                btnDelete.visibility = View.VISIBLE
            }
        } else {
            btnDelete.visibility = View.GONE
            btnCancelInvite.visibility = View.GONE
        }

        btnDelete.setOnClickListener {
            confirmAndDeleteMember(member)
        }
        btnCancelInvite.setOnClickListener {
            confirmAndCancelInvite(member)
        }

        memberListContainer.addView(memberView)
    }

    private fun confirmAndDeleteMember(member: MemberResponse) {
        AlertDialog.Builder(this)
            .setTitle("구성원 삭제")
            .setMessage("정말 ${member.username} 님을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                deleteMember(member)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun deleteMember(member: MemberResponse) {
        // member.id를 직접 사용!
        RetrofitClient.userApiService.deleteMember(myUserId, member.id)
            .enqueue(object : Callback<InviteResponse> {
                override fun onResponse(call: Call<InviteResponse>, response: Response<InviteResponse>) {
                    val res = response.body()
                    if (res?.success == true) {
                        Toast.makeText(this@MemberActivity, "삭제/취소 완료", Toast.LENGTH_SHORT).show()
                        refreshMemberList()
                    } else {
                        Toast.makeText(this@MemberActivity, res?.message ?: "실패", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<InviteResponse>, t: Throwable) {
                    Toast.makeText(this@MemberActivity, "통신 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun getMyUserIdFromPrefs(): Int {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val id = prefs.getInt("USER_ID", 0)
        Log.d("MemberActivity", "내 user id: $id")
        return id
    }

    private fun confirmAndCancelInvite(member: MemberResponse) {
        AlertDialog.Builder(this)
            .setTitle("초대 취소")
            .setMessage("아직 수락하지 않은 ${member.username} 님의 초대를 취소하시겠습니까?")
            .setPositiveButton("취소") { _, _ ->
                deleteMember(member)
            }
            .setNegativeButton("아니오", null)
            .show()
    }






}
