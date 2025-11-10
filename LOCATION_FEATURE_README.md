# Tính năng Định vị GPS cho Đơn hàng

## Tổng quan
Ứng dụng đã được cập nhật với tính năng tự động lấy vị trí GPS của người dùng khi đặt hàng và chuyển đổi tọa độ thành địa chỉ thực tế.

## Các thay đổi đã thực hiện

### 1. **AndroidManifest.xml**
- ✅ Thêm quyền `ACCESS_FINE_LOCATION` và `ACCESS_COARSE_LOCATION`

### 2. **build.gradle**
- ✅ Thêm dependency `com.google.android.gms:play-services-location:21.0.1`

### 3. **Order Model** (`Order.kt`)
- ✅ Thêm 2 trường mới:
  - `latitude: Double` - Vĩ độ
  - `longitude: Double` - Kinh độ

### 4. **CartActivity.kt**
- ✅ Tích hợp `FusedLocationProviderClient` để lấy vị trí GPS
- ✅ Yêu cầu quyền truy cập vị trí khi activity khởi động
- ✅ Tự động lấy vị trí khi người dùng nhấn nút "Đặt hàng"
- ✅ Lưu tọa độ GPS vào đơn hàng

### 5. **AddressActivity.kt**
- ✅ Tích hợp `Geocoder` để chuyển đổi tọa độ GPS thành địa chỉ
- ✅ Tự động lấy vị trí hiện tại khi activity khởi động
- ✅ Tự động điền địa chỉ từ GPS vào ô "Địa chỉ" khi người dùng mở dialog thêm địa chỉ
- ✅ Người dùng chỉ cần nhập **Tên** và **Số điện thoại**, địa chỉ đã được tự động điền

### 6. **Layout Updates**
- ✅ Cập nhật `layout_bottom_sheet_add_address.xml`:
  - Thêm label "(Tự động từ GPS)" 
  - Thêm hint cho EditText địa chỉ
  - Cho phép địa chỉ hiển thị nhiều dòng

## Cách hoạt động

### Khi người dùng mở màn hình Địa chỉ:
1. App yêu cầu quyền truy cập vị trí (nếu chưa có)
2. Tự động lấy vị trí GPS hiện tại
3. Sử dụng Geocoder để chuyển đổi tọa độ thành địa chỉ đầy đủ
4. Lưu địa chỉ vào biến `currentAddressText`

### Khi người dùng nhấn "Thêm địa chỉ":
1. Dialog mở ra với ô địa chỉ đã được tự động điền
2. Người dùng chỉ cần nhập:
   - ✏️ Tên đầy đủ
   - ✏️ Số điện thoại
   - ✅ Địa chỉ (đã tự động điền, có thể chỉnh sửa nếu cần)
3. Nhấn "Thêm" để lưu địa chỉ

### Khi người dùng đặt hàng:
1. Chọn phương thức thanh toán
2. Chọn địa chỉ giao hàng
3. Nhấn "Đặt hàng"
4. App tự động lấy vị trí GPS hiện tại
5. Lưu tọa độ (latitude, longitude) vào đơn hàng
6. Đơn hàng được tạo với đầy đủ thông tin vị trí

## Định dạng địa chỉ từ GPS

Địa chỉ được tạo theo format:
```
[Số nhà] [Tên đường], [Phường/Xã], [Quận/Huyện], [Tỉnh/Thành phố]
```

Ví dụ:
```
123 Nguyễn Huệ, Phường Bến Nghé, Quận 1, Hồ Chí Minh
```

## Xử lý lỗi

- ❌ **Không có quyền vị trí**: Hiển thị thông báo yêu cầu cấp quyền
- ❌ **Không lấy được vị trí**: Đơn hàng vẫn được tạo nhưng không có thông tin GPS
- ❌ **Geocoder thất bại**: Hiển thị thông báo "Không thể lấy địa chỉ từ vị trí hiện tại"

## Lưu ý khi test

1. **Bật GPS** trên thiết bị
2. **Cấp quyền vị trí** cho ứng dụng khi được yêu cầu
3. **Đợi vài giây** để GPS xác định vị trí chính xác
4. **Kiểm tra trên thiết bị thật** (emulator có thể không chính xác)

## Cải tiến trong tương lai

- 🔄 Thêm nút "Làm mới vị trí" để cập nhật địa chỉ mới
- 🗺️ Hiển thị bản đồ với marker tại vị trí đặt hàng
- 📍 Cho phép người dùng chọn vị trí trên bản đồ thay vì dùng GPS
- 🚚 Tính khoảng cách từ cửa hàng đến địa chỉ giao hàng
- 💰 Tính phí ship dựa trên khoảng cách

## Build & Run

1. Sync Gradle để tải dependency mới
2. Build project
3. Cài đặt trên thiết bị
4. Cấp quyền vị trí khi được yêu cầu
5. Test tính năng thêm địa chỉ và đặt hàng

---

**Ngày cập nhật**: 09/10/2025  
**Phiên bản**: 1.0
