package com.example.expensetracker.data.service

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.core.os.ConfigurationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service để quản lý ngôn ngữ ứng dụng
 * Hỗ trợ tiếng Việt và tiếng Anh với khả năng chuyển đổi động
 */
@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREF_NAME = "language_prefs"
        private const val KEY_SELECTED_LANGUAGE = "selected_language"
        private const val LANGUAGE_VIETNAMESE = "vi"
        private const val LANGUAGE_ENGLISH = "en"
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    // Các ngôn ngữ hỗ trợ
    data class SupportedLanguage(
        val code: String,
        val displayName: String,
        val nativeName: String
    )
    
    val supportedLanguages = listOf(
        SupportedLanguage(LANGUAGE_VIETNAMESE, "Vietnamese", "Tiếng Việt"),
        SupportedLanguage(LANGUAGE_ENGLISH, "English", "English")
    )
    
    // State cho ngôn ngữ hiện tại
    private val _currentLanguage = MutableStateFlow(getCurrentLanguage())
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()
    
    private val _isLanguageChanged = MutableStateFlow(false)
    val isLanguageChanged: StateFlow<Boolean> = _isLanguageChanged.asStateFlow()
    
    /**
     * Lấy ngôn ngữ hiện tại từ SharedPreferences
     */
    fun getCurrentLanguage(): String {
        return sharedPreferences.getString(KEY_SELECTED_LANGUAGE, getSystemLanguage()) 
            ?: getSystemLanguage()
    }
    
    /**
     * Lấy ngôn ngữ hệ thống
     */
    private fun getSystemLanguage(): String {
        val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ConfigurationCompat.getLocales(context.resources.configuration).get(0)
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
        
        return when (systemLocale?.language) {
            LANGUAGE_VIETNAMESE -> LANGUAGE_VIETNAMESE
            LANGUAGE_ENGLISH -> LANGUAGE_ENGLISH
            else -> LANGUAGE_VIETNAMESE // Mặc định là tiếng Việt
        }
    }
    
    /**
     * Thiết lập ngôn ngữ mới
     */
    fun setLanguage(languageCode: String) {
        if (languageCode == getCurrentLanguage()) return
        
        sharedPreferences.edit()
            .putString(KEY_SELECTED_LANGUAGE, languageCode)
            .apply()
        
        _currentLanguage.value = languageCode
        _isLanguageChanged.value = true
        
        // Áp dụng ngôn ngữ mới cho context
        updateContextLocale(context, languageCode)
    }
    
    /**
     * Cập nhật locale cho context
     */
    fun updateContextLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }
    
    /**
     * Lấy tên hiển thị của ngôn ngữ hiện tại
     */
    fun getCurrentLanguageDisplayName(): String {
        val currentLang = getCurrentLanguage()
        return supportedLanguages.find { it.code == currentLang }?.displayName 
            ?: "Tiếng Việt"
    }
    
    /**
     * Lấy tên bản địa của ngôn ngữ hiện tại
     */
    fun getCurrentLanguageNativeName(): String {
        val currentLang = getCurrentLanguage()
        return supportedLanguages.find { it.code == currentLang }?.nativeName 
            ?: "Tiếng Việt"
    }
    
    /**
     * Kiểm tra xem có phải ngôn ngữ tiếng Việt không
     */
    fun isVietnamese(): Boolean {
        return getCurrentLanguage() == LANGUAGE_VIETNAMESE
    }
    
    /**
     * Kiểm tra xem có phải ngôn ngữ tiếng Anh không
     */
    fun isEnglish(): Boolean {
        return getCurrentLanguage() == LANGUAGE_ENGLISH
    }
    
    /**
     * Chuyển đổi ngôn ngữ (toggle giữa tiếng Việt và tiếng Anh)
     */
    fun toggleLanguage() {
        val newLanguage = if (isVietnamese()) {
            LANGUAGE_ENGLISH
        } else {
            LANGUAGE_VIETNAMESE
        }
        setLanguage(newLanguage)
    }
    
    /**
     * Reset trạng thái thay đổi ngôn ngữ
     */
    fun resetLanguageChangeState() {
        _isLanguageChanged.value = false
    }
    
    /**
     * Lấy Configuration với locale được thiết lập
     */
    fun getLocalizedConfiguration(): Configuration {
        val configuration = Configuration(context.resources.configuration)
        val locale = Locale(getCurrentLanguage())
        configuration.setLocale(locale)
        return configuration
    }
    
    /**
     * Format số theo locale hiện tại
     */
    fun formatNumber(number: Long): String {
        val locale = when (getCurrentLanguage()) {
            LANGUAGE_ENGLISH -> Locale.US
            else -> Locale("vi", "VN")
        }
        return java.text.NumberFormat.getNumberInstance(locale).format(number)
    }
    
    /**
     * Format tiền tệ theo locale hiện tại
     */
    fun formatCurrency(amount: Long): String {
        return when (getCurrentLanguage()) {
            LANGUAGE_ENGLISH -> {
                val formatter = java.text.NumberFormat.getCurrencyInstance(Locale.US)
                formatter.format(amount / 23000.0) // Tỷ giá tham khảo VND -> USD
            }
            else -> {
                val formatter = java.text.NumberFormat.getNumberInstance(Locale("vi", "VN"))
                "${formatter.format(amount)}đ"
            }
        }
    }
    
    /**
     * Lấy ký hiệu tiền tệ
     */
    fun getCurrencySymbol(): String {
        return when (getCurrentLanguage()) {
            LANGUAGE_ENGLISH -> "$"
            else -> "đ"
        }
    }
}

