package com.example.avataruploader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.avataruploader.databinding.FragmentMainBinding
import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.coroutines.*
import java.net.CookieHandler
import java.net.CookieManager
import java.net.HttpURLConnection
import java.net.URL

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        val geometricView = binding.geometricView //將view調用的自定義view指派給geometricView變數
        val base64 = imageTask("sadasd")
        val bitmap = base64ToBitmap(base64!!)
        geometricView.setImage(bitmap!!)
//        binding.textView.text =( imageTask("hen85618")+"==").length.toString()
        return binding.root
    }

    fun image(username: String): String? {
        val url = "http://10.0.2.2:8080/javaweb-exercise-01-2/member/$username"
        var conn: HttpURLConnection? = null
        try {
            if (CookieHandler.getDefault() == null) {
                CookieHandler.setDefault(CookieManager())//紀錄cookie的值(實例化CookieManager取得資料)
            }
            conn = URL(url).openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            conn.useCaches = false
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                conn.inputStream.reader().use {
                    return Gson().fromJson(it, String::class.java)
                }
            }
        } catch (e: Exception) {
            conn?.disconnect()
            Log.e("main", e.toString())
        }
        return null
    }

    fun imageTask(username: String) = runBlocking {
        val result = async(Dispatchers.IO) {
            image(username)
        }.await()
        return@runBlocking result
    }

    fun base64ToBitmap(base64String: String): Bitmap? {
        val trimmedBase64String = base64String.trim() // 去除字符串两端的空格
        val decodedBytes = Base64.decode(trimmedBase64String,Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}
