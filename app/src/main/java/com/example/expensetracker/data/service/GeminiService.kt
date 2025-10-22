package com.example.expensetracker.data.service

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor() {
    
    private val apiKey = "AIzaSyD3bJtJFmTPPr3Grh62VMN3CMFSqKFzYXU"
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 1024
        }
    )
    
    suspend fun generateResponse(
        userMessage: String,
        chatHistory: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Tạo context cho AI về ứng dụng quản lý chi tiêu
            val systemPrompt = """
                Bạn là Nono trợ lý AI chuyên về quản lý tài chính cá nhân cho ứng dụng "NoNo Expense Tracker".

                THÔNG TIN VỀ ỨNG DỤNG:
                - Đây là ứng dụng quản lý chi tiêu cá nhân trên Android
                - Người dùng có thể thêm, sửa, xóa chi tiêu và thu nhập
                - Có các danh mục: Ăn uống, Đi lại, Mua sắm, Y tế, Giải trí, Khác
                - Có tính năng báo cáo, biểu đồ, và phân tích xu hướng
                - Có OCR để scan hóa đơn và tự động nhập chi tiêu

                NHIỆM VỤ CỦA BẠN:
                1. Hướng dẫn sử dụng ứng dụng một cách chi tiết
                2. Đưa ra lời khuyên tài chính thông minh và thực tế
                3. Phân tích xu hướng chi tiêu và đưa ra insights
                4. Gợi ý cách tiết kiệm và quản lý ngân sách hiệu quả
                5. Trả lời các câu hỏi về tài chính cá nhân
                6. Hỗ trợ người dùng hiểu về các tính năng của app

                PHONG CÁCH TRẢ LỜI:
                - Luôn trả lời bằng tiếng Việt
                - Thân thiện, nhiệt tình và dễ hiểu
                - Đưa ra ví dụ cụ thể khi có thể
                - Sử dụng emoji phù hợp để tạo cảm giác gần gũi
                - Trả lời ngắn gọn nhưng đầy đủ thông tin
                - Khuyến khích người dùng sử dụng các tính năng của app

                CÁC CHỦ ĐỀ BẠN CÓ THỂ GIÚP:
                - Cách thêm chi tiêu/thu nhập vào app
                - Phân tích chi tiêu theo danh mục
                - Lập kế hoạch ngân sách hàng tháng
                - Mẹo tiết kiệm tiền hiệu quả
                - Hiểu về các báo cáo tài chính
                - Sử dụng tính năng OCR để scan hóa đơn
                - Cách đọc và phân tích biểu đồ chi tiêu

                Hãy trả lời như một chuyên gia tài chính cá nhân thân thiện!
            """.trimIndent()
            
            // Xây dựng prompt với lịch sử chat
            val promptBuilder = StringBuilder(systemPrompt)
            promptBuilder.append("\n\nLịch sử cuộc trò chuyện:")
            
            chatHistory.forEach { (userMsg, aiMsg) ->
                promptBuilder.append("\nNgười dùng: $userMsg")
                promptBuilder.append("\nAI: $aiMsg")
            }
            
            promptBuilder.append("\n\nNgười dùng: $userMessage")
            promptBuilder.append("\nAI:")
            
            val response = generativeModel.generateContent(promptBuilder.toString())
            val responseText = response.text ?: "Xin lỗi, tôi không thể tạo phản hồi lúc này."
            
            Result.success(responseText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
