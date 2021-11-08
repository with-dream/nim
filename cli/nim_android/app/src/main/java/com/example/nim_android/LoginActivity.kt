package com.example.nim_android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.nim_android.databinding.ActivityLoginBinding

import com.example.sdk_nim.netty.IMContext

import com.example.sdk_nim.entity.BaseEntity
import com.google.gson.reflect.TypeToken
import com.example.sdk_nim.entity.UserResEntity

import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import com.example.sdk_nim.netty.IMConnCallback
import com.example.sdk_nim.utils.*

class LoginActivity : AppCompatActivity() {
    val gson = Gson()
    var userEntity: UserResEntity? = null
    lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        IMContext.instance().callback = IMConnCallback {
            L.p("LoginActivity==>$it");
            if (it == com.example.sdk_nim.utils.Constant.SUCC) {
                startActivity(Intent(this@LoginActivity, FriendActivity::class.java))
            }
        }
    }

    fun onclick(v: View) {
        when (v.id) {
            R.id.login -> {
                login(binding.userName.text.toString(), binding.pwd.text.toString())
            }
            R.id.register -> {
                startActivity(Intent(this, RegisterActivity::class.java))
            }
        }
    }


    private fun login(name: String, pwd: String) {
        IMContext.instance().clientToken = UUIDUtil.getClientToken()
        val request: Request = Request.Builder()
            .url(
                String.format(
                    "http://%s/user/login?name=%s&pwd=%s&clientToken=%d",
                    Constant.LOCAL_IP,
                    name,
                    pwd,
                    IMContext.instance().clientToken
                )
            )
            .get()
            .build()
        App.app.okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                val str: String? = response.body?.string()
                System.err.println("resEntity  1111==>$str")
                val res: BaseEntity<UserResEntity> =
                    gson.fromJson(str, object : TypeToken<BaseEntity<UserResEntity?>?>() {}.type)
                if (res.success()) {
                    userEntity = res.data
                    App.app.entity = userEntity;
                    L.p("resEntity==>" + userEntity.toString())
//                    IMContext.instance().setIpList(Arrays.asList(Constant.NETTY_IP))
                    IMContext.instance().uuid = userEntity?.uuid
                    val pair: KeyPair = RSAUtil.getKeyPair()
                    IMContext.instance().encrypt.privateRSAClientKey = RSAUtil.getPrivateKey(pair)
                    IMContext.instance().encrypt.publicRSAClientKey = RSAUtil.getPublicKey(pair)
                    IMContext.instance().encrypt.publicRSAServerKey = userEntity?.rsaPublicKey

                    val publicKey: PublicKey =
                        RSAUtil.string2PublicKey(IMContext.instance().encrypt.publicRSAServerKey)
                    var pubClientKeyByte: ByteArray = RSAUtil.publicEncrytype(
                        IMContext.instance().encrypt.publicRSAClientKey.toByteArray(),
                        publicKey
                    )

                    var pubClientKey: String =
                        JBase64.getEncoder().encodeToString(pubClientKeyByte)
                    encrypt1(pubClientKey)
                } else {
                    L.p("==>登录失败")
                }
            }
        })
    }

    private fun encrypt1(key: String) {
        L.p("encrypt1 ct==>" + IMContext.instance().clientToken)
        val body = FormBody.Builder()
            .add("uuid", userEntity!!.uuid)
            .add("clientToken", IMContext.instance().clientToken.toString())
            .add("key", key).build()
        val request: Request = Request.Builder()
            .url("http://${Constant.LOCAL_IP}/user/encrypt1")
            .addHeader("token", userEntity!!.token)
            .post(body)
            .build()
        App.app.okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                val str: String? = response.body?.string()
                L.p("resEntity  1111==>$str")
                val res: BaseEntity<UserResEntity> =
                    gson.fromJson(str, object : TypeToken<BaseEntity<UserResEntity?>?>() {}.type)
                if (res.success()) {
                    val entity = res.data
                    IMContext.instance().setIpList(userEntity?.serviceList);
                    val privateKey: PrivateKey =
                        RSAUtil.string2Privatekey(IMContext.instance().encrypt.privateRSAClientKey)
                    val aesKeyB: ByteArray =
                        RSAUtil.privateDecrypt(JBase64.getDecoder().decode(entity.aesPublicKey), privateKey)
                    IMContext.instance().encrypt.aesKey = String(aesKeyB)
                    L.p("c aesKey==>" + IMContext.instance().encrypt.aesKey)
                    Thread { IMContext.instance().connect() }.start()
                } else {
                    L.p("==>登录失败")
                }
            }
        })
    }

}