Ý tưởng kết hợp quản lý chi tiêu với hình ảnh (Visual Money Tracking) là một hướng tiếp cận rất trực quan và thú vị, đặc biệt phù hợp với những người thích ghi nhớ qua thị giác thay vì những con số khô khan.

Dưới đây là bản Đặc tả Yêu cầu (Product Specification) được cấu trúc chặt chẽ, sẵn sàng để làm tài liệu nền tảng cho quá trình phát triển (rất phù hợp nếu bạn đang áp dụng các phương pháp như Spec-Driven Development).

Lưu ý nhỏ về mặt thuật ngữ: Để tối ưu dung lượng khi lưu trữ và đồng bộ, chúng ta sẽ sử dụng kỹ thuật nén ảnh (compress) thay vì decompress (giải nén).
Đặc tả Yêu cầu: Ứng dụng "Visual Money Tracker" (Android)
1. Tổng quan dự án (Project Overview)

Ứng dụng Android hỗ trợ người dùng theo dõi dòng tiền (thu/chi) thông qua hình ảnh hóa. Thay vì nhập text mô tả dài dòng, người dùng chỉ cần chụp ảnh hóa đơn, món đồ đã mua, hoặc nguồn tiền nhận được và gắn tag số tiền.
2. Yêu cầu chức năng (Functional Requirements)
2.1. Quản lý luồng tiền (Cash Flow Management)

    Phân loại: Hệ thống chỉ ghi nhận 2 trạng thái cốt lõi:

        Chi tiêu (Expense) - Mặc định hiển thị màu Đỏ.

        Thu nhập (Income) - Mặc định hiển thị màu Xanh.

    Thuộc tính của một bản ghi (Transaction Item): * ID (Unique).

        Type (Income/Expense).

        Amount (Số tiền - kiểu số nguyên/thập phân).

        Category (Danh mục - tham chiếu đến Category ID).

        ImagePath (Đường dẫn lưu ảnh local).

        Timestamp (Ngày giờ tạo).

2.2. Luồng nhập dữ liệu (Input Flow)

    Khởi tạo: Nút Floating Action Button (FAB) với icon Camera chỉ hiển thị ở Tab Gallery (Tab 1).

    Tương tác: Khi bấm FAB, xuất hiện Bottom Sheet với 2 options:

        Chụp ảnh mới: Mở trực tiếp giao diện camera của ứng dụng.

        Chọn từ thư viện: Mở Android Photo Picker để lấy ảnh có sẵn.

    Nhập số liệu: Sau khi có ảnh, hiển thị màn hình nhập số tiền (Numpad), chọn loại giao dịch (Thu/Chi), chọn Category từ danh sách (bắt buộc, mặc định là "Khác"), và chọn Ví (bắt buộc — nếu chỉ có 1 ví thì tự động chọn). Xác nhận để lưu. Nếu chưa có ví nào, người dùng phải tạo ví trước khi tạo giao dịch đầu tiên.

2.3. Điều hướng chính (Bottom Navigation)

    Cấu trúc: Ứng dụng sử dụng Bottom Navigation Bar với 3 tab cố định:

        Tab 1 — Gallery: Màn hình lưới ảnh giao dịch (xem mục 2.3.1).

        Tab 2 — Analytics: Màn hình biểu đồ phân tích chi tiêu (xem mục 2.7).

        Tab 3 — Settings: Màn hình cài đặt chung (xem mục 2.4).

    FAB Camera: Chỉ hiển thị khi người dùng đang ở Tab 1 (Gallery). Ẩn hoàn toàn ở Tab 2 và Tab 3.

2.3.1. Hiển thị dữ liệu (Gallery View)

    Màn hình chính: Giao diện lưới (Grid View) tương tự như ứng dụng Thư viện ảnh (Gallery).

    Phân cụm (Grouping): Dữ liệu được nhóm theo từng Tháng (Ví dụ: "Tháng 3, 2026"). Có dropdown hoặc thao tác vuốt để chuyển tháng.

    Hiển thị Item: Mỗi giao dịch là một khung ảnh dạng vuông (thumbnail).

    Data Overlay: Số tiền được hiển thị nổi (Overlay) trực tiếp trên góc của hình ảnh với background bán trong suốt (semi-transparent) để dễ đọc chữ. Kèm theo icon mũi tên lên (Thu) hoặc xuống (Chi).

    Tổng số dư: Header của Gallery hiển thị tổng số dư của tất cả ví (hoặc số dư của ví đang được filter). Người dùng có thể filter giao dịch theo ví cụ thể hoặc xem tất cả ví.

2.4. Đồng bộ & Nhắc nhở (Sync & Reminder)

    Cơ chế lưu trữ: Toàn bộ dữ liệu (Database text + File ảnh) được lưu offline trên máy trước.

    Tích hợp Cloud: Cung cấp tính năng kết nối (OAuth 2.0) với Google Drive hoặc Box. Khi đồng bộ, ứng dụng sẽ nén Database và thư mục ảnh thành 1 file .zip đẩy lên cloud, hoặc tạo thư mục trên cloud chứa từng ảnh và 1 file JSON/CSV.

    Nhắc nhở: Tích hợp Notification nội bộ. Cài đặt cho phép người dùng chọn chu kỳ nhắc nhở:

        Hàng ngày (Ví dụ: 20:00 mỗi tối).

        Hàng tuần (Ví dụ: Tối Chủ Nhật).

        Hàng tháng (Ví dụ: Ngày cuối cùng của tháng).

    Import/Restore từ Cloud: Cung cấp tính năng tải dữ liệu từ Google Drive hoặc Box về máy. Khi import, ứng dụng cho phép người dùng chọn chiến lược xử lý conflict giữa dữ liệu cloud và dữ liệu local:

        Overwrite: Xóa toàn bộ dữ liệu local và thay thế bằng dữ liệu từ cloud.

        Merge: Giữ lại dữ liệu local, bổ sung thêm các bản ghi từ cloud mà chưa tồn tại trên máy (dựa theo ID). Bản ghi trùng ID thì ưu tiên giữ bản local.

    Tự động đồng bộ (Auto-sync): Thay vì chỉ hỗ trợ sync thủ công, hệ thống tự động thực hiện đồng bộ lên cloud theo chu kỳ cấu hình sẵn. Sử dụng WorkManager (tương tự cơ chế Reminder) để lên lịch tác vụ nền. Người dùng có thể bật/tắt và chọn chu kỳ:

        Hàng ngày (Ví dụ: 02:00 sáng).

        Khi kết nối WiFi (chỉ sync khi thiết bị đang dùng WiFi để tiết kiệm data).

        Auto-sync chỉ chạy khi người dùng đã xác thực OAuth với ít nhất một cloud provider.

    Chính sách giữ lại bản backup (Retention Policy): Sau mỗi lần sync thành công lên cloud, hệ thống tự động xóa các bản backup cũ, chỉ giữ lại N bản gần nhất (mặc định N = 3, người dùng có thể cấu hình từ 1 đến 10). Các file backup được xác định theo tên file có chứa timestamp. Mục đích: giải phóng dung lượng cloud tự động mà không cần người dùng can thiệp thủ công.

2.5. Tối ưu hóa dung lượng (Image Compression)

    Xử lý ảnh: Khi người dùng chụp hoặc chọn ảnh, hệ thống tự động nén ảnh trước khi lưu. Bản gốc dung lượng lớn (thường 3-5MB/ảnh) KHÔNG được lưu vào bộ nhớ ứng dụng — thay vào đó, chỉ bản đã nén được lưu lại. Người dùng vẫn xem và tracking ảnh bình thường.

    Tiêu chuẩn nén: * Resize: Thu nhỏ kích thước ảnh với max-width là 1440px (đủ để đọc rõ text trên hóa đơn khi zoom).

        Format & Quality: Chuyển đổi sang định dạng WebP ở mức chất lượng 85% để đảm bảo text và chi tiết nhỏ vẫn sắc nét.

        Mục tiêu: Đưa dung lượng mỗi ảnh xuống dưới 800KB để tiết kiệm bộ nhớ máy và tăng tốc độ tải lên Google Drive/Box.

2.6. Hệ thống danh mục (Category System)

    Cấu trúc Category: Mỗi Category có các thuộc tính:

        ID (Unique).

        Name (Tên danh mục).

        IsPreset (Boolean — đánh dấu danh mục mặc định, không thể xóa).

        Icon (Tùy chọn — icon đại diện cho danh mục).

    Danh mục mặc định (Preset Categories): Hệ thống cung cấp sẵn các danh mục sau khi cài đặt lần đầu:

        Ăn uống, Di chuyển, Mua sắm, Giải trí, Sức khỏe, Hóa đơn, Thu nhập, Khác.

    Quản lý danh mục tùy chỉnh: Người dùng có thể:

        Thêm category mới với tên tự đặt.

        Sửa tên category tùy chỉnh (không sửa được preset).

        Xóa category tùy chỉnh (không xóa được preset). Khi xóa một category đang được dùng bởi các giao dịch, hệ thống tự động chuyển các giao dịch đó sang category "Khác".

    Gán category khi tạo giao dịch: Trường Category là bắt buộc trong màn hình nhập liệu. Giá trị mặc định là "Khác" nếu người dùng không chọn.

2.7. Màn hình phân tích (Analytics View)

    Bộ lọc tháng: Màn hình Analytics có Month Switcher tương tự Tab Gallery, cho phép người dùng chuyển qua lại giữa các tháng.

    Tổng quan tháng: Hiển thị tổng Thu nhập và tổng Chi tiêu của tháng được chọn ở phần đầu màn hình.

    Toggle chế độ xem: Người dùng có thể toggle giữa 2 chế độ:

        Expense: Chỉ hiển thị phân tích các giao dịch Chi tiêu.

        Income: Chỉ hiển thị phân tích các giao dịch Thu nhập.

    Biểu đồ tròn (Pie Chart): Hiển thị tỷ lệ phân bổ theo từng Category trong tháng và chế độ xem được chọn. Mỗi phần (slice) của biểu đồ hiển thị:

        Tên Category.

        Phần trăm (%) so với tổng.

        Số tiền tuyệt đối.

    Danh sách chi tiết: Bên dưới biểu đồ, hiển thị danh sách các Category kèm màu sắc tương ứng, số tiền và phần trăm — nhất quán với màu trên Pie Chart.

    Bộ lọc ví: Người dùng có thể filter Analytics theo ví cụ thể hoặc xem tổng hợp tất cả ví. Khi filter theo ví, tổng thu/chi và biểu đồ chỉ tính các giao dịch thuộc ví đó.

2.8. Hệ thống ví / nguồn tiền (Wallet System)

    Cấu trúc Wallet: Mỗi Wallet có các thuộc tính:

        ID (Unique).

        Name (Tên ví — do người dùng tự đặt, ví dụ: "Tiền mặt", "Ngân hàng Vietcombank", "Ví MoMo").

        OpeningBalance (Số dư ban đầu — nhập khi tạo ví, có thể = 0).

        CreatedAt (Ngày giờ tạo).

    Số dư hiện tại: Số dư hiện tại của một ví = OpeningBalance + tổng Income - tổng Expense của tất cả giao dịch thuộc ví đó.

    Không có preset wallets: Tất cả ví đều do người dùng tạo. Hệ thống không cung cấp ví mặc định sẵn.

    Bắt buộc có ví trước khi tạo giao dịch: Người dùng phải tạo ít nhất 1 ví trước khi có thể tạo giao dịch đầu tiên.

    Quản lý ví: Người dùng có thể:

        Tạo ví mới với tên tùy chỉnh và opening balance.

        Sửa tên ví và opening balance.

        Xóa ví. Khi xóa ví đang có giao dịch, hệ thống hỏi người dùng muốn: (a) chuyển tất cả giao dịch sang ví khác, hoặc (b) xóa luôn tất cả giao dịch của ví đó.

    Gán ví khi tạo giao dịch: Trường Wallet là bắt buộc trong màn hình nhập liệu. Nếu chỉ có 1 ví, tự động chọn ví đó. Nếu có nhiều ví, người dùng phải chọn.

    Wallet Management Screen: Màn hình quản lý ví (trong Settings hoặc tab riêng) hiển thị danh sách tất cả ví kèm số dư hiện tại của từng ví.

3. Gợi ý Kiến trúc & Tech Stack (Android Native)

Để ứng dụng hoạt động mượt mà và dễ bảo trì, dưới đây là các công nghệ đề xuất:

    Ngôn ngữ & Kiến trúc: Kotlin, mô hình MVVM (Model-View-ViewModel) kết hợp Clean Architecture.

    Giao diện: Jetpack Compose (rất phù hợp để xây dựng giao diện Grid View dạng Gallery mượt mà và dễ custom overlay).

    Camera & Media: * CameraX để xử lý việc chụp ảnh trực tiếp.

        Coil hoặc Glide để load ảnh thumbnail trên lưới một cách tối ưu bộ nhớ (cache).

    Database & Background Task: * Room Database để lưu trữ dữ liệu local.

        WorkManager để thiết lập các tác vụ nhắc nhở (Reminders) chạy ngầm một cách chính xác.
