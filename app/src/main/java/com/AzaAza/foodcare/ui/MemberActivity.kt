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
import com.AzaAza.foodcare.models.AcceptInviteRequest
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
    private val ownerId: Int by lazy {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.getInt("OWNER_ID", getMyUserIdFromPrefs())
    }
    // 내 user id (본인 id)
    private val myUserId: Int by lazy {
        getMyUserIdFromPrefs()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MemberActivity", "onCreate: 내 user id = $myUserId")
        if (myUserId == 0) {
            Toast.makeText(this, "유저 정보가 비정상입니다. 다시 로그인 해주세요.", Toast.LENGTH_LONG).show()
        }

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
            btnAddMember.isEnabled = !isManageMode   // 관리모드면 추가 버튼 비활성화
            refreshMemberList()
        }


        refreshMemberList()

        checkPendingInviteAndShowDialog()

    }

    private fun refreshMemberList() {
        RetrofitClient.userApiService.getMembers(ownerId)

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
                    Log.d("초대", "응답 코드: ${response.code()} / body: ${response.body()} / error: ${response.errorBody()?.string()}")
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
            member.id == ownerId -> "대표"
            member.status == "pending" -> if (member.id == myUserId) "나 - 초대받음" else "초대함"
            member.id == myUserId -> "나 - 구성원"
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

        if (member.id == myUserId && !member.is_owner) {
            // 내 row, 그리고 내가 방장이 아닐 때만 나가기 버튼
            btnDelete.visibility = if (isManageMode) View.VISIBLE else View.GONE
            btnDelete.text = "나가기"
            btnDelete.setOnClickListener {
                confirmAndLeaveGroup(member)
            }
        }

    }
    private fun confirmAndLeaveGroup(member: MemberResponse) {
        AlertDialog.Builder(this)
            .setTitle("그룹 나가기")
            .setMessage("정말로 그룹에서 나가시겠습니까?")
            .setPositiveButton("나가기") { _, _ ->
                deleteMember(member)  // 기존 delete API 활용
            }
            .setNegativeButton("취소", null)
            .show()
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

    private fun checkPendingInviteAndShowDialog() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val myUserId = prefs.getInt("USER_ID", 0)
        RetrofitClient.userApiService.getPendingInvites(myUserId)
            .enqueue(object : Callback<List<InviteResponse>> {
                override fun onResponse(call: Call<List<InviteResponse>>, response: Response<List<InviteResponse>>) {
                    val invites = response.body() ?: emptyList()
                    if (invites.isNotEmpty()) {
                        showInviteAcceptDialog(invites[0]) // 여러 개면 첫 번째만, 필요하면 반복
                    }
                }
                override fun onFailure(call: Call<List<InviteResponse>>, t: Throwable) {}
            })
    }

    private fun showInviteAcceptDialog(invite: InviteResponse) {
        AlertDialog.Builder(this)
            .setTitle("구성원 초대")
            .setMessage("${invite.owner_username}님이 초대했습니다. 수락하시겠습니까?")
            .setPositiveButton("수락") { _, _ ->
                // null safe 처리
                val ownerId = invite.owner_id
                val memberId = invite.member_id
                if (ownerId != null && memberId != null) {
                    acceptInvite(ownerId, memberId)
                } else {
                    Toast.makeText(this, "초대 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("거절") { _, _ ->
                // 거절(= membership 삭제)
                if (invite.owner_id != null && invite.member_id != null) {
                    deleteInvite(invite.owner_id, invite.member_id)
                } else {
                    Toast.makeText(this, "초대 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            .show()
    }
    private fun deleteInvite(ownerId: Int, memberId: Int) {
        RetrofitClient.userApiService.deleteMember(ownerId, memberId)
            .enqueue(object : Callback<InviteResponse> {
                override fun onResponse(call: Call<InviteResponse>, response: Response<InviteResponse>) {
                    Toast.makeText(this@MemberActivity, "초대 거절 완료", Toast.LENGTH_SHORT).show()
                    refreshMemberList()
                }
                override fun onFailure(call: Call<InviteResponse>, t: Throwable) {
                    Toast.makeText(this@MemberActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun acceptInvite(ownerId: Int, memberId: Int) {
        val req = AcceptInviteRequest(owner_id = ownerId, member_id = memberId)
        RetrofitClient.userApiService.acceptInvite(req)
            .enqueue(object : Callback<InviteResponse> {
                override fun onResponse(call: Call<InviteResponse>, response: Response<InviteResponse>) {
                    // 여기서 ownerId를 SharedPreferences에 저장
                    val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putInt("OWNER_ID", ownerId).apply()
                    Toast.makeText(this@MemberActivity, "초대 수락 완료!", Toast.LENGTH_SHORT).show()
                    refreshMemberList()
                }
                override fun onFailure(call: Call<InviteResponse>, t: Throwable) {}
            })
    }


    override fun onResume() {
        super.onResume()
        refreshMemberList()
    }



}
