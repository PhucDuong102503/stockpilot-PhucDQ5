# Kiểm tra & Sửa lỗi cấu hình nhầm lẫn

Tài liệu này ghi lại các lỗi cấu hình nhầm lẫn đã được phát hiện và khắc phục kịp thời.

## 1. File `db/schema.sql`
- **Phát hiện**: Có câu lệnh `CREATE DATABASE stockpilot` ở dòng 1 nhưng không có dấu chấm phẩy kết thúc, đồng thời H2 Database nhúng không cần và không hỗ trợ trực tiếp câu lệnh này (H2 khởi tạo database qua URL kết nối file). Nếu giữ nguyên, việc thực thi file SQL này trên H2 sẽ gây lỗi cú pháp (Syntax Error).
- **Khắc phục**: Đã chuyển dòng này thành dòng chú thích (comment): `-- Create tables for StockPilot application` để đảm bảo tệp tin chạy mượt mà trên H2 Database.

## 2. File `src/main/resources/db.properties`
- **Phát hiện**: Dòng chỉ định driver `db.driver=org.h2.Driver` đã bị lỡ xóa mất khi cấu hình lại, và sau đó bị nhân đôi (duplicate) trong quá trình chỉnh sửa.
- **Khắc phục**: Đã chuẩn hóa lại tệp `db.properties` sạch sẽ, chỉ giữ lại một khóa duy nhất cho mỗi cấu hình.

## 3. Xác thực biên dịch
- Chạy lệnh `mvn compile` thành công 100% không gặp bất kỳ cảnh báo hay lỗi biên dịch nào.
