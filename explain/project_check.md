# Kiểm tra tổng thể dự án StockPilot (Project Audit)

Tài liệu này xác nhận tính toàn vẹn và sự chính xác của toàn bộ cấu trúc mã nguồn dự án StockPilot tính đến thời điểm hiện tại.

## 1. Cấu trúc thư mục & Tệp tin hiện tại
Tất cả các tệp tin nguồn Java được tổ chức chặt chẽ theo cấu trúc phân lớp Maven:
- `com.stockpilot`
  - `Main.java` (Điểm bắt đầu ứng dụng)
- `com.stockpilot.model` (Lớp dữ liệu nghiệp vụ)
  - `Product.java` (Validation SKU, giá, số lượng)
  - `Customer.java` (Validation Email, Phone)
  - `Order.java` (Chiết khấu, tính tổng số tiền)
  - `OrderItem.java` (Chi tiết số lượng, đơn giá)
- `com.stockpilot.repository` (Giao tiếp dữ liệu)
  - `Repository.java` (Giao diện Generic CRUD mẫu)
- `com.stockpilot.exception` (Ngoại lệ nghiệp vụ & dữ liệu)
  - `DataAccessException.java`
  - `InsufficientStockException.java`
  - `InvalidInputException.java`
  - `ProductNotFoundException.java`
- `com.stockpilot.util` (Tiện ích hệ thống)
  - `DatabaseConnection.java` (Tải cấu hình classpath, quản lý Driver, Connection & Chạy thử kết nối)

## 2. Kiểm tra Cấu hình & Cơ sở dữ liệu
- Tệp [db.properties](file:///C:/Users/PC/Documents/GitHub/stockpilot/src/main/resources/db.properties) đã được xác thực sử dụng cấu hình H2 chuẩn xác.
- Tệp [db/schema.sql](file:///C:/Users/PC/Documents/GitHub/stockpilot/db/schema.sql) đã được loại bỏ các câu lệnh MySQL không tương thích, sẵn sàng cho việc khởi tạo bảng trên H2 Database.

## 3. Kết quả chạy thử nghiệm
- **Biên dịch**: `mvn compile` đạt trạng thái `BUILD SUCCESS`.
- **Kết nối DB**: Lớp `DatabaseConnection` chạy thử thành công, tự động sinh file DB `stockpilot_db.mv.db` cục bộ tại thư mục dự án và in ra thông tin phiên bản H2 thành công.
