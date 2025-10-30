package com.example.expensetracker.data.service

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.expensetracker.data.entity.Expense
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service để xuất dữ liệu chi tiêu ra file Excel
 * Sử dụng Apache POI để tạo file XLSX với định dạng đẹp
 */
@Singleton
class ExcelExportService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ExcelExportService"
        private const val PROVIDER_AUTHORITY = "com.example.expensetracker.fileprovider"
    }
    
    // Export states
    sealed class ExportState {
        object Idle : ExportState()
        object InProgress : ExportState()
        data class Success(val filePath: String, val fileName: String) : ExportState()
        data class Error(val message: String) : ExportState()
    }

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()
    
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()
    
    /**
     * Xuất danh sách chi tiêu ra file Excel
     */
    suspend fun exportExpensesToExcel(
        expenses: List<Expense>,
        startDate: Date?,
        endDate: Date?,
        includeCharts: Boolean = true
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            _exportState.value = ExportState.InProgress
            _progress.value = 0f
            
            Log.d(TAG, "Starting Excel export with ${expenses.size} expenses")
            
            // Tạo workbook mới
            val workbook = XSSFWorkbook()
            
            // Tạo các sheet
            createExpenseSheet(workbook, expenses)
            _progress.value = 0.4f
            
            createSummarySheet(workbook, expenses, startDate, endDate)
            _progress.value = 0.7f
            
            if (includeCharts) {
                createAnalyticsSheet(workbook, expenses)
            }
            _progress.value = 0.9f
            
            // Lưu file
            val fileName = generateFileName(startDate, endDate)
            val file = saveWorkbookToFile(workbook, fileName)
            
            workbook.close()
            _progress.value = 1f
            
            val filePath = file.absolutePath
            _exportState.value = ExportState.Success(filePath, fileName)
            
            Log.d(TAG, "Excel export completed: $filePath")
            Result.success(filePath)
            
        } catch (e: Exception) {
            Log.e(TAG, "Excel export failed", e)
            val errorMessage = "Lỗi xuất Excel: ${e.message}"
            _exportState.value = ExportState.Error(errorMessage)
            Result.failure(e)
        }
    }
    
    /**
     * Tạo sheet chi tiết giao dịch
     */
    private fun createExpenseSheet(workbook: XSSFWorkbook, expenses: List<Expense>) {
        val sheet = workbook.createSheet("Chi tiết giao dịch")
        
        // Tạo style cho header
        val headerStyle = createHeaderStyle(workbook)
        val dateStyle = createDateStyle(workbook)
        val currencyStyle = createCurrencyStyle(workbook)
        
        // Tạo header row
        val headerRow = sheet.createRow(0)
        val headers = arrayOf(
            "STT", "Ngày", "Tiêu đề", "Loại", "Danh mục", "Số tiền", "Ghi chú"
        )
        
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }
        
        // Thêm dữ liệu
        expenses.forEachIndexed { index, expense ->
            val row = sheet.createRow(index + 1)
            
            // STT
            row.createCell(0).setCellValue((index + 1).toDouble())
            
            // Ngày
            val dateCell = row.createCell(1)
            dateCell.setCellValue(expense.date)
            dateCell.cellStyle = dateStyle
            
            // Tiêu đề
            row.createCell(2).setCellValue(expense.title)
            
            // Loại
            row.createCell(3).setCellValue(
                if (expense.isExpense) "Chi tiêu" else "Thu nhập"
            )
            
            // Danh mục
            row.createCell(4).setCellValue(expense.category)
            
            // Số tiền
            val amountCell = row.createCell(5)
            amountCell.setCellValue(expense.amount.toDouble())
            amountCell.cellStyle = currencyStyle
            
            // Ghi chú
            row.createCell(6).setCellValue(expense.note)
        }
        
        // Set column widths manually (autoSizeColumn doesn't work on Android)
        sheet.setColumnWidth(0, 3000) // STT
        sheet.setColumnWidth(1, 4000) // Ngày
        sheet.setColumnWidth(2, 6000) // Tiêu đề
        sheet.setColumnWidth(3, 4000) // Loại
        sheet.setColumnWidth(4, 5000) // Danh mục
        sheet.setColumnWidth(5, 4000) // Số tiền
        sheet.setColumnWidth(6, 8000) // Ghi chú
    }
    
    /**
     * Tạo sheet tổng kết
     */
    private fun createSummarySheet(
        workbook: XSSFWorkbook,
        expenses: List<Expense>,
        startDate: Date?,
        endDate: Date?
    ) {
        val sheet = workbook.createSheet("Tổng kết")
        
        val headerStyle = createHeaderStyle(workbook)
        val currencyStyle = createCurrencyStyle(workbook)
        val titleStyle = createTitleStyle(workbook)
        
        var rowIndex = 0
        
        // Tiêu đề báo cáo
        val titleRow = sheet.createRow(rowIndex++)
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("BÁO CÁO CHI TIÊU")
        titleCell.cellStyle = titleStyle
        
        // Khoảng thời gian
        if (startDate != null && endDate != null) {
            val periodRow = sheet.createRow(rowIndex++)
            periodRow.createCell(0).setCellValue("Khoảng thời gian:")
            periodRow.createCell(1).setCellValue(
                "${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(startDate)} - " +
                "${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate)}"
            )
        }
        
        rowIndex++ // Dòng trống
        
        // Tổng quan
        val overviewRow = sheet.createRow(rowIndex++)
        overviewRow.createCell(0).setCellValue("TỔNG QUAN")
        overviewRow.getCell(0).cellStyle = headerStyle
        
        val totalIncome = expenses.filter { !it.isExpense }.sumOf { it.amount }
        val totalExpense = expenses.filter { it.isExpense }.sumOf { it.amount }
        val balance = totalIncome - totalExpense
        
        // Thu nhập
        val incomeRow = sheet.createRow(rowIndex++)
        incomeRow.createCell(0).setCellValue("Tổng thu nhập:")
        val incomeCell = incomeRow.createCell(1)
        incomeCell.setCellValue(totalIncome.toDouble())
        incomeCell.cellStyle = currencyStyle
        
        // Chi tiêu
        val expenseRow = sheet.createRow(rowIndex++)
        expenseRow.createCell(0).setCellValue("Tổng chi tiêu:")
        val expenseCell = expenseRow.createCell(1)
        expenseCell.setCellValue(totalExpense.toDouble())
        expenseCell.cellStyle = currencyStyle
        
        // Số dư
        val balanceRow = sheet.createRow(rowIndex++)
        balanceRow.createCell(0).setCellValue("Số dư:")
        val balanceCell = balanceRow.createCell(1)
        balanceCell.setCellValue(balance.toDouble())
        balanceCell.cellStyle = currencyStyle
        
        rowIndex++ // Dòng trống
        
        // Chi tiêu theo danh mục
        val categoryRow = sheet.createRow(rowIndex++)
        categoryRow.createCell(0).setCellValue("CHI TIÊU THEO DANH MỤC")
        categoryRow.getCell(0).cellStyle = headerStyle
        
        val expensesByCategory = expenses
            .filter { it.isExpense }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { expense -> expense.amount } }
            .toList()
            .sortedByDescending { it.second }
        
        expensesByCategory.forEach { (category, amount) ->
            val catRow = sheet.createRow(rowIndex++)
            catRow.createCell(0).setCellValue(category)
            val catAmountCell = catRow.createCell(1)
            catAmountCell.setCellValue(amount.toDouble())
            catAmountCell.cellStyle = currencyStyle
        }
        
        // Set column widths manually
        sheet.setColumnWidth(0, 8000) // Label
        sheet.setColumnWidth(1, 6000) // Value
    }
    
    /**
     * Tạo sheet phân tích
     */
    private fun createAnalyticsSheet(workbook: XSSFWorkbook, expenses: List<Expense>) {
        val sheet = workbook.createSheet("Phân tích")
        
        val headerStyle = createHeaderStyle(workbook)
        val currencyStyle = createCurrencyStyle(workbook)
        
        var rowIndex = 0
        
        // Phân tích theo tháng
        val monthlyRow = sheet.createRow(rowIndex++)
        monthlyRow.createCell(0).setCellValue("PHÂN TÍCH THEO THÁNG")
        monthlyRow.getCell(0).cellStyle = headerStyle
        
        // Header cho bảng tháng
        val monthHeaderRow = sheet.createRow(rowIndex++)
        monthHeaderRow.createCell(0).setCellValue("Tháng")
        monthHeaderRow.createCell(1).setCellValue("Thu nhập")
        monthHeaderRow.createCell(2).setCellValue("Chi tiêu")
        monthHeaderRow.createCell(3).setCellValue("Tiết kiệm")
        
        // Nhóm theo tháng
        val monthlyData = expenses.groupBy {
            val calendar = Calendar.getInstance()
            calendar.time = it.date
            "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}"
        }
        
        monthlyData.forEach { (month, monthExpenses) ->
            val row = sheet.createRow(rowIndex++)
            val monthIncome = monthExpenses.filter { !it.isExpense }.sumOf { it.amount }
            val monthExpense = monthExpenses.filter { it.isExpense }.sumOf { it.amount }
            val monthSaving = monthIncome - monthExpense
            
            row.createCell(0).setCellValue(month)
            
            val incomeCell = row.createCell(1)
            incomeCell.setCellValue(monthIncome.toDouble())
            incomeCell.cellStyle = currencyStyle
            
            val expenseCell = row.createCell(2)
            expenseCell.setCellValue(monthExpense.toDouble())
            expenseCell.cellStyle = currencyStyle
            
            val savingCell = row.createCell(3)
            savingCell.setCellValue(monthSaving.toDouble())
            savingCell.cellStyle = currencyStyle
        }
        
        // Set column widths manually
        sheet.setColumnWidth(0, 5000) // Tháng
        sheet.setColumnWidth(1, 5000) // Thu nhập
        sheet.setColumnWidth(2, 5000) // Chi tiêu
        sheet.setColumnWidth(3, 5000) // Tiết kiệm
    }

    /**
     * Tạo style cho header
     */
    private fun createHeaderStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle() as XSSFCellStyle
        val font = workbook.createFont()

        font.bold = true
        font.color = IndexedColors.WHITE.index
        style.setFont(font)

        style.fillForegroundColor = IndexedColors.DARK_BLUE.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND

        style.setBorderTop(BorderStyle.THIN)
        style.setBorderBottom(BorderStyle.THIN)
        style.setBorderLeft(BorderStyle.THIN)
        style.setBorderRight(BorderStyle.THIN)

        return style
    }

    /**
     * Tạo style cho tiêu đề
     */
    private fun createTitleStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle() as XSSFCellStyle
        val font = workbook.createFont()

        font.bold = true
        font.fontHeightInPoints = 16
        style.setFont(font)

        return style
    }

    /**
     * Tạo style cho ngày tháng
     */
    private fun createDateStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle() as XSSFCellStyle
        val createHelper = workbook.creationHelper
        style.dataFormat = createHelper.createDataFormat().getFormat("dd/mm/yyyy")
        return style
    }

    /**
     * Tạo style cho tiền tệ
     */
    private fun createCurrencyStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle() as XSSFCellStyle
        val createHelper = workbook.creationHelper
        style.dataFormat = createHelper.createDataFormat().getFormat("#,##0 \"₫\"")
        return style
    }


    /**
     * Lưu workbook vào file
     */
    private fun saveWorkbookToFile(workbook: XSSFWorkbook, fileName: String): File {
        // Tạo thư mục Downloads nếu chưa có
        val downloadsDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "NoNo"
        )
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        
        val file = File(downloadsDir, fileName)
        
        FileOutputStream(file).use { outputStream ->
            workbook.write(outputStream)
        }
        
        return file
    }
    
    /**
     * Tạo tên file dựa trên khoảng thời gian
     */
    private fun generateFileName(startDate: Date?, endDate: Date?): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val timestamp = SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())
        
        return when {
            startDate != null && endDate != null -> {
                "ChiTieu_${dateFormat.format(startDate)}_${dateFormat.format(endDate)}_$timestamp.xlsx"
            }
            else -> {
                "ChiTieu_ToanBo_${dateFormat.format(Date())}_$timestamp.xlsx"
            }
        }
    }
    
    /**
     * Reset export state
     */
    fun resetExportState() {
        _exportState.value = ExportState.Idle
        _progress.value = 0f
    }
    
    /**
     * Lấy URI của file để chia sẻ
     */
    fun getFileUri(filePath: String): android.net.Uri {
        val file = File(filePath)
        return FileProvider.getUriForFile(context, PROVIDER_AUTHORITY, file)
    }
}

