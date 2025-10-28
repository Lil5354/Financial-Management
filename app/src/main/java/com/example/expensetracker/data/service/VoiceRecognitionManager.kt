package com.example.expensetracker.data.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceRecognitionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null
    
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening
    
    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _error.value = "Speech recognition không khả dụng trên thiết bị này"
            return
        }
        
        stopListening() // Stop any existing recognition
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                    _error.value = null
                }
                
                override fun onBeginningOfSpeech() {
                    // User started speaking
                }
                
                override fun onRmsChanged(rmsdB: Float) {
                    // Volume changed
                }
                
                override fun onBufferReceived(buffer: ByteArray?) {
                    // Partial recognition results
                }
                
                override fun onEndOfSpeech() {
                    _isListening.value = false
                }
                
                override fun onError(error: Int) {
                    _isListening.value = false
                    _error.value = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Lỗi ghi âm"
                        SpeechRecognizer.ERROR_CLIENT -> "Lỗi client"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Chưa cấp quyền microphone"
                        SpeechRecognizer.ERROR_NETWORK -> "Lỗi mạng"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Hết thời gian kết nối"
                        SpeechRecognizer.ERROR_NO_MATCH -> "Không nhận diện được giọng nói"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer đang bận"
                        SpeechRecognizer.ERROR_SERVER -> "Lỗi server"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Không phát hiện giọng nói"
                        else -> "Lỗi không xác định"
                    }
                }
                
                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { matches ->
                        if (matches.isNotEmpty()) {
                            _recognizedText.value = matches[0]
                        }
                    }
                }
                
                override fun onPartialResults(partialResults: Bundle?) {
                    // Partial recognition results
                    partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { matches ->
                        if (matches.isNotEmpty()) {
                            // Could update UI with partial results
                        }
                    }
                }
                
                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Reserved for future use
                }
            })
        }
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN") // Vietnamese
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _error.value = "Không thể khởi động speech recognition: ${e.message}"
            _isListening.value = false
        }
    }
    
    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            // Ignore errors when stopping
        }
        _isListening.value = false
    }
    
    fun clearText() {
        _recognizedText.value = ""
    }
    
    fun clearError() {
        _error.value = null
    }
}

