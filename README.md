# Hệ thống đấu giá trực tuyến

> Dự án môn **Lập trình Nâng cao** - Hệ thống Client/Server cho đấu giá sản phẩm theo thời gian thực.

## 1) Mô tả ngắn gọn bài toán và phạm vi hệ thống

Hệ thống mô phỏng quy trình đấu giá trực tuyến: người dùng có thể đăng ký/đăng nhập, xem sản phẩm, tạo phiên đấu giá, đặt giá và theo dõi lịch sử. Quản trị viên có quyền quản lý người dùng, sản phẩm và xử lý phiên đấu giá.

Phạm vi hệ thống:

- Kiến trúc `Client - Server` sử dụng Socket.
- Giao tiếp dữ liệu bằng Java object serialization.
- Giao diện người dùng xây dựng bằng JavaFX theo mô hình MVC.
- Có xử lý đồng thời, lịch tự đóng phiên và ghi log.

## 2) Danh sách chức năng đã hoàn thành

- Đăng ký, đăng nhập, đăng xuất người dùng.
- Quản lý tài khoản và thông tin cá nhân.
- Xem danh sách sản phẩm đấu giá.
- Tạo phiên đấu giá và hiển thị trạng thái phiên.
- Đặt giá / tham gia đấu giá theo thời gian thực.
- Tự động cập nhật giá hiện tại và người đang dẫn đầu.
- Xem lịch sử đấu giá và giao dịch.
- Quản lý người dùng, sản phẩm và phiên đấu giá ( có thể hủy phiên ) ở phía Admin.
- Tự động đóng phiên theo thời gian và ghi log hoạt động.
- AntiSnipping, BidHistory LineChart (Tính năng nâng cao).

## 3) Công nghệ sử dụng, môi trường chạy và yêu cầu cài đặt

### Công nghệ

- **Java 25**
- **Maven**
- **JavaFX**
- **MySQL**
- **SLF4J / Logback**
- **HikariCP** cho kết nối CSDL phía server

### Môi trường chạy

- Windows
- JDK 25 trở lên
- Maven 3.9+

### Yêu cầu cài đặt

- Cài JDK và cấu hình `JAVA_HOME`
- Cài Maven và thêm vào `PATH`
- Cài MySQL, tạo database theo cấu hình của server
- Nếu chạy bằng IDE, nên dùng IntelliJ IDEA

## 4) Cấu trúc thư mục / các module chính

Project dùng Maven multi-module:

```text
AuctionSystem_FINAL/
├── pom.xml                 # Parent POM
├── common/                 # Dữ liệu dùng chung cho client và server
│   └── src/main/java/auction/logic/
│       ├── enums/
│       ├── factory/
│       ├── manager/
│       ├── model/
│       ├── RequestDTO/
│       └── ResponseDTO/
├── client/                 # Ứng dụng JavaFX phía client
│   └── src/main/java/
│       ├── AppTrade.java
│       ├── adminController/
│       ├── clientController/
│       ├── service/
│       ├── stateManager/
│       └── Utils/
├── sever/                  # Backend server
│   └── src/main/java/sever/
│       ├── Server.java
│       ├── Main.java
│       ├── config/
│       ├── dao/
│       ├── handler/
│       ├── manager/
│       ├── scheduler/
│       └── service/
└── .github/
    └── workflows/          # CI/CD workflows (GitHub Actions)
```

## 5) Vị trí các file `.jar`

Sau khi build bằng Maven, file JAR nằm trong thư mục `target/` của từng module:

- `client/target/client.jar`
- `sever/target/server.jar` *(hoặc JAR cùng tên theo kết quả build trong `sever/target/`)*

Nếu build từ root, Maven sẽ sinh JAR theo từng module tương ứng trong thư mục `target/`.

## 6) Hướng dẫn chạy Server / Client theo thứ tự cụ thể

### Bước 1: Build project

Chạy ở thư mục gốc của project:

```powershell
mvn clean package
```

### Bước 2: Chạy Server trước

Chạy file JAR của server trong `sever/target/`:

```powershell
java -jar .\sever\target\server.jar
```

Server cần được khởi động trước để client kết nối.

### Bước 3: Chạy Client

Mở terminal mới và chạy:

```powershell
java -jar .\client\target\client.jar
```

### Thứ tự bắt buộc

1. Khởi động **Server**
2. Sau đó mới khởi động **Client**
3. Đăng nhập và sử dụng các chức năng đấu giá

## Ghi chú

- Nếu chạy trong IntelliJ IDEA, mở `sever/src/main/java/sever/Server.java` để chạy server và `client/src/main/java/AppTrade.java` để chạy client.
- Nếu kết nối CSDL lỗi, kiểm tra lại MySQL đang chạy và cấu hình kết nối trong mã nguồn server.


Link PDF báo cáo: https://drive.google.com/file/d/1jI0R0eC06zrD_NUoqKPy99LBGrSXsrQr/view?usp=sharing
Link Video Demo: https://drive.google.com/file/d/18lYXzI_xeUr-ovs6NCjDU8oXQVbbEN5b/view?usp=sharing