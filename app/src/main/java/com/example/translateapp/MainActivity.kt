package com.example.translateapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var mainLangue : Spinner
    private lateinit var toLangue : Spinner
    private lateinit var microphone : CardView
    private lateinit var translateBUtton : Button
    private lateinit var resultText : EditText
    private lateinit var sourceText : EditText

    private val fromLangue = arrayListOf<String>("Türkçe","English")
    private val toLangueList = arrayListOf<String>("English","Türkçe")

    private val REQUEST_PERMISSION_CODE = 1
    private var langueCode = 0
    private var mainLangueCode = 0
    private var toLangueCode = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainLangue = findViewById(R.id.mainLangue)
        toLangue = findViewById(R.id.toLangue)
        microphone = findViewById(R.id.mic)
        translateBUtton = findViewById(R.id.translate)
        resultText = findViewById(R.id.resultText)
        sourceText = findViewById(R.id.sourceText)

        mainLangue.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                mainLangueCode = getLanguageCode(fromLangue[p2])
                mainLanguage.setText(fromLangue[p2])
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        val mainLanguageAdapter = ArrayAdapter(this,R.layout.spinner_item,fromLangue)
        mainLanguageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mainLangue.adapter = mainLanguageAdapter

        toLangue.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                toLangueCode = getLanguageCode(toLangueList[p2])
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        val toLanguageAdapter = ArrayAdapter(this,R.layout.spinner_item,toLangueList)
        toLanguageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        toLangue.adapter = toLanguageAdapter

        translateBUtton.setOnClickListener {
            if(sourceText.text.toString().isEmpty()){
                Toast.makeText(this,"Lütfen metin girin",Toast.LENGTH_SHORT).show()
            }else if(mainLangueCode == 0){
                Toast.makeText(this,"Çevrilecek dili seçin!",Toast.LENGTH_SHORT).show()
            }else if(toLangueCode == 0){
                Toast.makeText(this,"Çevirmek istediğiniz dili seçin!",Toast.LENGTH_SHORT).show()
            }else{
                translate(mainLangueCode,toLangueCode, sourceText.text.toString())
            }
        }

        mic.setOnClickListener{
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Konuş ...")
            try {
                startActivityForResult(intent,REQUEST_PERMISSION_CODE)
            }catch (e : Exception){
                e.printStackTrace()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PERMISSION_CODE){
            if (resultCode == RESULT_OK && data != null){
                val result : ArrayList<String> = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)!!
                sourceText.setText(result.get(0))
            }
        }
    }

    private fun translate(mainLanguageCode:Int,toLanguageCode:Int,Text:String){

        resultText.setText("Model indiriliyor ...")
        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(mainLanguageCode)
            .setTargetLanguage(toLanguageCode)
            .build()

        val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
        val conditions = FirebaseModelDownloadConditions.Builder().build()

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener { task ->
            resultText.setText("Çevriliyor")
            translator.translate(Text).addOnSuccessListener { task1->
                resultText.setText(task1)
            }.addOnFailureListener { error ->
                Toast.makeText(this,"Hata",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this,"Hata",Toast.LENGTH_SHORT).show()
        }

    }

    private fun getLanguageCode(language : String): Int {
        var languageCode = 0
        if (language == "Türkçe") {
            languageCode = FirebaseTranslateLanguage.TR
        }else{
            languageCode = FirebaseTranslateLanguage.EN
        }
        return languageCode
    }
}