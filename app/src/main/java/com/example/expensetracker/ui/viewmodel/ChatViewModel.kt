package com.example.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.entity.ChatMessageEntity
import com.example.expensetracker.data.entity.Expense
import com.example.expensetracker.data.repository.ChatRepository
import com.example.expensetracker.data.repository.FirebaseRepository
import com.example.expensetracker.data.service.VoiceRecognitionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val firebaseRepository: FirebaseRepository,
    val voiceRecognitionManager: VoiceRecognitionManager,
    val receiptScannerManager: com.example.expensetracker.data.service.ReceiptScannerManager
) : ViewModel() {
    
    private val _chatMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessageEntity>> = _chatMessages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    private val _currentInput = MutableStateFlow("")
    val currentInput: StateFlow<String> = _currentInput.asStateFlow()
    
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
    
    init {
        loadChatHistory()
    }
    
    private fun loadChatHistory() {
        viewModelScope.launch {
            chatRepository.getChatMessages().collect { messages ->
                _chatMessages.value = messages
            }
        }
    }
    
    fun updateInput(input: String) {
        _currentInput.value = input
    }
    
    fun sendMessage(message: String? = null) {
        val messageToSend = message ?: _currentInput.value
        if (messageToSend.isBlank()) return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val result = chatRepository.sendMessage(messageToSend)
                
                if (result.isSuccess) {
                    _currentInput.value = ""
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Có lỗi xảy ra"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Có lỗi xảy ra"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearChatHistory() {
        viewModelScope.launch {
            chatRepository.clearChatHistory()
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteMessage(messageId)
        }
    }
    
    /**
     * Process voice command and perform CRUD operations
     */
    fun processVoiceCommand(voiceText: String) {
        if (voiceText.isBlank()) return
        
        android.util.Log.d("ChatViewModel", "🎤 Processing command: $voiceText")
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                _successMessage.value = null
                
                // Create prompt for Gemini AI to parse voice command
                val parsePrompt = """
                    Phân tích câu lệnh giọng nói sau và trả về JSON với format:
                    {
                      "action": "create" | "read" | "update" | "delete" | "bulk_update" | "bulk_delete",
                      "amount": số tiền mới (number, optional cho update),
                      "category": "Ăn uống" | "Giao thông" | "Mua sắm" | "Giải trí" | "Khác" (optional),
                      "title": "tên mới" (optional cho update),
                      "note": "ghi chú mới" (optional),
                      "isExpense": true (chi tiêu) | false (thu nhập),
                      "targetKeyword": "từ khóa tìm expense",
                      "bulkMode": "all" | "category" | "keyword" (cho bulk operations),
                      "updateFields": ["amount", "title", "category", "note"] (fields cần update),
                      "dateFilter": "today" | "yesterday" | "specific" | "week" | "month" | "none",
                      "dateValue": "DD/MM" hoặc "MM" (cho specific date/month)
                    }
                    
                    Câu lệnh: "$voiceText"
                    
                    EXAMPLES:
                    
                    CREATE:
                    - "thêm chi tiêu 50k cà phê" => {"action":"create", "amount":50000, "title":"cà phê", "category":"Ăn uống"}
                    
                    UPDATE 1 FIELD:
                    - "sửa bida thành 100k" => {"action":"update", "targetKeyword":"bida", "amount":100000, "updateFields":["amount"]}
                    
                    UPDATE NHIỀU FIELD:
                    - "cập nhật test: đổi tên test123, category Giải trí, 500k" => {"action":"update", "targetKeyword":"test", "title":"test123", "category":"Giải trí", "amount":500000, "updateFields":["title","category","amount"]}
                    - "sửa karaoke thành 200k và đổi category thành Giải trí" => {"action":"update", "targetKeyword":"karaoke", "amount":200000, "category":"Giải trí", "updateFields":["amount","category"]}
                    
                    DELETE 1 EXPENSE:
                    - "xóa bida" => {"action":"delete", "targetKeyword":"bida"}
                    
                    BULK DELETE:
                    - "xóa toàn bộ chi tiêu ăn uống" => {"action":"bulk_delete", "bulkMode":"category", "category":"Ăn uống"}
                    - "xóa tất cả karaoke" => {"action":"bulk_delete", "bulkMode":"keyword", "targetKeyword":"karaoke"}
                    - "xóa hết chi tiêu giải trí" => {"action":"bulk_delete", "bulkMode":"category", "category":"Giải trí"}
                    
                    BULK UPDATE:
                    - "cập nhật tất cả karaoke thành 200k" => {"action":"bulk_update", "bulkMode":"keyword", "targetKeyword":"karaoke", "amount":200000, "updateFields":["amount"]}
                    - "sửa hết ăn uống thành category Khác" => {"action":"bulk_update", "bulkMode":"category", "category":"Ăn uống", "updateFields":["category"]}
                    
                    DATE FILTERS:
                    - "xóa chi tiêu hôm nay" => {"action":"bulk_delete", "bulkMode":"all", "dateFilter":"today"}
                    - "sửa chi tiêu hôm qua thành 100k" => {"action":"bulk_update", "bulkMode":"all", "amount":100000, "dateFilter":"yesterday", "updateFields":["amount"]}
                    - "xóa chi tiêu ngày 25/10" => {"action":"bulk_delete", "bulkMode":"all", "dateFilter":"specific", "dateValue":"25/10"}
                    - "xóa chi tiêu tuần này" => {"action":"bulk_delete", "bulkMode":"all", "dateFilter":"week"}
                    - "xóa chi tiêu tháng 10" => {"action":"bulk_delete", "bulkMode":"all", "dateFilter":"month", "dateValue":"10"}
                    - "xóa chi tiêu ăn uống hôm nay" => {"action":"bulk_delete", "bulkMode":"category", "category":"Ăn uống", "dateFilter":"today"}
                    
                    RULES:
                    - Số tiền: 50k=50000, 2 triệu=2000000
                    - bulkMode: "category" (theo category), "keyword" (theo từ khóa), "all" (tất cả)
                    - updateFields: list các field cần update
                    - dateFilter: "today" (hôm nay), "yesterday" (hôm qua), "specific" (ngày cụ thể), "week" (tuần này), "month" (tháng), "none" (không filter)
                    - dateValue: chỉ có khi dateFilter = "specific" hoặc "month"
                    
                    CHỈ trả về JSON, không thêm text khác.
                """.trimIndent()
                
                // Send to Gemini AI to parse
                val parseResult = chatRepository.sendMessage(parsePrompt)
                
                if (parseResult.isFailure) {
                    _errorMessage.value = "Không thể phân tích lệnh: ${parseResult.exceptionOrNull()?.message}"
                    return@launch
                }
                
                // Wait a bit for AI response to be saved to database
                delay(1000)
                
                // Get last AI message (not user message)
                val messages = _chatMessages.value
                val aiMessage = messages.lastOrNull { !it.isUser }
                
                if (aiMessage == null) {
                    android.util.Log.e("ChatViewModel", "❌ No AI response found")
                    _errorMessage.value = "Không nhận được phản hồi từ AI"
                    return@launch
                }
                
                val aiResponse = aiMessage.content
                android.util.Log.d("ChatViewModel", "🤖 AI Response: $aiResponse")
                
                // Parse JSON response
                val actionResult = parseAndExecuteAction(aiResponse, voiceText)
                
                if (actionResult != null) {
                    // Success - show confirmation
                    val confirmationMessage = when {
                        actionResult.startsWith("Đã thêm") -> "✅ $actionResult"
                        actionResult.startsWith("Đã xóa") -> "🗑️ $actionResult"
                        actionResult.startsWith("Đã cập nhật") -> "✏️ $actionResult"
                        else -> actionResult
                    }
                    
                    android.util.Log.d("ChatViewModel", "✅ Success: $confirmationMessage")
                    _successMessage.value = confirmationMessage
                } else {
                    android.util.Log.w("ChatViewModel", "⚠️ No action result")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "❌ Error: ${e.message}", e)
                _errorMessage.value = "Lỗi xử lý lệnh: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Parse AI response and execute action
     */
    private suspend fun parseAndExecuteAction(aiResponse: String, originalCommand: String): String? {
        return try {
            // Remove markdown code fence and normalize whitespace
            val cleanResponse = aiResponse
                .replace("```json", "")
                .replace("```", "")
                .replace("\n", " ")  // Replace newlines with space
                .replace(Regex("\\s+"), " ")  // Replace multiple spaces with single space
                .trim()
            
            android.util.Log.d("ChatViewModel", "📋 Clean Response: $cleanResponse")
            
            // Simple JSON parsing (you might want to use Gson/Moshi for production)
            val action = extractJsonValue(cleanResponse, "action")
            val amountStr = extractJsonValue(cleanResponse, "amount")
            val category = extractJsonValue(cleanResponse, "category") ?: "Khác"
            val title = extractJsonValue(cleanResponse, "title") ?: originalCommand
            val note = extractJsonValue(cleanResponse, "note") ?: ""
            val isExpense = extractJsonValue(cleanResponse, "isExpense")?.toBoolean() ?: true
            
            when (action) {
                "create" -> {
                    val amount = amountStr?.toLongOrNull() ?: 0L
                    if (amount == 0L) {
                        _errorMessage.value = "Không xác định được số tiền. Vui lòng nói rõ hơn."
                        return null
                    }
                    
                    val expense = Expense(
                        title = title,
                        amount = amount,
                        category = category,
                        date = Date(),
                        note = note,
                        isExpense = isExpense,
                        createdAt = Date(),
                        updatedAt = Date()
                    )
                    
                    val result = firebaseRepository.addExpense(expense)
                    if (result.isSuccess) {
                        val type = if (isExpense) "chi tiêu" else "thu nhập"
                        "Đã thêm $type: $title - ${formatMoney(amount)} ($category)"
                    } else {
                        _errorMessage.value = "Không thể thêm chi tiêu: ${result.exceptionOrNull()?.message}"
                        null
                    }
                }
                
                "read" -> {
                    "Để xem chi tiêu, vui lòng vào tab Chi tiêu hoặc Báo cáo."
                }
                
                "update" -> {
                    val targetKeyword = extractJsonValue(cleanResponse, "targetKeyword")
                    if (targetKeyword.isNullOrBlank()) {
                        _errorMessage.value = "Không rõ cần sửa khoản nào. VD: 'Sửa chi tiêu bida thành 100k'"
                        return null
                    }
                    
                    // Tìm expense matching với keyword
                    val expensesResult = firebaseRepository.getExpenses()
                    if (expensesResult.isFailure) {
                        _errorMessage.value = "Không thể lấy danh sách chi tiêu: ${expensesResult.exceptionOrNull()?.message}"
                        return null
                    }
                    
                    val expenses = expensesResult.getOrNull() ?: emptyList()
                    val matchingExpense = expenses.firstOrNull { 
                        it.title.contains(targetKeyword, ignoreCase = true)
                    }
                    
                    if (matchingExpense == null) {
                        _errorMessage.value = "Không tìm thấy khoản '$targetKeyword' để sửa."
                        return null
                    }
                    
                    // Build updated expense with only changed fields
                    var updatedExpense: Expense = matchingExpense
                    val changes = mutableListOf<String>()
                    
                    // Check what fields need to be updated
                    amountStr?.toLongOrNull()?.let { newAmount ->
                        updatedExpense = updatedExpense.copy(amount = newAmount)
                        changes.add("số tiền → ${formatMoney(newAmount)}")
                    }
                    
                    if (!title.isNullOrBlank() && title != originalCommand && title != targetKeyword) {
                        updatedExpense = updatedExpense.copy(title = title)
                        changes.add("tên → $title")
                    }
                    
                    if (!category.isNullOrBlank() && category != "Khác") {
                        updatedExpense = updatedExpense.copy(category = category)
                        changes.add("danh mục → $category")
                    }
                    
                    if (!note.isNullOrBlank()) {
                        updatedExpense = updatedExpense.copy(note = note)
                        changes.add("ghi chú")
                    }
                    
                    if (changes.isEmpty()) {
                        _errorMessage.value = "Không có gì để cập nhật"
                        return null
                    }
                    
                    val result = firebaseRepository.updateExpense(updatedExpense)
                    if (result.isSuccess) {
                        "Đã sửa '$targetKeyword': ${changes.joinToString(", ")}"
                    } else {
                        _errorMessage.value = "Không thể sửa chi tiêu: ${result.exceptionOrNull()?.message}"
                        null
                    }
                }
                
                "delete" -> {
                    val targetKeyword = extractJsonValue(cleanResponse, "targetKeyword")
                    if (targetKeyword.isNullOrBlank()) {
                        _errorMessage.value = "Không rõ cần xóa khoản nào. VD: 'Xóa chi tiêu bida'"
                        return null
                    }
                    
                    // Tìm expense matching với keyword
                    val expensesResult = firebaseRepository.getExpenses()
                    if (expensesResult.isFailure) {
                        _errorMessage.value = "Không thể lấy danh sách chi tiêu: ${expensesResult.exceptionOrNull()?.message}"
                        return null
                    }
                    
                    val expenses = expensesResult.getOrNull() ?: emptyList()
                    val matchingExpense = expenses.firstOrNull { 
                        it.title.contains(targetKeyword, ignoreCase = true)
                    }
                    
                    if (matchingExpense == null) {
                        _errorMessage.value = "Không tìm thấy khoản '$targetKeyword' để xóa."
                        return null
                    }
                    
                    val result = firebaseRepository.deleteExpense(matchingExpense.id)
                    if (result.isSuccess) {
                        "Đã xóa chi tiêu: ${matchingExpense.title} - ${formatMoney(matchingExpense.amount)}"
                    } else {
                        _errorMessage.value = "Không thể xóa chi tiêu: ${result.exceptionOrNull()?.message}"
                        null
                    }
                }
                
                "bulk_update" -> {
                    val bulkMode = extractJsonValue(cleanResponse, "bulkMode")
                    val dateFilter = extractJsonValue(cleanResponse, "dateFilter")
                    val dateValue = extractJsonValue(cleanResponse, "dateValue")
                    
                    // Lấy danh sách expenses
                    val expensesResult = firebaseRepository.getExpenses()
                    if (expensesResult.isFailure) {
                        _errorMessage.value = "Không thể lấy danh sách chi tiêu"
                        return null
                    }
                    
                    val allExpenses = expensesResult.getOrNull() ?: emptyList()
                    
                    // Filter expenses theo bulkMode
                    var targetExpenses = when (bulkMode) {
                        "all" -> {
                            // Cập nhật TẤT CẢ expenses
                            allExpenses
                        }
                        "category" -> {
                            val targetCategory = category
                            allExpenses.filter { it.category.equals(targetCategory, ignoreCase = true) }
                        }
                        "keyword" -> {
                            val targetKeyword = extractJsonValue(cleanResponse, "targetKeyword")
                            if (targetKeyword.isNullOrBlank()) {
                                _errorMessage.value = "Không rõ từ khóa để tìm"
                                return null
                            }
                            allExpenses.filter { it.title.contains(targetKeyword, ignoreCase = true) }
                        }
                        else -> {
                            _errorMessage.value = "Không hỗ trợ bulkMode: $bulkMode"
                            return null
                        }
                    }
                    
                    // Apply date filter
                    targetExpenses = filterExpensesByDate(targetExpenses, dateFilter, dateValue)
                    
                    if (targetExpenses.isEmpty()) {
                        _errorMessage.value = "Không tìm thấy khoản nào để cập nhật"
                        return null
                    }
                    
                    // Update từng expense
                    var successCount = 0
                    targetExpenses.forEach { expense ->
                        var updated = expense
                        
                        amountStr?.toLongOrNull()?.let { updated = updated.copy(amount = it) }
                        if (!title.isNullOrBlank() && title != originalCommand) {
                            updated = updated.copy(title = title)
                        }
                        if (!category.isNullOrBlank() && category != "Khác" && bulkMode != "category") {
                            updated = updated.copy(category = category)
                        }
                        if (!note.isNullOrBlank()) {
                            updated = updated.copy(note = note)
                        }
                        
                        if (firebaseRepository.updateExpense(updated).isSuccess) {
                            successCount++
                        }
                    }
                    
                    if (successCount > 0) {
                        "Đã cập nhật $successCount khoản chi tiêu"
                    } else {
                        _errorMessage.value = "Không thể cập nhật"
                        null
                    }
                }
                
                "bulk_delete" -> {
                    val bulkMode = extractJsonValue(cleanResponse, "bulkMode")
                    val dateFilter = extractJsonValue(cleanResponse, "dateFilter")
                    val dateValue = extractJsonValue(cleanResponse, "dateValue")
                    
                    // Lấy danh sách expenses
                    val expensesResult = firebaseRepository.getExpenses()
                    if (expensesResult.isFailure) {
                        _errorMessage.value = "Không thể lấy danh sách chi tiêu"
                        return null
                    }
                    
                    val allExpenses = expensesResult.getOrNull() ?: emptyList()
                    
                    // Filter expenses theo bulkMode
                    var targetExpenses = when (bulkMode) {
                        "all" -> {
                            // Xóa TẤT CẢ expenses
                            allExpenses
                        }
                        "category" -> {
                            val targetCategory = category
                            allExpenses.filter { it.category.equals(targetCategory, ignoreCase = true) }
                        }
                        "keyword" -> {
                            val targetKeyword = extractJsonValue(cleanResponse, "targetKeyword")
                            if (targetKeyword.isNullOrBlank()) {
                                _errorMessage.value = "Không rõ từ khóa để tìm"
                                return null
                            }
                            allExpenses.filter { it.title.contains(targetKeyword, ignoreCase = true) }
                        }
                        else -> {
                            _errorMessage.value = "Không hỗ trợ bulkMode: $bulkMode"
                            return null
                        }
                    }
                    
                    // Apply date filter
                    targetExpenses = filterExpensesByDate(targetExpenses, dateFilter, dateValue)
                    
                    if (targetExpenses.isEmpty()) {
                        _errorMessage.value = "Không tìm thấy khoản nào để xóa"
                        return null
                    }
                    
                    // Xóa từng expense
                    var successCount = 0
                    targetExpenses.forEach { expense ->
                        if (firebaseRepository.deleteExpense(expense.id).isSuccess) {
                            successCount++
                        }
                    }
                    
                    if (successCount > 0) {
                        "Đã xóa $successCount khoản chi tiêu"
                    } else {
                        _errorMessage.value = "Không thể xóa"
                        null
                    }
                }
                
                else -> {
                    _errorMessage.value = "Không hiểu lệnh. Hãy thử: 'Thêm chi tiêu 50k cà phê' hoặc 'Thêm thu nhập 5 triệu lương'"
                    null
                }
            }
        } catch (e: Exception) {
            _errorMessage.value = "Lỗi phân tích: ${e.message}"
            null
        }
    }
    
    /**
     * Extract value from JSON string (simple parser)
     */
    private fun extractJsonValue(json: String, key: String): String? {
        return try {
            // Try string pattern
            val pattern = """"$key"\s*:\s*"([^"]*)"""".toRegex()
            val match = pattern.find(json)
            val result = match?.groupValues?.get(1)
            if (result != null) {
                android.util.Log.d("ChatViewModel", "🔍 Extracted '$key': '$result' (string)")
                return result
            }
            
            // Try number pattern
            val numberPattern = """"$key"\s*:\s*(\d+)""".toRegex()
            val numberMatch = numberPattern.find(json)
            val numberResult = numberMatch?.groupValues?.get(1)
            if (numberResult != null) {
                android.util.Log.d("ChatViewModel", "🔍 Extracted '$key': '$numberResult' (number)")
                return numberResult
            }
            
            // Try boolean pattern
            val boolPattern = """"$key"\s*:\s*(true|false)""".toRegex()
            val boolMatch = boolPattern.find(json)
            val boolResult = boolMatch?.groupValues?.get(1)
            if (boolResult != null) {
                android.util.Log.d("ChatViewModel", "🔍 Extracted '$key': '$boolResult' (boolean)")
                return boolResult
            }
            
            android.util.Log.w("ChatViewModel", "⚠️ Could not extract '$key' from JSON")
            null
        } catch (e: Exception) {
            android.util.Log.e("ChatViewModel", "❌ Error extracting '$key': ${e.message}")
            null
        }
    }
    
    /**
     * Filter expenses by date based on dateFilter and dateValue
     */
    private fun filterExpensesByDate(
        expenses: List<Expense>,
        dateFilter: String?,
        dateValue: String?
    ): List<Expense> {
        if (dateFilter == null || dateFilter == "none") {
            return expenses
        }
        
        val calendar = java.util.Calendar.getInstance()
        val today = calendar.time
        
        return when (dateFilter) {
            "today" -> {
                // Filter expenses from today
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfDay = calendar.time
                
                expenses.filter { it.date >= startOfDay }
            }
            
            "yesterday" -> {
                // Filter expenses from yesterday
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfYesterday = calendar.time
                
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                val startOfToday = calendar.time
                
                expenses.filter { it.date >= startOfYesterday && it.date < startOfToday }
            }
            
            "week" -> {
                // Filter expenses from this week (Monday - Sunday)
                calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfWeek = calendar.time
                
                expenses.filter { it.date >= startOfWeek }
            }
            
            "month" -> {
                // Filter expenses from specified month or current month
                val monthValue = dateValue?.toIntOrNull() ?: (calendar.get(java.util.Calendar.MONTH) + 1)
                val currentYear = calendar.get(java.util.Calendar.YEAR)
                
                calendar.set(currentYear, monthValue - 1, 1, 0, 0, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.time
                
                calendar.add(java.util.Calendar.MONTH, 1)
                val startOfNextMonth = calendar.time
                
                expenses.filter { it.date >= startOfMonth && it.date < startOfNextMonth }
            }
            
            "specific" -> {
                // Filter expenses from specific date (DD/MM)
                if (dateValue == null) return expenses
                
                val parts = dateValue.split("/")
                if (parts.size != 2) return expenses
                
                val day = parts[0].toIntOrNull() ?: return expenses
                val month = parts[1].toIntOrNull() ?: return expenses
                val currentYear = calendar.get(java.util.Calendar.YEAR)
                
                calendar.set(currentYear, month - 1, day, 0, 0, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfDay = calendar.time
                
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                val startOfNextDay = calendar.time
                
                expenses.filter { it.date >= startOfDay && it.date < startOfNextDay }
            }
            
            else -> expenses
        }
    }
    
    /**
     * Format money with Vietnamese format
     */
    private fun formatMoney(amount: Long): String {
        return when {
            amount >= 1_000_000 -> "${amount / 1_000_000} triệu đồng"
            amount >= 1_000 -> "${amount / 1_000}k đồng"
            else -> "$amount đồng"
        }
    }
    
    /**
     * Process scanned receipt data from Gemini Vision API
     */
    fun processReceiptData(jsonResponse: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ChatViewModel", "📊 Processing receipt data: $jsonResponse")
                
                _isLoading.value = true
                
                // Clean response (remove markdown code fences if present)
                val cleanResponse = jsonResponse
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()
                
                android.util.Log.d("ChatViewModel", "🧹 Clean response: $cleanResponse")
                
                // Extract values from JSON
                val amountStr = extractJsonValue(cleanResponse, "amount")
                val title = extractJsonValue(cleanResponse, "title")
                val category = extractJsonValue(cleanResponse, "category")
                val dateStr = extractJsonValue(cleanResponse, "date")
                val note = extractJsonValue(cleanResponse, "note")
                val merchant = extractJsonValue(cleanResponse, "merchant")
                
                android.util.Log.d("ChatViewModel", "📝 Extracted - amount: $amountStr, title: $title, category: $category")
                
                // Parse amount
                val amount = try {
                    amountStr?.replace("[^0-9.]".toRegex(), "")?.toDoubleOrNull() ?: 0.0
                } catch (e: Exception) {
                    android.util.Log.e("ChatViewModel", "Error parsing amount: ${e.message}")
                    0.0
                }
                
                // Parse date
                val date = try {
                    if (!dateStr.isNullOrBlank() && dateStr != "null") {
                        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        formatter.parse(dateStr) ?: Date()
                    } else {
                        Date()
                    }
                } catch (e: Exception) {
                    android.util.Log.w("ChatViewModel", "Using current date: ${e.message}")
                    Date()
                }
                
                // Create expense note
                val fullNote = buildString {
                    if (!merchant.isNullOrBlank() && merchant != "null") {
                        append("Cửa hàng: $merchant\n")
                    }
                    if (!note.isNullOrBlank() && note != "null") {
                        append(note)
                    }
                    if (isEmpty()) {
                        append("Scan từ hóa đơn")
                    }
                }
                
                // Create expense
                val expense = Expense(
                    id = "",
                    title = if (!title.isNullOrBlank() && title != "null") title else "Chi tiêu từ hóa đơn",
                    amount = amount.toLong(),
                    category = if (!category.isNullOrBlank() && category != "null") category else "Khác",
                    date = date,
                    note = fullNote,
                    isExpense = true
                )
                
                android.util.Log.d("ChatViewModel", "💰 Creating expense: $expense")
                
                // Save to Firebase
                firebaseRepository.addExpense(expense)
                
                _successMessage.value = "✅ Đã thêm chi tiêu ${formatMoney(amount.toLong())} từ hóa đơn!"
                _isLoading.value = false
                
                android.util.Log.d("ChatViewModel", "✅ Receipt processed successfully")
                
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "❌ Error processing receipt: ${e.message}", e)
                _errorMessage.value = "Lỗi xử lý hóa đơn: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}
