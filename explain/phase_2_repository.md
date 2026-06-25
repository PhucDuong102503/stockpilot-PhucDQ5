# Thiết kế Repository Pattern cho StockPilot

Tài liệu này giải thích cấu trúc Repository Pattern được thiết lập trong dự án StockPilot.

## 1. Tại sao sử dụng Repository Pattern?
Repository Pattern là một mẫu thiết kế trung gian giúp tách biệt (decouple) logic nghiệp vụ (Service Layer) khỏi phương thức lưu trữ dữ liệu (Data Access Layer - JDBC).
- **Lợi ích**: 
  - Độc lập lưu trữ: Service layer chỉ gọi các phương thức chung như `save`, `findById` mà không cần biết dữ liệu được lưu trữ trong DB SQL, File hay Memory.
  - Dễ bảo trì và test: Cho phép Mock repository dễ dàng khi viết Unit tests.

## 2. Generic Repository Interface
Chúng tôi thiết kế giao diện chung [Repository.java](file:///C:/Users/PC/Documents/GitHub/stockpilot/src/main/java/com/stockpilot/repository/Repository.java) sử dụng Generics `<T, ID>`:
- `T`: Kiểu dữ liệu của thực thể (Entity) như `Product`, `Customer`.
- `ID`: Kiểu dữ liệu của khóa chính (ví dụ: `Long`).

```java
public interface Repository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    T update(T entity);
    boolean deleteById(ID id);
}
```
Mỗi repository cụ thể (ví dụ: `ProductRepository`, `CustomerRepository`) sẽ kế thừa interface này để thừa hưởng các phương thức CRUD chuẩn mực.
