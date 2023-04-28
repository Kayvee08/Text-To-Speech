package com.example.speechrecognizer.data

import androidx.lifecycle.ViewModel
import java.util.*
import kotlin.collections.ArrayList

class SpeechViewModel :ViewModel() {

    //var locale: Locale = Locale.ENGLISH

    //var localeOptions  = arrayListOf<Locale>(Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN, Locale("hin"))

//    fun getSpinnerData(): ArrayList<Locale>{
//        return localeOptions
//    }

    var textData : String ?= null

}