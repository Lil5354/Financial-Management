package com.example.expensetracker.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptScannerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing
    
    private val _scannedData = MutableStateFlow<String?>(null)
    val scannedData: StateFlow<String?> = _scannedData
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _capturedImageUri = MutableStateFlow<Uri?>(null)
    val capturedImageUri: StateFlow<Uri?> = _capturedImageUri
    
    // Gemini Vision Model
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash-thinking-exp-1219",
        apiKey = "AIzaSyD3bJtJFmTPPr3Grh62VMN3CMFSqKFzYXU",
        generationConfig = com.google.ai.client.generativeai.type.generationConfig {
            temperature = 0.4f  // Lower temperature for more accurate OCR
            topK = 32
            topP = 1f
            maxOutputTokens = 2048
        }
    )
    
    /**
     * Create a temporary file URI for camera capture
     */
    fun createImageFileUri(): Uri {
        Log.d("ReceiptScanner", "📸 Creating image file URI")
        try {
            val imageFile = File(context.externalCacheDir, "receipt_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
            _capturedImageUri.value = uri
            Log.d("ReceiptScanner", "✅ Created URI: $uri")
            return uri
        } catch (e: Exception) {
            Log.e("ReceiptScanner", "❌ Error creating image file URI: ${e.message}", e)
            _error.value = "Không thể tạo file ảnh: ${e.message}"
            throw e
        }
    }
    
    /**
     * Process receipt image using Gemini Vision API
     */
    suspend fun processReceiptImage(imageUri: Uri) {
        Log.d("ReceiptScanner", "🔍 Processing receipt image: $imageUri")
        _isProcessing.value = true
        _error.value = null
        _scannedData.value = null
        
        try {
            // Load bitmap from URI
            val bitmap = loadBitmapFromUri(imageUri)
            if (bitmap == null) {
                _error.value = "Không thể đọc ảnh"
                Log.e("ReceiptScanner", "❌ Failed to load bitmap from URI")
                _isProcessing.value = false
                return
            }
            
            Log.d("ReceiptScanner", "✅ Loaded bitmap: ${bitmap.width}x${bitmap.height}")
            
            // Prompt for Gemini to extract receipt data
            val prompt = """
                Analyze this receipt/invoice image and extract the following information in JSON format:
                
                {
                  "amount": <total amount as number, extract only digits>,
                  "title": "<brief description of the purchase>",
                  "category": "<suggested category: Ăn uống, Mua sắm, Di chuyển, Giải trí, Y tế, Giáo dục, or Khác>",
                  "date": "<date in format DD/MM/YYYY if visible, otherwise use today's date>",
                  "merchant": "<merchant/store name if visible>",
                  "note": "<any additional notes or items>"
                }
                
                Important:
                - Extract ONLY the total/final amount, not individual item prices
                - If multiple currencies, convert to VND or indicate currency
                - Use Vietnamese category names
                - If information is not visible, use reasonable defaults
                - Return ONLY valid JSON, no markdown formatting
            """.trimIndent()
            
            Log.d("ReceiptScanner", "📤 Sending request to Gemini Vision API")
            
            // Send to Gemini Vision API
            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )
            
            val resultText = response.text ?: ""
            Log.d("ReceiptScanner", "📥 Received response from Gemini: $resultText")
            
            _scannedData.value = resultText
            _isProcessing.value = false
            
        } catch (e: Exception) {
            Log.e("ReceiptScanner", "❌ Error processing receipt: ${e.message}", e)
            _error.value = "Lỗi xử lý ảnh: ${e.message}"
            _isProcessing.value = false
        }
    }
    
    /**
     * Load bitmap from URI (handles both file:// and content:// URIs)
     */
    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            // Resize if too large to avoid memory issues
            if (bitmap != null && (bitmap.width > 2048 || bitmap.height > 2048)) {
                val scale = Math.min(2048f / bitmap.width, 2048f / bitmap.height)
                val width = (bitmap.width * scale).toInt()
                val height = (bitmap.height * scale).toInt()
                Bitmap.createScaledBitmap(bitmap, width, height, true)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            Log.e("ReceiptScanner", "Error loading bitmap: ${e.message}", e)
            null
        }
    }
    
    /**
     * Copy image from URI to cache for later use
     */
    fun copyImageToCache(sourceUri: Uri): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val outputFile = File(context.externalCacheDir, "receipt_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(outputFile)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outputFile
            )
            _capturedImageUri.value = uri
            uri
        } catch (e: Exception) {
            Log.e("ReceiptScanner", "Error copying image: ${e.message}", e)
            null
        }
    }
    
    fun clearData() {
        _scannedData.value = null
        _error.value = null
        _capturedImageUri.value = null
    }
    
    fun clearError() {
        _error.value = null
    }
}

