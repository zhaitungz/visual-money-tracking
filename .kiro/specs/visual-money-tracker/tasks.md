# Implementation Plan: Visual Money Tracker

## Overview

Triển khai ứng dụng Android theo Clean Architecture (Presentation → Domain → Data). Các task được sắp xếp theo thứ tự từ nền tảng (data layer) lên UI, đảm bảo mỗi bước tích hợp được với bước trước.

## Tasks

- [x] 1. Thiết lập cấu trúc dự án và dependencies
  - Tạo module structure theo design: `presentation/`, `domain/`, `data/`, `di/`
  - Thêm dependencies vào `build.gradle.kts`: Room, Hilt, Compose, CameraX, Coil, WorkManager, Kotest, Kotlinx-serialization
  - Cấu hình Hilt `@HiltAndroidApp` trong `Application` class
  - _Requirements: 3 (Tech Stack)_

- [x] 2. Xây dựng Domain Layer
  - [x] 2.1 Định nghĩa domain models và interfaces
    - Tạo `TransactionType` enum (`INCOME`, `EXPENSE`)
    - Tạo `Transaction` data class với các field: `id`, `type`, `amount`, `categoryId`, `walletId`, `imagePath`, `timestamp`
    - Tạo `Category` data class với các field: `id`, `name`, `isPreset`, `icon`
    - Tạo `Wallet` data class với các field: `id`, `name`, `openingBalance`, `createdAt`
    - Tạo `TransactionRepository` interface với `getTransactionsByMonth`, `getTransactionsByMonthAndWallet`, `saveTransaction`, `deleteTransaction`, `getAllTransactions`, `getTransactionsByWallet`, `reassignWallet`, `deleteTransactionsByWallet`
    - Tạo `CategoryRepository` interface với `getAll`, `getById`, `insert`, `update`, `delete`, `reassignToFallback`, `seedPresets`
    - Tạo `WalletRepository` interface với `getAll`, `getById`, `insert`, `update`, `delete`, `reassignTransactions`
    - Tạo `SyncRepository` interface và `CloudProvider` sealed class
    - _Requirements: 2.1.1, 2.1.2, 2.6.1, 2.8.1_

  - [x] 2.2 Viết property test cho Transaction type invariant
    - **Property 1: Transaction type invariant**
    - **Validates: Requirements 2.1.1**
    - Dùng `Arb.enum<TransactionType>()`, verify `type in {INCOME, EXPENSE}` với 100 iterations
    - Tag: `// Feature: visual-money-tracker, Property 1: Transaction type invariant`

  - [x] 2.3 Implement các Use Cases
    - Tạo `SaveTransactionUseCase`: nhận `rawImageUri`, `amount`, `type`, `categoryId`, `walletId` → gọi `ImageCompressor` rồi `repo.saveTransaction`
    - Tạo `GetTransactionsByMonthUseCase`: wrap `repo.getTransactionsByMonth` (có optional `walletId` filter) trả về `Flow`
    - Tạo `GetCategoriesUseCase`: wrap `repo.getAll` trả về `Flow<List<Category>>`
    - Tạo `SaveCategoryUseCase`, `DeleteCategoryUseCase` (tìm ID "Khác" → `reassignToFallback` → `delete`)
    - Tạo `GetAnalyticsUseCase`: tính `CategoryBreakdown` list (amount + percentage) cho tháng + type + optional walletId
    - Tạo `GetWalletsUseCase`: wrap `walletRepo.getAll` trả về `Flow<List<Wallet>>`
    - Tạo `SaveWalletUseCase`: validate tên không rỗng rồi `walletRepo.insert/update`
    - Tạo `DeleteWalletUseCase`: nếu `reassignToWalletId != null` → `transactionRepo.reassignWallet` rồi `walletRepo.delete`; nếu null → `transactionRepo.deleteTransactionsByWallet` rồi `walletRepo.delete`
    - Tạo `GetWalletBalanceUseCase`: tính `openingBalance + sum(INCOME) - sum(EXPENSE)` cho tất cả transactions của ví
    - Tạo `DeleteTransactionUseCase`, `SyncToCloudUseCase`, `ScheduleReminderUseCase`
    - _Requirements: 2.1, 2.2.3, 2.4.2, 2.4.3, 2.6.3, 2.7, 2.8.2, 2.8.5_

  - [x] 2.4 Viết unit tests cho SaveTransactionUseCase
    - Test: lưu thành công trả về ID hợp lệ
    - Test: compression failure → trả về `Result.failure`, không gọi `repo.saveTransaction`
    - Test: amount = 0 → không được lưu (validation)
    - _Requirements: 2.1.2, 2.5.1_

- [ ] 3. Xây dựng Data Layer – Room Database
  - [x] 3.1 Tạo Room entities, DAO và Database
    - Tạo `TransactionEntity` với `@Entity(tableName = "transactions")` và các field `categoryId: Long`, `walletId: Long`
    - Tạo `CategoryEntity` với `@Entity(tableName = "categories")` và các field: `id`, `name`, `isPreset`, `icon`
    - Tạo `WalletEntity` với `@Entity(tableName = "wallets")` và các field: `id`, `name`, `openingBalance`, `createdAt`
    - Tạo `TransactionDao` với queries `getByMonth` (Flow), `getByMonthAndWallet` (Flow), `insert`, `delete`, `getAll`, `getByWallet`, `reassignCategory`, `reassignWallet`, `deleteByWallet`
    - Tạo `CategoryDao` với queries `getAll` (Flow), `getById`, `getPresetByName`, `insert`, `update`, `delete` (chỉ xóa non-preset)
    - Tạo `WalletDao` với queries `getAll` (Flow), `getById`, `insert`, `update`, `delete`
    - Tạo `AppDatabase` với `@Database` bao gồm cả 3 entities, cấu hình `fallbackToDestructiveMigration`
    - Tạo mapper functions: `TransactionEntity.toDomain()`, `Transaction.toEntity()`, `CategoryEntity.toDomain()`, `Category.toEntity()`, `WalletEntity.toDomain()`, `Wallet.toEntity()`
    - _Requirements: 2.1.2, 2.4.1, 2.6.1, 2.8.1_

  - [x] 3.2 Viết property test cho Unique transaction IDs
    - **Property 2: Unique transaction IDs**
    - **Validates: Requirements 2.1.2**
    - Dùng Room in-memory DB, insert N transactions ngẫu nhiên, verify `ids.size == ids.toSet().size`
    - Dùng `Arb.list(arbTransaction, 1..50)` với 100 iterations
    - Tag: `// Feature: visual-money-tracker, Property 2: Unique transaction IDs`

  - [x] 3.3 Implement TransactionRepositoryImpl và CategoryRepositoryImpl
    - Implement `TransactionRepository` interface dùng `TransactionDao`; bao gồm `getTransactionsByMonthAndWallet`, `getTransactionsByWallet`, `reassignWallet`, `deleteTransactionsByWallet`
    - Implement `CategoryRepository` interface dùng `CategoryDao`; implement `seedPresets` để insert 8 preset categories khi DB rỗng
    - Map entities ↔ domain models trong repository
    - _Requirements: 2.1, 2.4.1, 2.6.2_

  - [x] 3.4 Implement WalletRepositoryImpl
    - Implement `WalletRepository` interface dùng `WalletDao`
    - Map `WalletEntity` ↔ `Wallet` domain model
    - _Requirements: 2.8.1, 2.8.5_

  - [x] 3.4 Viết property test cho Preset categories cannot be deleted
    - **Property 13: Preset categories cannot be deleted**
    - **Validates: Requirements 2.6.3**
    - Dùng Room in-memory DB, seed presets, gọi `CategoryDao.delete(id)` với preset ID, verify row vẫn còn trong DB
    - Dùng `Arb.element(presetIds)` với 100 iterations
    - Tag: `// Feature: visual-money-tracker, Property 13: Preset categories cannot be deleted`

  - [x] 3.5 Viết property test cho Cascade delete reassigns to "Khác"
    - **Property 12: Cascade delete reassigns all transactions to "Khác"**
    - **Validates: Requirements 2.6.3**
    - Dùng `Arb.list(arbTransaction, 1..30)` gán vào custom category, gọi `DeleteCategoryUseCase`, verify tất cả transactions có `categoryId` = ID của "Khác", category bị xóa không còn tồn tại
    - Tag: `// Feature: visual-money-tracker, Property 12: Cascade delete reassigns all transactions to Khác`

  - [x] 3.6 Viết property test cho Wallet balance calculation correctness
    - **Property 14: Wallet balance calculation correctness**
    - **Validates: Requirements 2.8.2**
    - Dùng `Arb.double()` cho `openingBalance` và `Arb.list(arbTransaction, 0..50)` với mixed INCOME/EXPENSE, gọi `GetWalletBalanceUseCase`, verify kết quả = `openingBalance + sum(INCOME) - sum(EXPENSE)`
    - Tag: `// Feature: visual-money-tracker, Property 14: Wallet balance calculation correctness`

  - [-] 3.7 Viết property test cho No orphan transactions after wallet delete
    - **Property 15: No orphan transactions after wallet delete**
    - **Validates: Requirements 2.8.5**
    - Dùng `Arb.list(arbTransaction, 1..30)` gán vào wallet W, dùng `Arb.boolean()` để chọn reassign vs cascade delete, gọi `DeleteWalletUseCase`, verify không còn transaction nào có `walletId == W.id`
    - Tag: `// Feature: visual-money-tracker, Property 15: No orphan transactions after wallet delete`

- [ ] 4. Xây dựng Data Layer – Image Compression
  - [ ] 4.1 Implement ImageCompressor
    - Tạo `ImageCompressor` interface và `ImageCompressorImpl`
    - Logic: decode URI → resize nếu width > 1440px (giữ aspect ratio) → compress sang WebP_LOSSY quality 85
    - Lưu file vào `files/images/{timestamp}_{uuid}.webp` trong internal storage
    - Trả về `Result<String>` (path) hoặc `Result.failure` nếu OOM/corrupt
    - _Requirements: 2.5.1, 2.5.2_

  - [ ] 4.2 Viết property test cho Image compression output constraints
    - **Property 6: Image compression output constraints**
    - **Validates: Requirements 2.5.1, 2.5.2**
    - Generate random bitmap với `Arb.int(100..4000)` cho width/height, compress, verify: width ≤ 1440px, file size < 800KB, `imagePath` trỏ đến file nén (không phải source URI)
    - Tag: `// Feature: visual-money-tracker, Property 6: Image compression output constraints`

  - [ ] 4.3 Implement ImageStorageManager
    - Tạo `ImageStorageManager` để quản lý tạo/xóa file ảnh trong internal storage
    - Xử lý xóa file ảnh khi transaction bị delete
    - _Requirements: 2.4.1, 2.5.1_

- [ ] 5. Checkpoint – Kiểm tra Domain và Data Layer
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 6. Xây dựng Cloud Sync
  - [ ] 6.1 Implement SyncManifest serialization
    - Tạo data class `SyncManifest` và `SyncTransactionItem` với `@Serializable`
    - Implement serialize `List<Transaction>` → JSON manifest và deserialize ngược lại
    - _Requirements: 2.4.2_

  - [ ] 6.2 Viết property test cho Sync manifest round-trip
    - **Property 4: Sync manifest round-trip**
    - **Validates: Requirements 2.4.2**
    - Dùng `Arb.list(arbTransaction)`, serialize → deserialize, verify equality (id, type, amount, timestamp, imageFile)
    - Tag: `// Feature: visual-money-tracker, Property 4: Sync manifest round-trip`

  - [ ] 6.3 Implement CloudSyncManager
    - Tạo `CloudSyncManager` interface và skeleton implementations cho `GoogleDriveService`, `BoxService`
    - Implement `ZipUpload` strategy: zip DB + images folder → upload
    - Implement `FolderWithManifest` strategy: upload từng ảnh + manifest JSON
    - Xử lý OAuth 2.0 token refresh và retry với exponential backoff (tối đa 3 lần)
    - _Requirements: 2.4.2_

  - [ ] 6.4 Implement SyncRepositoryImpl
    - Wire `CloudSyncManager` vào `SyncRepository` interface
    - Xử lý `authenticate` flow cho từng `CloudProvider`
    - _Requirements: 2.4.2_

  - [ ] 6.5 Implement RestoreUseCase
    - Tạo `ConflictStrategy` enum (`OVERWRITE`, `MERGE`) và `RestoreResult` data class
    - Implement `RestoreUseCase.invoke(provider, conflictStrategy)`:
      - OVERWRITE: dùng Room transaction để xóa toàn bộ local rồi insert dữ liệu cloud; rollback nếu xóa thất bại giữa chừng
      - MERGE: giữ local, chỉ insert các bản ghi cloud có ID chưa tồn tại; bỏ qua bản ghi trùng ID (ưu tiên local)
    - Download backup từ cloud trước khi thay đổi DB; trả về `Result.failure` nếu download/parse thất bại (không thay đổi local)
    - _Requirements: 2.4.4_

  - [ ] 6.6 Viết property test cho Restore Overwrite replaces local data entirely
    - **Property 7: Restore Overwrite replaces local data entirely**
    - **Validates: Requirements 2.4.4 (Overwrite)**
    - Dùng `Arb.list(arbTransaction)` × 2 cho local và cloud sets, apply OVERWRITE, verify local DB chứa đúng các transaction từ cloud (không hơn, không kém)
    - Tag: `// Feature: visual-money-tracker, Property 7: Restore Overwrite replaces local data entirely`

  - [ ] 6.7 Viết property test cho Restore Merge preserves local and adds new cloud records
    - **Property 8: Restore Merge preserves local and adds new cloud records**
    - **Validates: Requirements 2.4.4 (Merge)**
    - Dùng `Arb.list(arbTransaction)` với shared ID arb để tạo overlapping sets, apply MERGE, verify: mọi ID local vẫn còn với giá trị gốc; mọi ID cloud không có trong local được thêm vào; ID trùng giữ giá trị local
    - Tag: `// Feature: visual-money-tracker, Property 8: Restore Merge preserves local and adds new cloud records`

  - [ ] 6.8 Implement RetentionPolicyManager
    - Tạo `RetentionPolicy` data class (`keepCount: Int = 3`, range 1..10) và `CloudBackupFile` data class
    - Implement `RetentionPolicyManager.applyPolicy(provider, policy)`:
      - Gọi `cloudSyncManager.listBackups` để lấy danh sách
      - Parse `timestampMillis` từ tên file theo convention `backup_{ISO8601}.zip`; bỏ qua file không parse được (log warning, không xóa)
      - Sắp xếp giảm dần theo timestamp, xóa tất cả ngoài N bản đầu tiên
      - Xóa file thất bại → log lỗi, tiếp tục xóa các file còn lại
    - Gọi `applyPolicy` sau mỗi lần `CloudSyncManager.sync` thành công
    - _Requirements: 2.4.6_

  - [ ] 6.9 Viết property test cho Retention policy keeps exactly the N most recent backups
    - **Property 10: Retention policy keeps exactly the N most recent backups**
    - **Validates: Requirements 2.4.6**
    - Dùng `Arb.list(arbBackupFile, 0..20)` và `Arb.int(1..10)` cho keepCount, apply policy, verify: số file còn lại ≤ keepCount; các file còn lại là N file có timestamp lớn nhất; nếu tổng < N thì không xóa gì
    - Tag: `// Feature: visual-money-tracker, Property 10: Retention policy keeps exactly the N most recent backups`

- [ ] 7. Xây dựng WorkManager Reminder
  - [ ] 7.1 Implement ReminderWorker và scheduler
    - Tạo `ReminderWorker : CoroutineWorker` post local notification
    - Tạo `ReminderSettings` data class và `ReminderFrequency` enum
    - Implement `ScheduleReminderUseCase`: build `PeriodicWorkRequest` theo frequency (DAILY→24h, WEEKLY→7 days, MONTHLY→30 days)
    - Xử lý notification permission (Android 13+) rationale
    - _Requirements: 2.4.3_

  - [ ] 7.2 Viết property test cho Reminder schedule interval correctness
    - **Property 5: Reminder schedule interval correctness**
    - **Validates: Requirements 2.4.3**
    - Dùng `Arb.enum<ReminderFrequency>()` + time arbs, build `PeriodicWorkRequest`, verify interval: DAILY=24h, WEEKLY=7×24h, MONTHLY≈30×24h
    - Tag: `// Feature: visual-money-tracker, Property 5: Reminder schedule interval correctness`

  - [ ] 7.3 Implement AutoSyncWorker và AutoSyncSettings
    - Tạo `AutoSyncFrequency` enum (`DAILY`, `WIFI_ONLY`) và `AutoSyncSettings` data class
    - Implement `AutoSyncWorker : CoroutineWorker`:
      - Đọc `AutoSyncSettings` từ DataStore; nếu `provider == null` (chưa xác thực) → trả về `Result.success()` ngay, không thực hiện bất kỳ network call nào
      - Nếu đã xác thực → gọi `CloudSyncManager.sync` rồi `RetentionPolicyManager.applyPolicy`
      - Sync thất bại → WorkManager retry theo backoff policy; thông báo lỗi chỉ khi thất bại liên tiếp > 3 lần
    - Implement `ScheduleAutoSyncUseCase`: build `PeriodicWorkRequest` với constraint `NetworkType.UNMETERED` khi `WIFI_ONLY`, enqueue với `ExistingPeriodicWorkPolicy.UPDATE`
    - Lưu `AutoSyncSettings` qua Jetpack DataStore (Preferences)
    - _Requirements: 2.4.5_

  - [ ] 7.4 Viết property test cho Auto-sync does not run without valid OAuth
    - **Property 9: Auto-sync does not run without valid OAuth**
    - **Validates: Requirements 2.4.5**
    - Dùng `Arb.bind(...)` để generate `AutoSyncSettings` với `provider = null`, chạy `AutoSyncWorker.doWork()` với mock `CloudSyncManager`, verify không có network call nào được thực hiện và worker trả về `Result.success()`
    - Tag: `// Feature: visual-money-tracker, Property 9: Auto-sync does not run without valid OAuth`

- [x] 8. Xây dựng Presentation Layer – GalleryScreen
  - [x] 8.1 Implement GalleryViewModel
    - Tạo `GalleryUiState` data class với `wallets`, `selectedWalletId`, `totalBalance`
    - Implement `GalleryViewModel` với `StateFlow<GalleryUiState>`
    - Gọi `GetTransactionsByMonthUseCase` (có wallet filter), group kết quả theo `YearMonth`
    - Gọi `GetWalletsUseCase` để load danh sách ví; tính `totalBalance` từ `GetWalletBalanceUseCase`
    - Expose actions: `onMonthSelected`, `onDeleteTransaction`, `onWalletFilterSelected`
    - _Requirements: 2.3.1, 2.3.2, 2.8.7_

  - [x] 8.2 Viết property test cho Monthly grouping correctness
    - **Property 3: Monthly grouping correctness**
    - **Validates: Requirements 2.3.2**
    - Dùng `Arb.list(arbTransaction)` với `Arb.localDateTime()` ngẫu nhiên, apply grouping function, verify mọi transaction trong group có cùng year-month với group key
    - Tag: `// Feature: visual-money-tracker, Property 3: Monthly grouping correctness`

  - [x] 8.3 Implement GalleryScreen Composables
    - Tạo `GalleryScreen` với `LazyVerticalGrid`
    - Tạo `MonthHeader` hiển thị tên tháng + tổng thu/tổng chi
    - Tạo `WalletBalanceHeader` hiển thị tổng số dư (tất cả ví hoặc ví đang filter) + `WalletFilterChips`
    - Tạo `TransactionCard`: thumbnail vuông dùng Coil + `AmountOverlay`
    - Tạo `AmountOverlay`: số tiền + icon mũi tên, background semi-transparent, góc dưới ảnh, màu đỏ (Expense) / xanh (Income)
    - Tạo `CameraFab` cố định góc phải dưới
    - _Requirements: 2.3.1, 2.3.2, 2.3.3, 2.3.4, 2.8.7_

  - [x] 8.4 Viết UI tests cho GalleryScreen
    - Test: render với mock data → FAB hiển thị (2.2.1)
    - Test: `TransactionCard` overlay màu đỏ cho Expense, xanh cho Income
    - Test: `MonthHeader` hiển thị đúng tổng thu/chi
    - _Requirements: 2.2.1, 2.3.3, 2.3.4_

- [x] 9. Xây dựng Presentation Layer – Input Flow
  - [x] 9.1 Implement InputViewModel và AmountEntryScreen
    - Tạo `AmountEntryUiState` data class với field `selectedCategoryId`, `availableCategories`, `selectedWalletId`, `availableWallets`
    - Implement `InputViewModel`: xử lý numpad input, toggle Income/Expense, load categories từ `GetCategoriesUseCase`, load wallets từ `GetWalletsUseCase` (auto-select nếu chỉ có 1 ví), gọi `SaveTransactionUseCase` với `categoryId` và `walletId`
    - Tạo `AmountEntryScreen` Composable: preview ảnh, numpad, toggle Thu/Chi, category picker (bắt buộc, default "Khác"), wallet picker (bắt buộc, auto-select nếu 1 ví), nút Lưu
    - Disable nút Lưu khi amount rỗng hoặc = 0, hoặc chưa chọn ví; hiển thị lỗi khi amount > 999,999,999,999
    - Nếu chưa có ví nào, hiển thị prompt yêu cầu tạo ví trước
    - _Requirements: 2.2.3, 2.6.4, 2.8.4, 2.8.6_

  - [x] 9.2 Implement BottomSheet và Camera/Gallery integration
    - Tạo `InputBottomSheet` với 2 options: "Chụp ảnh mới" và "Chọn từ thư viện"
    - Tích hợp CameraX cho chụp ảnh trực tiếp
    - Tích hợp Android Photo Picker (`ActivityResultContracts.PickVisualMedia`)
    - Xử lý CAMERA permission: nếu bị từ chối → dialog giải thích → Settings
    - CameraX failure → fallback về Photo Picker
    - _Requirements: 2.2.1, 2.2.2, 2.2.3_

  - [x] 9.3 Viết unit tests cho InputViewModel
    - Test: amount rỗng → Save button disabled
    - Test: amount = 0 → Save button disabled
    - Test: không có ví → Save button disabled, hiển thị prompt tạo ví
    - Test: amount hợp lệ + category được chọn + wallet được chọn → gọi `SaveTransactionUseCase` với đúng `categoryId` và `walletId`
    - Test: save thành công → navigate back
    - Test: không chọn category → default về "Khác"
    - Test: chỉ có 1 ví → tự động chọn ví đó
    - _Requirements: 2.2.3, 2.6.4, 2.8.4, 2.8.6_

- [x] 10. Xây dựng Presentation Layer – Category Management
  - [x] 10.1 Implement CategoryViewModel và CategoryManagementScreen
    - Tạo `CategoryViewModel` với `StateFlow<List<Category>>`; load từ `GetCategoriesUseCase`
    - Expose actions: `onAddCategory(name)`, `onRenameCategory(id, newName)`, `onDeleteCategory(id)`
    - Tạo `CategoryManagementScreen` Composable: danh sách categories, phân biệt preset (không có nút xóa/sửa) và custom (có nút sửa + xóa)
    - Thêm dialog xác nhận khi xóa category đang được dùng (thông báo cascade sang "Khác")
    - _Requirements: 2.6.2, 2.6.3_

  - [x] 10.2 Viết unit tests cho CategoryViewModel
    - Test: xóa preset category → không gọi `DeleteCategoryUseCase`
    - Test: xóa custom category → gọi `DeleteCategoryUseCase` với đúng ID
    - Test: thêm category mới → gọi `SaveCategoryUseCase`
    - _Requirements: 2.6.2, 2.6.3_

- [x] 11. Xây dựng Presentation Layer – Analytics Screen
  - [x] 11.1 Implement AnalyticsViewModel
    - Tạo `AnalyticsUiState` data class với `selectedMonth`, `totalIncome`, `totalExpense`, `viewMode`, `breakdowns`, `wallets`, `selectedWalletId`
    - Implement `AnalyticsViewModel`: gọi `GetAnalyticsUseCase` khi tháng, viewMode hoặc walletId thay đổi; gọi `GetWalletsUseCase` để load danh sách ví
    - Expose actions: `onMonthSelected`, `onToggleViewMode`, `onWalletFilterSelected`
    - _Requirements: 2.7.1, 2.7.2, 2.7.3, 2.7.6_

  - [x] 11.2 Viết property test cho Pie chart percentages sum to 100%
    - **Property 11: Pie chart percentages sum to 100%**
    - **Validates: Requirements 2.7.4**
    - Dùng `Arb.list(arbTransaction, 1..100)` với random `categoryId`, gọi `GetAnalyticsUseCase`, verify `breakdowns.sumOf { it.percentage } ≈ 100f` (±0.1%)
    - Tag: `// Feature: visual-money-tracker, Property 11: Pie chart percentages sum to 100%`

  - [x] 11.3 Implement AnalyticsScreen Composables
    - Tạo `AnalyticsScreen` với `MonthSwitcher`, `MonthlySummaryHeader`, `ExpenseIncomeToggle`, `WalletFilterChips`, `CategoryPieChart`, `CategoryBreakdownList`
    - `CategoryPieChart`: mỗi slice hiển thị tên category, %, số tiền tuyệt đối
    - `CategoryBreakdownList`: danh sách bên dưới chart, màu nhất quán với chart slices
    - `WalletFilterChips`: chip "Tất cả" + chip cho từng ví, filter analytics theo ví được chọn
    - _Requirements: 2.7.1, 2.7.2, 2.7.3, 2.7.4, 2.7.5, 2.7.6_

  - [x] 11.4 Viết UI tests cho AnalyticsScreen
    - Test: toggle Expense/Income → breakdowns thay đổi tương ứng
    - Test: tháng không có giao dịch → hiển thị tổng = 0, chart trống
    - _Requirements: 2.7.2, 2.7.3_

- [x] 12. Xây dựng SettingsScreen
  - [x] 12.1 Implement SettingsViewModel và SettingsScreen
    - Tạo `SettingsViewModel` với state cho reminder settings, cloud provider và auto-sync settings
    - Tạo `SettingsScreen` Composable: toggle reminder on/off, chọn frequency, time picker, chọn cloud provider
    - Thêm section Auto-sync: toggle bật/tắt, chọn `AutoSyncFrequency` (DAILY / WIFI_ONLY), chọn giờ chạy
    - Thêm section Retention Policy: input số bản backup giữ lại (1–10, mặc định 3)
    - Thêm entry point đến `CategoryManagementScreen`
    - Thêm entry point đến `WalletManagementScreen`
    - Wire `ScheduleReminderUseCase` và `ScheduleAutoSyncUseCase` khi settings thay đổi
    - Wire `SyncToCloudUseCase` với OAuth flow; hiển thị trạng thái xác thực cho từng provider
    - _Requirements: 2.4.2, 2.4.3, 2.4.5, 2.4.6, 2.6.2, 2.8.7_

- [ ] 13. Xây dựng Wallet Management
  - [x] 13.1 Implement WalletViewModel và WalletManagementScreen
    - Tạo `WalletUiState` data class với `wallets: List<WalletWithBalance>`
    - Implement `WalletViewModel`: load wallets từ `GetWalletsUseCase`, tính balance từ `GetWalletBalanceUseCase` cho từng ví
    - Expose actions: `onAddWallet(name, openingBalance)`, `onUpdateWallet(id, name, openingBalance)`, `onDeleteWallet(id, reassignToWalletId)`
    - Tạo `WalletManagementScreen` Composable: danh sách ví với số dư hiện tại, nút thêm/sửa/xóa
    - Tạo `AddWalletDialog`: nhập tên ví và opening balance
    - Tạo `DeleteWalletDialog`: hỏi chuyển giao dịch sang ví khác (dropdown chọn ví) hay xóa luôn
    - _Requirements: 2.8.1, 2.8.3, 2.8.5, 2.8.7_

  - [x] 13.2 Viết unit tests cho WalletViewModel
    - Test: thêm ví mới → gọi `SaveWalletUseCase` với đúng tên và opening balance
    - Test: xóa ví với reassign → gọi `DeleteWalletUseCase` với `reassignToWalletId` hợp lệ
    - Test: xóa ví với cascade delete → gọi `DeleteWalletUseCase` với `reassignToWalletId = null`
    - Test: số dư hiển thị đúng = openingBalance + sum(INCOME) - sum(EXPENSE)
    - _Requirements: 2.8.1, 2.8.3, 2.8.5_

- [x] 14. Dependency Injection với Hilt
  - Tạo Hilt modules trong `di/`: `DatabaseModule`, `RepositoryModule`, `UseCaseModule`, `WorkerModule`
  - Bind tất cả interfaces với implementations, bao gồm `CategoryRepository` và `WalletRepository`
  - Inject `ImageCompressor`, `CloudSyncManager`, `WorkManager` vào đúng scope
  - _Requirements: 3 (Tech Stack)_

- [x] 15. Navigation và App wiring
  - Tạo `BottomNavTab` enum với 3 tab: `GALLERY`, `ANALYTICS`, `SETTINGS`
  - Tạo `NavGraph` với Compose Navigation và `BottomNavigationBar`: `GalleryScreen`, `AnalyticsScreen`, `SettingsScreen` là top-level destinations; `AmountEntryScreen` và `WalletManagementScreen` là nested routes
  - `CameraFab` chỉ hiển thị khi `currentRoute == Gallery` (ẩn hoàn toàn ở Analytics và Settings)
  - Wire `MainActivity` với Hilt và NavHost
  - Đảm bảo back stack đúng sau khi lưu transaction (pop về Gallery)
  - Khi FAB được bấm mà chưa có ví nào → navigate đến `WalletManagementScreen` thay vì mở camera
  - _Requirements: 2.2.1, 2.2.2, 2.2.3, 2.3.1, 2.3.2, 2.8.4_

- [ ] 16. Final Checkpoint – Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks đánh dấu `*` là optional, có thể bỏ qua để ra MVP nhanh hơn
- Mỗi task tham chiếu requirements cụ thể để đảm bảo traceability
- 15 property tests (P1–P15) dùng Kotest với tối thiểu 100 iterations mỗi test
- Property tests đặt gần implementation tương ứng để phát hiện lỗi sớm
- Unit tests tập trung vào edge cases và error conditions
- P7, P8 nằm trong `RestoreUseCaseTest.kt`; P9 trong `AutoSyncWorkerTest.kt`; P10 trong `RetentionPolicyManagerTest.kt`
- P11 trong `GetAnalyticsUseCaseTest.kt`; P12 trong `DeleteCategoryUseCaseTest.kt`; P13 trong `CategoryDaoTest.kt`
- P14 trong `GetWalletBalanceUseCaseTest.kt`; P15 trong `DeleteWalletUseCaseTest.kt`
