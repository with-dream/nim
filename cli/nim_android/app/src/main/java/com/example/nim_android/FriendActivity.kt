package com.example.nim_android

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.nim_android.databinding.ActivityFriendBinding
import com.example.nim_android.entity.FriendInfoEntity
import com.example.nim_android.entity.RequestEntity
import com.example.sdk_nim.entity.BaseEntity
import com.example.sdk_nim.entity.UserResEntity
import com.example.sdk_nim.netty.IMContext
import com.example.sdk_nim.utils.L
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class FriendActivity : AppCompatActivity() {
    private val JSONType: MediaType =
        "application/json; charset=utf-8".toMediaTypeOrNull()!!
    val gson = Gson()
    private val fragmentList = listOf(FriendListFragment(), GroupListFragment())
    lateinit var binding: ActivityFriendBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewpager.adapter = PagerAdapter(supportFragmentManager, fragmentList)
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.friend -> {

            }
            R.id.group -> {

            }
            R.id.addF -> {
                val re = RequestEntity()
                re.friendId = binding.uuidET.toString()
                re.targetType = RequestEntity.FRIEND
                addFriend(re)
            }
            R.id.addG -> {

            }
        }
    }

    private fun addFriend(re: RequestEntity) {
        val body = gson.toJson(re).toRequestBody(JSONType)

        val request: Request = Request.Builder()
            .url("http://${Constant.LOCAL_IP}/user/addFriendReq")
            .addHeader("token", App.app.entity!!.token)
            .post(body)
            .build()
        App.app.okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                val str: String? = response.body?.string()
                L.p("resEntity  1111==>$str")
                val res: BaseEntity<List<FriendInfoEntity>> =
                    gson.fromJson(
                        str,
                        object : TypeToken<BaseEntity<List<FriendInfoEntity>>?>() {}.type
                    )
                if (res.success()) {
                    val entity = res.data

                } else {
                    L.p("==>登录失败")
                }
            }
        })
    }

    class PagerAdapter(fm: FragmentManager, private val data: List<Fragment>) :
        FragmentPagerAdapter(fm) {
        override fun getCount(): Int {
            return data.size
        }

        override fun getItem(position: Int): Fragment {
            return data[position]
        }
    }
}