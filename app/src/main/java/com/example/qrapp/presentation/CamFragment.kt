package com.example.qrapp.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.qrapp.databinding.FragmentCamBinding


class CamFragment : Fragment() {

    private var _binding: FragmentCamBinding? = null
    private val binding: FragmentCamBinding
        get() = _binding ?: throw java.lang.RuntimeException("FragmentCamBinding = null")


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCamBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}