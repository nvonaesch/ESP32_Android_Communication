package com.example.bluetoothesp32

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    private val _espList = MutableLiveData<List<ESP>>()
    val espList: LiveData<List<ESP>> get() = _espList


    fun addOrUpdateESP(newESP: ESP) {
        val updatedList = _espList.value.orEmpty().toMutableList()
        val existingIndex = updatedList.indexOfFirst { it.macAddress == newESP.macAddress }
        if (existingIndex != -1) {
            updatedList[existingIndex] = newESP
        } else {
            updatedList.add(newESP)
        }
        _espList.postValue(updatedList)
        Log.d("SharedViewModel", "Liste des ESP mise à jour : $updatedList")
    }


}
