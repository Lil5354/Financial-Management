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
        android.util.Log.d("VoiceRecognition", "🎤 startListening() called")
        
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            android.util.Log.e("VoiceRecognition", "❌ Speech recognition NOT available")
            _error.value = "Speech recognition không khả dụng trên thiết bị này"
            return
        }
        
        android.util.Log.d("VoiceRecognition", "✅ Speech recognition is available")
        cleanup() // Clean up any existing recognition before starting new one
        _error.value = null // Clear previous errors
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    android.util.Log.d("VoiceRecognition", "✅ onReadyForSpeech")
                    _isListening.value = true
                    _error.value = null
                }
                
                override fun onBeginningOfSpeech() {
                    android.util.Log.d("VoiceRecognition", "🗣️ onBeginningOfSpeech - User started speaking")
                }
                
                override fun onRmsChanged(rmsdB: Float) {
                    // Volume changed (too noisy to log)
                }
                
                override fun onBufferReceived(buffer: ByteArray?) {
                    android.util.Log.d("VoiceRecognition", "📦 onBufferReceived")
                }
                
                override fun onEndOfSpeech() {
                    android.util.Log.d("VoiceRecognition", "🛑 onEndOfSpeech")
                    _isListening.value = false
                }
                
                override fun onError(error: Int) {
                    val errorMsg = when (error) {
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
                    android.util.Log.e("VoiceRecognition", "❌ onError: $error - $errorMsg")
                    _isListening.value = false
                    _error.value = errorMsg
                    cleanup() // Clean up after error
                }
                
                override fun onResults(results: Bundle?) {
                    android.util.Log.d("VoiceRecognition", "✅ onResults called")
                    _isListening.value = false
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { matches ->
                        android.util.Log.d("VoiceRecognition", "📝 Recognized ${matches.size} matches")
                        if (matches.isNotEmpty()) {
                            val text = matches[0]
                            android.util.Log.d("VoiceRecognition", "🎯 Recognized text: '$text'")
                            _recognizedText.value = text
                        }
                    } ?: run {
                        android.util.Log.w("VoiceRecognition", "⚠️ onResults but results bundle is null")
                    }
                    cleanup() // Clean up after getting results
                }
                
                override fun onPartialResults(partialResults: Bundle?) {
                    partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { matches ->
                        if (matches.isNotEmpty()) {
                            android.util.Log.d("VoiceRecognition", "🔄 Partial: ${matches[0]}")
                        }
                    }
                }
                
                override fun onEvent(eventType: Int, params: Bundle?) {
                    android.util.Log.d("VoiceRecognition", "📢 onEvent: $eventType")
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
            android.util.Log.d("VoiceRecognition", "🚀 Starting speech recognizer with language: vi-VN")
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            android.util.Log.e("VoiceRecognition", "❌ Exception starting listener: ${e.message}", e)
            _error.value = "Không thể khởi động speech recognition: ${e.message}"
            _isListening.value = false
        }
    }
    
    fun stopListening() {
        android.util.Log.d("VoiceRecognition", "⏹️ stopListening() called - will wait for results")
        try {
            // IMPORTANT: Only call stopListening(), NOT destroy()!
            // This allows onResults() to be called before cleanup
            speechRecognizer?.stopListening()
            
            // Delay destroy to give time for onResults() callback
            // Note: onResults() or onError() will handle cleanup
            android.util.Log.d("VoiceRecognition", "⏹️ Waiting for onResults() or onError()...")
        } catch (e: Exception) {
            android.util.Log.w("VoiceRecognition", "⚠️ Error stopping: ${e.message}")
            cleanup()
        }
        _isListening.value = false
    }
    
    private fun cleanup() {
        android.util.Log.d("VoiceRecognition", "🧹 cleanup() - destroying recognizer")
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            android.util.Log.w("VoiceRecognition", "⚠️ Error during cleanup: ${e.message}")
        }
    }
    
    fun clearText() {
        android.util.Log.d("VoiceRecognition", "🧹 clearText()")
        _recognizedText.value = ""
    }
    
    fun clearError() {
        android.util.Log.d("VoiceRecognition", "🧹 clearError()")
        _error.value = null
    }
}

