package com.example.proyectoandroid.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyectoandroid.data.AprendeClase
import com.example.proyectoandroid.data.AprendeModelo

class AprendeViewModel : ViewModel() {

    val aprendeModel = MutableLiveData<AprendeModelo>()

    fun ramdomClick() {
        val aprende = AprendeClase().ramdom()
        aprendeModel.postValue(aprende)
    }

}