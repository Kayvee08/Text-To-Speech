package com.example.speechrecognizer.view

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.speechrecognizer.R
import com.example.speechrecognizer.data.SpeechViewModel
import com.example.speechrecognizer.databinding.ActivityMainBinding
import com.example.speechrecognizer.util.requestPermission
import com.google.android.material.snackbar.Snackbar
import java.io.File
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
        if (savedInstanceState?.containsKey("text") == true) viewModel.textData =
            savedInstanceState.getString("text")

        setTTS()
        setUpUi()

        if (!com.example.speechrecognizer.util.checkPermission(this@MainActivity))
            requestPermission(this@MainActivity)
    }

    private fun setTTS() {
        textToSpeech = TextToSpeech(this, this)
    }

    private fun setUpUi() {
        if (!viewModel.textData.isNullOrBlank()) {
            binding.txt.editText?.setText(viewModel.textData)
        }
        setListeners()
    }

    private fun setListeners() {
        binding.txt.setEndIconOnClickListener {
            if (textToSpeech.isSpeaking) textToSpeech.stop()
            binding.txt.editText?.setText("")
        }
        binding.speak.setOnClickListener {
            if (!binding.txt.editText?.text.isNullOrBlank()) viewModel.textData =
                binding.txt.editText?.text.toString()
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

        } else if (status == TextToSpeech.ERROR) {
            Toast.makeText(
                this, "Error occurred while initializing Text-To-Speech engine", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun speakOut() {
        if (textToSpeech.isSpeaking) textToSpeech.stop()
        val text = binding.txt.editText?.text.toString()
        val result = textToSpeech.setLanguage(Locale("hin"))
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(
                this, "The Language not supported!", Toast.LENGTH_SHORT
            ).show()
        } else {
            val params: HashMap<String, String> = HashMap()
            params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "123"

            showAlertDialog()
            textToSpeech.setSpeechRate(0.8f)
            textToSpeech.speak(
                text, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID
            )
        }
    }

    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Save Audio File?")

        builder.setPositiveButton(R.string.yes) { _, _ ->
            saveFileListener()
        }

        builder.setNegativeButton(R.string.no) { _, _ ->
        }
        builder.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (!binding.txt.editText?.text.isNullOrBlank()) {
            outState.putString("text", (binding.txt.editText?.text ?: "").toString())
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

    private fun saveFileListener() {
        if (viewModel.textData.isNullOrBlank()) Toast.makeText(
            this@MainActivity,
            "Nothing to save",
            Toast.LENGTH_SHORT
        ).show()
        else {
            val text = viewModel.textData

            val sdDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "TTSAudio"
            )
            viewModel.folderPath = sdDir.absolutePath
            if (!sdDir.exists())
                sdDir.mkdir()
            val mAudioFilename =
                (sdDir.absolutePath + "/" + System.currentTimeMillis().toString()) + ".wav"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech.synthesizeToFile(text, null, File(mAudioFilename), "123")
                Snackbar.make(
                    findViewById(android.R.id.content), "File saved", Snackbar.LENGTH_SHORT
                ).setAction(
                    "OPEN"
                ) {
                    openFile(File(mAudioFilename))
                }.show()

            } else {
                val hm: HashMap<String, String> = HashMap()
                hm[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "123"
                textToSpeech.synthesizeToFile(text, hm, mAudioFilename)
                Toast.makeText(this@MainActivity, "Saved to $mAudioFilename", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun openFile(selectedItem: File) {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.setDataAndType(
            Uri.parse(viewModel.folderPath),
            "*/*"
        );
        return startActivity(intent)
    }

}