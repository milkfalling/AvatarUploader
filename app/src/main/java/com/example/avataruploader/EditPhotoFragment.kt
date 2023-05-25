package com.example.avataruploader

import android.app.Activity
import android.content.Intent
import android.content.Intent.getIntent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.avataruploader.databinding.FragmentEditPhotoBinding
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.CookieHandler
import java.net.CookieManager
import java.net.HttpURLConnection
import java.net.URL

class EditPhotoFragment : Fragment() {
    private lateinit var binding: FragmentEditPhotoBinding
    private lateinit var contentUri: Uri
    private lateinit var myTag:String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentEditPhotoBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val geometricView = binding.geometricView2 //將view調用的自定義view指派給geometricView變數
        val base64 = imageTask("hen85618")
        val bitmap = base64ToBitmap(base64!!)
        geometricView.setImage(bitmap!!)
        with(binding) {
            btTakePhoto.setOnClickListener {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE) //告訴裝置要使用相機拍照
                //在外部檔案目錄中建立一個File檔案並命名為picture.jpg        type:不指定子目錄
                val file = File(requireContext().getExternalFilesDir(null), "picture.jpg")
                //將上面的file轉換為相應的內容URI(requireContext()獲取上下文,requireContext().packageName獲取應用程式的套件名(為了不要重複識別),File物件)
                contentUri =
                    FileProvider.getUriForFile(requireContext(), requireContext().packageName, file)
                //在intent多加一個欄位，並將上面取得的Uri放到裡面
                intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri)
                //啟動Launcher
                takePictureLauncher.launch(intent)
            }
            btEditPhoto.setOnClickListener {
                //製作一個意圖
                val intent = Intent(
                    //指定此意圖為用於選取一個特定的內容項目
                    Intent.ACTION_PICK,
                    //指定此意圖開啟手機內建的相簿或圖庫
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                pickPictureLauncher.launch(intent)//將意圖傳入啟動器
            }
        }
    }

    private var takePictureLauncher =
        //註冊Activity結果處理器(當Activity結束並返回結果時，將執行註冊的結果處理器{result})
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                crop(contentUri)
            }
        }

    private var pickPictureLauncher =
        //註冊Activity結果處理器(當Activity結束並返回結果時，將執行註冊的結果處理器{result})
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {//使用者確定要這張的話
                result.data?.data?.let { uri -> crop(uri) }
            }
        }

    private fun crop(sourceImageUri: Uri) {
        val file = File(requireContext().getExternalFilesDir(null), "picture_cropped.jpg")
        val destinationUri = Uri.fromFile(file)
        val cropIntent: Intent = UCrop.of(
            sourceImageUri,//來源的目的地
            destinationUri//目的地的Uri
        )
            // .withAspectRatio(16, 9) // 設定裁減比例
            // .withMaxResultSize(500, 500) // 設定結果尺寸不可超過指定寬高
            .getIntent(requireContext())
        cropPictureLauncher.launch(cropIntent)//這邊拿到的會是要拿來編輯的圖，把他傳給Launcher
    }

    private var cropPictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {//只要user選擇要這張截圖了
                result.data?.let { intent ->
                    UCrop.getOutput(intent)?.let { uri ->
                        val bitmap = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                            BitmapFactory.decodeStream(//將uri讀進來變成bitmap
                                requireContext().contentResolver.openInputStream(uri)
                            )
                        } else {
                            val source = ImageDecoder.createSource(
                                requireContext().contentResolver, uri
                            )
                            ImageDecoder.decodeBitmap(source)
                        }
                        // 有圖片即顯示，沒圖片則套用no_image圖片
                        if (bitmap != null) {
                            uploadTask(bitmapToBase64(bitmap)){result:Int? ->
                                if(result == 1){
                                    val base64 = imageTask("hen85618")
                                    val bitmap = base64ToBitmap(base64!!)
                                    binding.geometricView2.setImage(bitmap!!)
                                }
                            }

                        } else {
                            return@registerForActivityResult
                        }
                    }
                }
            }
        }

    //把Bitmap轉成base64
    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()//要先將bitmap壓縮存到輸出流之中，可以理解為暫存區
        //將圖片壓縮為PNG檔(指定壓縮的格式﹑壓縮品質0-100﹑輸出目標(暫存區))
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()//去暫存區裡面拿壓好的圖片
        val base64 = Base64.encodeToString(byteArray, Base64.DEFAULT)//將押好的圖片轉為Base64.DEFAULT格式
        return base64
    }
    fun base64ToBitmap(base64String: String): Bitmap? {
        val trimmedBase64String = base64String.trim() // 去除字符串两端的空格
        val decodedBytes = Base64.decode(trimmedBase64String,Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    fun uploadImage(base64: String): Int? {
        val url = "http://10.0.2.2:8080/javaweb-exercise-01-2/member"
        var conn: HttpURLConnection? = null
        try {
            if (CookieHandler.getDefault() == null) {
                CookieHandler.setDefault(CookieManager())
            }
            conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"//要做一個member物件
            conn.useCaches = false
            conn.setRequestProperty("Content-Type", "application/json")
            val json = JSONObject()
            json.put("username","hen85618")//這邊有把帳號寫死
            json.put("avatar", base64)
            conn.outputStream.use { outputStream ->
                outputStream.write(json.toString().toByteArray())
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = conn.inputStream
                val response = inputStream.bufferedReader().use { it.readText() }
                return Gson().fromJson(response, Int::class.javaObjectType)
            } else {
                Log.e("request2", "Response code: ${conn.responseCode}")
            }
        } catch (e: Exception) {
            conn?.disconnect()
            Log.e("main", e.toString())
        } finally {
            conn?.disconnect()
        }
        return null
    }


    fun uploadTask(base64: String, callback: (Int?) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val result = uploadImage(base64)
            withContext(Dispatchers.Main) {
                callback(result)
            }
        }
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
}