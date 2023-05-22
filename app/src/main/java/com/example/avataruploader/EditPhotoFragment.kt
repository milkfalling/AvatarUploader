package com.example.avataruploader

import android.app.Activity
import android.content.Intent
import android.content.Intent.getIntent
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.avataruploader.databinding.FragmentEditPhotoBinding
import com.yalantis.ucrop.UCrop
import java.io.File

class EditPhotoFragment : Fragment() {
    private lateinit var binding: FragmentEditPhotoBinding
    private lateinit var contentUri:Uri

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentEditPhotoBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            button.setOnClickListener {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE) //Intent()
                val file = File(requireContext().getExternalFilesDir(null),"picture.jpg")
                contentUri = FileProvider.getUriForFile(requireContext(),requireContext().packageName,file)
                intent.putExtra(MediaStore.EXTRA_OUTPUT,contentUri)
                takePictureLauncher.launch(intent)
            }
        }
    }
    private var takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                crop(contentUri)
            }
        }

    private var pickPictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {//使用者確定要這張的話
                result.data?.data?.let { uri -> crop(uri) }//跑迴圈呼叫crop取
            }
        }

    private fun crop(sourceImageUri: Uri) {
        val file = File(requireContext().getExternalFilesDir(null), "picture_cropped.jpg")
        val destinationUri = Uri.fromFile(file)
        val cropIntent: Intent = UCrop.of(//Intent可以不用寫只是老師特別標出來，
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
                            binding.geometricView2.setImage(bitmap)
                        } else {
                            return@registerForActivityResult

                        }
                    }
                }
            }
        }

}