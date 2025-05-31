package com.AzaAza.foodcare.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.models.MemberResponse
import com.AzaAza.foodcare.ui.MemberActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class MemberAdapter(
    private val memberList: List<MemberResponse>,
    private val onMemberClick: (MemberResponse) -> Unit // 클릭 시 람다 전달
) : RecyclerView.Adapter<MemberAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.memberPhoto)
        val nameText: TextView = view.findViewById(R.id.memberName)
        val roleText: TextView = view.findViewById(R.id.memberRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.member_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = memberList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val member = memberList[position]
        Log.d("PROFILE_TEST", "username=${member.username}, profile_image_url=${member.profile_image_url}")

        holder.nameText.text = member.username
        holder.roleText.text = if (member.is_owner) "대표" else "구성원"

        // ⭐️ 프로필 사진 세팅
        if (!member.profile_image_url.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load("https://foodcare-69ae76eec1bf.herokuapp.com" + member.profile_image_url)
                .circleCrop()
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_profile)
        }

        // 아이템 클릭 시 상세 팝업
        holder.itemView.setOnClickListener {
            onMemberClick(member)
        }
    }

}
