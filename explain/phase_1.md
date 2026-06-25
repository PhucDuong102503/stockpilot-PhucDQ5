# Giải thích thiết kế & Giải pháp - Giai đoạn 1: Nền tảng & Cấu trúc

Tài liệu này giải thích các quyết định thiết kế và giải pháp kỹ thuật được sử dụng cho Giai đoạn 1 của dự án StockPilot.

## 1. Cấu hình Dự án (Maven & pom.xml)
- **Giải pháp**: Sử dụng Java 17+ (phiên bản JDK được cài đặt là Java 24, tương thích ngược tốt).
- **Thư viện dependency**:
  - **H2 Database**: Chọn cơ sở dữ liệu quan hệ gọn nhẹ chạy ở chế độ nhúng (embedded/file mode). Lợi ích là không cần cài đặt một DBMS độc lập phức tạp như MySQL hay SQL Server, giúp dễ dàng thiết lập và chạy trực tiếp chỉ bằng Maven.
  - **JUnit 5**: Thư viện kiểm thử chuẩn mực cho ứng dụng Java hiện đại để thực hiện viết unit tests ở các giai đoạn sau.
  - **SLF4J & Logback**: Cung cấp log hệ thống chuyên nghiệp để tránh việc lạm dụng `System.out.println` hoặc `e.printStackTrace()` trong luồng chạy chuẩn.
- **Runnable JAR**: Cấu hình `maven-assembly-plugin` với `descriptorRefs` là `jar-with-dependencies` để tạo ra một file Fat JAR độc lập chứa toàn bộ thư viện cần thiết, giúp chạy ứng dụng đơn giản bằng lệnh `java -jar`.

## 2. Thiết kế Cơ sở dữ liệu (schema.sql)
- Phân chia thành 4 bảng chuẩn hóa:
  - `products`: Quản lý thông tin mặt hàng. SKU được đặt thuộc tính `UNIQUE` để đảm bảo định danh duy nhất trong kho và ràng buộc kiểm tra số lượng tồn kho không âm (`CHECK (stock_quantity >= 0)`).
  - `customers`: Quản lý thông tin khách hàng bán lẻ. Email là duy nhất (`UNIQUE`).
  - `orders`: Lưu trữ thông tin đơn hàng tổng quát. Có khóa ngoại tham chiếu đến khách hàng.
  - `order_items`: Lưu chi tiết các sản phẩm của đơn hàng. Thiết lập khóa ngoại `ON DELETE CASCADE` tham chiếu đến `orders` để đảm bảo tính toàn vẹn dữ liệu khi xóa đơn hàng thì các dòng chi tiết tương ứng cũng bị xóa sạch.

## 3. Các ngoại lệ tùy biến (Custom Exceptions)
 định nghĩa các ngoại lệ thừa kế từ `RuntimeException` (Unchecked Exceptions) gồm:
- `InvalidInputException`: Ném ra khi người dùng nhập dữ liệu không hợp lệ (ví dụ: sai định dạng SKU, email, số điện thoại, hoặc giá trị âm).
- `ProductNotFoundException`: Ném ra khi không tìm thấy sản phẩm trong hệ thống/cơ sở dữ liệu.
- `InsufficientStockException`: Ném ra khi số lượng sản phẩm trong kho không đủ để đáp ứng đơn đặt hàng.
- `DataAccessException`: Dùng để bọc (wrap) các ngoại lệ cấp thấp như `SQLException` nhằm che giấu chi tiết SQL cụ thể khỏi các lớp nghiệp vụ cấp trên (Service/CLI), tuân thủ kiến trúc phân lớp.
