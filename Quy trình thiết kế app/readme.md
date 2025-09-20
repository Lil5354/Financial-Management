📌 Quy trình phát triển ứng dụng Quản Lý Chi Tiêu Cá Nhân (Personal Expense Tracker)

1. Giới thiệu

Ứng dụng giúp người dùng ghi chép, quản lý, phân tích chi tiêu cá nhân. Công nghệ sử dụng Java + Android Studio + SQLite, áp dụng kiến trúc MVVM, và tích hợp AI (OCR & auto-categorization) để nâng cao trải nghiệm.

⸻

2. Yêu cầu chức năng
	•	Chức năng cơ bản (MVP)
	•	Thêm, sửa, xóa, xem chi tiêu (CRUD).
	•	Quản lý danh mục chi tiêu (Ăn uống, Đi lại, Mua sắm, Khác…).
	•	Hiển thị danh sách chi tiêu.
	•	Báo cáo chi tiêu theo ngày/tháng/năm.
	•	Biểu đồ trực quan (cột/tròn).
	•	Chức năng nâng cao
	•	Nhắc nhở nhập chi tiêu bằng Notification.
	•	Đồng bộ dữ liệu với Firebase (backup, multi-device).
	•	Chức năng AI (để đạt điểm D5)
	•	OCR hóa đơn: chụp ảnh → nhận diện số tiền & mô tả.
	•	Tự động phân loại chi tiêu (auto-categorization) bằng từ khóa hoặc ML Kit Text Classifier.
	•	Dự đoán chi tiêu (forecasting) bằng regression model (TensorFlow Lite).

⸻

3. Công nghệ & thư viện
	•	Ngôn ngữ: Java
	•	IDE: Android Studio, Gradle
	•	Database: SQLite (Room ORM nếu muốn clean code)
	•	UI/UX: XML (Material Design)
	•	Biểu đồ: MPAndroidChart
	•	Cloud & Auth: Firebase (Firestore/Realtime DB, Auth)
	•	Notification: Firebase Cloud Messaging / Android Notification Manager
	•	AI: Google ML Kit (Text Recognition, Text Classification), TensorFlow Lite

⸻

4. Thiết kế hệ thống

4.1. Kiến trúc
	•	MVVM: Activity/Fragment (View) ↔ ViewModel ↔ Repository ↔ Database/API
	•	Version Control: GitHub/GitLab

4.2. Cơ sở dữ liệu
	•	Expense(id, title, amount, category, date, note)
	•	Category(id, name, icon)

⸻

5. Quy trình phát triển

Giai đoạn 1: Chuẩn bị & Thiết kế
	•	Thu thập yêu cầu, xác định tính năng bắt buộc & nâng cao.
	•	Thiết kế UI/UX (wireframe).
	•	Thiết kế database & kiến trúc ứng dụng.

Giai đoạn 2: Phát triển MVP
	•	Cài đặt database (Room/SQLite).
	•	Xây dựng các màn hình chính:
	•	Thêm chi tiêu.
	•	Danh sách chi tiêu (RecyclerView).
	•	Chỉnh sửa / xóa chi tiêu.
	•	Báo cáo cơ bản (tổng chi tiêu, thống kê theo tháng).
	•	Hiển thị biểu đồ bằng MPAndroidChart.

Giai đoạn 3: Tính năng nâng cao
	•	Đồng bộ dữ liệu với Firebase.
	•	Notification nhắc nhở nhập chi tiêu.
	•	Quản lý danh mục tùy chỉnh.

Giai đoạn 4: AI Integration
	•	OCR hóa đơn (ML Kit).
	•	Auto-categorization (ML Kit Text Classifier hoặc rule-based).
	•	Forecasting chi tiêu bằng TensorFlow Lite regression.

Giai đoạn 5: Hoàn thiện & Demo
	•	Test toàn bộ chức năng.
	•	Viết báo cáo & slide thuyết trình.
	•	Demo CRUD + Báo cáo + Tính năng AI (điểm nhấn).

⸻

6. Kế hoạch đánh giá
	•	D1 (3 điểm): Hoàn thiện ứng dụng (MVP + nâng cao).
	•	D2 (3 điểm): Báo cáo chi tiết, bố cục rõ ràng.
	•	D3 (2 điểm): Bảo vệ, trả lời câu hỏi.
	•	D4 (1 điểm): Tiến độ, thái độ, đóng góp nhóm.
	•	D5 (1 điểm): Tích hợp AI (OCR, auto-categorization, forecasting).
