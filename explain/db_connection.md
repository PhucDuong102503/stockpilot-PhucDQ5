# Giải thích về H2 Database và Cách kiểm tra kết nối

Tài liệu này giải thích cách hoạt động của H2 Database (hệ quản trị cơ sở dữ liệu được đề bài yêu cầu), cấu hình kết nối của nó và cách bạn có thể kiểm tra kết nối này.

## 1. H2 Database hoạt động như thế nào?
Khác với các hệ quản trị cơ sở dữ liệu độc lập như MySQL hay SQL Server (yêu cầu bạn phải cài đặt một phần mềm Server chạy ngầm trên máy tính), **H2 Database** là một thư viện Java nhúng (Embedded Java Database):
- **Không cần cài đặt**: Toàn bộ công cụ quản lý cơ sở dữ liệu chạy trực tiếp bên trong chương trình Java của bạn. Bạn không cần cài đặt thêm bất kỳ phần mềm cơ sở dữ liệu nào khác trên Windows.
- **File Database**: Khi bạn chạy ứng dụng, H2 sẽ tự động tạo một hoặc nhiều file lưu trữ dữ liệu tại thư mục dự án của bạn. Cụ thể cấu hình `jdbc:h2:./stockpilot_db` sẽ tạo ra một file tên là `stockpilot_db.mv.db` trong thư mục `C:\Users\PC\Documents\GitHub\stockpilot\`. Nếu chưa có file này, H2 sẽ tự tạo mới.
- **Tài khoản mặc định**: H2 mặc định sử dụng username là `sa` (System Administrator) và mật khẩu rỗng (không có mật khẩu). Bạn không cần thay đổi thành `root` hay `555888` (vốn là tài khoản MySQL trên máy của bạn).

## 2. Giải thích các tham số cấu hình trong `db.properties`
- `db.url=jdbc:h2:./stockpilot_db;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE`
  - `jdbc:h2:./stockpilot_db`: Kết nối tới H2 Database được lưu ở dạng file có tên bắt đầu bằng `stockpilot_db` trong thư mục hiện tại.
  - `DB_CLOSE_DELAY=-1`: Đảm bảo rằng cơ sở dữ liệu không bị đóng/khởi tạo lại khi tất cả các kết nối tạm thời đóng (keep database alive trong suốt vòng đời của JVM).
  - `AUTO_SERVER=TRUE`: Cho phép các phần mềm bên ngoài (như IntelliJ Database Tool hoặc Web Console của H2) có thể truy cập vào file DB cùng một lúc khi ứng dụng Java đang chạy.
- `db.username=sa`: Tài khoản quản trị mặc định.
- `db.password=`: Mật khẩu mặc định là rỗng.
- `db.driver=org.h2.Driver`: Lớp Driver của H2 giúp JDBC biết cách giao tiếp với H2.

## 3. Cách chạy kiểm tra kết nối H2
Để chạy thử kết nối H2 này, bạn chạy lệnh sau trên PowerShell tại thư mục dự án `C:\Users\PC\Documents\GitHub\stockpilot`:
```bash
& "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.1\plugins\maven\lib\maven3\bin\mvn.cmd" exec:java -Dexec.mainClass="com.stockpilot.util.DatabaseConnection"
```

Khi chạy thành công, H2 sẽ tự tạo file dữ liệu và in ra màn hình:
```text
Testing database connection...
Connection successful! Database product name: H2
```
Và bạn sẽ thấy xuất hiện một file mới tên là `stockpilot_db.mv.db` trong thư mục dự án.
