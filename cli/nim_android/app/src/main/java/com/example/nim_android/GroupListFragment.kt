package com.example.nim_android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.nim_android.databinding.FragmentFriendListBinding
import com.example.sdk_nim.entity.BaseEntity
import com.example.sdk_nim.user.FriendEntity
import com.example.sdk_nim.user.GroupInfoEntity
import com.example.sdk_nim.utils.L
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class GroupListFragment : Fragment() {
    val gson = Gson()
    val adapter = GroupListAdapter()
    lateinit var binding: FragmentFriendListBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentFriendListBinding.inflate(layoutInflater)
        getGroupList()

        adapter.setOnItemClickListener { _, view, position ->

        }
        return binding.root
    }

    private fun getGroupList() {
        val request: Request = Request.Builder()
            .url(String.format("http://%s/user/groupList", Constant.LOCAL_IP))
            .get()
            .addHeader("token", App.app.entity.token)
            .build()
        App.app.okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                val str = response.body!!.string()
                System.err.println("resEntity  1111==>$str")
                val res = gson.fromJson<BaseEntity<List<GroupInfoEntity>>>(
                    str,
                    object : TypeToken<BaseEntity<List<GroupInfoEntity?>?>?>() {}.type
                )
                if (res.success()) {
                    L.p("getAllFriend==>" + res.data)
                    adapter.addData(res.data)
                }
            }
        })
    }
}

class GroupListAdapter :
    BaseQuickAdapter<GroupInfoEntity, GroupListHolder>(R.layout.item_friend) {
    override fun convert(holder: GroupListHolder, item: GroupInfoEntity) {
        holder.setText(R.id.name, item.name)
    }
}

class GroupListHolder(view: View) : BaseViewHolder(view) {
    init {

    }
}