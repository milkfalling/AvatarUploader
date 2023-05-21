package com.example.avataruploader

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.avataruploader.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private lateinit var binding:FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        val geometricView = binding.geometricView //將view調用的自定義view指派給geometricView變數
        val imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.img) //將圖片轉為Bitmap
        geometricView.setImage(imageBitmap) //產圖
        return binding.root
    }
}