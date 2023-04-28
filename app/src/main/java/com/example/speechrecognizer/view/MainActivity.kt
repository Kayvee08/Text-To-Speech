package com.example.speechrecognizer.view

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.speechrecognizer.data.SpeechViewModel
import com.example.speechrecognizer.databinding.ActivityMainBinding
import java.util.*


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var viewModel: SpeechViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[SpeechViewModel::class.java]
        if (savedInstanceState?.containsKey("text") == true)
            viewModel.textData = savedInstanceState.getString("text")

        setTTS()
        setUpUi()

    }

    private fun setTTS() {
        textToSpeech = TextToSpeech(
            this, this
        )
    }

    private fun setUpUi() {
        if (!viewModel.textData.isNullOrBlank()) {
            binding.txt.editText?.setText(viewModel.textData)
        }
        setListeners()
    }

    private fun setListeners() {
        binding.txt.setEndIconOnClickListener {
            if (textToSpeech.isSpeaking)
                textToSpeech.stop()
            binding.txt.editText?.setText("")
        }
        binding.speak.setOnClickListener {
            speakOut()
        }
        binding.stop.setOnClickListener {
            if (textToSpeech.isSpeaking) {
                textToSpeech.stop()
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    //Here we can add logic when the it starts speaking
                }

                override fun onDone(utteranceId: String?) {
                    //Here we can add logic when it completes speaking
                }

                override fun onError(utteranceId: String?) {
                    //Here we can show user some kind of error message
                }

            })
        } else if (status == TextToSpeech.ERROR) {
            Toast.makeText(
                this,
                "Error occurred while initializing Text-To-Speech engine", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun speakOut() {
        if (textToSpeech.isSpeaking)
            textToSpeech.stop()
        val text = binding.txt.editText?.text.toString()
        val result = textToSpeech.setLanguage(Locale("hin"))
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(
                this,
                "The Language not supported!", Toast.LENGTH_SHORT
            ).show()
        } else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (!binding.txt.editText?.text.isNullOrBlank()) {
            viewModel.textData = (binding.txt.editText?.text ?: "").toString()
            outState.putString("text", (binding.txt.editText?.text ?: "").toString())
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

}