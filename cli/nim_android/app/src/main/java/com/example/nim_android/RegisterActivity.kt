package com.example.nim_android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.nim_android.databinding.ActivityRegisterBinding
import com.example.sdk_nim.entity.BaseEntity
import com.example.sdk_nim.utils.L

import com.example.sdk_nim.utils.StrUtil
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException


class RegisterActivity : AppCompatActivity() {
    val gson = Gson()
    lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun onclick(v: View) {
        when (v.id) {
            R.id.register -> {
                register(binding.userName.text.toString(), binding.pwd.text.toString())
            }
        }
    }


    private fun register(userName: String, pwd: String) {
        if (StrUtil.isEmpty(userName) || StrUtil.isEmpty(pwd)) {
            L.e("用户名 密码不能空")
            return
        }
        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder()
            .url(
                java.lang.String.format(
                    "http://%s/user/register?name=%s&pwd=%s",
                    Constant.LOCAL_IP,
                    userName,
                    pwd
                )
            )
            .get()
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                L.e("register onFailure ==>$e")
            }

            override fun onResponse(call: Call, response: Response) {
                val str = response.body?.string()
                L.e("register onResponse ==>$str")
                val res = gson.fromJson(str, BaseEntity::class.java)
                var text = "注册成功"
                if (!res.success())
                    text = "注册失败 ${res.code}"
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, text, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}